package masroofy.controller;

import masroofy.model.BudgetCycle;
import masroofy.data.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * CycleController
 * 
 * MVC Role   : Controller
 * Sequence   : SD-1 (US1 - Set Initial Budget Cycle) / FR-1, FR-2
 * Responsible: Ramez
 * 
 * Responsibilities:
 *  - Validates user input from InitSetupPage
 *  - Creates a new BudgetCycle domain object
 *  - Persists it to SQLite via Database singleton
 *  - Signals the UI to navigate to Dashboard
 *  - Checks if an active cycle already exists
 */
public class CycleController {

    // ── singleton DB connection ──────────────────────────────────────────────
    private final Connection connection;

    public CycleController() {
        // get the single shared connection from the Database singleton
        // matches: Database.getInstance().getConnection() in class diagram
        this.connection = Database.getInstance().getConnection();
    }

    // ────────────────────────────────────────────────────────────────────────
    // SD-1 Step 2: createCycle(amount, start, end)
    // Called by InitSetupPage after student enters amount and dates
    // Returns the created BudgetCycle on success, null on failure
    // ────────────────────────────────────────────────────────────────────────
    public BudgetCycle createCycle(float amount, LocalDate startDate, LocalDate endDate) {

        // ── SD-1: [valid input] branch ───────────────────────────────────────
        // Validate: amount must be > 0 and endDate must be after startDate
        // Matches alt fragment: [valid input] in sequence diagram
        if (!validateInput(amount, startDate, endDate)) {
            // SD-1 Step 6: return ValidationError to UI
            // UI will show: "Allowance must be a positive number"
            return null;
        }

        // SD-1 Step 2: create BudgetCycle domain object
        // BudgetCycle constructor sets all fields
        BudgetCycle cycle = new BudgetCycle(amount, startDate, endDate);

        // SD-1 Step 3: calculateDailyLimit() — called on BudgetCycle itself
        // SafeDailyLimit = TotalAllowance / TotalDays  (shown in SD-1 note)
        // BudgetCycle owns this calculation per class diagram
        cycle.calculateBalance();

        // SD-1 Step 4: saveSQLite()
        // Persist the new cycle to the SQLite database
        boolean saved = saveCycleToDB(cycle);

        if (!saved) {
            // DB error — treat as failure
            return null;
        }

        // SD-1 Step 5: navigateToDashboard()
        // Controller signals success; UI layer handles the actual navigation
        // We return the cycle so the UI knows navigation is safe to proceed
        return cycle;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Checks whether there is already an active cycle in the database
    // Called by InitSetupPage on app launch to decide which scene to show
    // ────────────────────────────────────────────────────────────────────────
    public BudgetCycle checkActiveCycle() {
        String sql = "SELECT * FROM Cycles WHERE isActive = 1 LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Build and return the active BudgetCycle from DB row
                return mapRowToCycle(rs);
            }

        } catch (SQLException e) {
            System.err.println("[CycleController] checkActiveCycle error: " + e.getMessage());
        }

        // No active cycle found → UI shows InitSetupPage
        return null;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Resets (deletes) the current active cycle and all its expenses
    // Called by SettingsScene → US11
    // ────────────────────────────────────────────────────────────────────────
    public boolean resetCycle(int cycleId) {
        // Delete expenses linked to this cycle first (foreign key order)
        String deleteExpenses = "DELETE FROM Transactions WHERE budgetCycleId = ?";
        String deleteCycle    = "DELETE FROM Cycles WHERE budgetCycleId = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement s1 = connection.prepareStatement(deleteExpenses)) {
                s1.setInt(1, cycleId);
                s1.executeUpdate();
            }

            try (PreparedStatement s2 = connection.prepareStatement(deleteCycle)) {
                s2.setInt(1, cycleId);
                s2.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("[CycleController] resetCycle error: " + e.getMessage());
            try { connection.rollback(); connection.setAutoCommit(true); }
            catch (SQLException ignored) {}
            return false;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Validates user input before creating a cycle.
     * SD-1 alt fragment: [valid input] condition
     *   - amount must be > 0
     *   - endDate must be strictly after startDate
     *   - startDate cannot be null
     */
    private boolean validateInput(float amount, LocalDate startDate, LocalDate endDate) {
        if (amount <= 0) return false;
        if (startDate == null || endDate == null) return false;
        if (!endDate.isAfter(startDate)) return false;
        return true;
    }

    /**
     * Persists a BudgetCycle to the SQLite Cycles table.
     * SD-1 Step 4: saveSQLite()
     * Uses Database singleton connection (matches class diagram).
     */
    private boolean saveCycleToDB(BudgetCycle cycle) {
        String sql = """
                INSERT INTO Cycles
                    (totalAmount, startDate, endDate,
                     remainingBalance, safeDailyLimit, isActive)
                VALUES (?, ?, ?, ?, ?, 1)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setFloat(1, cycle.getTotalAmount());
            stmt.setString(2, cycle.getStartDate().toString());
            stmt.setString(3, cycle.getEndDate().toString());
            stmt.setFloat(4, cycle.getRemainingBalance());
            stmt.setFloat(5, cycle.getSafeDailyLimit());
            stmt.executeUpdate();

            // Retrieve the auto-generated ID and set it on the object
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cycle.setBudgetCycleId(keys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println("[CycleController] saveCycleToDB error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a BudgetCycle object.
     * Used by checkActiveCycle() to rebuild state from DB on app launch.
     */
    private BudgetCycle mapRowToCycle(ResultSet rs) throws SQLException {
        BudgetCycle cycle = new BudgetCycle(
            rs.getFloat("totalAmount"),
            LocalDate.parse(rs.getString("startDate")),
            LocalDate.parse(rs.getString("endDate"))
        );
        cycle.setBudgetCycleId(rs.getInt("budgetCycleId"));
        cycle.setRemainingBalance(rs.getFloat("remainingBalance"));
        cycle.setSafeDailyLimit(rs.getFloat("safeDailyLimit"));
        cycle.setActive(rs.getBoolean("isActive"));
        return cycle;
    }
}
