package model;
import java.util.Optional;

public class CritereRecherche {
    private Optional<String> nom;
    private Optional<String> prenom;
    private Optional<Boolean> estVivant;
    private Optional<LienParente> lien;

    public CritereRecherche(String nom, String prenom, Boolean estVivant, LienParente lien) {
        this.nom = Optional.ofNullable(nom);
        this.prenom = Optional.ofNullable(prenom);
        this.estVivant = Optional.ofNullable(estVivant);
        this.lien = Optional.ofNullable(lien);
    }

    /**
     * Vérifie si une personne correspond au critère de recherche
     */
    public boolean match(Personne p) {
        // Si un critère est présent, il doit être respecté
        if (nom.isPresent() && !p.getNom().equalsIgnoreCase(nom.get())) return false;
        if (prenom.isPresent() && !p.getPrenom().equalsIgnoreCase(prenom.get())) return false;
        if (estVivant.isPresent() && p.isEstVivant() != estVivant.get()) return false;
        if (lien.isPresent()) {
            boolean lienTrouve = false;
            for (LienParente l : p.getLiens().values()) {
                if (l == lien.get()) {
                    lienTrouve = true;
                    break;
                }
            }
            if (!lienTrouve) return false;
        }

        return true;
    }
}

