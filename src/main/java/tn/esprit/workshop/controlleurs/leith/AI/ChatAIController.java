package tn.esprit.workshop.controlleurs.leith.AI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ChatAIController {

    @FXML private TextArea taChat;
    @FXML private TextField tfMessage;
    @FXML private Button btnSend;
    private Integer busId;
    private Integer enfantId;

    private final HttpClient http = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        taChat.appendText("AI: Salut ! Pose-moi une question sur ZAYNA.\n\n");
    }

    public void init(Integer busId, Integer enfantId) {
        this.busId = busId;
        this.enfantId = enfantId;
        taChat.appendText("Système: Contexte chargé (busId=" + busId + ", enfantId=" + enfantId + ")\n\n");

    }
    @FXML
    void openAI(ActionEvent event) {
        SceneNavigator.openChatAI(1, 1);
    }

    @FXML
    void send(ActionEvent event) {
        String msg = tfMessage.getText();
        if (msg == null || msg.isBlank()) return;

        taChat.appendText("Moi: " + msg + "\n");
        tfMessage.clear();

        btnSend.setDisable(true);

        new Thread(() -> {
            try {
                int b = (busId != null ? busId : 1);
                int e = (enfantId != null ? enfantId : 1);

                String json =
                        "{"
                                + "\"message\":\"" + escape(msg) + "\","
                                + "\"busId\":" + b + ","
                                + "\"enfantId\":" + e
                                + "}";
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/ai/chat"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

                System.out.println("STATUS = " + res.statusCode());
                System.out.println("BODY   = " + res.body());

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