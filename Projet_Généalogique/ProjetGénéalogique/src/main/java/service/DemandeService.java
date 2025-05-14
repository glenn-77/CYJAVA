package service;

import model.Demande;
import model.Personne;

import java.util.*;

/**
 * Centralized service for storing, retrieving, and managing pending family link requests.
 */
public class DemandeService {

    private static final List<Demande> demandesEnAttente = new ArrayList<>();

    /** Adds a new pending request. */
    public static void ajouterDemande(Demande demande) {
        demandesEnAttente.add(demande);
    }

    /** Returns all requests sent to a specific recipient. */
    public static List<Demande> getDemandesPour(Personne destinataire) {
        List<Demande> resultats = new ArrayList<>();
        for (Demande d : demandesEnAttente) {
            if (d.getDestinataire().equals(destinataire)) {
                resultats.add(d);
            }
        }
        return resultats;
    }

    /** Deletes a previously sent request. */
    public static void supprimerDemande(Demande demande) {
        demandesEnAttente.remove(demande);
    }
}

