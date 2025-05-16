package service;

import model.LienParente;
import model.Personne;

/**
 * Represents a familial link request between two people.
 */
public class Demande {

    private Personne emetteur;
    private Personne destinataire;
    private LienParente lien;

    public Demande(Personne emetteur, Personne destinataire, LienParente lien) {
        this.emetteur = emetteur;
        this.destinataire = destinataire;
        this.lien = lien;
    }

    public Personne getEmetteur() {
        return emetteur;
    }

    public Personne getDestinataire() {
        return destinataire;
    }

    public LienParente getLien() {
        return lien;
    }
}
