package masroofy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import masroofy.model.BudgetCycle;
import masroofy.model.Category;
import masroofy.model.Expense;

/**
 * Provides database CRUD operations for Masroofy's SQLite schema.
 *
 * <p>This class centralizes SQL access for cycles, expenses, categories, alerts, daily snapshots, and
 * settings.</p>
 */
public class DAOLayer {

    private final Connection connection;

    /**
     * Daily snapshot record used to persist rollover results.
     *
     * @param checkDate the date the snapshot applies to
     * @param prevSpent the total spent on the previous day
     * @param deficit the difference between the safe daily limit and {@code prevSpent}
     * @param isNegative whether {@code deficit} is negative (overspent)
     */
    public record Snapshot(LocalDate checkDate, float prevSpent, float deficit, boolean isNegative) { }

    /**
     * Creates a DAO backed by the application's singleton database connection.
     */
    public DAOLayer() {
        this.connection = Database.getInstance().getConnection();
    }

    /**
     * Inserts a new budget cycle and returns its generated database id.
     *
     * @param cycle the cycle to insert
     * @return the generated id, or {@code -1} on failure
     */
    public int insertCycle(BudgetCycle cycle){
        String sql = """
            INSERT INTO Cycles
                (totalAmount, startDate, endDate, remainingBalance, safeDailyLimit, isActive)
            VALUES (?, ?, ?, ?, ?, 1)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setFloat(1, cycle.getTotalAmount());
            stmt.setString(2, cycle.getStartDate().toString());
            stmt.setString(3, cycle.getEndDate().toString());
            stmt.setFloat(4, cycle.getRemainingBalance());
            stmt.setFloat(5, cycle.getSafeDailyLimit());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                cycle.setBudgetCycleId(id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[DAOLayer] insertCycle: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Returns the currently active cycle, if any.
     *
     * @return the active {@link BudgetCycle}, or {@code null} when none exists
     */
    public BudgetCycle findActiveCycle() {
        String sql = "SELECT * FROM Cycles WHERE isActive = 1 LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return mapCycle(rs);
        } catch (SQLException e) {
            System.err.println("[DAOLayer] findActiveCycle: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the persisted safe daily limit and remaining balance for a cycle.
     *
     * @param cycleId the cycle id
     * @param newLimit the new safe daily limit
     * @param newBalance the new remaining balance
     */
    public void updateCycleSafeDailyLimit(int cycleId, float newLimit, float newBalance){
        String sql = "UPDATE Cycles SET safeDailyLimit=?, remainingBalance=? WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setFloat(1, newLimit);
            stmt.setFloat(2, newBalance);
            stmt.setInt(3, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] updateCycleSafeDailyLimit: " + e.getMessage());
        }
    }

    /**
     * Loads all categories.
     *
     * @return list of categories (possibly empty)
     */
    public List<Category> getCategoryList(){
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM Categories";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("categoryId"),
                    rs.getString("name"),
                    rs.getFloat("amount"),
                    rs.getBoolean("isDefault")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DAOLayer] getCategoryList: " + e.getMessage());
        }
        return list;
    }

    /**
     * Inserts a new expense transaction and returns its generated id.
     *
     * @param expense the expense to insert
     * @return the generated id, or {@code -1} on failure
     */
    public int insertExpense(Expense expense){
        String sql = """
            INSERT INTO Transactions (budgetCycleId, amount, categoryId, date, note)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setInt(1, expense.getBudgetCycleId());
            stmt.setFloat(2, expense.getAmount());
            stmt.setInt(3, expense.getCategoryId());
            stmt.setString(4, expense.getDate().toString());
            stmt.setString(5, expense.getNote());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()){
                int id = keys.getInt(1);
                expense.setExpenseId(id);
                return id;
            }
        }catch (SQLException e){
            System.err.println("[DAOLayer] insertExpense: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Loads all expenses for a given cycle ordered by date descending.
     *
     * @param cycleId the cycle id
     * @return list of expenses (possibly empty)
     */
    public List<Expense> getAllExpenses(int cycleId){
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE budgetCycleId=? ORDER BY date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapExpense(rs));
        }catch (SQLException e){
            System.err.println("[DAOLayer] getAllExpenses: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns the total amount spent on a specific date within a cycle.
     *
     * @param cycleId the cycle id
     * @param date the date to sum
     * @return total spent for that day
     */
    public float getTotalSpentOnDate(int cycleId, LocalDate date) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM Transactions WHERE budgetCycleId=? AND date=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.setString(2, date.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getFloat(1);
        } catch (SQLException e) {
            System.err.println("[DAOLayer] getTotalSpentOnDate: " + e.getMessage());
        }
        return 0f;
    }

    /**
     * Returns aggregated expense totals by category for a cycle.
     *
     * @param cycleId the cycle id
     * @return list of rows where each row is {@code [String categoryName, Float total]}
     */
    public List<Object[]> getExpensesByCategory(int cycleId){
        List<Object[]> result = new ArrayList<>();
        String sql = """
            SELECT c.name, SUM(t.amount) as total
            FROM Transactions t
            JOIN Categories c ON t.categoryId = c.categoryId
            WHERE t.budgetCycleId = ?
            GROUP BY t.categoryId
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new Object[]{rs.getString("name"), rs.getFloat("total")});
            }
        } catch (SQLException e) {
            System.err.println("[DAOLayer] getExpensesByCategory: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns the latest daily snapshot for a cycle.
     *
     * @param cycleId the cycle id
     * @return latest snapshot, or {@code null} if none exist
     */
    public Snapshot getLatestSnapshot(int cycleId){
        String sql = """
            SELECT checkDate, prevSpent, deficit, isNegative
            FROM DailySnapshots
            WHERE cycleId=?
            ORDER BY snapshotId DESC LIMIT 1
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setInt(1, cycleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Snapshot(
                    LocalDate.parse(rs.getString("checkDate")),
                    rs.getFloat("prevSpent"),
                    rs.getFloat("deficit"),
                    rs.getBoolean("isNegative")
                );
            }
        }catch (SQLException e){
            System.err.println("[DAOLayer] getLatestSnapshot: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a daily snapshot row for today's date.
     *
     * @param cycleId the cycle id
     * @param prevSpent total spent yesterday
     * @param deficit difference between safe daily limit and yesterday's spend
     * @param isNegative whether the user overspent (negative deficit)
     */
    public void insertDailySnapshot(int cycleId, float prevSpent, float deficit, boolean isNegative) {
        String sql = """
            INSERT INTO DailySnapshots (cycleId, checkDate, prevSpent, deficit, isNegative)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.setString(2, LocalDate.now().toString());
            stmt.setFloat(3, prevSpent);
            stmt.setFloat(4, deficit);
            stmt.setInt(5, isNegative ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] insertDailySnapshot: " + e.getMessage());
        }
    }

    /**
     * Updates remaining balance and safe daily limit for a cycle.
     *
     * @param cycleId the cycle id
     * @param newBalance the updated remaining balance
     * @param newDailyLimit the updated safe daily limit
     */
    public void updateCycleBalance(int cycleId, float newBalance, float newDailyLimit){
        String sql = "UPDATE Cycles SET remainingBalance=?, safeDailyLimit=? WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setFloat(1, newBalance);
            stmt.setFloat(2, newDailyLimit);
            stmt.setInt(3, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] updateCycleBalance: " + e.getMessage());
        }
    }

    /**
     * Updates only the remaining balance for a cycle.
     *
     * @param cycleId the cycle id
     * @param newBalance the updated remaining balance
     */
    public void updateCycleRemainingBalance(int cycleId, float newBalance) {
        String sql = "UPDATE Cycles SET remainingBalance=? WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setFloat(1, newBalance);
            stmt.setInt(2, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] updateCycleRemainingBalance: " + e.getMessage());
        }
    }

    /**
     * Inserts an alert log entry.
     *
     * @param cycleId the cycle id
     * @param message the alert message
     * @param type the alert type key used for de-duplication
     */
    public void insertAlertLog(int cycleId, String message, String type){
        String sql = """
            INSERT INTO AlertLog (cycleId, message, type, isRead, createdAt)
            VALUES (?, ?, ?, 0, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.setString(2, message);
            stmt.setString(3, type);
            stmt.setString(4, LocalDateTime.now().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] insertAlertLog: " + e.getMessage());
        }
    }

    /**
     * Returns whether an alert of the specified type has already been fired for the cycle.
     *
     * @param cycleId the cycle id
     * @param type the alert type key
     * @return {@code true} if at least one matching log row exists
     */
    public boolean wasAlertFired(int cycleId, String type) {
        String sql = "SELECT COUNT(*) FROM AlertLog WHERE cycleId=? AND type=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[DAOLayer] wasAlertFired: " + e.getMessage());
        }
        return false;
    }

    /**
     * Loads a persisted application setting.
     *
     * @param key the setting key
     * @return the stored value, or {@code null} if missing
     */
    public String getSetting(String key) {
        String sql = "SELECT value FROM Settings WHERE key=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) {
            System.err.println("[DAOLayer] getSetting: " + e.getMessage());
        }
        return null;
    }

    /**
     * Deletes all expenses for a cycle.
     *
     * @param cycleId the cycle id
     */
    public void deleteExpensesByCycle(int cycleId) {
        String sql = "DELETE FROM Transactions WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] deleteExpensesByCycle: " + e.getMessage());
        }
    }

    /**
     * Deletes the cycle row.
     *
     * @param cycleId the cycle id
     */
    public void deleteCycle(int cycleId) {
        String sql = "DELETE FROM Cycles WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] deleteCycle: " + e.getMessage());
        }
    }

    /**
     * Resets a cycle by deleting the cycle and all related transactions, snapshots, and alerts.
     *
     * @param cycleId the cycle id
     * @return {@code true} if the cycle row was deleted
     */
    public boolean resetCycle(int cycleId) {
        if (cycleId <= 0) return false;

        boolean previousAutoCommit;
        try {
            previousAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] resetCycle(autoCommit): " + e.getMessage());
            return false;
        }

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement delTransactions =
                     connection.prepareStatement("DELETE FROM Transactions WHERE budgetCycleId=?");
                 PreparedStatement delSnapshots =
                     connection.prepareStatement("DELETE FROM DailySnapshots WHERE cycleId=?");
                 PreparedStatement delAlerts =
                     connection.prepareStatement("DELETE FROM AlertLog WHERE cycleId=?");
                 PreparedStatement delCycle =
                     connection.prepareStatement("DELETE FROM Cycles WHERE budgetCycleId=?")) {

                delTransactions.setInt(1, cycleId);
                delTransactions.executeUpdate();

                delSnapshots.setInt(1, cycleId);
                delSnapshots.executeUpdate();

                delAlerts.setInt(1, cycleId);
                delAlerts.executeUpdate();

                delCycle.setInt(1, cycleId);
                int cyclesDeleted = delCycle.executeUpdate();

                connection.commit();
                return cyclesDeleted > 0;
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) { }
                System.err.println("[DAOLayer] resetCycle: " + e.getMessage());
                return false;
            } finally {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) { }
            }
        } catch (SQLException e) {
            System.err.println("[DAOLayer] resetCycle(tx): " + e.getMessage());
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) { }
            return false;
        }
    }

    /**
     * Saves (inserts or replaces) a setting value.
     *
     * @param key the setting key
     * @param value the value to store
     */
    public void saveSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO Settings (key, value) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] saveSetting: " + e.getMessage());
        }
    }

    /**
     * Maps a cycle row to a {@link BudgetCycle} and derives UI-friendly computed fields.
     *
     * @param rs result set positioned on a cycle row
     * @return mapped cycle
     * @throws SQLException if reading columns fails
     */
    private BudgetCycle mapCycle(ResultSet rs) throws SQLException {
        BudgetCycle c = new BudgetCycle(
            rs.getFloat("totalAmount"),
            LocalDate.parse(rs.getString("startDate")),
            LocalDate.parse(rs.getString("endDate"))
        );
        c.setBudgetCycleId(rs.getInt("budgetCycleId"));
        c.setRemainingBalance(rs.getFloat("remainingBalance"));
        c.calculateWeeklyLimit();
        c.calculateBalance();
        c.setActive(rs.getBoolean("isActive"));
        return c;
    }

    /**
     * Maps a transaction row to an {@link Expense}.
     *
     * @param rs result set positioned on an expense row
     * @return mapped expense
     * @throws SQLException if reading columns fails
     */
    private Expense mapExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense(
            rs.getFloat("amount"),
            rs.getInt("categoryId"),
            rs.getInt("budgetCycleId"),
            rs.getString("note")
        );
        e.setExpenseId(rs.getInt("expenseId"));
        e.setDate(LocalDate.parse(rs.getString("date")));
        return e;
    }
}
