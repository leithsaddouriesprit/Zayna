package tn.esprit.workshop.services.admin;

import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only data for admin display screens. No business logic changes.
 */
public class AdminDataService {

    public static final class AgentRow {
        public int id;
        public String nom;
        public String prenom;
        public int idEcole;
        public String ecoleNom;

        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public int getIdEcole() { return idEcole; }
        public String getEcoleNom() { return ecoleNom != null ? ecoleNom : "—"; }
    }

    public static final class TrajetRow {
        public int id;
        public String nom;
        public String ecoleNom;
        public String busMatricule;
        public String chauffeurNom;
        public int idEcole;
        public int idBus;
        public int enfantsCount;

        public int getId() { return id; }
        public String getNom() { return nom != null ? nom : ""; }
        public String getEcoleNom() { return ecoleNom != null ? ecoleNom : "—"; }
        public String getBusMatricule() { return busMatricule != null ? busMatricule : "—"; }
        public String getChauffeurNom() { return chauffeurNom != null ? chauffeurNom : "—"; }
        public int getEnfantsCount() { return enfantsCount; }
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public List<AgentRow> getAgentsWithSchool() throws SQLException {
        String sql = "SELECT ae.id, ae.nom, ae.prenom, ae.id_ecole, e.nom AS ecole_nom FROM agent_ecole ae LEFT JOIN ecole e ON ae.id_ecole = e.id ORDER BY ae.id";
        List<AgentRow> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AgentRow r = new AgentRow();
                r.id = rs.getInt("id");
                r.nom = rs.getString("nom");
                r.prenom = rs.getString("prenom");
                r.idEcole = rs.getInt("id_ecole");
                r.ecoleNom = rs.getString("ecole_nom");
                list.add(r);
            }
        }
        return list;
    }

    public List<TrajetRow> getTrajetsWithDetails(Integer filterIdEcole) throws SQLException {
        String sql = "SELECT t.id, t.nom, t.id_bus, t.id_ecole, e.nom AS ecole_nom, b.matricule AS bus_matricule, " +
                "ch.nom AS ch_nom, ch.prenom AS ch_prenom, " +
                "(SELECT COUNT(*) FROM enfant en WHERE en.trajet_id = t.id AND en.actif = 1) AS enfants_count " +
                "FROM trajet t " +
                "LEFT JOIN ecole e ON t.id_ecole = e.id " +
                "LEFT JOIN bus b ON t.id_bus = b.id " +
                "LEFT JOIN chauffeur ch ON b.id_chauffeur = ch.id " +
                (filterIdEcole != null ? "WHERE t.id_ecole = ? " : "") +
                "ORDER BY t.id";
        List<TrajetRow> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (filterIdEcole != null) ps.setInt(1, filterIdEcole);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TrajetRow r = new TrajetRow();
                    r.id = rs.getInt("id");
                    r.nom = rs.getString("nom");
                    r.idBus = rs.getInt("id_bus");
                    if (rs.wasNull()) r.idBus = 0;
                    r.idEcole = rs.getInt("id_ecole");
                    r.ecoleNom = rs.getString("ecole_nom");
                    r.busMatricule = rs.getString("bus_matricule");
                    String cn = rs.getString("ch_nom");
                    String cp = rs.getString("ch_prenom");
                    r.chauffeurNom = (cn != null ? cn : "") + " " + (cp != null ? cp : "").trim();
                    if (r.chauffeurNom != null) r.chauffeurNom = r.chauffeurNom.trim();
                    r.enfantsCount = rs.getInt("enfants_count");
                    list.add(r);
                }
            }
        }
        return list;
    }
}
