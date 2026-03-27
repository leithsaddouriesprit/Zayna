package tn.esprit.workshop.model;

public class Meteo {
    private String ville;
    private double temperature;
    private double ressenti;
    private String description;
    private String icone;
    private int humidite;
    private double vent;

    public Meteo() {}

    // Getters et Setters
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getRessenti() { return ressenti; }
    public void setRessenti(double ressenti) { this.ressenti = ressenti; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public int getHumidite() { return humidite; }
    public void setHumidite(int humidite) { this.humidite = humidite; }

    public double getVent() { return vent; }
    public void setVent(double vent) { this.vent = vent; }

    // Obtenir l'icône en fonction de la description
    public String getEmoji() {
        if (description.contains("pluie")) return "🌧️";
        if (description.contains("nuage")) return "☁️";
        if (description.contains("soleil") || description.contains("clair")) return "☀️";
        if (description.contains("orage")) return "⛈️";
        if (description.contains("neige")) return "❄️";
        return "🌡️";
    }
}
