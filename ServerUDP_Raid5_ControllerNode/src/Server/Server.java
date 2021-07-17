package Server;

import Domain.Fragment;
import Domain.MetaData;
import Domain.Node;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
 
    /**
     * Constructor of Server
     * @param port : Server Port
     * @param numberDisk : Number of disks in the raid
     */
    public Server(int port,int numberDisk){
       PORT = port;
       this.listMetadata = new ArrayList<MetaData>();
       this.listNodes = new ArrayList<Node>();
       System.out.println("Start server UDP");
       createRaid5Folder();
       createNodes(numberDisk);
    }
   
    /**
     * Start server
     */
    @Override
    public void run(){
        try {                   
            socketUDP = new DatagramSocket(PORT);
            
            while (true) {                          
                               
                try {
                    buffer = new byte[1024];
                    System.out.println("Waiting...");
                    DatagramPacket petition = new DatagramPacket(buffer, buffer.length);
                    socketUDP.receive(petition);

                    message = new String(petition.getData(),0,petition.getLength());
                    System.out.println("\n************\n"+message+"\n************n");
                    if(message.equals("stockfile")){   
                        receiveFile();
                    }
                    

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
    
    /**
     * @param name : Name of file
     * @param size : Size of file
     * @param content : Content of file
     * 
     * Receive file from client
     */
    public void receiveFile(){
        String nameFile = receive();
        int sizeFile = Integer.parseInt(receive());
        String content = receive(sizeFile);
        
        saveSplitFile(splitFile(nameFile, sizeFile, content));
    }
    
    /**
     * @param name : Name of file
     * @param sizeFile : Size of File
     * @param file : Content of file
     * @return MetaData : Data of book
     * 
     * Receive the content of the book
     * file: abcdefghijklmnopqrstuvwxyz
     * 
     * Separate the book into pieces, with the respective node(Disk) where it will be saved
     * 
     * |*Disk_0*|*Disk_1*|*Disk_2*|**Disk_3**|***Disk_4**Patity**(byte)***|
     * |********|********|********|**********|****************************|
     * | abcdef | ghijkl | mnopqr | stuvwxyz | abcdefghijklmnopqrstuvwxyz |
     * |********|********|********|**********|****************************|
     * 
     *  
     */
    public MetaData splitFile(String name, int sizeFile, String file){
        
        int sizeFragment = sizeFile / (this.numberDisk - 1); // 26 / 4 = 6.5
        int residuo = sizeFile % (this.numberDisk - 1);      // 26 % 4 = 2
        int j = 0; 
        
        MetaData metadata = new MetaData(this.listMetadata.size(), name);
        String fragmentBook="";
        
        int[] array = shuffleArray();
        
        for (int i = 0; i < this.numberDisk-1; i++) {
            if((i+1) == this.numberDisk-1){
                fragmentBook = file.substring(j, j + sizeFragment + residuo);
            }else{
                fragmentBook = file.substring(j, j + sizeFragment);
            }
            j += sizeFragment;
            metadata.getFragments().add(new Fragment(i, array[i], name, fragmentBook));
        }          
         
        metadata.getFragments().add(new Fragment(this.numberDisk, array[this.numberDisk-1], name, file));
        
        return metadata;
    }
    /**
     * Receive a MetaData object
     * @param metadata 
     * 
     * Save the respective fragments of the book in each corresponding node, with the name and the content
     * 
     */
    public void saveSplitFile(MetaData metadata){
        this.listMetadata.add(metadata);       
        
        for (int i = 0; i < this.listNodes.size(); i++) {
            this.listNodes.get(metadata.getFragments().get(i).getDisk()).modeWrite(
                    metadata.getFragments().get(i).getData().getName(),
                    metadata.getFragments().get(i).getData().getContent()
            );
        }
    }
    
    /**
     * Unorder an array to allocate disks
     * 
     * Part of the code obtained from
     * https://es.stackoverflow.com/questions/296944/como-desordenar-mezclar-barajar-un-array-en-java
     * @return Disordered disk array
     */
    private int[] shuffleArray(){
        
        int[] array= new int[this.numberDisk];
        for (int i = 0; i < this.numberDisk; i++) {
            array[i] = i;
        }

        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
    }
    
    public String getSplitFile(List<String> fragmentsBook){
        
        /**
         * File archivo = new File("file.txt");
         * if (!archivo.exists()) {
         *     System.out.println("OJO: ¡¡No existe el archivo de configuración!!");
         * }
         * 
         * **GetBook**
         * 
         *  All discs without problem
         * 
         * |*Disk_0*|*Disk_1*|*Disk_2*|**Disk_3**|
         * |********|********|********|**********|
         * | abcdef | ghijkl | mnopqr | stuvwxyz |
         * |********|********|********|**********|
         * 
         * |************ Book ************|
         * |* abcdefghijklmnopqrstuvwxyz *|
         * |******************************|
         * 
         *  A damaged disk
         * 
         * |*Disk_0*|*Disk_1*|*Disk_2*|**Disk_3**|
         * |********|********|********|**********|
         * | abcdef | XXXXXX | mnopqr | stuvwxyz |
         * |********|********|********|**********|
         * 
         * |************ Book ************|
         * |* abcdefXXXXXXmnopqrstuvwxyz *|
         * |******************************|
         * 
         * |*********Disk_4**Patity**(byte)******|
         * |********|********|********|**********|
         * | abcdef | ghijkl | mnopqr | stuvwxyz |
         * |*************************************|
         * 
         * |************ Book ************|
         * |* abcdefghijklmnopqrstuvwxyz *|
         * |******************************|
         * 
         */
        
        return null;
    }
    
    /**
     * Method that listens to the clients
     * @return String with client data
     */
    public String receive(){
        buffer = new byte[1024];
        try {
            DatagramPacket petition = new DatagramPacket(buffer, buffer.length);
            socketUDP.receive(petition);
            
            message = new String(petition.getData(),0,petition.getLength());
            
            return message;
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Method that listen to the clients, but with a dynamic size buffer
     * @return String with client data book
     */
    public String receive(int size){
        buffer = new byte[size];
        try {
            DatagramPacket petition = new DatagramPacket(buffer, buffer.length);
            socketUDP.receive(petition);
            
            message = new String(petition.getData(),0,petition.getLength());
            
            return message;
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
    
    /**
     * @param numberDisk : Number of discs in the raid
     * Create and add nodes to list
     * Initialize the nodes(Disk)
     */
    public void createNodes(int numberDisk){
        this.numberDisk = numberDisk;
        
        for (int i = 0; i < this.numberDisk; i++) {
            this.listNodes.add(new Node(i));
            this.listNodes.get(i).start();
            System.out.println("Start Node: "+i);
        }
    }
    
    /**
     * Create the folder for the raid disks
     */
    public void createRaid5Folder(){
        File file = new File("Raid5");  
        file.mkdir();
    }
}
