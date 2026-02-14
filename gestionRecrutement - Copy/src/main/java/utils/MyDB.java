package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {

    private Connection conn;
    private static MyDB instance;

    private final String url = "jdbc:mysql://localhost:3306/vos";
    private final String user = "root";
    private final String pwd = "";

    private MyDB() {
        try {
            conn = DriverManager.getConnection(url, user, pwd);
            System.out.println("Connexion établie !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null)
            instance = new MyDB();
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
}