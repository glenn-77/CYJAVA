package model;

import service.DemandeAdminService;
import service.MailService;

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

    public void demanderModification(Personne cible, LienParente lien) {
        // Vérifie que le lien est autorisé
        Set<LienParente> liensAutorises = Set.of(LienParente.PERE, LienParente.MERE, LienParente.FILS, LienParente.FILLE);
        if (!liensAutorises.contains(lien)) {
            System.out.println("❌ Ce type de lien n'est pas autorisé.");
            return;
        }

        // Crée une demande pour l’admin
        DemandeAdminService.DemandeAdmin demande = new DemandeAdminService.DemandeAdmin(this, cible, lien);
        DemandeAdminService.ajouterDemande(demande);

        // Envoie un mail à l’administrateur
        String sujet = "📬 Nouvelle demande de lien à valider";
        String corps = String.format(
                "Bonjour Admin,\n\n%s %s souhaite ajouter un lien \"%s\" avec %s %s.\n" +
                        "Merci de traiter cette demande depuis votre interface administrateur.",
                this.getPrenom(), this.getNom(),
                lien.name().toLowerCase(),
                cible.getPrenom(), cible.getNom()
        );

        MailService.envoyerEmail("diffoglenn007@gmail.com", sujet, corps);

        System.out.println("📨 Votre demande a été transmise à l’administrateur.");
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
