package utilis;

import entities.Utilisateur;

public class UserSession {
    
    private static UserSession instance;
    private Utilisateur currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
    }
}
