package orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Connector {
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/soft_uni";
    private static Connection con;

    public static void createConnection(String user, String password, String dbName) throws SQLException {
        Properties prop = new Properties();
        prop.setProperty("user", user);
        prop.setProperty("password", password);
        con = DriverManager.getConnection(CONNECTION_STRING, prop);
        System.out.println("Connection successful");

    }

    public static Connection getConnection() {
        return con;
    }



}

