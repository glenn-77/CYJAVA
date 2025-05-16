package model;

import service.MailService;

import java.util.Map;

public class Admin extends Compte {

    private String role;

    public Admin(String login, String motDePasse, String numero,
                 String email, String telephone, String adresse,
                 Personne proprietaire, String role) {
        super(login, motDePasse, numero, email, telephone, adresse);
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
     * Vérifie si le lien proposé fait partie des liens autorisés
     */
    private boolean isLienAutorise(LienParente lien) {
        return lien == LienParente.PERE || lien == LienParente.MERE
                || lien == LienParente.FILS || lien == LienParente.FILLE;
    }

    /**
     * Modifie les champs autorisés d'une personne
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
}
