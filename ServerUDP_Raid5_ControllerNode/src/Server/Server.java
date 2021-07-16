package Server;

import Domain.MetaData;
import Domain.Node;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mario
 */
public class Server extends Thread{
    
    /**
     * PORT: Server port
     * buffer: Buffer por data
     * socketUDP: Socket
     * message: Data transformed into string
     * listMetadata: List of books with data required for reconstruction
     * listNodes: List of nodes representing physical disks
     * numberDisk: Number of discs in the raid
     */
    public final int PORT;
    private byte[] buffer;
    private DatagramSocket socketUDP;
    private String message;
    private List<MetaData> listMetadata;
    private List<Node> listNodes;
    private int numberDisk;
 
    public Server(int port,int numberDisk){
       PORT = port;
       this.listMetadata = new ArrayList<MetaData>();
       this.listNodes = new ArrayList<Node>();
       createNodes(numberDisk);
    }
   
    @Override
    public void run(){
        try {
            /**
             * Start server
             */
            System.out.println("Start server UDP");
            socketUDP = new DatagramSocket(PORT);
            
            while (true) {                          
                               
                try {
                    buffer = new byte[1024];
                    System.out.println("Waiting...");
                    DatagramPacket petition = new DatagramPacket(buffer, buffer.length);
                    socketUDP.receive(petition);

                    message = new String(petition.getData());
                    System.out.println(message);
                    
                    

                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                this.sleep(1);
            }
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public MetaData splitFile(String file){
        return null;
    }
    
    /**
     * Method that listens to the customer
     * @return DatagramPacket with client data
     */
    public DatagramPacket receive(){
        buffer = new byte[1024];
        try {
            DatagramPacket petition = new DatagramPacket(buffer, buffer.length);
            socketUDP.receive(petition);
            
            message = new String(petition.getData());
            System.out.println(message);
            
            return petition;
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * @param petition : Clients petition
     * @param send : Message for send
     */
    public void send(DatagramPacket petition, String send){
        buffer = new byte[1024];
        try {
            int portClient = petition.getPort();
            InetAddress address = petition.getAddress();
            
            buffer = send.getBytes();
            
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, address, portClient);
            
            socketUDP.send(response);
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createNodes(int numberDisk){
        this.numberDisk = numberDisk;
        
        for (int i = 0; i < this.numberDisk; i++) {
            this.listNodes.add(new Node(i));
        }
    }
}
