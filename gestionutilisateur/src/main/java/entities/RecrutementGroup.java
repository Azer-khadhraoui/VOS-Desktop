package entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Represents a grouped user with their recruitments
 */
public class RecrutementGroup {
    private final SimpleIntegerProperty userId;
    private final SimpleStringProperty userName;
    private final SimpleIntegerProperty recrutementCount;
    private final ObservableList<Recrutement> recrutements;
    private boolean expanded;

    public RecrutementGroup(int userId, String userName, List<Recrutement> recrutements) {
        this.userId = new SimpleIntegerProperty(userId);
        this.userName = new SimpleStringProperty(userName);
        this.recrutements = FXCollections.observableArrayList(recrutements);
        this.recrutementCount = new SimpleIntegerProperty(recrutements.size());
        this.expanded = false;
    }

    // Getters
    public int getUserId() {
        return userId.get();
    }

    public SimpleIntegerProperty userIdProperty() {
        return userId;
    }

    public String getUserName() {
        return userName.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public int getRecrutementCount() {
        return recrutementCount.get();
    }

    public SimpleIntegerProperty recrutementCountProperty() {
        return recrutementCount;
    }

    public ObservableList<Recrutement> getRecrutements() {
        return recrutements;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}
