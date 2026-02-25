package tn.esprit.workshop.utilis;

/**
 * Session applicative : parent connecté et enfant sélectionné (pas d'IDs statiques).
 */
public final class AppSession {

    private static final AppSession INSTANCE = new AppSession();

    private int parentId;
    private Integer selectedEnfantId;
    private Integer chauffeurId;

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public Integer getSelectedEnfantId() {
        return selectedEnfantId;
    }

    public void setSelectedEnfantId(Integer selectedEnfantId) {
        this.selectedEnfantId = selectedEnfantId;
    }

    public Integer getChauffeurId() {
        return chauffeurId;
    }

    public void setChauffeurId(Integer chauffeurId) {
        this.chauffeurId = chauffeurId;
    }
}
