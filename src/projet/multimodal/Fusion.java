/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projet.multimodal;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;


public class Fusion extends javax.swing.JFrame {
    String adresse;
    Ivy bus;
    Point coord;
    Boolean inForm, isInObj;
    HashMap<String, Geste> dicoGestes;
    Geste geste;
    
    Commande command;
    // variable temporaire
    int xTemp, yTemp;
    String nomTemp, colorTemp;
    HashMap<String, String> listeTemp;
    
    Boolean sayHere, sayColor, sayThisColor, sayObject;   
    
    private enum PossibleState{
        IDLE,
        CREER,      // créer
        CLIC_C,
        DIRE_C,
        COLOR_C,
        THIS_COLOR,
        DEPL,       // déplacer
        DIRE_POS,
        CLIC_POS,
        CLIC_OBJ,
        DIRE_OBJ,
        OBJ_D,
        SUPPR,      // supprimer
        DIRE_S,
        CLIC_S,
        FIN_S;      
    }
    
    private PossibleState currentState;
    
    private void setState(PossibleState aState){
        currentState = aState;
    }
   
    public Fusion() {
        initComponents();
        setState(PossibleState.IDLE);
        adresse = "localhost:2010";
        bus = new Ivy("Interface Ivy", "", null);
        coord = new Point(0, 0);
        dicoGestes = new HashMap<>();
        listeTemp = new HashMap<>();;
        geste = new Geste();
        initDicoGestes();
        command = new Commande();
        
        //Start bus ivy
        try {
            bus.start(adresse);
            bus.sendMsg("Palette:CreerEllipse x=" + 0 + " y=" + 0 + " longueur=15 hauteur=30 couleurFond=Green couleurContour=Green");
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {            
            bus.bindMsg("^Palette:MousePressed.* x=(.*) y=(.*)", new IvyMessageListener() { //^=tous les message qui commencent par Palette:MousePressed)
                @Override
                public void receive(IvyClient arg0, String[] arg1) {
                    try {
                        isInObj = false;
                        xTemp = Integer.parseInt(arg1[0]); yTemp = Integer.parseInt(arg1[1]);
                        bus.sendMsg("Palette:TesterPoint x="+xTemp+" y="+yTemp);
                        bus.bindMsg("Palette:ResultatTesterPoint x=(.*) y=(.*) nom=(.*)", new IvyMessageListener() {
                            @Override
                            public void receive(IvyClient ic, String[] strings) {
                                if ("".equals(strings[2])) {
                                    isInObj = false;
                                } else {
                                    isInObj = true;
                                    try {
                                        bus.sendMsg("Palette:DemanderInfo nom="+ strings[2]);  
                                        bus.bindMsg("Palette:Info nom=(.*) x=(.*) y=(.*) "
                                                + "longueur=(.*) hauteur=(.*) couleurFond=(.*) "
                                                + "couleurContour=(.*)", new IvyMessageListener() {
                                            @Override
                                            public void receive(IvyClient ic, String[] arg) {
                                                System.out.println(arg[0] + " " + arg[1] +
                                                        " " + arg[2] + " " + arg[3] + " " + 
                                                        arg[4] + " " + arg[5] + " " + arg[6]);
                                                nomTemp = arg[0];
                                                colorTemp = arg[5];
                                                if("Supprimer".equals(command.getAction()) || "Deplacer".equals(command.getAction())) {
                                                    listeTemp.put(arg[5], arg[0]);
                                                }
                                            }
                                        });
                                    } catch (IvyException ex) {
                                        Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        });
                        switch (currentState){
                            case IDLE :
                                bus.sendMsg("Palette:CreerEllipse x=" + xTemp + " y="
                                        + yTemp + " longueur=8 hauteur=8 couleurFond=Green couleurContour=Green");
                                geste.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                                break; 
                            case CREER :
                                if (isInObj == true) {
                                    setState(PossibleState.COLOR_C);
                                }
                                break;
                            case CLIC_C :
                                break;
                            case DIRE_C :
                                setState(PossibleState.CREER);
                                command.setPosX(Integer.parseInt(arg1[0]));
                                command.setPosY(Integer.parseInt(arg1[1]));
                                labelPosition1.setText("x : " + command.getPosX() +  " y : " + command.getPosY());
                                break; 
                            case COLOR_C :
                                break; 
                            case THIS_COLOR :
                                setState(PossibleState.CREER);
                                command.setCouleur(colorTemp);
                                labelCouleur1.setText(command.getCouleur());
                                break;  
  
                            case DEPL :
                                if(isInObj) {
                                   setState(PossibleState.CLIC_OBJ);  
                                }
                                else {
                                  setState(PossibleState.CLIC_POS);  
                                }
                                break; 
                            case DIRE_POS :
                                setState(PossibleState.DEPL);
                                command.setPosX(Integer.parseInt(arg1[0]));
                                command.setPosY(Integer.parseInt(arg1[1]));
                                labelPosition1.setText(" x : " + command.getPosX() + " y : " + command.getPosY());
                                break;
                            case CLIC_POS : 
                                break;
                            case CLIC_OBJ :
                                break;
                            case DIRE_OBJ :
                                setState(PossibleState.OBJ_D);
                                break;
                            case OBJ_D :
                                break;  
                            case SUPPR :        // Supprimer
                                setState(PossibleState.CLIC_S);
                                /*command.setPosX(Integer.parseInt(arg1[0]));
                                command.setPosY(Integer.parseInt(arg1[1]));*/
                                command.setNom(nomTemp);
                                break;
                            case DIRE_S : 
                                setState(PossibleState.FIN_S);
                                break;
                            case CLIC_S :
                                break;
                            case FIN_S :          
                                break;
                        }
                        timer.restart();
                    } catch (IvyException ex) {
                        Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            bus.bindMsg("^Palette:MouseReleased.* x=(.*) y=(.*)", new IvyMessageListener() { 
                @Override
                public void receive(IvyClient arg0, String[] arg1) {
                    try {
                        switch (currentState) {
                            case IDLE :
                                bus.sendMsg("Palette:CreerEllipse x=" + arg1[0] + " y=" + arg1[1] + " longueur=8 hauteur=8 couleurFond=Red couleurContour=Red");
                                geste.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                                geste.normalize();
                                compareGeste();
                                geste = new Geste();
                        }
                    } catch (IvyException ex) {
                        Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            bus.bindMsg("^Palette:MouseDragged.* x=(.*) y=(.*)", new IvyMessageListener() { 
                @Override
                public void receive(IvyClient arg0, String[] arg1) {
                    try {
                        switch (currentState) {
                            case IDLE :
                                bus.sendMsg("Palette:CreerEllipse x=" + arg1[0] + " y=" + arg1[1] + " longueur=5 hauteur=5 couleurFond=Grey couleurContour=Grey");
                                geste.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                            }
                        } catch (IvyException ex) {
                            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Vocal
        try {
            bus.bindMsg("^sra5 Parsed=(.*) Confidence=.* NP=.* Num_A=.*", new IvyMessageListener() {// abonnement
                public void receive(IvyClient client,String[] args) {
                    sayHere = false; sayColor = false; 
                    sayThisColor = false; sayObject = false;
                    String couleur = null;
                    if ( "position".equals(args[0])) {
                        sayHere = true;
                    } else if ("objet".equals(args[0])) {
                        sayObject = true;
                    } else if (args[0].contains("couleur")) {
                        sayColor = true;
                        couleur = args[0].split("couleur")[1];
                    } else if ("thisCouleur".equals(args[0])) {
                        sayThisColor = true;
                    }

                    switch (currentState){
                        case IDLE :
                            break; 
                        case CREER :
                            if (sayHere) {
                                setState(PossibleState.DIRE_C);
                            } else if (sayThisColor) {
                                setState(PossibleState.THIS_COLOR);
                            } else if (sayColor) {
                                command.setCouleur(couleur);
                            }
                            break;
                        case CLIC_C :
                            if (sayHere) {
                                setState(PossibleState.CREER);
                                command.setPosX(xTemp);
                                command.setPosY(yTemp);
                                labelPosition1.setText("x : " + command.getPosX() +  " y : " + command.getPosY());
                            }
                            break;
                        case DIRE_C :
                            break; 
                        case COLOR_C :
                            if (sayThisColor) {
                                setState(PossibleState.CREER);
                                command.setCouleur(colorTemp);
                                labelCouleur1.setText(command.getCouleur());
                            }
                            break; 
                        case THIS_COLOR :
                            break;
                        case DEPL :
                            if (sayHere) {
                                setState(PossibleState.DIRE_POS);
                            } else if (sayObject) {
                                setState(PossibleState.DIRE_OBJ);
                            }
                            break; 
                        case DIRE_POS :
                            break;
                        case CLIC_POS : 
                            if (sayHere) {
                                setState(PossibleState.DEPL);
                                command.setPosX(xTemp);
                                command.setPosY(yTemp);
                                labelPosition1.setText("x : " + xTemp + " y : " + yTemp);
                            }
                            break;
                        case CLIC_OBJ :
                            if (sayObject) {
                                setState(PossibleState.OBJ_D);
                            }
                            break;
                        case DIRE_OBJ :
                            break;
                        case OBJ_D :
                            if (sayColor) {
                                setState(PossibleState.DEPL);
                                command.setNom(listeTemp.get(couleur.toLowerCase()));
                                command.setCouleur(args[0].split("couleur")[1]);
                                labelCouleur1.setText(command.getCouleur());
                                labelObjet1.setText(command.getNom());
                            }
                            break;

                        case SUPPR : 
                            if (sayObject) {
                                setState(PossibleState.DIRE_S);
                            }
                            break;
                        case DIRE_S : 
                            break;
                        case CLIC_S :
                            if (sayObject) {
                                setState(PossibleState.FIN_S);
                            }
                            break;
                        case FIN_S :
                            if (sayColor) {
                                setState(PossibleState.IDLE);
                                command.setNom(listeTemp.get(couleur.toLowerCase()));
                                command.setCouleur(args[0].split("couleur")[1]);
                                labelCouleur1.setText(command.getCouleur());
                                labelObjet1.setText(command.getNom());
                                supprObjet(command.getNom());
                                command.clear();
                            }
                            break;
                    }
                    timer.restart();
                }
            });
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
        pack();
        setVisible(true);
        
    }
    
    /*
    * Fonction permettant d'intialiser le dictionnaiire des gestes
    */
    public void initDicoGestes() {
        Geste rect = new Geste();
        Geste cercle = new Geste();
        Geste trait = new Geste();
        Geste croix = new Geste();
        
        for(int i=0; i<=100; i+=10) {
            //Rectangle : gauche
            rect.addPoint(0, i);
            //Trait
            trait.addPoint(i , 100);
            //Croix : diagonale gauche
            croix.addPoint(i, i);   
        }
        
        for(int i=0; i<=200; i+=10) {
            //Rectangle : bas
            rect.addPoint(i, 100);
        }
        
        for(int i=100; i>=0; i-=10) {
            //Rectangle : droit
            rect.addPoint(200, i);
            //Croix : bas
            croix.addPoint(i, 100);
        }
        
        int j=0;
        for(int i=100; i>=0; i-=10) {
            //Croix : diagonale droite
            croix.addPoint(j, i);
            j+=10;
        }
        
        for(int i=200; i>=0; i-=10) {
            //Rectangle : haut
            rect.addPoint(i, 0);
        }
        
        for(int i=0; i<=10; i++) {
            //Cercle
            double tetha = ((2*Math.PI)*i)/10;
            double x = 100+50*Math.cos(tetha);
            double y = 100+50*Math.sin(tetha);
            cercle.addPoint((int) x , (int)y);
        }
        
        // Normalisation des gestes
        rect.normalize();
        trait.normalize();
        croix.normalize();
        cercle.normalize();
  
        
        // Ajout des gestes normalisés au dictionnaire
        this.dicoGestes.put("Rectangle", rect);
        this.dicoGestes.put("Cercle", cercle);
        this.dicoGestes.put("Deplacer", trait);
        this.dicoGestes.put("Supprimer", croix);
    }
    
        /*
    * Fonction permettant de comparer le geste réalisé sur la palette avec le 
    * dictionnaire des gestes
    * Retourne le geste le plus proche de celui réalisé
    */
    public String compareGeste() {

        double sommeRect=0;
        double sommeTrait=0;
        double sommeCercle=0;
        double sommeCroix=0;
        
        // Parcours des points du geste réalisé sur la palette
        for(int i=0; i<geste.size(); i++) {
            double x = geste.getPoints().get(i).x;
            double y = geste.getPoints().get(i).y;
            
            double xRect = dicoGestes.get("Rectangle").getPoints().get(i).x;
            double yRect = dicoGestes.get("Rectangle").getPoints().get(i).y;
            sommeRect = Math.sqrt( Math.pow((x - xRect), 2) + Math.pow((y - yRect), 2) );
            
            double xCercle = dicoGestes.get("Cercle").getPoints().get(i).x;
            double yCercle = dicoGestes.get("Cercle").getPoints().get(i).y;
            sommeCercle = Math.sqrt( Math.pow((x - xCercle), 2) + Math.pow((y - yCercle), 2) );
            
            double xTrait = dicoGestes.get("Deplacer").getPoints().get(i).x;
            double yTrait = dicoGestes.get("Deplacer").getPoints().get(i).y;
            sommeTrait = Math.sqrt( Math.pow((x - xTrait), 2) + Math.pow((y - yTrait), 2) );
            
            double xCroix= dicoGestes.get("Supprimer").getPoints().get(i).x;
            double yCroix = dicoGestes.get("Supprimer").getPoints().get(i).y;
            sommeCroix = Math.sqrt( Math.pow((x - xCroix), 2) + Math.pow((y - yCroix), 2) );
        }
        
        //Calcul des distances entre le geste réalisés et ceux du dictionnaire de gestes
        double distanceRect = sommeRect/geste.size();
        double distanceCercle = sommeCercle/geste.size();
        double distanceTrait= sommeTrait/geste.size();
        double distanceCroix = sommeCroix/geste.size();
        
        HashMap<String, Double> distances = new HashMap<>();
        distances.put("Rectangle", distanceRect);
        distances.put("Cercle", distanceCercle);
        distances.put("Supprimer", distanceCroix);
        distances.put("Deplacer", distanceTrait);
        
        String geste = "";
        Double min = null;
        // Parcours de la map avec les distances pour récupérer la plus petite
        for (Map.Entry<String, Double> map : distances.entrySet()) {
            if(min==null) {
                min = map.getValue();
                geste = map.getKey();
            }
            else if(map.getValue()<min || min==null) {
                min = map.getValue();
                geste = map.getKey();
            }
        }

        launch(geste);
        this.labelAction1.setText(geste);
        clearVarTemp();
        command.setAction(geste);
        
        return geste;
    }
    
    public void clearVarTemp() {
        listeTemp.clear();
        xTemp = -1;
        yTemp = -1;
        nomTemp = null;
        colorTemp = null;
    }
    
    public void clearLabels() {
        this.labelAction1.setText("null");
        this.labelCouleur1.setText("null");
        this.labelPosition1.setText("null");
        this.labelObjet1.setText("null");
        
    }
    
    
    Timer timer = new Timer(5000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            timerStartActionPerformed(e);
        }
    });
        
    public void launch (String gesture) {
        command.setAction(gesture);
        if ("Cercle".equals(gesture)) {
           setState(PossibleState.CREER);
           timer.start();
        } else if ("Rectangle".equals(gesture)) {
            setState(PossibleState.CREER);
            timer.start();
        } else if ("Deplacer".equals(gesture)) {
            setState(PossibleState.DEPL);
            timer.start();
        } else if ("Supprimer".equals(gesture)) {
            setState(PossibleState.SUPPR);
            timer.start();
        }     
    }
    
    private void timerStartActionPerformed(ActionEvent e) {
        switch (currentState){
            case IDLE :
                break; 
            case CREER : 
                setState(PossibleState.IDLE);
                if ("Rectangle".equals(command.getAction())) {
                    creerRect(command.getCouleur(), command.getPosX(), command.getPosY());
                } else if ("Cercle".equals(command.getAction())) {
                    creerEllipse(command.getCouleur(), command.getPosX(), command.getPosY());
                }
                clearLabels();
                timer.stop();
                break;
            case CLIC_C :
                setState(PossibleState.CREER);
                break;
            case DIRE_C :
                setState(PossibleState.CREER);
                break; 
            case COLOR_C :
                setState(PossibleState.CREER);
                break; 
            case THIS_COLOR :
                setState(PossibleState.CREER);
                command.setCouleur(colorTemp);
                break;
            case DEPL :
                setState(PossibleState.IDLE);
                if (command.getPosX() >= 0 && command.getPosY() >= 0 && command.getNom() != null) {
                    deplObjet(command.getNom(), command.getPosX(), command.getPosY());
                    command.clear();
                } else {
                    command.clear();   
                }
                 clearLabels();
                 timer.stop();
                break; 
            case DIRE_POS :
                setState(PossibleState.DEPL);
                break;
            case CLIC_POS : 
                setState(PossibleState.DEPL);
                break;
            case CLIC_OBJ :
                setState(PossibleState.DEPL);
                break;
            case DIRE_OBJ :
                setState(PossibleState.DEPL);
                break;
            case OBJ_D :
                setState(PossibleState.DEPL);
                command.setNom(nomTemp);
                command.setCouleur(colorTemp);
                this.labelObjet1.setText(command.getNom());
                break;
            case SUPPR : 
                setState(PossibleState.IDLE);
                command.clear();
                clearLabels();
                timer.stop();
                break;
            case DIRE_S : 
                setState(PossibleState.IDLE);
                command.clear();
                clearLabels();
                timer.stop();
                break;
            case CLIC_S :
                setState(PossibleState.IDLE);
                command.clear();
                clearLabels();
                timer.stop();
                break;
            case FIN_S :
                setState(PossibleState.IDLE);
                command.setNom(nomTemp);
                supprObjet(command.getNom()); 
                command.clear();
                clearLabels();
                timer.stop();
                break; 
        }
    }
    
    public void creerRect(String color, int x, int y){
        try {
            bus.sendMsg("Palette:CreerRectangle x=" + x + " y=" + y + " longueur=100 "
                    + "hauteur=50 couleurFond=" + color + " couleurContour=" + color);
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void creerEllipse(String color, int x, int y){
        try {
            bus.sendMsg("Palette:CreerEllipse x=" + x + " y=" + y + " longueur=50 "
                    + "hauteur=50 couleurFond=" + color + " couleurContour=" + color);
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void supprObjet(String nom){
        try {
            bus.sendMsg("Palette:SupprimerObjet nom=" + nom);
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void deplObjet(String nom, int x, int y){
        try {
            bus.sendMsg("Palette:DeplacerObjetAbsolu nom=" + nom + " x=" + x +
                    " y=" + y);
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void inObject(int x, int y){
        try {
            bus.sendMsg("Palette:TesterPoint x="+x+" y="+y);
            bus.bindMsg("Palette:ResultatTesterPoint x=(.*) y=(.*) nom=(.*)", new IvyMessageListener() {
                @Override
                public void receive(IvyClient ic, String[] strings) {
                    if ("".equals(strings[2])) {
                        isInObj = false;
                    } else {
                        isInObj = true;
                    }
                }
            });
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
        inForm = isInObj;
        System.err.println("Le clic est dans la zone ou pas : " + isInObj);
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelAction = new javax.swing.JLabel();
        labelAction1 = new javax.swing.JLabel();
        labelObjet = new javax.swing.JLabel();
        labelPosition = new javax.swing.JLabel();
        labelCouleur = new javax.swing.JLabel();
        labelObjet1 = new javax.swing.JLabel();
        labelPosition1 = new javax.swing.JLabel();
        labelCouleur1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        labelAction.setText("Action en cours :");

        labelAction1.setText("null");

        labelObjet.setText("Objet :");

        labelPosition.setText("Position :");

        labelCouleur.setText("Couleur : ");

        labelObjet1.setText("null");

        labelPosition1.setText("null");

        labelCouleur1.setText("null");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelAction, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelAction1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelObjet, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelCouleur, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelObjet1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(labelPosition1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelCouleur1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(172, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAction)
                    .addComponent(labelAction1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelObjet)
                    .addComponent(labelObjet1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPosition)
                    .addComponent(labelPosition1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCouleur)
                    .addComponent(labelCouleur1))
                .addContainerGap(166, Short.MAX_VALUE))
        );

        labelAction.getAccessibleContext().setAccessibleName("jLabel1");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Fusion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Fusion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Fusion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Fusion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Fusion().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelAction;
    private javax.swing.JLabel labelAction1;
    private javax.swing.JLabel labelCouleur;
    private javax.swing.JLabel labelCouleur1;
    private javax.swing.JLabel labelObjet;
    private javax.swing.JLabel labelObjet1;
    private javax.swing.JLabel labelPosition;
    private javax.swing.JLabel labelPosition1;
    // End of variables declaration//GEN-END:variables
}

///-----------------------------  TEMPLATE ------------------------------------
//switch (currentState){
//            case IDLE :
//                break; 
//            case CREER : 
//                break;
//            case CLIC_C :
//                break;
//            case DIRE_C :
//                break; 
//            case COLOR_C :
//                break; 
//            case THIS_COLOR :
//                break;
//            case DEPL :
//                break; 
//            case DIRE_POS :
//                break;
//            case CLIC_POS : 
//                break;
//            case CLIC_OBJ :
//                break;
//            case DIRE_OBJ :
//                break;
//            case OBJ_D :
//                break;
//            case SUPPR : 
//                break;
//            case DIRE_S : 
//                break;
//            case CLIC_S :
//                break;
//            case FIN_S :          
//                break;
//        }
