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
        /**
         * Create server with port 5000
         */
        String numberDisk = JOptionPane.showInputDialog(null, "Enter the number of disks:");
        Server server = new Server(5000, Integer.parseInt(numberDisk));
        server.start();
    }
    
}
