package projet.multimodal;


public class Commande {
    private String action;
    private int posX, posY;
    private String couleur, nom;
    
    public Commande(){
        action = null;
        posX= -1;
        posY= -1;
        couleur = "Black";
        nom = null; //nom de l'objet (ex: R123)
    }

    public String getAction() {
        return action;
    }

    public String getCouleur() {
        return couleur;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public String getNom() {
        return nom;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public void clear(){
        action = null;
        posX= -1;
        posY= -1;
        couleur = "Black";
        nom = null;
    }

    // Completude de la structure
    public boolean estComplete(){
        return action!= null&& nom!=null&& posX!=-1 && posY!=-1;
    }

}
