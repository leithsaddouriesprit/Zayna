package tn.esprit.workshop.services;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

    public class EcoleAffichageService {

        private Connection connection;

        public EcoleAffichageService() {
            connection = MyBDConnexion.getInstance().getConnection();
        }

        // ================= AFFICHER TOUTES LES ÉCOLES =================
        public List<Ecole> selectAllEcoles() throws SQLException {

            List<Ecole> ecoles = new ArrayList<>();
            String req = "SELECT * FROM ecole ORDER BY nom";

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {

                Ecole e = new Ecole();

                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setPosition(rs.getString("position"));
                e.setPrixMensuel(rs.getDouble("prix_mensuel"));
                e.setDescription(rs.getString("description"));
                e.setInformations(rs.getString("informations"));
                e.setAgentId(rs.getInt("agent_id"));

                ecoles.add(e);
            }

            return ecoles;
        }

        // ================= RECHERCHER PAR NOM =================
        public List<Ecole> searchEcoles(String nom) throws SQLException {

            List<Ecole> ecoles = new ArrayList<>();
            String req = "SELECT * FROM ecole WHERE nom LIKE ? ORDER BY nom";

            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Ecole e = new Ecole();

                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setPosition(rs.getString("position"));
                e.setPrixMensuel(rs.getDouble("prix_mensuel"));
                e.setDescription(rs.getString("description"));
                e.setInformations(rs.getString("informations"));
                e.setAgentId(rs.getInt("agent_id"));

                ecoles.add(e);
            }

            return ecoles;
        }

        // ================= COMPTER LES ÉCOLES =================
        public int countEcoles() throws SQLException {
            String req = "SELECT COUNT(*) FROM ecole";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

