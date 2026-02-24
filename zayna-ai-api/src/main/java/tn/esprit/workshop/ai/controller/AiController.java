package tn.esprit.workshop.ai.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.workshop.ai.dto.ChatRequest;
import tn.esprit.workshop.ai.dto.ChatResponse;
import tn.esprit.workshop.ai.service.OllamaClient;
import tn.esprit.workshop.ai.service.ContextBuilderService;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final OllamaClient ollama;
    private final ContextBuilderService contextBuilder;
    public AiController(OllamaClient ollama, ContextBuilderService contextBuilder) {
        this.ollama = ollama;
        this.contextBuilder = contextBuilder;
    }





    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {

        String ctx = contextBuilder.build(req.busId, req.enfantId);

        String prompt =
                """
                Tu es l'assistant officiel de ZAYNA.
        
                RÈGLE ABSOLUE:
                - Utilise uniquement les données du JSON fourni dans CONTEXTE_ZAYNA.
                - N'invente aucune information.
                - Si l'information demandée n'est pas présente dans le JSON, réponds exactement:
                  "Je n'ai pas cette information."
        
                CONTEXTE_ZAYNA (JSON):
                """ + ctx + """

        QUESTION_UTILISATEUR:
        """ + req.message + """

        Réponds en français en une seule phrase claire.
        """;

        String answer = ollama.generate(prompt);
        return new ChatResponse(answer);

    }

    @GetMapping("/test")
    public ChatResponse test() {
        return new ChatResponse(ollama.generate("Réponds en une phrase : qui es-tu ?"));
    }
}
