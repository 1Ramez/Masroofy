package masroofy.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import masroofy.data.DAOLayer;

/**
 * ChartGenerator
 * MVC Role : Controller
 * SD-4     : generatePieChart() → Python subprocess → chart image
 *
 * Bug Fix  : Python script path now resolved correctly for mvn javafx:run
 */
public class ChartGenerator {

    private final DAOLayer daoLayer;

    // Output chart path — in project root so it's always findable
    private static final String CHART_OUTPUT = "chart_output.png";

    public ChartGenerator() {
        this.daoLayer = new DAOLayer();
    }

    // SD-4 Step 1: generatePieChart() : void
    // Returns path to chart image, or null if no data / Python not available
    public String generatePieChart(int cycleId) {

        // SD-4 Step 2+3: SELECT * FROM Transactions JOIN Categories
        List<Object[]> expenses = daoLayer.getExpensesByCategory(cycleId);

        // SD-4 alt [no expenses logged]
        if (expenses == null || expenses.isEmpty()) {
            System.out.println("[ChartGenerator] No expenses found.");
            return null;
        }

        // Format data as CSV for Python
        String csvData = formatDataToCSV(expenses);

        // Write CSV to temp file
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

        // Resolve Python script path
        // Try multiple locations to support both mvn javafx:run and IDE run
        String scriptPath = resolvePythonScriptPath();
        if (scriptPath == null) {
            System.err.println("[ChartGenerator] chart_generator.py not found.");
            return null;
        }

        // SD-4 Step 7: createPieChart(df: DataFrame) — call Python
        return callPython(scriptPath, tempCsv.getAbsolutePath(), CHART_OUTPUT);
    }

    // Tries multiple common locations for the Python script
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

        // Also try relative to jar location
        try {
            String jarDir = new File(
                ChartGenerator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getParent();
            String fromJar = jarDir + "/chart_generator.py";
            if (new File(fromJar).exists()) return fromJar;
        } catch (Exception ignored) {}

        return null;
    }

    // Calls Python subprocess and waits for chart to be generated
    private String callPython(String scriptPath, String csvPath, String outputPath) {
        // Try python3 first, fallback to python
        for (String pythonCmd : new String[]{"python3", "python"}) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    pythonCmd, scriptPath, csvPath, outputPath
                );
                pb.redirectErrorStream(true);
                pb.directory(new File("."));    // run from project root
                Process process = pb.start();

                // Print Python output for debugging
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
                // This python command not found, try next
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

    // Format expense data as CSV string for Python
    private String formatDataToCSV(List<Object[]> data) {
        StringBuilder sb = new StringBuilder("category,amount\n");
        for (Object[] row : data) {
            sb.append(row[0]).append(",").append(row[1]).append("\n");
        }
        return sb.toString();
    }
}