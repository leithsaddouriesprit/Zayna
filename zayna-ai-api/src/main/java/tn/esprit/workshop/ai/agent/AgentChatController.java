package tn.esprit.workshop.ai.agent;

import org.springframework.web.bind.annotation.*;
import tn.esprit.workshop.ai.agent.dto.AgentChatRequestDto;
import tn.esprit.workshop.ai.agent.dto.AgentChatResponseDto;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read-only agent assistant API. Scope is always derived from {@code userId} → {@code agent_ecole.id_ecole}.
 */
@RestController
@RequestMapping("/ai/agent")
public class AgentChatController {

    private static final Logger LOG = Logger.getLogger(AgentChatController.class.getName());

    private final AgentChatService agentChatService;

    public AgentChatController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @PostMapping("/chat")
    public AgentChatResponseDto chat(@RequestBody AgentChatRequestDto request) {
        try {
            return agentChatService.handle(request);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Agent chat request failed", e);
            return AgentChatResponseDto.ok(
                    "Le service assistant agent est momentanément indisponible.",
                    "TECHNICAL_FAILURE");
        }
    }
}
