package projet.multimodal;

import java.awt.Color;


public class Commande {
    String action, objet;
    int posX, posY;
    String couleur;
    
    public Commande(){
        action = null;
        objet = null;
        posX= -1;
        posY= -1;
        couleur = "Black";
    }

    public String getAction() {
        return action;
    }

    public String getCouleur() {
        return couleur;
    }

    public String getObjet() {
        return objet;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}
