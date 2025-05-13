package model;

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
}
