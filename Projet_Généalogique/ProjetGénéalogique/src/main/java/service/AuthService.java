package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import model.ArbreGenealogique;
import model.Compte;
import model.Personne;
import initialisation.InitialisationCSV;

/**
 * Service d'authentification et gestion des utilisateurs et arbres généalogiques.
 */
public class AuthService {

    private final Map<String, Personne> utilisateurs; // Email -> Personne
    private final List<ArbreGenealogique> arbres; // Liste des arbres généalogiques

    private static final String UTILISATEURS_FILE_PATH = "Projet_Généalogique/ProjetGénéalogique/ressources/utilisateurs.csv";

    public AuthService() {
        this.utilisateurs = new HashMap<>();
        this.arbres = new ArrayList<>();
        try {
            chargerUtilisateursDepuisCSV();
            try (BufferedReader reader = new BufferedReader(new FileReader("ressources/compteur.txt"))) {
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

    private void chargerUtilisateursDepuisCSV() throws IOException {
        InitialisationCSV loader = new InitialisationCSV();
        try (var inputStream = getClass().getClassLoader().getResourceAsStream("utilisateurs.csv")) {
            if (inputStream == null) {
                throw new IOException("Fichier utilisateurs.csv introuvable dans les ressources.");
            }
            List<Personne> utilisateursList = loader.chargerUtilisateurs(inputStream);
            for (Personne personne : utilisateursList) {
                utilisateurs.put(personne.getCompte().getEmail(), personne);
            }
        }
    }

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

    public boolean existe(String email) {
        return utilisateurs.containsKey(email);
    }

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

    public List<ArbreGenealogique> getArbres() {
        return arbres;
    }

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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("ressources/compteur.txt"))) {
            writer.write(String.valueOf(Compte.getCompteur()));
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du compteur.");
        }
    }

    public void sauvegarderNouvelUtilisateur(Personne personne) throws IOException {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);

        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean fichierExiste = Files.exists(path);
        boolean fichierVide = !fichierExiste || Files.size(path) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            if (fichierVide) {
                writer.write("nss,prenom,nom,dateNaissance,nationalite,carteIdentite,email,telephone,adresse,codePrive,nssPere,nssMere,genre,login,motDePasse,numero,premiereConnexion");
                writer.newLine();
            }

            Compte c = personne.getCompte();
            String ligne = String.join(",",
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
                    c.isPremiereConnexion() ? "true" : "false");
            writer.write(ligne);
            writer.newLine();
        }
    }

    public void mettreAJourUtilisateur(Personne personne) {
        Path path = Paths.get(UTILISATEURS_FILE_PATH);
        List<String> lignes = new ArrayList<>();

        try {
            lignes = Files.readAllLines(path);
            String nss = personne.getNss();

            for (int i = 1; i < lignes.size(); i++) {
                String[] champs = lignes.get(i).split(",");
                if (champs.length > 12 && champs[0].equals(nss)) {
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
                            c.isPremiereConnexion() ? "true" : "false");

                    lignes.set(i, nouvelleLigne);
                    break;
                }
            }

            Files.write(path, lignes);
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

}
