package view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Personne;
import model.ArbreGenealogique;

import java.util.*;

public class AffichageArbre {

    private final Personne utilisateurConnecte;
    private final Stage stage;

    // Espacements standards pour les calculs d'affichage
    private static final double VERTICAL_SPACING = 200; // Espacement vertical entre les générations
    private static final double HORIZONTAL_SPACING = 150; // Espacement horizontal minimum entre nœuds

    // Pour éviter les doublons et gérer les cycles
    private final Map<Personne, double[]> positions = new HashMap<>();
    private final Set<Personne> dejaTraites = new HashSet<>();

    // Profondeur maximale de l'arbre (limite des générations descendantes)
    private static final int MAX_PROFONDEUR = 10;

    public AffichageArbre(Personne utilisateurConnecte, Stage stage) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.stage = stage;
    }

    public void afficher(Group group) {
        group.getChildren().clear();

        // Vérifications initiales
        if (utilisateurConnecte == null) {
            System.out.println("Erreur : Aucun utilisateur connecté pour afficher l'arbre.");
            return;
        }

        ArbreGenealogique arbre = utilisateurConnecte.getArbre();
        if (arbre == null) {
            System.out.println("Erreur : L'utilisateur n'a pas d'arbre généalogique associé.");
            return;
        }

        Personne racine = arbre.getProprietaire();
        if (racine == null) {
            System.out.println("Erreur : L'arbre généalogique n'a pas de propriétaire.");
            return;
        }

        // Construire une grille logique avec générations
        Map<Integer, List<Personne>> niveaux = creerNiveaux(racine);
        double centreX = group.getBoundsInLocal().getWidth() / 2;

        // Calculer les positions logiques des nœuds
        calculerPositions(niveaux, centreX);

        // Dessiner l'arbre complet (nœuds + liens)
        dessinerArbreEtLiens(group, racine, 0);
    }

    private Map<Integer, List<Personne>> creerNiveaux(Personne racine) {
        Map<Integer, List<Personne>> niveaux = new HashMap<>();
        construireNiveaux(racine, niveaux, 0);
        return niveaux;
    }

    private void construireNiveaux(Personne personne, Map<Integer, List<Personne>> niveaux, int niveau) {
        // Vérifiez si la personne est null, déjà visitée ou que la profondeur est atteinte
        if (personne == null || dejaTraites.contains(personne) || niveau > MAX_PROFONDEUR) {
            return; // Stopper la récursion
        }

        // Marquer la personne comme visitée
        dejaTraites.add(personne);

        // Ajouter la personne au niveau actuel
        niveaux.computeIfAbsent(niveau, k -> new ArrayList<>()).add(personne);

        // Explorer les parents (niveau supérieur)
        if (personne.getPere() != null) {
            construireNiveaux(personne.getPere(), niveaux, niveau - 1);
        }
        if (personne.getMere() != null) {
            construireNiveaux(personne.getMere(), niveaux, niveau - 1);
        }

        // Explorer les enfants (niveau inférieur)
        for (Personne enfant : personne.getEnfants()) {
            construireNiveaux(enfant, niveaux, niveau + 1);
        }
    }

    private void calculerPositions(Map<Integer, List<Personne>> niveaux, double centreX) {
        for (int niveau : niveaux.keySet()) {
            List<Personne> personnesNiveau = niveaux.get(niveau);
            int nombrePersonnes = personnesNiveau.size();

            // Largeur totale pour centrer le niveau
            double largeurTotale = nombrePersonnes * HORIZONTAL_SPACING;

            // Position de départ à gauche
            double xStart = centreX - (largeurTotale / 2);

            int index = 0;
            for (Personne personne : personnesNiveau) {
                double xPosition = xStart + (index * HORIZONTAL_SPACING);
                double yPosition = niveau * VERTICAL_SPACING;

                // Stocker la position dans la map
                positions.put(personne, new double[]{xPosition, yPosition});
                index++;
            }
        }
    }

    private void dessinerArbreEtLiens(Group group, Personne personne, int profondeur) {
        if (personne == null || !positions.containsKey(personne) || profondeur > MAX_PROFONDEUR) {
            return;
        }

        // Dessiner le nœud actuel
        double[] position = positions.get(personne);
        dessinerNoeud(group, personne.getPrenom() + " " + personne.getNom(), position[0], position[1]);

        // Dessiner les liens vers les parents
        if (personne.getPere() != null) {
            dessinerLien(group, personne, personne.getPere());
            dessinerArbreEtLiens(group, personne.getPere(), profondeur + 1);
        }

        if (personne.getMere() != null) {
            dessinerLien(group, personne, personne.getMere());
            dessinerArbreEtLiens(group, personne.getMere(), profondeur + 1);
        }

        // Dessiner les liens vers les enfants
        for (Personne enfant : personne.getEnfants()) {
            dessinerLien(group, enfant, personne);
            dessinerArbreEtLiens(group, enfant, profondeur + 1);
        }
    }

    private void dessinerNoeud(Group group, String nom, double x, double y) {
        double largeurNoeud = 130;
        double hauteurNoeud = 40;

        // Rectangle représentant le nœud
        Rectangle cadre = new Rectangle(x - largeurNoeud / 2, y - hauteurNoeud / 2, largeurNoeud, hauteurNoeud);
        cadre.setFill(Color.LIGHTYELLOW);
        cadre.setStroke(Color.BLACK);
        cadre.setArcWidth(10);
        cadre.setArcHeight(10);

        // Texte limité avec wrapping pour éviter toute surcharge
        Text texte = new Text(nom);
        texte.setWrappingWidth(largeurNoeud - 10); // Largeur limitée pour éviter les débordements
        texte.setX(cadre.getX() + 10); // Petit décalage horizontal
        texte.setY(cadre.getY() + hauteurNoeud / 1.5); // Centré verticalement
        texte.setFill(Color.BLACK);

        group.getChildren().addAll(cadre, texte);
    }

    private void dessinerLien(Group group, Personne enfant, Personne parent) {
        if (!positions.containsKey(enfant) || !positions.containsKey(parent)) {
            return;
        }

        double[] positionEnfant = positions.get(enfant);
        double[] positionParent = positions.get(parent);

        // Ligne reliant parent et enfant
        Line ligne = new Line(positionParent[0], positionParent[1] + 20,
                positionEnfant[0], positionEnfant[1] - 20);
        ligne.setStroke(Color.GRAY);
        ligne.setStrokeWidth(1.5);

        group.getChildren().add(ligne);
    }
}