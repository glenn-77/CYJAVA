package entites;

import entites.enums.*;
import service.CoherenceVerifier;
import service.MailService;
import service.DemandeAdminService;
import service.DemandeAdminService.DemandeAdmin;
import service.AuthService;
import view.AffichageArbre;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an administrator account, inheriting from {@link Compte}.
 * The admin is responsible for validating or rejecting user requests related to the genealogical tree.
 * It includes logic to handle link additions, deletions, profile updates, and person removals.
 */
public class Admin extends Compte {

    private final String role;

    /**
     * Constructs a new admin account.
     *
     * @param login     the username
     * @param motDePasse the password
     * @param email     the email address
     * @param telephone the phone number
     * @param adresse   the postal address
     */
    public Admin(String login, String motDePasse,
                 String email, String telephone, String adresse) {
        super(login, motDePasse, email, telephone, adresse);
        this.role = "admin";
    }

    /**
     * Processes a user request. Depending on the type and whether it is accepted or not,
     * it performs the appropriate action and notifies the requester.
     *
     * @param demande the administrative request to process
     * @param accepter true to accept the request, false to reject
     */
    public void traiterDemande(DemandeAdmin demande, boolean accepter) throws IOException {
        Personne demandeur = demande.getDemandeur();
        Personne cible = demande.getCible();
        LienParente lien = demande.getLien();
        TypeDemande type = demande.getType();

        if (accepter) {
            switch (type) {
                case AJOUT_LIEN:
                    validerAjoutLien(demandeur, cible, lien);
                    break;
                case SUPPRESSION_LIEN:
                    AffichageArbre.reattribuerLienAprèsSuppression(cible, demandeur.getArbre());
                    break;
                case AJOUT_PERSONNE:
                    if (demandeur.equals(cible)) validerInscription(demandeur);
                    else {
                        creerPersonneSansCompte(cible.getNom(), cible.getPrenom(), cible.getDateNaissance(), cible.getNationalite(), cible.getGenre());
                        validerAjoutLien(demandeur, cible, lien);
                    }
                    break;
                case MODIFICATION_INFO:
                    try {
                        AuthService.modifierPersonne(cible.getNom(), cible.getPrenom(), cible.getNationalite(), cible.getGenre());
                        if (cible.getCompte().getEmail() != null) MailService.envoyerEmail(cible.getCompte().getEmail(),
                                "✅ Informations modifiées",
                                "Votre demande de modification de vos informations a été acceptée.");
                    } catch (IOException e) {
                        System.out.println("Erreur lors de la modification des informations : " + e.getMessage());
                    }
                    break;
                case SUPPRESSION_PERSONNE:
                    AffichageArbre.reattribuerLienAprèsSuppression(cible, demandeur.getArbre());
                    new AuthService().supprimerUtilisateurParNSS(cible.getNss());
                    MailService.envoyerEmail(demandeur.getCompte().getEmail(), "Suppression d'un membre de famille", "Votre demande de suppression de " + cible.getPrenom() + cible.getNom() + " a été validée");
                    break;
            }
            demande.setStatut(Statut.ACCEPTEE);
        } else {
            switch (type) {
                case AJOUT_LIEN:
                    refuserAjoutLien(demandeur, cible);
                    break;
                case SUPPRESSION_LIEN:
                    refuserSuppressionLien(demandeur, cible);
                    break;
                case AJOUT_PERSONNE:
                    new AuthService().supprimerUtilisateurParNSS(cible.getNss());
                    MailService.envoyerEmail(demandeur.getCompte().getEmail(), "Demande refusée ", "Votre demande d'ajout de " + cible.getNom() + cible.getPrenom() + " a été refusé.");
                    break;
                case MODIFICATION_INFO:
                    MailService.envoyerEmail(demandeur.getCompte().getEmail(), "Demande refusée ", "Votre demande de modification de " + cible.getNom() + cible.getPrenom() + " a été refusé.");
                    break;
                case SUPPRESSION_PERSONNE:
                    MailService.envoyerEmail(demandeur.getCompte().getEmail(), "Demande refusée ", "Votre demande de Suppression de " + cible.getNom() + cible.getPrenom() + " a été refusé.");
                    break;
            }
            demande.setStatut(Statut.REFUSEE);
        }

        DemandeAdminService.supprimerDemande(demande);
    }


    /**
     * Validates the addition of a relationship between two users.
     */
    public void validerAjoutLien(Personne demandeur, Personne cible, LienParente lien) {
        if (!isLienAutorise(lien)) {
            System.out.println("❌ Lien non autorisé : " + lien);
            return;
        }

        ArbreGenealogique arbreDemandeur = demandeur.getArbre();
        if (!arbreDemandeur.contient(cible)) {
            if (!CoherenceVerifier.verifierToutesLesCoherences(arbreDemandeur)) {
                arbreDemandeur.getNoeuds().remove(cible);
                return;
            }
            demandeur.ajouterLien(cible, lien);
            if (lien == LienParente.FILS || lien == LienParente.FILLE) demandeur.getEnfants().add(cible);
            if (lien == LienParente.PERE) demandeur.setPere(cible);
            if (lien == LienParente.MERE) demandeur.setMere(cible);
        }

        if (cible.isEstInscrit()) {
            LienParente lienInverse = cible.inverseLien(lien);
            if (lienInverse == LienParente.FILS || lienInverse == LienParente.FILLE) cible.getEnfants().add(demandeur);
            if (lienInverse == LienParente.PERE) cible.setPere(demandeur);
            if (lienInverse == LienParente.MERE) cible.setMere(demandeur);
        }

        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "✅ Demande acceptée",
                "Votre demande de lien avec " + cible.getNom() + " a été approuvée.");

    }

    /**
     * Sends a rejection email for a link addition request.
     */
    public void refuserAjoutLien(Personne demandeur, Personne cible) {
        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "❌ Demande refusée",
                "Votre demande de lien avec " + cible.getNom() + " a été refusée par l'administrateur.");
    }

    /**
     * Sends a rejection email for a link removal request.
     */
    public void refuserSuppressionLien(Personne demandeur, Personne cible) {
        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "❌ Suppression refusée",
                "Votre demande de suppression du lien avec " + cible.getNom() + " a été refusée par l'administrateur.");
    }

    /**
     * Validates a user registration request and notifies the user.
     */
    public void validerInscription(Personne p) {
        p.setValideParAdmin(true);
        new AuthService().mettreAJourUtilisateur(p);
        MailService.envoyerEmail(p.getCompte().getEmail(),
                "✅ Inscription validée",
                "Votre inscription a été validée. Vous pouvez maintenant vous connecter.");
    }


    /**
     * Checks if the requested relationship type is allowed.
     *
     * @param lien the relationship type
     * @return true if the link is allowed, false otherwise
     */
    private boolean isLienAutorise(LienParente lien) {
        return lien == LienParente.PERE || lien == LienParente.MERE
                || lien == LienParente.FILS || lien == LienParente.FILLE;
    }

    /**
     * Creates a person without an account for tree linking purposes.
     */
    public void creerPersonneSansCompte(String nom, String prenom, LocalDate dateNaissance, String nationalite, Genre genre) throws IOException {
        final String nss = UUID.randomUUID().toString().substring(0, 8);
        new Personne(nss, prenom, nom, dateNaissance, nationalite, null, null, genre, null, null);
        System.out.println("✅ Nouvelle personne créée : " + prenom + " " + nom);
    }
}
