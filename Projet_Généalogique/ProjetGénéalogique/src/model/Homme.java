package model;

import java.time.LocalDate;

public class Homme extends Personne {
    public Homme(String nss, String prenom, String nom, LocalDate dateNaissance, String nationalite, Compte compte) {
        // Appel au constructeur de Personne avec l'objet Compte
        super(nss, prenom, nom, dateNaissance, nationalite, null, null, Genre.HOMME, compte);
    }
}
