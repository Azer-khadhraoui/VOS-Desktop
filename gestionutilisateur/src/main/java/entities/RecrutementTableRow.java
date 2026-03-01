package entities;

import javafx.beans.property.*;

/**
 * Represents both group header rows and detail rows in the recruitment table
 * This allows mixing of grouped and expanded item rows in a single TableView
 */
public class RecrutementTableRow {
    private final BooleanProperty isGroupHeader;
    private final SimpleIntegerProperty userId;
    private final SimpleStringProperty userName;
    private final SimpleIntegerProperty recrutementCount;
    private final BooleanProperty expanded;

    // Detail row properties
    private final SimpleIntegerProperty recruitmentId;
    private final SimpleStringProperty decisionDate;
    private final SimpleStringProperty decision;
    private final SimpleIntegerProperty interviewId;

    // Reference to parent group (for detail rows)
    private RecrutementGroup parentGroup;
    private Recrutement recrutement;

    /**
     * Constructor for GROUP HEADER row
     */
    public RecrutementTableRow(RecrutementGroup group) {
        this.isGroupHeader = new SimpleBooleanProperty(true);
        this.userId = new SimpleIntegerProperty(group.getUserId());
        this.userName = new SimpleStringProperty(group.getUserName());
        this.recrutementCount = new SimpleIntegerProperty(group.getRecrutementCount());
        this.expanded = new SimpleBooleanProperty(false);
        this.parentGroup = group;

        // Detail properties (unused for group header)
        this.recruitmentId = new SimpleIntegerProperty(0);
        this.decisionDate = new SimpleStringProperty("");
        this.decision = new SimpleStringProperty("");
        this.interviewId = new SimpleIntegerProperty(0);
    }

    /**
     * Constructor for DETAIL row
     */
    public RecrutementTableRow(RecrutementGroup parentGroup, Recrutement recrutement) {
        this.isGroupHeader = new SimpleBooleanProperty(false);
        this.parentGroup = parentGroup;

        // Group properties (unused for detail row)
        this.userId = new SimpleIntegerProperty(recrutement.getId_utilisateur());
        this.userName = new SimpleStringProperty("");
        this.recrutementCount = new SimpleIntegerProperty(0);
        this.expanded = new SimpleBooleanProperty(false);

        // Detail properties
        this.recruitmentId = new SimpleIntegerProperty(recrutement.getId_recrutement());
        this.decisionDate = new SimpleStringProperty(recrutement.getDate_decision().toString());
        this.decision = new SimpleStringProperty(recrutement.getDecision_finale());
        this.interviewId = new SimpleIntegerProperty(recrutement.getId_entretien());
        this.recrutement = recrutement;
    }

    // ========== Group Properties ==========
    public boolean isGroupHeader() {
        return isGroupHeader.get();
    }

    public BooleanProperty isGroupHeaderProperty() {
        return isGroupHeader;
    }

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

    public boolean isExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void setExpanded(boolean value) {
        expanded.set(value);
    }

    public void toggleExpanded() {
        expanded.set(!expanded.get());
    }

    // ========== Detail Properties ==========
    public int getRecruitmentId() {
        return recruitmentId.get();
    }

    public SimpleIntegerProperty recruitmentIdProperty() {
        return recruitmentId;
    }

    public String getDecisionDate() {
        return decisionDate.get();
    }

    public SimpleStringProperty decisionDateProperty() {
        return decisionDate;
    }

    public String getDecision() {
        return decision.get();
    }

    public SimpleStringProperty decisionProperty() {
        return decision;
    }

    public int getInterviewId() {
        return interviewId.get();
    }

    public SimpleIntegerProperty interviewIdProperty() {
        return interviewId;
    }

    // ========== Helper ==========
    public RecrutementGroup getParentGroup() {
        return parentGroup;
    }

    public Recrutement getRecrutement() {
        return recrutement;
    }
}
