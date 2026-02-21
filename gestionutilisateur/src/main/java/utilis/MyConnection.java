package utilis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private String url = "jdbc:mysql://localhost:3306/vos";
    private String user = "root";
    private String pwd = "";

    private Connection cnx;

    private static MyConnection instance;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, user, pwd);
            System.out.println("✅ Connexion établie !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur connexion !");
            e.printStackTrace();
        }
    }

    public static MyConnection getInstance() {
        if (instance == null)
            instance = new MyConnection();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
