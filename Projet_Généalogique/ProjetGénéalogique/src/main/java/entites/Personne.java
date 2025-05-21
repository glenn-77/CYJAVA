package entites;

import entites.enums.Genre;
import entites.enums.LienParente;
import entites.enums.NiveauVisibilite;
import javafx.scene.control.TreeItem;

import java.time.LocalDate;
import java.util.*;

public class Personne {

    private final String nss;  
    private final String prenom;
    private final String nom;
    private final LocalDate dateNaissance;
    private final String nationalite;
    private final String carteIdentite;
    private final Genre genre;
    private boolean estVivant;
    private String familleId;
    private String codePrive;
    private String urlPhoto;

    private Personne pere;
    private Personne mere;
    private Set<Personne> enfants;

    private boolean estInscrit;
    private boolean valideParAdmin;


    private NiveauVisibilite niveauVisibilite;
    private int generation;
    private LienParente lien;
    private Map<Personne, LienParente> liensParente;
    private ArbreGenealogique arbre;

    private Compte compte; 

    public Personne(String nss, String prenom, String nom, LocalDate dateNaissance, 
                    String nationalite, String carteIdentite, String codePrive, Genre genre, Compte compte, ArbreGenealogique arbre) {
        this.nss = nss;
        this.prenom = prenom;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.nationalite = nationalite;
        this.carteIdentite = carteIdentite;
        this.codePrive = codePrive;
        this.genre = genre;
        this.compte = compte;
        this.setFamilleId("FAM_" + nom.toUpperCase());

        this.enfants = new HashSet<>();
        this.liensParente = new HashMap<>();
        this.estInscrit = false;
        this.setValideParAdmin(false);
        this.niveauVisibilite = NiveauVisibilite.PUBLIQUE; 
        this.generation = 0;
        this.arbre = arbre;
        this.estVivant = true;
    }

    public Personne(String nom,String prenom, LocalDate dateNaissance, String nationalite, Genre genre) {
        this.nom = nom;
        this.genre = genre;
        this.dateNaissance = dateNaissance;
        this.nss = null;
        this.carteIdentite = null;
        this.prenom = prenom;
        this.nationalite = nationalite;
    }
    
    public String getNss() {
        return nss;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public String getNationalite() {
        return nationalite;
    }

    public String getCarteIdentite() {
        return carteIdentite;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getCodePrive() {
        return codePrive;
    }

    public Personne getPere() {
        return pere;
    }

    public void setPere(Personne pere) {
        this.pere = pere;
    }

    public Personne getMere() {
        return mere;
    }

    public void setMere(Personne mere) {
        this.mere = mere;
    }

    public Set<Personne> getEnfants() {
        return enfants;
    }

    public void addEnfant(Personne enfant) {
        this.enfants.add(enfant);
    }

    public boolean isEstInscrit() {
        return estInscrit;
    }

    public void setEstInscrit(boolean estInscrit) {
        this.estInscrit = estInscrit;
    }

    public NiveauVisibilite getNiveauVisibilite() {
        return niveauVisibilite;
    }

    public void setNiveauVisibilite(NiveauVisibilite niveauVisibilite) {
        this.niveauVisibilite = niveauVisibilite;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public LienParente getLien() {
        return lien;
    }

    public void setLien(LienParente lien) {
        this.lien = lien;
    }

    public boolean infosIdentiques(Personne autre) {
        return this.equals(autre);
    }

    public boolean estVisiblePar(Personne demandeur) {
        if (this.compte instanceof Admin) {
            return true;
        }
        switch (this.niveauVisibilite) {
            case PUBLIQUE:
                return true;

            case PRIVEE:
                // Seul le propriétaire (créateur de l'arbre) peut voir cette personne
                return this.equals(demandeur);

            case PROTEGEE:
                // Vérifie si l’utilisateur est dans le même arbre généalogique
                ArbreGenealogique arbreUtilisateur = demandeur.getArbre();
                return arbreUtilisateur != null && arbreUtilisateur.contient(this);

            default:
                return false;
        }
    }

    public ArbreGenealogique getArbre() {
        return arbre;
    }

    public void setArbre(ArbreGenealogique arbre) {
        this.arbre = arbre;
    }

    public Map<Personne, LienParente> getLiens() {
        return this.liensParente;
    }

    public boolean isEstVivant() {
        return estVivant;
    }

    public void setEstVivant(boolean estVivant) {
        this.estVivant = estVivant;
    }

    public void ajouterLien(Personne autre, LienParente lien) {
        this.liensParente.put(autre, lien);
        if(autre.isEstInscrit()) autre.liensParente.put(this, inverseLien(lien));
    }

    public void supprimerLien(Personne autre) {
        this.liensParente.remove(autre);
        if(autre.isEstInscrit()) autre.liensParente.remove(this);

    }

    public LienParente inverseLien(LienParente lien) {
        return switch (lien) {
            case PERE, MERE -> this.genre == Genre.HOMME ? LienParente.FILS : LienParente.FILLE;
            case FILS, FILLE -> this.genre == Genre.HOMME ? LienParente.PERE : LienParente.MERE;

            default -> {
                System.out.println("⚠️ Lien inverse non défini pour : " + lien);
                yield null;
            }
        };
    }

    public void afficherFamille() {
        System.out.println("=== Famille de " + prenom + " " + nom + " ===");

        // Parents
        System.out.print("Père : ");
        if (pere != null) {
            System.out.println(pere.getPrenom() + " " + pere.getNom());
        } else {
            System.out.println("Inconnu");
        }

        System.out.print("Mère : ");
        if (mere != null) {
            System.out.println(mere.getPrenom() + " " + mere.getNom());
        } else {
            System.out.println("Inconnue");
        }

        // Frères et sœurs (au moins un parent en commun)
        Set<Personne> freresSoeurs = new HashSet<>();
        if (pere != null) {
            for (Personne enfantPere : pere.getEnfants()) {
                if (!enfantPere.equals(this)) freresSoeurs.add(enfantPere);
            }
        }
        if (mere != null) {
            for (Personne enfantMere : mere.getEnfants()) {
                if (!enfantMere.equals(this)) freresSoeurs.add(enfantMere);
            }
        }

        System.out.print("Frères/Sœurs : ");
        if (freresSoeurs.isEmpty()) {
            System.out.println("Aucun");
        } else {
            for (Personne frereSoeur : freresSoeurs) {
                System.out.print(frereSoeur.getPrenom() + " " + frereSoeur.getNom() + "; ");
            }
            System.out.println();
        }

        // Enfants
        System.out.print("Enfants : ");
        if (enfants.isEmpty()) {
            System.out.println("Aucun");
        } else {
            for (Personne enfant : enfants) {
                System.out.print(enfant.getPrenom() + " " + enfant.getNom() + "; ");
            }
            System.out.println();
        }
    }

    public TreeItem<String> genererArbreFamilial() {
        return genererArbreFamilial(new HashSet<>());
    }

    private TreeItem<String> genererArbreFamilial(Set<Personne> dejaVisites) {
        // Si cette personne a déjà été visitée, on arrête la récursion
        if (dejaVisites.contains(this)) {
            return null;
        }

        // Marquer cette personne comme visitée
        dejaVisites.add(this);

        // Créer le noeud racine pour cette personne
        TreeItem<String> racine = new TreeItem<>(
                prenom + " " + nom + " (" + (genre != null ? genre : "Genre inconnu") + ")"
        );

        // Ajouter les parents
        if (pere != null) {
            TreeItem<String> arbrePere = pere.genererArbreFamilial(dejaVisites);
            if (arbrePere != null) {
                racine.getChildren().add(arbrePere);
            }
        }
        if (mere != null) {
            TreeItem<String> arbreMere = mere.genererArbreFamilial(dejaVisites);
            if (arbreMere != null) {
                racine.getChildren().add(arbreMere);
            }
        }

        // Ajouter chaque enfant
        for (Personne enfant : enfants) {
            TreeItem<String> arbreEnfant = enfant.genererArbreFamilial(dejaVisites);
            if (arbreEnfant != null) {
                racine.getChildren().add(arbreEnfant);
            }
        }

        return racine;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Personne autre = (Personne) obj;

        if (this.nss != null && autre.nss != null) {
            return this.nss.equals(autre.nss);
        }

        return Objects.equals(this.nom, autre.nom)
                && Objects.equals(this.prenom, autre.prenom)
                && Objects.equals(this.dateNaissance, autre.dateNaissance);
    }

    @Override
    public int hashCode() {
        return nss != null ?
                Objects.hash(nss) :
                Objects.hash(nom, prenom, dateNaissance);
    }

    public String getFamilleId() {
        return familleId;
    }

    public void setFamilleId(String familleId) {
        this.familleId = familleId;
    }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }

    public String getUrlPhoto() {
        if (urlPhoto == null) return "images/default.png";
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public boolean isValideParAdmin() {
        return valideParAdmin;
    }

    public void setValideParAdmin(boolean valideParAdmin) {
        this.valideParAdmin = valideParAdmin;
    }
}
