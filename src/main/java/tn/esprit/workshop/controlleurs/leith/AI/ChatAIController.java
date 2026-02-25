package tn.esprit.workshop.controlleurs.leith.AI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ChatAIController {

    @FXML private TextArea taChat;
    @FXML private TextField tfMessage;
    @FXML private Button btnSend;
    private Integer enfantId;

    private final HttpClient http = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        taChat.appendText("AI: Salut ! Pose-moi une question sur ZAYNA.\n\n");
    }

    /**
     * Initialise le contexte avec l'enfant sélectionné.
     * Si enfantId est null, utilise AppSession.getSelectedEnfantId().
     */
    public void init(Integer enfantId) {
        this.enfantId = enfantId != null ? enfantId : AppSession.getInstance().getSelectedEnfantId();
        /*
        if (this.enfantId != null) {
            taChat.appendText("Système: Contexte chargé (enfantId=" + this.enfantId + ").\n\n");
        }

         */
    }

    @FXML
    void send(ActionEvent event) {
        String msg = tfMessage.getText();
        if (msg == null || msg.isBlank()) return;

        Integer eid = enfantId != null ? enfantId : AppSession.getInstance().getSelectedEnfantId();
        if (eid == null) {
            taChat.appendText("AI: Erreur -> Aucun enfant sélectionné.\n\n");
            return;
        }

        taChat.appendText("Moi: " + msg + "\n");
        tfMessage.clear();
        btnSend.setDisable(true);

        int parentId = AppSession.getInstance().getParentId();

        new Thread(() -> {
            try {
                String json = "{"
                        + "\"message\":\"" + escape(msg) + "\","
                        + "\"enfantId\":" + eid + ","
                        + "\"parentId\":" + parentId
                        + "}";
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/ai/chat"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                String reply = extractReply(res.body());

                Platform.runLater(() -> {
                    taChat.appendText("AI: " + reply + "\n\n");
                    btnSend.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    taChat.appendText("AI: Erreur -> " + e.getMessage() + "\n\n");
                    btnSend.setDisable(false);
                });
            }
        }).start();
    }


    @FXML
    void goBack(ActionEvent event) {
        ((Stage) taChat.getScene().getWindow()).close();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Attendu: {"reply":"..."}
    private String extractReply(String body) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readTree(body).path("reply").asText();
        } catch (Exception e) {
            return body; // fallback
        }
    }


}