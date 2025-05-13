package model;

import java.time.LocalDate;

public class Femme extends Personne {
    public Femme(String nss, String prenom, String nom, LocalDate dateNaissance, 
                 String nationalite, String carteIdentite, String codePrive, Compte compte) {
        // Appel au constructeur de Personne avec les bons paramètres
        super(nss, prenom, nom, dateNaissance, nationalite, carteIdentite, codePrive, Genre.FEMME, compte);
    }
}
