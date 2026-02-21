package tn.esprit.workshop.ai.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.workshop.ai.dto.ChatRequest;
import tn.esprit.workshop.ai.dto.ChatResponse;
import tn.esprit.workshop.ai.service.OllamaClient;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final OllamaClient ollama;

    public AiController(OllamaClient ollama) {
        this.ollama = ollama;
    }

    @GetMapping("/context")
    public String context() {
        return """
        Contexte ZAYNA:
        - busId: 12
        - nextStop: Ecole
        - etaMinutes: 8
        - speedKmh: 30
        - status: EN ROUTE
        """;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {

        String ctx = context(); // <-- ICI on récupère le contexte
        String prompt = ctx
                + "\nQuestion utilisateur: " + req.message
                + "\nRéponds brièvement et clairement.";

        String answer = ollama.generate(prompt);
        return new ChatResponse(answer);
    }

    @GetMapping("/test")
    public ChatResponse test() {
        return new ChatResponse(ollama.generate("Réponds en une phrase : qui es-tu ?"));
    }
}
