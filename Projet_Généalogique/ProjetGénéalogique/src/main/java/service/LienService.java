package service;

import model.LienParente;
import model.Personne;

/**
 * Utility service to manage link requests and email notifications for link addition or deletion.
 */
public class LienService {

    /** Sends a link request and notifies the recipient by email. */
    public static void envoyerDemandeLien(Personne emetteur, Personne destinataire, LienParente lien) {
        try {
            Demande demande = new Demande(emetteur, destinataire, lien);
            DemandeService.ajouterDemande(demande);

            String sujet = "Demande de lien de parenté sur Arbre Généalogique Pro++";
            String corps = String.format(
                    "Bonjour %s,\n\n%s souhaite vous ajouter à son arbre généalogique comme \"%s\".\nVeuillez vous connecter pour accepter ou refuser cette demande.\n\nL'équipe Arbre Généalogique Pro++",
                    destinataire.getNom(),
                    emetteur.getNom() + " " + emetteur.getPrenom(),
                    lien.name().toLowerCase().replace("_", " ")
            );

            MailService.envoyerEmail(destinataire.getCompte().getEmail(), sujet, corps);
            System.out.println("Mail envoyé!");
        } catch (Exception e) {
            System.out.println("Echec de l'envoi du mail : " + e.getMessage());
        }
    }

    /** Accepts a link request and updates both trees. Sends confirmation emails. */
    public static boolean accepterDemandeLien(Demande demande) {
        try {
            Personne destinataire = demande.getDestinataire();
            Personne emetteur = demande.getEmetteur();
            LienParente lien = demande.getLien();

            destinataire.ajouterLien(emetteur, lien);
            DemandeService.supprimerDemande(demande);

            // Envoi des mails
            String sujet = "✅ Demande acceptée";
            String corpsEmetteur = String.format("Bonjour %s,\n\n%s a accepté votre demande de lien en tant que %s.",
                    emetteur.getNom(), destinataire.getNom(), lien);
            String corpsDestinataire = String.format("Bonjour %s,\n\nVous avez accepté la demande de %s.",
                    destinataire.getNom(), emetteur.getNom());

            MailService.envoyerEmail(emetteur.getCompte().getEmail(), sujet, corpsEmetteur);
            MailService.envoyerEmail(destinataire.getCompte().getEmail(), sujet, corpsDestinataire);

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /** Refuses a link request and notifies both parties. */
    public static boolean refuserDemandeLien(Demande demande) {
        try {
            Personne destinataire = demande.getDestinataire();
            Personne emetteur = demande.getEmetteur();
            LienParente lien = demande.getLien();

            DemandeService.supprimerDemande(demande);

            String sujet = "❌ Demande refusée";
            String corpsEmetteur = String.format("Bonjour %s,\n\n%s a refusé votre demande de lien en tant que %s.",
                    emetteur.getNom(), destinataire.getNom(), lien);
            String corpsDestinataire = String.format("Bonjour %s,\n\nVous avez refusé la demande de %s.",
                    destinataire.getNom(), emetteur.getNom());

            MailService.envoyerEmail(emetteur.getCompte().getEmail(), sujet, corpsEmetteur);
            MailService.envoyerEmail(destinataire.getCompte().getEmail(), sujet, corpsDestinataire);

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /** Sends a link removal request and notifies the recipient. */
    public static void demandeSuppressionLien(Personne emetteur, Personne destinataire, LienParente lien) {
        try {
            Demande demande = new Demande(emetteur, destinataire, lien);
            DemandeService.ajouterDemande(demande);

            String sujet = "Demande de lien de parenté sur Arbre Généalogique Pro++";
            String corps = String.format(
                    "Bonjour %s,\n\n%s souhaite vous supprimer de son arbre généalogique comme \"%s\".\n" +
                            "Veuillez vous connecter pour accepter ou refuser cette demande.\n\n" +
                            "L'équipe Arbre Généalogique Pro++",
                    destinataire.getNom(),
                    emetteur.getNom() + " " + emetteur.getPrenom(),
                    lien.name().toLowerCase().replace("_", "-")
            );

            MailService.envoyerEmail(destinataire.getCompte().getEmail(), sujet, corps);
            System.out.println("Mail envoyé!");
        } catch (Exception e) {
            System.out.println("Echec de l'envoi du mail : " + e.getMessage());
        }
    }

    /** Accepts a link removal request and updates the tree. */
    public static boolean accepterDemandeSuppression(Demande demande) {
        try {
            Personne destinataire = demande.getDestinataire();
            Personne emetteur = demande.getEmetteur();

            emetteur.supprimerLien(destinataire);
            DemandeService.supprimerDemande(demande);

            String sujet = "✅ Demande acceptée";
            String corpsEmetteur = String.format(
                    "Bonjour %s,\n\n%s a accepté votre demande de suppression de lien en tant que %s.",
                    demande.getEmetteur().getNom(),
                    destinataire.getNom(),
                    demande.getLien()
            );
            String corpsDestinataire = String.format(
                    "Bonjour %s,\n\nVous avez accepté la demande de suppression de %s.",
                    destinataire.getNom(),
                    demande.getEmetteur().getNom()
            );

            MailService.envoyerEmail(emetteur.getCompte().getEmail(), sujet, corpsEmetteur);
            MailService.envoyerEmail(destinataire.getCompte().getEmail(), sujet, corpsDestinataire);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Refuses a link removal request. */
    public static boolean refuserDemandeSuppression(Demande demande) {
        try {
            Personne destinataire = demande.getDestinataire();
            Personne emetteur = demande.getEmetteur();
            DemandeService.supprimerDemande(demande);

            String sujet = "❌ Demande refusée";
            String corpsEmetteur = String.format(
                    "Bonjour %s,\n\n%s a refusé votre demande de suppression de lien en tant que %s.",
                    destinataire.getNom(),
                    emetteur.getNom(),
                    demande.getLien()
            );
            String corpsDestinataire = String.format(
                    "Bonjour %s,\n\nVous avez refusé la demande de suppression de %s.",
                    destinataire.getNom(),
                    demande.getEmetteur().getNom()
            );

            MailService.envoyerEmail(emetteur.getCompte().getEmail(), sujet, corpsEmetteur);
            MailService.envoyerEmail(destinataire.getCompte().getEmail(), sujet, corpsDestinataire);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
