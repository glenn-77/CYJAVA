package model;

public class Compte {
    private String login;
    private String motDePasse;
    private String numero;
    private String email;
    private String telephone;
    private String adresse;


    public Compte(String login, String motDePasse, String numero, String email, String telephone, String adresse) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.numero = numero;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
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

}

   