package view;

import entites.enums.Genre;
import entites.enums.LienParente;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import entites.Personne;
import entites.ArbreGenealogique;
import service.CoherenceVerifier;


import java.util.*;

/**
 * A JavaFX-based graphical tree renderer for displaying a genealogical tree.
 * Includes logic for node layout, parent-child linking, and interactivity such as popups and coloring.
 */
public class AffichageArbre {

    private final Personne utilisateurConnecte;
    private final Stage stage;

    // Espacements standards pour les calculs d'affichage
    private static final double VERTICAL_SPACING = 200; // Espacement vertical entre les générations
    private static final double HORIZONTAL_SPACING = 150; // Espacement horizontal minimum entre nœuds
    private boolean darkMode = false;


    // Pour éviter les doublons et gérer les cycles
    private final Map<Personne, double[]> positions = new HashMap<>();
    private final Set<Personne> dejaTraites = new HashSet<>();

    // Profondeur maximale de l'arbre (limite des générations descendantes)
    private static final int MAX_PROFONDEUR = 10;

    /**
     * Constructs the tree visualization for the given connected user and JavaFX stage.
     *
     * @param utilisateurConnecte the logged-in user
     * @param stage               the JavaFX stage
     */
    public AffichageArbre(Personne utilisateurConnecte, Stage stage) {
        this.utilisateurConnecte = utilisateurConnecte;
        this.stage = stage;
    }

    /**
     * Renders the genealogical tree in the provided group pane.
     *
     * @param group the JavaFX group where the tree nodes and links will be drawn
     */
    public void afficher(Group group) {
        group.getChildren().clear();

        // Réinitialisation des structures d'état
        dejaTraites.clear();
        positions.clear();

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

    /**
     * Builds the hierarchy levels of the tree for layout purposes.
     */
    private Map<Integer, List<Personne>> creerNiveaux(Personne racine) {
        Map<Integer, List<Personne>> niveaux = new HashMap<>();
        construireNiveaux(racine, niveaux, 0);
        return niveaux;
    }

    /**
     * Recursively constructs tree levels by exploring parents and children.
     */
    private void construireNiveaux(Personne personne, Map<Integer, List<Personne>> niveaux, int niveau) {
        // Vérifiez si la personne est null, déjà visitée ou que la profondeur est atteinte
        if (personne == null) {
            System.err.println("⚠️ Tentative de construction avec une personne null.");
            return; // Stopper la récursion
        }
        if (dejaTraites.contains(personne)) {
            System.out.println("➖ Personne déjà traitée : " + personne.getPrenom() + " " + personne.getNom());
            return; // Éviter les cycles
        }
        if (niveau > MAX_PROFONDEUR) {
            System.out.println("⛔ Profondeur maximale atteinte pour : " + personne.getPrenom() + " " + personne.getNom());
            return;
        }

        if (!CoherenceVerifier.verifierToutesLesCoherences(personne.getArbre())) {
            System.err.println("Arbre non cohérent pour : " + personne.getPrenom() + " " + personne.getNom() + "");
            return;
        }

        // Marquer la personne comme visitée
        dejaTraites.add(personne);

        // Ajouter la personne au niveau actuel
        niveaux.computeIfAbsent(niveau, k -> new ArrayList<>()).add(personne);

        // Explorer les parents (niveau supérieur)
        if (personne.getPere() != null) {
            construireNiveaux(personne.getPere(), niveaux, niveau - 1);
            personne.ajouterLien(personne.getPere(), LienParente.PERE);
        } else {
            System.out.println("ℹ️ Aucun père défini pour " + personne.getPrenom() + " " + personne.getNom());
        }

        if (personne.getMere() != null) {
            construireNiveaux(personne.getMere(), niveaux, niveau - 1);
            personne.ajouterLien(personne.getMere(), LienParente.MERE);
        } else {
            System.out.println("ℹ️ Aucun mère définie pour " + personne.getPrenom() + " " + personne.getNom());
        }

        // Explorer les enfants (niveau inférieur)
        for (Personne enfant : personne.getEnfants()) {
            if (enfant == null) {
                System.err.println("⚠️ Enfant null détecté pour : " + personne.getPrenom() + " " + personne.getNom());
                continue; // Ignorer les enfants null
            }

            construireNiveaux(enfant, niveaux, niveau + 1);

            // Ajouter des liens basés sur le genre
            if (enfant.getGenre() != null) {
                if (enfant.getGenre() == Genre.HOMME) {
                    personne.ajouterLien(enfant, LienParente.FILS);
                } else if (enfant.getGenre() == Genre.FEMME) {
                    personne.ajouterLien(enfant, LienParente.FILLE);
                }
            } else {
                System.err.println("⚠️ Genre non défini pour : " + enfant.getPrenom() + " " + enfant.getNom());
            }
        }
    }

    /**
     * Computes screen positions for each person in the tree.
     */
    private void calculerPositions(Map<Integer, List<Personne>> niveaux, double centreX) {
        for (int niveau : niveaux.keySet()) {
            List<Personne> personnesNiveau = niveaux.get(niveau);
            int nombrePersonnes = personnesNiveau.size();

            // Largeur totale pour centrer le niveau
            double largeurTotale = nombrePersonnes * HORIZONTAL_SPACING;

            // Position de départ à gauche
            double xStart = centreX - (largeurTotale / 2);

            for (int i = 0; i < personnesNiveau.size(); i++) {
                Personne personne = personnesNiveau.get(i);
                double xPosition = xStart + (i * HORIZONTAL_SPACING);
                double yPosition = niveau * VERTICAL_SPACING;
                positions.put(personne, new double[]{xPosition, yPosition});
            }
        }
    }

    /**
     * Draws all nodes and links of the tree recursively.
     */
    private void dessinerArbreEtLiens(Group group, Personne personne, int profondeur) {
        if (personne == null || !positions.containsKey(personne) || profondeur > MAX_PROFONDEUR) {
            return;
        }

        // Dessiner le nœud actuel
        double[] position = positions.get(personne);
        dessinerNoeud(group, personne, position[0], position[1]);

        // Dessiner les liens vers les parents
        if (personne.getPere() != null && positions.containsKey(personne.getPere())) {
            dessinerLien(group, personne, personne.getPere());
            dessinerArbreEtLiens(group, personne.getPere(), profondeur + 1);
        }

        if (personne.getMere() != null && positions.containsKey(personne.getMere())) {
            dessinerLien(group, personne, personne.getMere());
            dessinerArbreEtLiens(group, personne.getMere(), profondeur + 1);
        }

        // Dessiner les liens vers les enfants
        for (Personne enfant : personne.getEnfants()) {
            if (enfant == null || !positions.containsKey(enfant)) {
                System.err.println("⚠️ Impossible de dessiner un lien : enfant manquant ou non positionné.");
                continue;
            }
            dessinerLien(group, enfant, personne);
            dessinerArbreEtLiens(group, enfant, profondeur + 1);
        }
    }


    /**
     * Renders a node (person) at the specified position.
     */
    private void dessinerNoeud(Group group, Personne personne, double x, double y) {
        double largeurNoeud = 150;
        double hauteurNoeud = 50;

        // Rectangle représentant le nœud
        Rectangle cadre = new Rectangle(x - largeurNoeud / 2, y - hauteurNoeud / 2, largeurNoeud, hauteurNoeud);
        cadre.setFill(personne.isEstInscrit() ? Color.LIGHTBLUE : Color.LIGHTGREEN);
        cadre.setStroke(Color.BLACK);
        cadre.setArcWidth(15);
        cadre.setArcHeight(15);

        // Vérification des règles de visibilité
        String prenomVisible = personne.getPrenomVisible(utilisateurConnecte);
        String nomVisible = personne.getNomVisible(utilisateurConnecte);

        // Texte limité avec les noms visibles
        Text texte = new Text(prenomVisible + " " + nomVisible);
        texte.setFont(Font.font("Arial", 14));
        texte.setX(cadre.getX() + 10);
        texte.setY(cadre.getY() + 30);
        texte.setFill(Color.DARKBLUE);

        // Effets sur le nœud (au survol et au clic)
        cadre.setOnMouseEntered(e -> cadre.setStroke(Color.DARKRED));
        cadre.setOnMouseExited(e -> cadre.setStroke(Color.BLACK));

        cadre.setOnMouseClicked((MouseEvent e) -> {
            // Afficher plus de détails dans un popup
            PersonneDetailView.showPopup(personne, utilisateurConnecte);
        });

        // Ajouter le rectangle et le texte au group
        group.getChildren().addAll(cadre, texte);
    }

    /**
     * Draws a line connecting two people in the tree.
     */
    private void dessinerLien(Group group, Personne enfant, Personne parent) {
        if (!positions.containsKey(enfant) || !positions.containsKey(parent)) {
            return;
        }

        double[] positionEnfant = positions.get(enfant);
        double[] positionParent = positions.get(parent);

        // Ligne reliant parent et enfant
        Line ligne = new Line(positionParent[0], positionParent[1] + 25,
                positionEnfant[0], positionEnfant[1] - 25);
        ligne.setStroke(Color.GRAY);
        ligne.setStrokeWidth(2);

        group.getChildren().add(ligne);
    }

    /**
     * Removes a person from the tree and reassigns their children to the person’s parents if available.
     * - The removed person is disconnected from the tree.
     * - Their children are re-linked to either the father or the mother, depending on availability.
     * - All corresponding relationship links are updated.
     *
     * @param cible the person to remove from the genealogical tree
     * @param arbre the genealogical tree from which the person is being removed
     */
    public static void reattribuerLienAprèsSuppression(Personne cible, ArbreGenealogique arbre) {
        // Step 1: retrieve parents
        Personne pere = cible.getPere();
        Personne mere = cible.getMere();

        // Step 2: for each child of the removed person
        for (Personne enfant : new ArrayList<>(cible.getEnfants())) {
            // Remove the link with the removed person
            enfant.supprimerLien(cible);
            if (cible.getGenre() == Genre.HOMME) enfant.setPere(null);
            else enfant.setMere(null);
            cible.getEnfants().remove(enfant);

            // Reassign to a grandparent if available
            if (pere != null) {
                if(enfant.getGenre() == Genre.HOMME) {
                    pere.ajouterLien(enfant, LienParente.FILS);
                }
                else pere.ajouterLien(enfant, LienParente.FILLE);
                pere.addEnfant(enfant);
                enfant.setPere(pere);
            } else if (mere != null) {
                if(enfant.getGenre() == Genre.HOMME) {
                    mere.ajouterLien(enfant, LienParente.FILS);
                }
                else mere.ajouterLien(enfant, LienParente.FILLE);
                mere.addEnfant(enfant);
                enfant.setMere(mere);
            }
        }

        // Step 3: remove the person from the tree
        arbre.getNoeuds().remove(cible);

        // Remove links to the person's parents
        if (pere != null) {
            pere.supprimerLien(cible);
            cible.setPere(null);
            pere.getEnfants().remove(cible);
        }
        if (mere != null) {
            mere.supprimerLien(cible);
            cible.setMere(null);
            mere.getEnfants().remove(cible);
        }
    }
}