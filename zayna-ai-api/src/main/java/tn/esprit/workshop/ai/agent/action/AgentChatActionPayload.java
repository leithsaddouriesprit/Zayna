package tn.esprit.workshop.ai.agent.action;

/**
 * Données résolues pour une action en attente de confirmation (périmètre déjà validé côté résolution).
 */
public record AgentChatActionPayload(
        AgentChatActionType type,
        Integer candidatureChauffeurId,
        String chauffeurDisplayName,
        Integer candidatureEnfantId,
        String enfantDisplayName,
        Integer trajetIdForEnfantAccept,
        Integer maitresseId,
        String maitresseDisplayName,
        Integer busId,
        String busDisplayLabel
) {
    public static AgentChatActionPayload approveChauffeur(int id, String name) {
        return new AgentChatActionPayload(AgentChatActionType.APPROVE_CANDIDATURE_CHAUFFEUR, id, name,
                null, null, null, null, null, null, null);
    }

    public static AgentChatActionPayload rejectChauffeur(int id, String name) {
        return new AgentChatActionPayload(AgentChatActionType.REJECT_CANDIDATURE_CHAUFFEUR, id, name,
                null, null, null, null, null, null, null);
    }

    public static AgentChatActionPayload approveEnfant(int id, String name, Integer trajetId) {
        return new AgentChatActionPayload(AgentChatActionType.APPROVE_CANDIDATURE_ENFANT, null, null,
                id, name, trajetId, null, null, null, null);
    }

    public static AgentChatActionPayload rejectEnfant(int id, String name) {
        return new AgentChatActionPayload(AgentChatActionType.REJECT_CANDIDATURE_ENFANT, null, null,
                id, name, null, null, null, null, null);
    }

    public static AgentChatActionPayload assignMaitresse(int mid, String mName, int busId, String busLabel) {
        return new AgentChatActionPayload(AgentChatActionType.ASSIGN_MAITRESSE_TO_BUS, null, null,
                null, null, null, mid, mName, busId, busLabel);
    }

    public static AgentChatActionPayload unassignMaitresse(int mid, String mName) {
        return new AgentChatActionPayload(AgentChatActionType.UNASSIGN_MAITRESSE_FROM_BUS, null, null,
                null, null, null, mid, mName, null, null);
    }
}
