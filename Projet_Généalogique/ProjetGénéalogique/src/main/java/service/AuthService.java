package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import entites.ArbreGenealogique;
import entites.Compte;
import entites.Personne;
import initialisation.InitialisationCSV;
import entites.enums.Genre;
import service.DemandeAdminService.DemandeAdmin;

/**
 * Service d'authentification et gestion des utilisateurs et arbres généalogiques.
 */
public class AuthService {

    private final Map<String, Personne> utilisateurs; // Email -> Personne
    private final List<ArbreGenealogique> arbres;// Liste des arbres généalogiques
    private final Set<DemandeAdmin> demandesAdmins;

    private static final String UTILISATEURS_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";
    private static final String DEMANDE_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/demandes.csv";

    public AuthService() {
        this.utilisateurs = new HashMap<>();
        this.arbres = new ArrayList<>();
        this.demandesAdmins = new HashSet<>();
        try {
            chargerUtilisateursDepuisCSV();
            try (BufferedReader reader = new BufferedReader(new FileReader("Projet_Généalogique/ProjetGénéalogique/ressources/compteur.txt"))) {
                String ligne = reader.readLine();
                if (ligne != null) {
                    int valeur = Integer.parseInt(ligne.trim());
                    Compte.setCompteur(valeur);
                }
            } catch (IOException e) {
                System.out.println("Aucun fichier de compteur trouvé, valeur par défaut utilisée.");
            }
            chargerDemandesDepuisCSV();
            try (BufferedReader reader = new BufferedReader(new FileReader("Projet_Généalogique/ProjetGénéalogique/ressources/compteurD.txt"))) {
                String ligne = reader.readLine();
                if (ligne != null) {
                    int valeur = Integer.parseInt(ligne.trim());
                    Compte.setCompteur(valeur);
                }
            } catch (IOException e) {
                System.out.println("Aucun fichier de compteur trouvé, valeur par défaut utilisée.");
            }
            chargerArbres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge les utilisateurs depuis un fichier CSV.
     */
    private void chargerUtilisateursDepuisCSV() throws IOException {
        InitialisationCSV loader = new InitialisationCSV();
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("utilisateurs.csv")) {
            if (inputStream == null) {
                throw new IOException("Fichier utilisateurs.csv introuvable dans les ressources.");
            }
            List<Personne> utilisateursList = loader.chargerUtilisateurs(inputStream);
            for (Personne personne : utilisateursList) {
                utilisateurs.put(personne.getNss(), personne); // Indexation par NSS
            }
        }
    }

    /**
     * Initialise les demandes administrateur.
     */
    private void chargerDemandesDepuisCSV() throws IOException {
        InitialisationCSV loader = new InitialisationCSV();
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("demandes.csv")) {
            if (inputStream == null) {
                throw new IOException("Fichier utilisateurs.csv introuvable dans les ressources.");
            }
            Set<DemandeAdmin> demandes = loader.chargerDemandes(inputStream);
            for (DemandeAdmin demande : demandes) {
                demandesAdmins.add(demande);
            }
        }
    }

    /**
     * Initialise les arbres généalogiques en fonction des utilisateurs.
     */
    private void chargerArbres() {
        Map<String, ArbreGenealogique> arbresParFamille = new HashMap<>();

        for (Personne utilisateur : utilisateurs.values()) {
            if (utilisateur.getArbre() != null) continue;

            Personne racine = trouverAncetre(utilisateur);
            String cleFamille = racine.getNss();

            if (!arbresParFamille.containsKey(cleFamille)) {
                ArbreGenealogique arbre = new ArbreGenealogique(racine);
                arbresParFamille.put(cleFamille, arbre);
                GlobalTreesManager.ajouterArbre(arbre);
                arbres.add(arbre);
            }

            ArbreGenealogique arbreFamille = arbresParFamille.get(cleFamille);
            utilisateur.setArbre(arbreFamille);
        }
    }

    private Personne trouverAncetre(Personne p) {
        Set<Personne> visites = new HashSet<>();
        while ((p.getPere() != null || p.getMere() != null) && !visites.contains(p)) {
            visites.add(p);
            if (p.getPere() != null) p = p.getPere();
            else if (p.getMere() != null) p = p.getMere();
        }
        return p;
    }

    /**
     * Vérifie si un utilisateur existe via son email.
     *
     * @param email L'email de l'utilisateur.
     * @return true s'il existe, sinon false.
     */
    public boolean existe(String email) {
        return utilisateurs.containsKey(email);
    }

    /**
     * Authentifie un utilisateur via son email et mot de passe.
     *
     * @param identifiant       L'email ou login de l'utilisateur.
     * @param motDePasse  Le mot de passe de l'utilisateur.
     * @return L'objet Personne correspondant, ou null si l'authentification échoue.
     */
    public Personne authentifier(String identifiant, String motDePasse) {
        for (Personne utilisateur : utilisateurs.values()) {
            Compte compte = utilisateur.getCompte();
            if ((compte.getEmail().equals(identifiant) || compte.getLogin().equals(identifiant) || utilisateur.getCodePrive().equals(identifiant))
                    && compte.getMotDePasse().equals(motDePasse)) {
                return utilisateur;
            }
        }
        return null;
    }

    /**
     * Ajoute un utilisateur et met à jour le fichier CSV.
     *
     * @param personne La personne à ajouter.
     */
    public void ajouterUtilisateur(Personne personne) {
        utilisateurs.put(personne.getCompte().getEmail(), personne);
        try {
            sauvegarderNouvelUtilisateur(personne);

            if (personne.getArbre() != null) {
                arbres.add(personne.getArbre());
                GlobalTreesManager.ajouterArbre(personne.getArbre());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Projet_Généalogique/ProjetGénéalogique/ressources/compteur.txt"))) {
            writer.write(String.valueOf(Compte.getCompteur()));
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du compteur.");
        }
    }

    public void ajouterDemande(DemandeAdmin demande){
        demandesAdmins.add(demande);
        DemandeAdminService.ajouterDemande(demande);
        try {
            sauvegarderDemande(demande);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Projet_Généalogique/ProjetGénéalogique/ressources/compteurD.txt"))) {
            writer.write(String.valueOf(DemandeAdmin.getCompteur()));
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du compteur.");
        }
    }

    /**
     * Sauvegarde uniquement un nouvel utilisateur dans le fichier CSV.
     *
     * @param personne La personne à enregistrer.
     * @throws IOException Si une erreur d'écriture se produit.
     */
    public void sauvegarderNouvelUtilisateur(Personne personne) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);

        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            if (fichierVide) {
                writer.write("nss,prenom,nom,dateNaissance,nationalite,carteIdentite,email,telephone,adresse,codePrive,nssPere,nssMere,genre,login,motDePasse,numero,premiereConnexion,familleId,photo,inscrit");
                writer.newLine();
            }
            String ligne;

            if (!personne.isEstInscrit()) {
                ligne = String.join(",",
                        "",
                        personne.getPrenom(),
                        personne.getNom(),
                        personne.getDateNaissance().toString(),
                        personne.getNationalite(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        personne.getMere() != null ? personne.getMere().getNss() : "",
                        personne.getPere() != null ? personne.getPere().getNss() : "",
                        personne.getGenre().toString(),
                        "",
                        "",
                        "",
                        "",
                        personne.getFamilleId(),
                        "",
                        "false");
            } else {
                Compte c = personne.getCompte();
                ligne = String.join(",",
                        personne.getNss(),
                        personne.getPrenom(),
                        personne.getNom(),
                        personne.getDateNaissance().toString(),
                        personne.getNationalite(),
                        personne.getCarteIdentite(),
                        c.getEmail(),
                        c.getTelephone(),
                        c.getAdresse(),
                        personne.getCodePrive(),
                        personne.getPere() != null ? personne.getPere().getNss() : "",
                        personne.getMere() != null ? personne.getMere().getNss() : "",
                        personne.getGenre().toString(),
                        c.getLogin(),
                        c.getMotDePasse(),
                        c.getNumero(),
                        c.isPremiereConnexion() ? "true" : "false",
                        personne.getFamilleId(),
                        personne.getUrlPhoto(),
                        personne.isEstInscrit() ? "true" : "false");
            }
            writer.write(ligne);
            writer.newLine();
        }

    }

    public void sauvegarderDemande(DemandeAdmin demande) throws IOException {
        Path path = Paths.get(DEMANDE_FILE_PATH);

        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            if (fichierVide) {
                writer.write("id,demandeurNSS,cibleNSS,dateCreation,lien,statut");
                writer.newLine();
            }
            String line = String.join(",",
                    demande.getId(),
                    demande.getType().toString(),
                    demande.getDemandeur().getNss(),
                    demande.getCible().getNss(),
                    demande.getDateCreation().toString(),
                    demande.getLien().toString(),
                    demande.getStatut().toString());
            writer.write(line);
            writer.newLine();
        }
    }

    public void mettreAJourUtilisateur(Personne personne) {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);
        List<String> lignes;

        try {
            lignes = Files.readAllLines(path);
            String nss = personne.getNss();

            for (int i = 1; i < lignes.size(); i++) {
                String[] champs = lignes.get(i).split(",");
                if (champs.length > 18 && champs[0].equals(nss)) {
                    Compte c = personne.getCompte();
                    String nouvelleLigne = String.join(",",
                            personne.getNss(),
                            personne.getPrenom(),
                            personne.getNom(),
                            personne.getDateNaissance().toString(),
                            personne.getNationalite(),
                            personne.getCarteIdentite(),
                            c.getEmail(),
                            c.getTelephone(),
                            c.getAdresse(),
                            personne.getCodePrive(),
                            personne.getPere() != null ? personne.getPere().getNss() : "",
                            personne.getMere() != null ? personne.getMere().getNss() : "",
                            personne.getGenre().toString(),
                            c.getLogin(),
                            c.getMotDePasse(),
                            c.getNumero(),
                            c.isPremiereConnexion() ? "true" : "false",
                            personne.getFamilleId(),
                            personne.getUrlPhoto(),
                            personne.isEstInscrit() ? "true" : "false");

                    lignes.set(i, nouvelleLigne);
                    break;
                }
            }

            Files.write(path, lignes);
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    // Modifie la nationalité et le genre d'une personne existante non inscrite identifiée par son NSS
    public static void modifierPersonne(String nomCible, String prenomCible, String nouvelleNat, Genre nouveauGenre) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);
        List<String> lignes = Files.readAllLines(path);
        for (int i = 1; i < lignes.size(); i++) {  // i=1 pour sauter l'en-tête
            String[] champs = lignes.get(i).split(",");
            if (champs.length >= 18 && champs[1].equals(prenomCible) && champs[2].equals(nomCible)) {
                // Mise à jour des champs Nationalité (index 4) et Genre (index 12)
                champs[4]  = nouvelleNat;
                champs[12] = nouveauGenre.toString();
                lignes.set(i, String.join(",", champs));
                break;
            }
        }
        Files.write(path, lignes);
    }

    public Personne getPersonneParNSS(String nss) {
        if (utilisateurs.containsKey(nss)) {
            System.out.println("✅ Personne trouvée pour NSS : " + nss); // DEBUG
            return utilisateurs.get(nss);
        } else {
            System.out.println("❌ Personne introuvable pour NSS : " + nss); // DEBUG
            return null;
        }
    }
}