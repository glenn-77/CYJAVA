package service;

import model.Demande;
import model.Personne;

import java.util.*;

public class DemandeService {

    // Liste statique pour centraliser toutes les demandes
    private static final List<Demande> demandesEnAttente = new ArrayList<>();

    public static void ajouterDemande(Demande demande) {
        demandesEnAttente.add(demande);
    }

    public static List<Demande> getDemandesPour(Personne destinataire) {
        List<Demande> resultats = new ArrayList<>();
        for (Demande d : demandesEnAttente) {
            if (d.getDestinataire().equals(destinataire)) {
                resultats.add(d);
            }
        }
        return resultats;
    }

    public static void supprimerDemande(Demande demande) {
        demandesEnAttente.remove(demande);
    }
}

