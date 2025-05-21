package service;

import entites.Personne;
import entites.enums.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Service responsible for handling administrative requests (e.g., adding, modifying, or deleting individuals in the family tree).
 */
public class DemandeAdminService {

    /**
     * Represents an administrative request initiated by a user concerning another user or tree action.
     * Includes details such as requester, target, request type, relationship, and status.
     */
    public static class DemandeAdmin {
        private String id;
        private final Personne demandeur;
        private final Personne cible;
        private final LienParente lien;
        private Statut statut = Statut.EN_ATTENTE;
        private final TypeDemande type;
        private final LocalDate dateCreation = LocalDate.now();

        private static int compteur = 1;

        /**
         * Constructs a new administrative request.
         *
         * @param demandeur the user who submitted the request
         * @param cible     the person concerned by the request
         * @param lien      the type of relationship involved (e.g., parent, child)
         * @param type      the type of request (e.g., ADD, MODIFY, DELETE)
         */
        public DemandeAdmin(Personne demandeur, Personne cible, LienParente lien, TypeDemande type) {
            this.setId(String.format("%03d", compteur++));
            this.demandeur = demandeur;
            this.cible = cible;
            this.lien = lien;
            this.type = type;
        }

        /**
         * Gets the global counter used for assigning request IDs.
         *
         * @return the current counter value
         */
        public static int getCompteur() {
            return compteur;
        }

        public Personne getDemandeur() {
            return demandeur;
        }

        public Personne getCible() {
            return cible;
        }

        public LienParente getLien() {
            return lien;
        }

        public Statut getStatut() {
            return statut;
        }

        public void setStatut(Statut statut) {
            this.statut = statut;
        }

        public TypeDemande getType() {
            return type;
        }

        public LocalDate getDateCreation() {
            return dateCreation;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static final Set<DemandeAdmin> demandes = new HashSet<>();

    /**
     * Adds a new administrative request to the system.
     *
     * @param demande the request to add
     */
    public static void ajouterDemande(DemandeAdmin demande) {
        demandes.add(demande);
    }

    /**
     * Returns a copy of all administrative requests.
     *
     * @return a set of requests
     */
    public static Set<DemandeAdmin> getDemandes() {
        return new HashSet<>(demandes);
    }

    /**
     * Removes a given administrative request.
     *
     * @param demande the request to remove
     */
    public static void supprimerDemande(DemandeAdmin demande) {
        demandes.remove(demande);
    }
}
