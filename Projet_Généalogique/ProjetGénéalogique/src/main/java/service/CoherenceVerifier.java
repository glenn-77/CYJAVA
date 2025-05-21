package service;

import entites.ArbreGenealogique;
import entites.enums.LienParente;
import entites.Personne;

import java.util.*;


/**
 * Provides various validation checks on a genealogical tree to ensure data consistency.
 * Includes checks for logical relationship structures, parental constraints, and chronological coherence.
 */
public class CoherenceVerifier {

    /**
     * Verifies that all declared relationships are reciprocal and symmetric.
     * For example, if A is the parent of B, then B must be the child of A.
     *
     * @param arbre the genealogical tree to verify
     * @return true if all reciprocal relationships are valid, false otherwise
     */
    public static boolean verifierReciprocite(ArbreGenealogique arbre) {
        for (Personne p : arbre.getNoeuds()) {
            for (Map.Entry<Personne, LienParente> entry : p.getLiens().entrySet()) {
                Personne autre = entry.getKey();
                LienParente lien = entry.getValue();
                LienParente lienAttendu = autre.inverseLien(lien);

                LienParente lienEffectif = autre.getLiens().get(p);
                if (lienEffectif != null && !lienEffectif.equals(lienAttendu)) {
                    System.out.println("❌ Incohérence de réciprocité entre " + p.getNom() + " et " + autre.getNom());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ensures that no person in the tree has more than two biological parents (one mother and one father).
     *
     * @param arbre the genealogical tree to verify
     * @return true if no one has more than two parents, false otherwise
     */
    public static boolean verifierNbParents(ArbreGenealogique arbre) {
        for (Personne enfant : arbre.getNoeuds()) {
            int nbParents = 0;
            for (LienParente lien : enfant.getLiens().values()) {
                if (lien == LienParente.PERE || lien == LienParente.MERE) {
                    nbParents++;
                }
            }
            if (nbParents > 2) {
                System.out.println("❌ Trop de parents biologiques pour " + enfant.getNom());
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies that no person is linked to themselves.
     *
     * @param arbre the genealogical tree to verify
     * @return true if no self-links exist, false otherwise
     */
    public static boolean verifierAutoLien(ArbreGenealogique arbre) {
        for (Personne p : arbre.getNoeuds()) {
            for (Personne autre : p.getLiens().keySet()) {
                if (p.equals(autre)) {
                    System.out.println("❌ Auto-lien détecté pour " + p.getNom());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks that relationship directions align with birth dates.
     * Parents must be born before their children, and vice versa.
     *
     * @param arbre the genealogical tree to verify
     * @return true if chronological coherence is respected, false otherwise
     */
    public static boolean verifierCoherence(ArbreGenealogique arbre) {
        Set<LienParente> liensAscendants = Set.of(
                LienParente.PERE, LienParente.MERE
        );

        Set<LienParente> liensDescendants = Set.of(
                LienParente.FILS, LienParente.FILLE
        );

        for (Personne p : arbre.getNoeuds()) {
            for (Map.Entry<Personne, LienParente> entry : p.getLiens().entrySet()) {
                Personne autre = entry.getKey();
                LienParente lien = entry.getValue();

                if (p.getDateNaissance() == null || autre.getDateNaissance() == null) continue;

                if (liensAscendants.contains(lien)) {
                    if (!autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                        System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né après " + p.getNom());
                        return false;
                    }
                } else if (liensDescendants.contains(lien)) {
                    if (autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                        System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né avant " + p.getNom());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Runs all coherence verification methods (reciprocity, parent count, self-link, and date-based logic).
     *
     * @param arbre the genealogical tree to verify
     * @return true if all checks pass, false otherwise
     */
    public static boolean verifierToutesLesCoherences(ArbreGenealogique arbre) {
        verifierReciprocite(arbre);
        verifierNbParents(arbre);
        verifierAutoLien(arbre);
        verifierCoherence(arbre);
        return verifierReciprocite(arbre) && verifierNbParents(arbre) && verifierAutoLien(arbre) && verifierCoherence(arbre);
    }
}
