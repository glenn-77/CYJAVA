package service;

import model.ArbreGenealogique;
import model.LienParente;
import model.Personne;

import java.util.*;

/**
 * Provides consistency checks on the genealogical tree to validate logical and biological rules.
 */
public class CoherenceVerifier {

    /** Ensures reciprocal relationships are symmetrical between two persons. */
    public static void verifierReciprocite(ArbreGenealogique arbre) {
        for (Personne p : arbre.getNoeuds()) {
            for (Map.Entry<Personne, LienParente> entry : p.getLiens().entrySet()) {
                Personne autre = entry.getKey();
                LienParente lien = entry.getValue();
                LienParente lienAttendu = autre.inverseLien(lien);

                LienParente lienEffectif = autre.getLiens().get(p);
                if (lienEffectif != null && !lienEffectif.equals(lienAttendu)) {
                    System.out.println("❌ Incohérence de réciprocité entre " + p.getNom() + " et " + autre.getNom());
                }
            }
        }
    }

    /** Ensures a person does not have more than two biological parents. */
    public static void verifierNbParents(ArbreGenealogique arbre) {
        for (Personne enfant : arbre.getNoeuds()) {
            int nbParents = 0;
            for (LienParente lien : enfant.getLiens().values()) {
                if (lien == LienParente.PERE || lien == LienParente.MERE) {
                    nbParents++;
                }
            }
            if (nbParents > 2) {
                System.out.println("❌ Trop de parents biologiques pour " + enfant.getNom());
            }
        }
    }

    /** Detects and flags if a person is linked to themselves. */
    public static void verifierAutoLien(ArbreGenealogique arbre) {
        for (Personne p : arbre.getNoeuds()) {
            for (Personne autre : p.getLiens().keySet()) {
                if (p.equals(autre)) {
                    System.out.println("❌ Auto-lien détecté pour " + p.getNom());
                }
            }
        }
    }

    /**
     * Checks date-of-birth-based consistency (e.g., parents must be older).
     */
    public static void verifierCoherence(ArbreGenealogique arbre) {
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
                    }
                } else if (liensDescendants.contains(lien)) {
                    if (autre.getDateNaissance().isBefore(p.getDateNaissance())) {
                        System.out.println("❌ Incohérence : " + autre.getNom() + " (" + lien + ") ne peut pas être né avant " + p.getNom());
                    }
                }
            }
        }
    }

    /** Runs all coherence verification methods. */
    public static void verifierToutesLesCoherences(ArbreGenealogique arbre) {
        verifierReciprocite(arbre);
        verifierNbParents(arbre);
        verifierAutoLien(arbre);
        verifierCoherence(arbre); // Appelle ta vérification de dates
    }
}
