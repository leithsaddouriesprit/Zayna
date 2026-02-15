
    package tn.esprit.workshop.model;

    public class Ecole {
        private int id;
        private String nom;
        private String position;
        private double prixMensuel;
        private String description;
        private String informations;
        private int agentId;  // ID de l'agent qui a créé cette école

        public Ecole() {}

        public Ecole(int id, String nom, String position, double prixMensuel, String description, String informations, int agentId) {
            this.id = id;
            this.nom = nom;
            this.position = position;
            this.prixMensuel = prixMensuel;
            this.description = description;
            this.informations = informations;
            this.agentId = agentId;
        }

        // Getters et Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public double getPrixMensuel() { return prixMensuel; }
        public void setPrixMensuel(double prixMensuel) { this.prixMensuel = prixMensuel; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getInformations() { return informations; }
        public void setInformations(String informations) { this.informations = informations; }

        public int getAgentId() { return agentId; }
        public void setAgentId(int agentId) { this.agentId = agentId; }
    }


