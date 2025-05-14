package test;

import model.*;

import java.util.*;

public class InverseTest {

    public static void main(String[] args) {
        Personne homme = new Personne("Jean", Genre.HOMME);
        Personne femme = new Personne("Marie", Genre.FEMME);

        Map<LienParente, LienParente> attentesHomme = new HashMap<>();
        attentesHomme.put(LienParente.PERE, LienParente.FILS);
        attentesHomme.put(LienParente.MERE, LienParente.FILS);
        attentesHomme.put(LienParente.FILS, LienParente.PERE);
        attentesHomme.put(LienParente.FILLE, LienParente.PERE);
        attentesHomme.put(LienParente.ONCLE, LienParente.NEVEU);
        attentesHomme.put(LienParente.TANTE, LienParente.NEVEU);
        attentesHomme.put(LienParente.GRAND_PERE, LienParente.PETIT_FILS);
        attentesHomme.put(LienParente.GRAND_MERE, LienParente.PETIT_FILS);

        Map<LienParente, LienParente> attentesFemme = new HashMap<>();
        attentesFemme.put(LienParente.PERE, LienParente.FILLE);
        attentesFemme.put(LienParente.MERE, LienParente.FILLE);
        attentesFemme.put(LienParente.FILS, LienParente.MERE);
        attentesFemme.put(LienParente.FILLE, LienParente.MERE);
        attentesFemme.put(LienParente.ONCLE, LienParente.NIECE);
        attentesFemme.put(LienParente.TANTE, LienParente.NIECE);
        attentesFemme.put(LienParente.GRAND_PERE, LienParente.PETITE_FILLE);
        attentesFemme.put(LienParente.GRAND_MERE, LienParente.PETITE_FILLE);

        System.out.println("🔎 Tests pour homme:");
        verifierLiens(homme, attentesHomme);

        System.out.println("\n🔎 Tests pour femme:");
        verifierLiens(femme, attentesFemme);
    }

    public static void verifierLiens(Personne p, Map<LienParente, LienParente> attentes) {
        for (Map.Entry<LienParente, LienParente> entry : attentes.entrySet()) {
            LienParente donne = entry.getKey();
            LienParente attendu = entry.getValue();
            LienParente resultat = p.inverseLien(donne);

            if (resultat == attendu) {
                System.out.println("✅ " + donne + " -> " + resultat);
            } else {
                System.out.println("❌ " + donne + " -> " + resultat + " (attendu : " + attendu + ")");
            }
        }
    }
}

