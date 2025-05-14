package test;

import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import service.LienService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ArbreGenealogiqueTest {

    private ArbreGenealogique arbre;
    private Personne proprietaire;
    private Personne enfant;

    @BeforeEach
    public void setUp() {
        proprietaire = new Personne("000", "John", "Doe", LocalDate.of(1980, 1, 1), "français", "carte", "12474", Genre.HOMME, new Compte("johnjoe", "mdp1233", "0022", "glenndiffo12@gmail.com", "061715699", "4 rue"), new ArbreGenealogique(proprietaire));
        arbre = new ArbreGenealogique(proprietaire);
        arbre.getNoeuds().add(proprietaire);

        enfant = new Personne("001", "James", "Doe", LocalDate.of(2005, 1, 1), "français", "carte1", "124784", Genre.FEMME, new Compte("jamese", "mdp123", "0021", "diffoglenn007@gmail.com", "06171569", "4 rue"), new ArbreGenealogique(enfant));
    }

    @Test
    public void testCreationArbre() {
        assertEquals(1, arbre.getNoeuds().size());
        assertEquals(proprietaire, arbre.getProprietaire());
    }

    @Test
    public void testAjoutNoeud() {
        boolean resultat = arbre.ajouterLien(proprietaire, enfant, LienParente.FILLE);
        assertTrue(arbre.getNoeuds().contains(enfant));
        assertTrue(resultat);
    }

    @Test
    public void testVerifierCoherenceAge() {
        arbre.ajouterLien(proprietaire, enfant, LienParente.FILLE);
        // Appel manuel car la méthode ne renvoie pas d'erreur mais affiche à l'écran
        arbre.verifierCoherence();
    }

    @Test
    public void testModifierNoeud() {
        arbre.ajouterLien(proprietaire, enfant, LienParente.FILLE);
        Map<String, String> nouvellesInfos = new HashMap<>();
        nouvellesInfos.put("email", "jane.doe@email.com");
        nouvellesInfos.put("adresse", "123 rue du test");

        boolean modifie = arbre.modifierNoeud(enfant, nouvellesInfos);
        assertTrue(modifie);
        assertEquals("jane.doe@email.com", enfant.getCompte().getEmail());
    }

    @Test
    public void testSuppressionNoeud() {
        arbre.ajouterLien(proprietaire, enfant, LienParente.FILLE);
        boolean supprime = arbre.supprimerNoeud(enfant);
        assertTrue(supprime);
        assertFalse(arbre.getNoeuds().contains(enfant));
    }

    @Test
    public void testEnvoiEmailDemandeLien() {
        Personne autre = new Personne("78", "Smith", "Alice", LocalDate.of(1990, 1, 1), "espagnol", "carte2", "10247", Genre.FEMME, new Compte("adsmith", "smith4", "447171", null, null, null), new ArbreGenealogique(enfant) );
        autre.getCompte().setEmail("zenitsukaminari5@gmail.com");

        // Just for checking that no exceptions occur
        assertDoesNotThrow(() -> LienService.envoyerDemandeLien(proprietaire, autre, LienParente.SOEUR));
    }
}

