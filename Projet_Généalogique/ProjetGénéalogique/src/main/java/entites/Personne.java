package entites;

import entites.enums.Genre;
import entites.enums.LienParente;
import entites.enums.NiveauVisibilite;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a person in the genealogical tree.
 * Contains identity data, relationships (parents, children), and account info.
 * Supports link management, visibility rules, and genealogical logic.
 */
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

    /**
     * Full constructor to initialize a person and link it to a user account and a tree.
     */
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
        this.familleId = "FAM" + this.nom.toUpperCase();

        this.enfants = new HashSet<>();
        this.liensParente = new HashMap<>();
        this.estInscrit = false;
        this.setValideParAdmin(false);
        this.niveauVisibilite = NiveauVisibilite.PUBLIQUE;
        this.generation = 0;
        this.arbre = arbre;
        this.estVivant = true;
    }

    /**
     * Lightweight constructor used for temporary or unregistered persons.
     */
    public Personne(String nom,String prenom, LocalDate dateNaissance, String nationalite, Genre genre){
        this.nom = nom;
        this.genre = genre;
        this.dateNaissance = dateNaissance;
        this.nss = null;
        this.carteIdentite = null;
        this.prenom = prenom;
        this.nationalite = nationalite;
    }

    // Getters, setters, and helper methods...
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

    /**
     * Adds a child to the person and updates tree links.
     */
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

    public LienParente getLien() {
        return lien;
    }

    public void setLien(LienParente lien) {
        this.lien = lien;
    }

    public boolean infosIdentiques(Personne autre) {
        return this.equals(autre);
    }

    /**
     * Checks if a person is visible to the requester based on visibility level and roles.
     */
    public boolean estVisiblePar(Personne demandeur) {
        if (this.compte instanceof Admin) {
            return true;
        }
        return switch (this.niveauVisibilite) {
            case PUBLIQUE -> true;
            case PRIVEE ->
                // Seul le propriétaire (créateur de l'arbre) peut voir cette personne
                    this.equals(demandeur);
            case PROTEGEE ->
                // Vérifie si le demandeur appartient au même arbre
                    this.arbre != null && this.arbre.contient(demandeur);
        };
    }

    /**
     * Returns the name or a hidden placeholder depending on visibility rules.
     */
    public String getNomVisible(Personne demandeur) {
        return this.estVisiblePar(demandeur) ? this.nom : "???";
    }

    /**
     * Returns the first name or a hidden placeholder depending on visibility rules.
     */
    public String getPrenomVisible(Personne demandeur) {
        return this.estVisiblePar(demandeur) ? this.prenom : "???";
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

    /**
     * Adds a bidirectional relationship between this person and another.
     *
     * @param autre the other person
     * @param lien the relationship from this person to the other
     */
    public void ajouterLien(Personne autre, LienParente lien) {
        this.liensParente.put(autre, lien);
        this.arbre.getNoeuds().add(autre);
        autre.liensParente.put(this, inverseLien(lien));
        if(autre.isEstInscrit()) {
            autre.arbre.getNoeuds().add(this);
        }
    }

    /**
     * Removes a bidirectional relationship with another person.
     */
    public void supprimerLien(Personne autre) {
        this.liensParente.remove(autre);
        this.arbre.getNoeuds().remove(autre);
        autre.liensParente.remove(this);
        if(autre.isEstInscrit()) {
            autre.arbre.getNoeuds().remove(this);
        }

    }

    /**
     * Determines the inverse of a given relationship depending on the person's gender.
     *
     * @param lien the current relationship
     * @return the inverse relationship
     */
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



    /**
     * Defines equality based on NSS (if present), or by identity data.
     */
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
