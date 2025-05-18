package pjatk.tpo.tpo6.Services;

import org.springframework.stereotype.Service;
import pjatk.tpo.tpo6.Models.ReminderModel;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RemindersService {
    private Connection conn;

    //Establish connection
    public RemindersService() {
        try {
            conn = DriverManager.getConnection("jdbc:h2:mem:remindersdb", "sa", "");
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Create new table in memory database if it doesn't exist
    private void createTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS Reminders (ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                "NAME VARCHAR(30) NOT NULL," +
                "DESCRIPTION VARCHAR(100)," +
                "DATE DATETIME NOT NULL," +
                "COMPLETED BOOLEAN DEFAULT FALSE" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            boolean created = stmt.execute(query);
            if (created) {
                System.out.println("Table created");
            }
        }
    }

    //Get all reminders
    public List<ReminderModel> getReminders() {
        String query = "SELECT * FROM Reminders";
        return executeQuery(query);
    }

    //Get reminder by id
    public ReminderModel getReminder(int id) {
        String query = "SELECT * FROM Reminders WHERE ID = ?";
        return executeQuery(query, new Object[]{id}).getFirst();
    }

    //Set reminder to completed
    public void toggleCompleted(int id) {
        String query = "UPDATE Reminders SET COMPLETED = NOT COMPLETED WHERE ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Get all completed reminders
    public List<ReminderModel> getCompletedReminders() {
        return executeQuery("SELECT * FROM Reminders WHERE COMPLETED = TRUE");
    }

    //Insert new reminder
    public ReminderModel insertReminder(ReminderModel reminder) {
        String query = "INSERT INTO Reminders (NAME, DESCRIPTION, DATE) VALUES (?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, reminder.getName());
            stmt.setString(2, reminder.getDescription());
            stmt.setObject(3, reminder.getDate());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reminder.setId(generatedKeys.getInt(1));
                    return reminder;
                }
            }
            throw new SQLException();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert reminder", e);
        }
    }

    //Delete reminder
    public boolean deleteReminder(int id) {
        String query = "DELETE FROM Reminders WHERE ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete reminder", e);
        }
    }

    //Update reminder
    public boolean updateReminder(ReminderModel reminder) {
        String query = "UPDATE Reminders SET NAME = ?, DESCRIPTION = ?, DATE = ? WHERE ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, reminder.getName());
            stmt.setString(2, reminder.getDescription());
            stmt.setObject(3, reminder.getDate());
            stmt.setInt(4, reminder.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reminder", e);
        }
    }


    //Execute query and return the returned reminders
    private List<ReminderModel> executeQuery(String query) {
        List<ReminderModel> reminders = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            queryToList(reminders, stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reminders;
    }

    //Execute query with multiple parameters and return the returned reminders
    private List<ReminderModel> executeQuery(String query, Object[] parameters) {
        List<ReminderModel> reminders = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                Object param = parameters[i];
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Timestamp) {
                    stmt.setTimestamp(i + 1, (Timestamp) param);
                }
            }
            queryToList(reminders, stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reminders;
    }

    //Add the returned reminders to a single list
    private void queryToList(List<ReminderModel> reminders, PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("ID");
            String name = rs.getString("NAME");
            String description = rs.getString("DESCRIPTION");
            LocalDateTime date = rs.getObject("DATE", LocalDateTime.class);
            boolean completed = rs.getBoolean("COMPLETED");
            reminders.add(new ReminderModel(id, name, description, date, completed));
        }
    }
}
