package pack_log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Connection_database {
    public Connection connection = null;
    public PreparedStatement ps = null;
    public void newConnect() throws SQLException {
        String url = "jdbc:postgresql://127.0.0.1:5432/DATA";
        String name = "postgres";
        String password = "qwerty";
        connection = DriverManager.getConnection(url, name, password);
    }
}
