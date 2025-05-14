package model;

public class DemandeLien {

    private Personne emetteur;
    private Personne destinataire;
    private LienParente lien;

    public DemandeLien(Personne emetteur, Personne destinataire, LienParente lien) {
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
