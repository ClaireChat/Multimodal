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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;


public class Fusion extends javax.swing.JFrame {
    String adresse;
    Ivy bus;
    Point coord;
    String color, gesture, inForm = "";
    int X, Y;
    Object obj;
    HashMap<String, Geste> dicoGestes;
    Geste geste = new Geste();
    
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
        color = "Black";
        dicoGestes = new HashMap<>();
        initDicoGestes();
        
        gesture = "";
        X = -10;
        Y = -10;
        obj = null;
        
        
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
                        X = Integer.parseInt(arg1[0]);
                        Y = Integer.parseInt(arg1[1]);
                        bus.sendMsg("Palette:CreerEllipse x=" + 0 + " y=" + 0 + " longueur=100 hauteur=100 couleurFond=Yellow couleurContour=Green");
                        
                        bus.sendMsg("Palette:TesterPoint x="+X+" y="+Y);
                        bus.bindMsg("^Palette:ResultatTesterPoint x=(.*) y=(.*) nom=(.*)", new IvyMessageListener() {
                            @Override
                            public void receive(IvyClient ic, String[] strings) {
                                inForm = strings[2];
                                System.out.print(inForm + "----------------" + strings[0] +""+strings[1]);
                            }
                        });
                        
                        
                        
                        switch (currentState){
                            case IDLE :
                                bus.sendMsg("Palette:CreerEllipse x=" + X + " y=" + Y + " longueur=8 hauteur=8 couleurFond=Green couleurContour=Green");
                                geste.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                                break; 
                            case CREER :        // Créer
                                // tester si c'est dans un objet
                                break;
                            case CLIC_C :
                                break;
                            case DIRE_C :
                                setState(PossibleState.CREER);
                                // do A4
                                break; 
                            case COLOR_C :
                                break; 
                            case THIS_COLOR :
                                setState(PossibleState.CREER);
                                // do A5
                                break;
                            case DEPL :         //Déplacer
                                //tester si dans l'objet
                                break; 
                            case DIRE_POS :
                                setState(PossibleState.DEPL);
                                // do A5-depl
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
                                setState(PossibleState.DIRE_S);
                                // do A5
                                break;
                            case DIRE_S : 
                                break;
                            case CLIC_S :
                                setState(PossibleState.FIN_S);
                                // do A5-suppr
                                break;
                            case FIN_S :          
                                break;
                        }
                    } catch (IvyException ex) {
                        Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            bus.bindMsg("^Palette:MouseReleased.* x=(.*) y=(.*)", new IvyMessageListener() { 
                @Override
                public void receive(IvyClient arg0, String[] arg1) {
                    try {
                        bus.sendMsg("Palette:CreerEllipse x=" + arg1[0] + " y=" + arg1[1] + " longueur=8 hauteur=8 couleurFond=Red couleurContour=Red");
                        geste.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                        geste.normalize();
                        System.out.println(compareGeste());
                        geste = new Geste(); 
                        System.out.println(currentState);
                        //bus.sendMsg("Palette:SupprimerTout");
                    } catch (IvyException ex) {
                        Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            bus.bindMsg("^Palette:MouseDragged.* x=(.*) y=(.*)", new IvyMessageListener() { 
                @Override
                public void receive(IvyClient arg0, String[] arg1) {
                    try {
                        bus.sendMsg("Palette:CreerEllipse x=" + arg1[0] + " y=" + arg1[1] + " longueur=5 hauteur=5 couleurFond=Grey couleurContour=Grey");
                        geste.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
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
            bus.bindMsg("^sra5 Text=(.*) Confidence=.*", new IvyMessageListener() {// abonnement
                public void receive(IvyClient client,String[] args) {
                    System.out.print(args[0]);
                }
            });
        } catch (IvyException ex) {
            Logger.getLogger(Fusion.class.getName()).log(Level.SEVERE, null, ex);
        }
        pack();
        setVisible(true);
        
    }
    
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
        
        rect.normalize();
        trait.normalize();
        croix.normalize();
        cercle.normalize();
        
        this.dicoGestes.put("Rectangle", rect);
        this.dicoGestes.put("Cercle", cercle);
        this.dicoGestes.put("Deplacer", trait);
        this.dicoGestes.put("Supprimer", croix);
    }
    
    public String compareGeste() {

        double sommeRect=0;
        double sommeTrait=0;
        double sommeCercle=0;
        double sommeCroix=0;
        
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
        for (Map.Entry<String, Double> map : distances.entrySet()) {
            if(min==null) {
                min = map.getValue();
                geste = map.getKey();
            }
            else if(map.getValue()<min || min==null) {
                min = map.getValue();
                geste = map.getKey();
            }
            /*if(map.getValue()<min || min==null) {
                min = map.getValue();
                geste = map.getKey();
            }*/
        }
        
//        System.out.println("rect : " + distanceRect);
//        System.out.println("cercle : " + distanceCercle);
//        System.out.println("trait : " + distanceTrait);
//        System.out.println("croix : " + distanceCroix);
//        double score = 1 - (distanceRect / (1/2 * Math.sqrt(Math.pow(dicoGestes.get("Rectangle").size(), 2)+Math.pow(dicoGestes.get("Rectangle").size(), 2))));
//        System.out.println("score : " + score );
        
        launch(geste);
        
        return geste;
    }
    
    Timer timer = new Timer(3000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            timerStartActionPerformed(e);
        }
    });
        
    public void launch (String gesture) {
        if (gesture == "Cercle") {
           setState(PossibleState.CREER);
        } else if (gesture == "Rectangle") {
            setState(PossibleState.CREER);
        } else if (gesture == "Deplacer") {
            setState(PossibleState.DEPL);
            timer.start();
        } else if (gesture == "Supprimer") {
            setState(PossibleState.SUPPR);
        }     
    }
    
    private void timerStartActionPerformed(ActionEvent e) {
        switch (currentState){
            case IDLE :
                break; 
            case CREER : 
                setState(PossibleState.IDLE);
                if (gesture == "rect") {
                    //do A1
                } else if (gesture == "oval") {
                    //do A2
                }
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
                break;
            case DEPL :
                System.err.println(currentState);
                setState(PossibleState.IDLE);
                if (X > 0 && Y > 0 && obj == null) {
                    //do A1 && A3
                } else {
                    //do A2 && A3
                }
                System.err.println(currentState);
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
                break;
            case SUPPR : 
                setState(PossibleState.IDLE);
                //do A3
                break;
            case DIRE_S : 
                setState(PossibleState.IDLE);
                //do A3
                break;
            case CLIC_S :
                setState(PossibleState.IDLE);
                //do A3
                break;
            case FIN_S :    
                setState(PossibleState.IDLE);
                //do A1 && A3
                break;
        }
    }
   


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Passerelle Ivy");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Fusion().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

////-----------------------------  TEMPLATE ------------------------------------
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
