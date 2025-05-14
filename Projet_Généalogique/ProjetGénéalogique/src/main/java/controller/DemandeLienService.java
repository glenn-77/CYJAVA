package controller;

import model.DemandeLien;
import model.Personne;

import java.util.*;

public class DemandeLienService {

    // Liste statique pour centraliser toutes les demandes
    private static final List<DemandeLien> demandesEnAttente = new ArrayList<>();

    public static void ajouterDemande(DemandeLien demande) {
        demandesEnAttente.add(demande);
    }

    public static List<DemandeLien> getDemandesPour(Personne destinataire) {
        List<DemandeLien> resultats = new ArrayList<>();
        for (DemandeLien d : demandesEnAttente) {
            if (d.getDestinataire().equals(destinataire)) {
                resultats.add(d);
            }
        }
        return resultats;
    }

    public static void supprimerDemande(DemandeLien demande) {
        demandesEnAttente.remove(demande);
    }
}

