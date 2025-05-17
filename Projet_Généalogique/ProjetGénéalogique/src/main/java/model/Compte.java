package model;

public class Compte {
    private String login;
    private String motDePasse;
    private String numero;
    private String email;
    private String telephone;
    private String adresse;
    private boolean isPremiereConnexion;
    private static int compteur = 17;


    public Compte(String login, String motDePasse, String email, String telephone, String adresse) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.numero = String.format("%03d", compteur);
        compteur++;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.isPremiereConnexion = true;
    }

    public static int getCompteur() {
        return compteur;
    }

    public static void setCompteur(int nvcompteur) {
        Compte.compteur = nvcompteur;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public boolean isPremiereConnexion() {
        return this.isPremiereConnexion;
    }

    public void setPremiereConnexion(boolean premiereConnexion) {
        isPremiereConnexion = premiereConnexion;
    }
}

   