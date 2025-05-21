package entites;

import entites.enums.*;
import service.CoherenceVerifier;
import service.MailService;
import service.DemandeAdminService;
import service.DemandeAdminService.DemandeAdmin;
import service.AuthService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class Admin extends Compte {

    private final String role;

    public Admin(String login, String motDePasse,
                 String email, String telephone, String adresse) {
        super(login, motDePasse, email, telephone, adresse);
        this.role = "admin";
    }

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
                    validerSuppressionLien(demandeur, cible);
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
            }
            demande.setStatut(Statut.REFUSEE);
        }

        DemandeAdminService.supprimerDemande(demande);
    }


    /**
     * Valide une demande d'ajout de lien faite par un utilisateur
     */
    public void validerAjoutLien(Personne demandeur, Personne cible, LienParente lien) {
        if (!isLienAutorise(lien)) {
            System.out.println("❌ Lien non autorisé : " + lien);
            return;
        }

        // Ajout du lien dans l'arbre du demandeur
        ArbreGenealogique arbreDemandeur = demandeur.getArbre();
        if (!arbreDemandeur.contient(cible)) {
            arbreDemandeur.getNoeuds().add(cible);
            if (!CoherenceVerifier.verifierToutesLesCoherences(arbreDemandeur)) {
                arbreDemandeur.getNoeuds().remove(cible);
                return;
            }
            demandeur.ajouterLien(cible, lien);
            if (lien == LienParente.FILS || lien == LienParente.FILLE) demandeur.getEnfants().add(cible);
            if (lien == LienParente.PERE) demandeur.setPere(cible);
            if (lien == LienParente.MERE) demandeur.setMere(cible);
        }

        // Ajout du lien inverse dans l'arbre de la cible (si elle est inscrite)
        if (cible.isEstInscrit()) {
            ArbreGenealogique arbreCible = cible.getArbre();
            if (!arbreCible.contient(demandeur)) {
                arbreCible.getNoeuds().add(demandeur);
            }
            LienParente lienInverse = cible.inverseLien(lien);
            cible.ajouterLien(demandeur, lienInverse);
            if (lienInverse == LienParente.FILS || lienInverse == LienParente.FILLE) cible.getEnfants().add(demandeur);
            if (lienInverse == LienParente.PERE) cible.setPere(demandeur);
            if (lienInverse == LienParente.MERE) cible.setMere(demandeur);
        }

        // Notification
        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "✅ Demande acceptée",
                "Votre demande de lien avec " + cible.getNom() + " a été approuvée.");

    }

    /**
     * Refuse une demande d'ajout de lien faite par un utilisateur
     */
    public void refuserAjoutLien(Personne demandeur, Personne cible) {
        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "❌ Demande refusée",
                "Votre demande de lien avec " + cible.getNom() + " a été refusée par l'administrateur.");
    }

    /**
     * Accepte une demande de suppression de lien entre deux personnes
     */
    public void validerSuppressionLien(Personne p1, Personne p2) {
        p1.supprimerLien(p2);
        p2.supprimerLien(p1);
        p1.getArbre().getNoeuds().remove(p2);
        p2.getArbre().getNoeuds().remove(p1);

        if (p1.getCompte().getEmail() != null) MailService.envoyerEmail(p1.getCompte().getEmail(),
                "✅ Suppression de lien acceptée",
                "Le lien entre vous et " + p2.getNom() + " a été supprimé par l'administrateur.");

        if (p2.getCompte().getEmail() != null) MailService.envoyerEmail(p2.getCompte().getEmail(),
                "✅ Suppression de lien acceptée",
                "Le lien entre vous et " + p1.getNom() + " a été supprimé par l'administrateur.");
    }

    /**
     * Refuse une demande de suppression de lien
     */
    public void refuserSuppressionLien(Personne demandeur, Personne cible) {
        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "❌ Suppression refusée",
                "Votre demande de suppression du lien avec " + cible.getNom() + " a été refusée par l'administrateur.");
    }

    public void validerInscription(Personne p) {
        p.setValideParAdmin(true);
        new AuthService().mettreAJourUtilisateur(p);
        MailService.envoyerEmail(p.getCompte().getEmail(),
                "✅ Inscription validée",
                "Votre inscription a été validée. Vous pouvez maintenant vous connecter.");
    }


    /**
     * Vérifie si le lien proposé fait partie des liens autorisés
     */
    private boolean isLienAutorise(LienParente lien) {
        return lien == LienParente.PERE || lien == LienParente.MERE
                || lien == LienParente.FILS || lien == LienParente.FILLE;
    }

    /**
     * Edits mutable fields of a person (email, phone, etc.), skipping immutable fields.
     * @param cible The person to edit.
     * @param nouvellesInfos Map of fields to update.
     * @return true if successful, false otherwise.
     */
    public boolean modifierChamps(Personne cible, Map<String, String> nouvellesInfos) {
        for (Map.Entry<String, String> entry : nouvellesInfos.entrySet()) {
            String champ = entry.getKey().toLowerCase();
            String valeur = entry.getValue();

            switch (champ) {
                case "adresse":
                    cible.getCompte().setAdresse(valeur);
                    break;
                case "email":
                    cible.getCompte().setEmail(valeur);
                    break;
                case "telephone":
                    cible.getCompte().setTelephone(valeur);
                    break;
                case "login":
                    cible.getCompte().setLogin(valeur);
                    break;
                case "motdepasse":
                    cible.getCompte().setMotDePasse(valeur);
                    break;
                default:
                    System.out.println("⚠️ Champ non modifiable ou inconnu : " + champ);
            }
        }

        MailService.envoyerEmail(cible.getCompte().getEmail(),
                "✏️ Mise à jour de votre fiche",
                "Votre profil a été mis à jour par l'administrateur.");

        return true;
    }

    /**
     * Crée une nouvelle personne non inscrite à partir d'un formulaire rempli par un utilisateur
     */
    public void creerPersonneSansCompte(String nom, String prenom, LocalDate dateNaissance, String nationalite, Genre genre) throws IOException {
        final String nss = UUID.randomUUID().toString().substring(0, 8);
        new Personne(nss, prenom, nom, dateNaissance, nationalite, null, null, genre, null, null);
        System.out.println("✅ Nouvelle personne créée : " + prenom + " " + nom);
    }
}
