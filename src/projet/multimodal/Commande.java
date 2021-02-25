package projet.multimodal;

import java.awt.Color;


public class Commande {
    String action, objet;
    int posX, posY;
    Color couleur;
    
    public Commande(){
        action = null;
        objet = null;
        posX= -1;
        posY= -1;
        couleur = null;
    }
    
    // Completude de la structure
    public boolean estComplete(){
        return action!= null&& objet!=null&& posX!=-1 && posY!=-1;
    }
  
}
