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

/*
 SD-5  : getSnapshotData()
 SD-7  : persistExpense(), restoreFromSettings(), updateCycleRemainingBalance()
 */

public class DAOLayer {

    private final Connection connection;

    public record Snapshot(LocalDate checkDate, float prevSpent, float deficit, boolean isNegative) { }

    public DAOLayer() {
        this.connection = Database.getInstance().getConnection();
    }

    //Used in SD-1
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

    //Used in SD-1 & SD-3 & SD-5 & SD-7
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

    //Used in SD-3
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

    //Used in SD-2
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

    //Used in SD-2 & SD-7
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

    //Used in SD-4
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

    //Used in SD-4
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

    //Used in SD-5
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

    //Used in SD-5
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

    //Used in SD-5 & SD-7
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

    //Used in SD-6
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

    //Used in SD-6
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

    // ── SD-7: SELECT * FROM Settings (restore on crash) ──────────────────────
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

    // ── US11: delete all expenses for a cycle ────────────────────────────────
    public void deleteExpensesByCycle(int cycleId) {
        String sql = "DELETE FROM Transactions WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] deleteExpensesByCycle: " + e.getMessage());
        }
    }

    // ── US11: delete a cycle ──────────────────────────────────────────────────
    public void deleteCycle(int cycleId) {
        String sql = "DELETE FROM Cycles WHERE budgetCycleId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cycleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAOLayer] deleteCycle: " + e.getMessage());
        }
    }

    // Settings: reset current cycle and related data
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

    // ── SD-7: save setting ───────────────────────────────────────────────────
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

    // HELPERS
    private BudgetCycle mapCycle(ResultSet rs) throws SQLException {
        BudgetCycle c = new BudgetCycle(
            rs.getFloat("totalAmount"),
            LocalDate.parse(rs.getString("startDate")),
            LocalDate.parse(rs.getString("endDate"))
        );
        c.setBudgetCycleId(rs.getInt("budgetCycleId"));
        c.setRemainingBalance(rs.getFloat("remainingBalance"));
        // Fixed daily limit (based on cycle length), then derive days-left fields for today.
        c.calculateWeeklyLimit();
        c.calculateBalance();
        c.setActive(rs.getBoolean("isActive"));
        return c;
    }

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
