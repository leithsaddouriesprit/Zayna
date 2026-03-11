package tn.esprit.workshop.ai.dto;

/**
 * Tracking Context Contract: request for AI chat.
 * Client can send full context (selectedChild, selectedBus, trackingSnapshot, dataQuality)
 * or legacy busId/enfantId so the API builds context from DB.
 */
public class ChatRequest {
    /** User message (required). */
    public String userMessage;
    /** Legacy: still supported for backward compatibility. */
    public String message;
    /** FR (default), EN, AR */
    public String language = "FR";
    /** PARENT, AGENT, DRIVER */
    public String userRole = "PARENT";
    public String sessionId;

    /** Selected child (optional if sent from client; otherwise filled from enfantId). */
    public SelectedChildDto selectedChild;
    /** Selected bus (optional; otherwise filled from busId). */
    public SelectedBusDto selectedBus;
    /** Tracking snapshot (optional; preferred when client has it; otherwise API builds from DB). */
    public TrackingSnapshotDto trackingSnapshot;
    /** Data quality flags (optional; API can compute from context). */
    public DataQualityDto dataQuality;

    // Legacy: API uses these to build context when trackingSnapshot/selectedChild not provided
    public Integer busId;
    public Integer enfantId;
    public Integer parentId;
}
