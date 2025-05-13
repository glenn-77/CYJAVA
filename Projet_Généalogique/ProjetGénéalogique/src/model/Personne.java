package model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Personne {

    private final String nss;  
    private final String prenom;
    private final String nom;
    private final LocalDate dateNaissance;
    private final String nationalite;
    private final String carteIdentite;
    private final Genre genre;

    private String codePrive;

    private Personne pere;
    private Personne mere;
    private Set<Personne> enfants;

    private boolean estInscrit;

    private NiveauVisibilite niveauVisibilite; 
    private int generation;

    private Compte compte; 

    public Personne(String nss, String prenom, String nom, LocalDate dateNaissance, 
                    String nationalite, String carteIdentite, String codePrive, Genre genre, Compte compte) {
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
        this.estInscrit = false;
        this.niveauVisibilite = NiveauVisibilite.PUBLIQUE; 
        this.generation = 0;  
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
}
