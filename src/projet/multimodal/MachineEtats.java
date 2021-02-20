package projet.multimodal;


public class MachineEtats {
    
     private enum PossibleState{
        E1(true,false,false,false), 
        E2(false,true,false,false), 
        E3(false,false,true,false),
        E4(false,false,false,true);
        


        public boolean isB1Enabled() {
            return b1Enabled;
        }

        public boolean isB2Enabled() {
            return b2Enabled;
        }

        public boolean isB3Enabled() {
            return b3Enabled;
        }

        public boolean isB4Enabled() {
            return b4Enabled;
        }
        
        
        private PossibleState(boolean b1Enabled, boolean b2Enabled, boolean b3Enabled, boolean b4Enabled) {
            this.b1Enabled = b1Enabled;
            this.b2Enabled = b2Enabled;
            this.b3Enabled = b3Enabled;
            this.b4Enabled = b4Enabled;
        }
        
        private final boolean b1Enabled;
        private final boolean b2Enabled;
        private final boolean b3Enabled;
        private final boolean b4Enabled;
    
        
    }
}
