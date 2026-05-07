package masroofy.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import masroofy.data.DAOLayer;

/**
 * Generates spending charts for the UI by delegating rendering to a Python script.
 *
 * <p>The generator aggregates expenses by category, writes a temporary CSV file, and invokes
 * {@code chart_generator.py} via {@link ProcessBuilder} to produce a PNG.</p>
 */
public class ChartGenerator {

    private final DAOLayer daoLayer;

    private static final String CHART_OUTPUT = "chart_output.png";

    /**
     * Creates a new chart generator backed by the DAO layer.
     */
    public ChartGenerator() {
        this.daoLayer = new DAOLayer();
    }

    /**
     * Generates a pie chart PNG for the given cycle.
     *
     * @param cycleId the cycle id to chart
     * @return absolute path to the generated image, or {@code null} when no data is available or
     *         Python execution fails
     */
    public String generatePieChart(int cycleId) {
        List<Object[]> expenses = daoLayer.getExpensesByCategory(cycleId);

        if (expenses == null || expenses.isEmpty()) {
            System.out.println("[ChartGenerator] No expenses found.");
            return null;
        }

        String csvData = formatDataToCSV(expenses);

        File tempCsv;
        try {
            tempCsv = File.createTempFile("masroofy_chart_", ".csv");
            tempCsv.deleteOnExit();
            try (FileWriter fw = new FileWriter(tempCsv)) {
                fw.write(csvData);
            }
        } catch (IOException e) {
            System.err.println("[ChartGenerator] Could not write CSV: " + e.getMessage());
            return null;
        }

        String scriptPath = resolvePythonScriptPath();
        if (scriptPath == null) {
            System.err.println("[ChartGenerator] chart_generator.py not found.");
            return null;
        }

        return callPython(scriptPath, tempCsv.getAbsolutePath(), CHART_OUTPUT);
    }

    /**
     * Tries multiple common locations for the Python script.
     *
     * @return an existing script path, or {@code null} if not found
     */
    private String resolvePythonScriptPath() {
        String[] candidates = {
            "src/main/resources/python/chart_generator.py",
            "resources/python/chart_generator.py",
            "chart_generator.py",
        };

        for (String path : candidates) {
            if (new File(path).exists()) {
                System.out.println("[ChartGenerator] Found script at: " + path);
                return path;
            }
        }

        try {
            String jarDir = new File(
                ChartGenerator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getParent();
            String fromJar = jarDir + "/chart_generator.py";
            if (new File(fromJar).exists()) return fromJar;
        } catch (Exception ignored) { }

        return null;
    }

    /**
     * Calls the Python subprocess and waits for the output file to be created.
     *
     * @param scriptPath python script path
     * @param csvPath input csv path
     * @param outputPath output png path
     * @return absolute output path, or {@code null} on failure
     */
    private String callPython(String scriptPath, String csvPath, String outputPath) {
        for (String pythonCmd : new String[]{"python3", "python"}) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    pythonCmd, scriptPath, csvPath, outputPath
                );
                pb.redirectErrorStream(true);
                pb.directory(new File("."));
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println("[Python] " + line);
                }

                int exitCode = process.waitFor();

                if (exitCode == 0 && new File(outputPath).exists()) {
                    System.out.println("[ChartGenerator] Chart generated: " + outputPath);
                    return new File(outputPath).getAbsolutePath();
                } else {
                    System.err.println("[ChartGenerator] Python output: " + output);
                }

            } catch (IOException e) {
                System.out.println("[ChartGenerator] " + pythonCmd + " not found, trying next...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ChartGenerator] Process interrupted.");
                return null;
            }
        }

        System.err.println("[ChartGenerator] Python not available. Is it installed?");
        return null;
    }

    /**
     * Formats aggregated category totals into a CSV string for the Python script.
     *
     * @param data aggregated rows from {@link DAOLayer#getExpensesByCategory(int)}
     * @return csv string formatted as {@code category,amount}
     */
    private String formatDataToCSV(List<Object[]> data) {
        StringBuilder sb = new StringBuilder("category,amount\n");
        for (Object[] row : data) {
            sb.append(row[0]).append(",").append(row[1]).append("\n");
        }
        return sb.toString();
    }
}

