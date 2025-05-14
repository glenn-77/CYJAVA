package model;

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

    private String codePrive;

    private Personne pere;
    private Personne mere;
    private Set<Personne> enfants;

    private boolean estInscrit;

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

        this.enfants = new HashSet<>();
        this.liensParente = new HashMap<>();
        this.estInscrit = false;
        this.niveauVisibilite = NiveauVisibilite.PUBLIQUE; 
        this.generation = 0;
        this.arbre = arbre;
    }

    public Personne(String nom, Genre genre){
        this.nom = nom;
        this.genre = genre;
        this.dateNaissance = LocalDate.now();
        this.nss = null;
        this.carteIdentite = null;
        this.prenom = null;
        this.nationalite = null;
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

    public void setCodePrive(String codePrive) {
        this.codePrive = codePrive;
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
            case ONCLE, TANTE -> this.genre == Genre.HOMME ? LienParente.NEVEU : LienParente.NIECE;
            case FILS, FILLE -> this.genre == Genre.HOMME ? LienParente.PERE : LienParente.MERE;
            case GRAND_MERE, GRAND_PERE -> this.genre == Genre.HOMME ? LienParente.PETIT_FILS : LienParente.PETITE_FILLE;
            case FRERE, SOEUR -> this.genre == Genre.HOMME ? LienParente.FRERE : LienParente.SOEUR;
            case PETIT_FILS, PETITE_FILLE -> this.genre == Genre.FEMME ? LienParente.GRAND_MERE : LienParente.GRAND_PERE;
            case NEVEU, NIECE -> this.genre == Genre.HOMME ? LienParente.ONCLE : LienParente.TANTE;
            case DEMI_FRERE, DEMI_SOEUR -> this.genre == Genre.HOMME ? LienParente.DEMI_FRERE : LienParente.DEMI_SOEUR;
            case BEAU_FILS, BELLE_FILLE -> this.genre == Genre.HOMME ? LienParente.BEAU_PERE : LienParente.BELLE_MERE;
            case BEAU_PERE, BELLE_MERE -> this.genre == Genre.HOMME ? LienParente.BEAU_FILS : LienParente.BELLE_FILLE;
            case ARRIERE_GRAND_PERE, ARRIERE_GRAND_MERE -> this.genre == Genre.HOMME ? LienParente.ARRIERE_PETIT_FILS : LienParente.ARRIERE_PETITE_FILLE;
            case ARRIERE_PETIT_FILS, ARRIERE_PETITE_FILLE -> this.genre == Genre.FEMME ? LienParente.ARRIERE_GRAND_MERE : LienParente.ARRIERE_GRAND_PERE;
            case COUSIN, COUSINE -> this.genre == Genre.HOMME ? LienParente.COUSIN : LienParente.COUSINE;
            default -> {
                System.out.println("⚠️ Lien inverse non défini pour : " + lien);
                yield null;
            }
        };
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
}
