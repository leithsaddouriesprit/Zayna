package tn.esprit.workshop.utilis;

/**
 * Session applicative : parent connecté et enfant sélectionné (pas d'IDs statiques).
 */
public final class AppSession {

    private static final AppSession INSTANCE = new AppSession();

    private int parentId;
    private Integer selectedEnfantId;
    private Integer chauffeurId;
    /** id de la table agent_ecole (identité métier agent). */
    private Integer agentId;
    /** id de la table ecole (filtrage écrans Agent). */
    private Integer ecoleId;
    /** Nom affiché pour la barre de shell (ex. "Leith Saddouri"). */
    private String connectedUserName;
    /** Rôle affiché (ex. "Parent", "Chauffeur", "Agent École", "Administrateur"). */
    private String connectedUserRole;
    /** users.id du compte connecté (agent, maîtresse, etc.). */
    private Integer connectedUserId;
    /** maitresse.id pour l’espace maîtresse. */
    private Integer maitresseId;

    /** Parent : rétablir la sélection d’école au retour sur Demande transport. */
    private Integer pendingDemandeTransportEcoleRestoreId;
    /** Parent : école cible pour la page inscription (consommé au chargement). */
    private Integer pendingInscriptionEcoleId;
    /** Parent : école pour la page météo (coords / repli Tunis). */
    private Integer pendingMeteoEcoleId;

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

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public Integer getEcoleId() {
        return ecoleId;
    }

    public void setEcoleId(Integer ecoleId) {
        this.ecoleId = ecoleId;
    }

    public String getConnectedUserName() {
        return connectedUserName != null ? connectedUserName : "";
    }

    public void setConnectedUserName(String connectedUserName) {
        this.connectedUserName = connectedUserName;
    }

    public String getConnectedUserRole() {
        return connectedUserRole != null ? connectedUserRole : "";
    }

    public void setConnectedUserRole(String connectedUserRole) {
        this.connectedUserRole = connectedUserRole;
    }

    public Integer getConnectedUserId() {
        return connectedUserId;
    }

    public void setConnectedUserId(Integer connectedUserId) {
        this.connectedUserId = connectedUserId;
    }

    public Integer getMaitresseId() {
        return maitresseId;
    }

    public void setMaitresseId(Integer maitresseId) {
        this.maitresseId = maitresseId;
    }

    public void setPendingDemandeTransportEcoleRestoreId(Integer id) {
        this.pendingDemandeTransportEcoleRestoreId = id;
    }

    public Integer getAndClearPendingDemandeTransportEcoleRestoreId() {
        Integer id = pendingDemandeTransportEcoleRestoreId;
        pendingDemandeTransportEcoleRestoreId = null;
        return id;
    }

    public void setPendingInscriptionEcoleId(Integer id) {
        this.pendingInscriptionEcoleId = id;
    }

    public Integer getAndClearPendingInscriptionEcoleId() {
        Integer id = pendingInscriptionEcoleId;
        pendingInscriptionEcoleId = null;
        return id;
    }

    public void setPendingMeteoEcoleId(Integer id) {
        this.pendingMeteoEcoleId = id;
    }

    public Integer getAndClearPendingMeteoEcoleId() {
        Integer id = pendingMeteoEcoleId;
        pendingMeteoEcoleId = null;
        return id;
    }
}
