package serverudp_raid5_controllernode;

import Server.Server;
import javax.swing.JOptionPane;

/**
 *
 * @author mario
 */
public class ServerUDP_Raid5_ControllerNode {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String numberDisk="";
        do { 
            if(numberDisk!=""){
                JOptionPane.showMessageDialog(null, "The number of discs must be at least 3 discs");
            }
            numberDisk = JOptionPane.showInputDialog(null, "Enter the number of disks:");
            if(numberDisk==null)numberDisk=String.valueOf(0);
        } while ((Integer.parseInt(numberDisk)<3 || Integer.parseInt(numberDisk)>10) && Integer.parseInt(numberDisk)!=0);
        
        /**
         * Create server with port 5000
         */
        if(Integer.parseInt(numberDisk)!=0){
            
            Server server = new Server(5000, Integer.parseInt(numberDisk));
            server.start();
            
        }else{
            System.out.println("***************************");
            System.out.println("Error insert discs for raid");
            System.out.println("    Stopping server...     ");
            System.out.println("***************************");
        }
    }
    
}
