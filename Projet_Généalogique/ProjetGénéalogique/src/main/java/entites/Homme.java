package entites;

import entites.enums.Genre;

import java.time.LocalDate;

public class Homme extends Personne {
    public Homme(String nss, String prenom, String nom, LocalDate dateNaissance, String nationalite, String carteIdentite, String codePrive, Compte compte, ArbreGenealogique arbre) {
        // Appel au constructeur de Personne avec l'objet Compte
        super(nss, prenom, nom, dateNaissance, nationalite, carteIdentite, codePrive, Genre.HOMME, compte, arbre);
    }
}
