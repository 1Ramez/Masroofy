package masroofy.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @file Database.java
 * @brief Handles database connection and table initialization.
 */

/**
 * @class Database
 * @brief Singleton class responsible for managing the SQLite database connection.
 */
public class Database {

    private static Database instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:masroofy.db";

    /**
     * @brief Private constructor that initializes the database connection and tables.
     */
    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            initTables();
            System.out.println("[Database] Connected.");
        } catch (Exception e) {
            System.err.println("[Database] Error: " + e.getMessage());
        }
    }

    /**
     * @brief Returns the singleton instance of the Database class.
     * @return Database instance
     */
    public static Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    /**
     * @brief Retrieves the active database connection.
     * @return Database connection object
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @brief Closes the database connection if it is open.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            System.err.println("[Database] Close error: " + e.getMessage());
        }
    }

    /**
     * @brief Creates database tables and inserts default data if necessary.
     * @throws SQLException If a database error occurs
     */
    private void initTables() throws SQLException {
        Statement s = connection.createStatement();

        s.execute("""
            CREATE TABLE IF NOT EXISTS Cycles (
                budgetCycleId    INTEGER PRIMARY KEY AUTOINCREMENT,
                totalAmount      REAL    NOT NULL,
                startDate        TEXT    NOT NULL,
                endDate          TEXT    NOT NULL,
                remainingBalance REAL    NOT NULL,
                safeDailyLimit   REAL    NOT NULL,
                isActive         INTEGER DEFAULT 1
            )""");

        s.execute("""
            CREATE TABLE IF NOT EXISTS Transactions (
                expenseId     INTEGER PRIMARY KEY AUTOINCREMENT,
                budgetCycleId INTEGER NOT NULL,
                amount        REAL    NOT NULL,
                categoryId    INTEGER NOT NULL,
                date          TEXT    NOT NULL,
                note          TEXT,
                FOREIGN KEY (budgetCycleId) REFERENCES Cycles(budgetCycleId)
            )""");

        s.execute("""
            CREATE TABLE IF NOT EXISTS Categories (
                categoryId INTEGER PRIMARY KEY AUTOINCREMENT,
                name       TEXT    NOT NULL,
                amount     REAL    DEFAULT 0,
                isDefault  INTEGER DEFAULT 0
            )""");

        s.execute("""
            CREATE TABLE IF NOT EXISTS Users (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT,
                pin      TEXT,
                selected INTEGER DEFAULT 0
            )""");

        s.execute("""
            CREATE TABLE IF NOT EXISTS AlertLog (
                alertId   INTEGER PRIMARY KEY AUTOINCREMENT,
                cycleId   INTEGER NOT NULL,
                message   TEXT    NOT NULL,
                type      TEXT    NOT NULL,
                isRead    INTEGER DEFAULT 0,
                createdAt TEXT    NOT NULL
            )""");

        s.execute("""
            CREATE TABLE IF NOT EXISTS DailySnapshots (
                snapshotId INTEGER PRIMARY KEY AUTOINCREMENT,
                cycleId    INTEGER NOT NULL,
                checkDate  TEXT    NOT NULL,
                prevSpent  REAL    NOT NULL,
                deficit    REAL    NOT NULL,
                isNegative INTEGER NOT NULL
            )""");

        s.execute("""
            CREATE TABLE IF NOT EXISTS Settings (
                key   TEXT PRIMARY KEY,
                value TEXT
            )""");

        s.execute("""
            INSERT OR IGNORE INTO Categories (categoryId, name, isDefault)
            VALUES (1,'Food',1),(2,'Transport',1),(3,'Entertainment',1),(4,'Other',1)
            """);

        s.close();
        System.out.println("[Database] Tables ready.");
    }
}