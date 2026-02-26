package utils;
//Singleton = Une SEULE instance de classe pour toute l'application


import java.sql.Connection; // Interface représentant une connexion BDD
import java.sql.DriverManager; // Gestionnaire de drivers JDBC (établit la connexion)
import java.sql.SQLException; // Exception lancée en cas d'erreur SQL

public class MyConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/vos";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private MyConnection() {}

    public static Connection getInstance() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connection established successfully!");
            } catch (SQLException e) {
                System.err.println("Connection failed: " + e.getMessage());
            }
        }
        return connection;
    }
}