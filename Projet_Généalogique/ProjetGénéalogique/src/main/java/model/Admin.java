package model;

import service.CoherenceVerifier;
import service.MailService;

import java.time.LocalDate;
import java.util.Map;

public class Admin extends Compte {

    private final String role;

    public Admin(String login, String motDePasse,
                 String email, String telephone, String adresse,
                 Personne proprietaire) {
        super(login, motDePasse, email, telephone, adresse);
        this.role = "admin";
    }

    public String getRole() {
        return role;
    }

    /**
     * Valide une demande d'ajout de lien faite par un utilisateur
     */
    public boolean validerAjoutLien(Personne demandeur, Personne cible, LienParente lien) {
        if (!isLienAutorise(lien)) {
            System.out.println("❌ Lien non autorisé : " + lien);
            return false;
        }

        // Ajout du lien dans l'arbre du demandeur
        ArbreGenealogique arbreDemandeur = demandeur.getArbre();
        if (!arbreDemandeur.contient(cible)) {
            arbreDemandeur.getNoeuds().add(cible);
            if (!CoherenceVerifier.verifierToutesLesCoherences(arbreDemandeur)) {
                arbreDemandeur.getNoeuds().remove(cible);
                return false;
            }
        }
        demandeur.ajouterLien(cible, lien);

        // Ajout du lien inverse dans l'arbre de la cible (si elle est inscrite)
        if (cible.isEstInscrit()) {
            ArbreGenealogique arbreCible = cible.getArbre();
            if (!arbreCible.contient(demandeur)) {
                arbreCible.getNoeuds().add(demandeur);
            }
            LienParente lienInverse = cible.inverseLien(lien);
            cible.ajouterLien(demandeur, lienInverse);
        }

        // Notification
        MailService.envoyerEmail(demandeur.getCompte().getEmail(),
                "✅ Demande acceptée",
                "Votre demande de lien avec " + cible.getNom() + " a été approuvée.");

        return true;
    }

    /**
     * Refuse une demande d'ajout de lien faite par un utilisateur
     */
    public void refuserAjoutLien(Personne demandeur, Personne cible, LienParente lien) {
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

        MailService.envoyerEmail(p1.getCompte().getEmail(),
                "✅ Suppression de lien acceptée",
                "Le lien entre vous et " + p2.getNom() + " a été supprimé par l'administrateur.");

        MailService.envoyerEmail(p2.getCompte().getEmail(),
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
                case "numero":
                    cible.getCompte().setNumero(valeur);
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
    public Personne creerPersonneSansCompte(String nom, String prenom, LocalDate dateNaissance, String nationalite, Genre genre) {
        Personne nouvelle = new Personne(null, prenom, nom, dateNaissance, nationalite, null, null, genre, null, null);
        System.out.println("✅ Nouvelle personne créée : " + prenom + " " + nom);
        return nouvelle;
    }
}
