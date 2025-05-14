package service;

import model.ArbreGenealogique;
import model.LienParente;
import model.Personne;

import java.util.*;

public class CoherenceVerifier {

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

    public static void verifierAutoLien(ArbreGenealogique arbre) {
        for (Personne p : arbre.getNoeuds()) {
            for (Personne autre : p.getLiens().keySet()) {
                if (p.equals(autre)) {
                    System.out.println("❌ Auto-lien détecté pour " + p.getNom());
                }
            }
        }
    }

    public static void verifierToutesLesCoherences(ArbreGenealogique arbre) {
        verifierReciprocite(arbre);
        verifierNbParents(arbre);
        verifierAutoLien(arbre);
        arbre.verifierCoherence(); // Appelle ta vérification de dates
    }
}
