package Server;

import Domain.Fragment;
import Domain.MetaData;
import Domain.Node;
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
    private final List<MetaData> listMetadata;
    private final List<Node> listNodes;
    private final int numberDisk;
 
    /**
     * Constructor of Server
     * @param port : Server Port
     * @param numberDisk : Number of disks in the raid
     */
    public Server(int port,int numberDisk){
       PORT = port;
       this.listMetadata = new ArrayList<>();
       this.listNodes = new ArrayList<>();   
       this.numberDisk = numberDisk;
    }
   
    /**
     * Start server
     */
    @Override
    public void run(){
        try {                   
            socketUDP = new DatagramSocket(PORT);
            System.out.println("Start server UDP");
            createRaid5Folder();
            createNodes(this.numberDisk);
            
            while (true) {                          
                               
                try {
                    buffer = new byte[256];
                    System.out.println("Server waiting...");
                    DatagramPacket petition = new DatagramPacket(buffer, buffer.length);
                    socketUDP.receive(petition);

                    message = new String(petition.getData(),0,petition.getLength());
                    System.out.println("\n************\n"+message+"\n************\n");
                    
                    if(message.equals(Utility.MyUtility.STOCKFILE)){   
                        receiveFile();                       
                    }
                    if(message.equals(Utility.MyUtility.GETFILENAMES)){
                        receiveGetNames(petition);                       
                    }
                    if(message.equals(Utility.MyUtility.GETFILENAME)){
                        receiveSearchName(petition);
                    }
                    if(message.equals(Utility.MyUtility.GETFILE)){
                        joinFile(petition, receiveGetFile());
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Server.sleep(500);
            }
        } catch (SocketException | InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * name : Name of the file received from the client
     * size : Size of the file received from the client
     * content : Content of the file received from the client
     * 
     * Receive file from client
     */
    public void receiveFile(){

        String nameFile = receive();
        int sizeFile = Integer.parseInt(receive());
        String content = receive(sizeFile);
        
        if(searchMetaData(nameFile)==null){
            saveSplitFile(splitFile(nameFile, content));
        }
    }
    
    /**
     * @param petition : Client request with which you will be answered
     * 
     * Receive request to obtain the names
     * Submit the number of names
     * Submit the names
     */
    public void receiveGetNames(DatagramPacket petition){
        send(petition, String.valueOf(this.listMetadata.size()));
        for (int i = 0; i < this.listMetadata.size(); i++) {
            send(petition, this.listMetadata.get(i).getName());
        }
        //System.out.println("Submitted names");
    }
    
    public void receiveSearchName(DatagramPacket petition){
        String name = receive();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < this.listMetadata.size(); i++) {
            if(this.listMetadata.get(i).getName().contains(name)){
                names.add(this.listMetadata.get(i).getName());
            }
        }
        send(petition,String.valueOf(names.size()));
        for (String element : names) {
            send(petition, element);
            System.out.println("send: "+element);
        }
    }
    /**
     * 
     * Get the file name
     * 
     * Look for the MetaData that corresponds to the file with that name
     * 
     * |*******Metadata********|
     * | id | name | fragments |
     * |***********************|
     * 
     * |***********Fragment************|
     * | position | disk | name | data |
     * |*******************************|
     * 
     * |************Data***********|
     * | position | name | content |
     * |***************************|
     * @return 
     * 
     */
    public List<String> receiveGetFile(){
        //System.out.println("\nreceiveGetFile\n");
        String nameFile = receive();
        
        MetaData metadata = searchMetaData(nameFile);
        
        for (int i = 0; i < this.listNodes.size(); i++) {
            //System.out.println("\nFor1: "+i);
            this.listNodes.get(metadata.getFragments().get(i).getDisk()).modeRead(nameFile);
        }
        
        List<String> fragmentsFile = new ArrayList<>();

        for (int i = 0; i < metadata.getFragments().size(); i++) {
            //System.out.println("\nFor2: "+i);
            while(!this.listNodes.get(metadata.getFragments().get(i).getDisk()).isReady()){System.out.print("");} //while notReady
            if(!metadata.getFragments().get(i).getData().isParity()){
                String content = this.listNodes.get(metadata.getFragments().get(i).getDisk()).getContent();
                if(content.equals("FileNotFound")){
                    fragmentsFile.add("XXXXXXX");
                }else{
                    fragmentsFile.add(content);
                }
            }else{
                this.listNodes.get(metadata.getFragments().get(i).getDisk()).setReady(false);
            }
        }

        return fragmentsFile;
    }
    
    /**
     * @param petition : Client request with which you will be answered
     * @param fragmentsFile 
     */
    public void joinFile(DatagramPacket petition, List<String> fragmentsFile){
        //System.out.println("\njoinFile\n");
        String fullFile = "";
        for (String element : fragmentsFile) {
            fullFile += element;
        }
        System.out.println(fullFile);
        send(petition, fullFile);
    }
    
    /**
     * @param name : File name
     * @return Metadata : Metadata with the data of the searched file
     * 
     * Look for the file MetaData with the requested name
     */
    private MetaData searchMetaData(String name){
        
        for (int i = 0; i < this.listMetadata.size(); i++) {
            if(this.listMetadata.get(i).getName().equals(name)){
                //System.out.println("searchMetaData: "+i);
                return this.listMetadata.get(i);
            }
        }
        
        return null;
    }
    
    /**
     * @param name : Name of file
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
    public MetaData splitFile(String name, String file){

        int sizeFragment = file.length() / (this.numberDisk - 1); // 557 / 2 = 778.5
        int residuo = file.length() % (this.numberDisk - 1);      // 557 % 2 = 1
        int j = 0; 
        
        MetaData metadata = new MetaData(this.listMetadata.size(), name);
        String fragmentBook;
        
        int[] array = shuffleArray();
        
        for (int i = 0; i < this.numberDisk-1; i++) {
            if((i+1) == this.numberDisk-1){
                fragmentBook = file.substring(j, j + sizeFragment + residuo);
            }else{
                fragmentBook = file.substring(j, j + sizeFragment);
            }
            j += sizeFragment;
            metadata.getFragments().add(new Fragment(i, array[i], name, fragmentBook, false));
        }          
         
        metadata.getFragments().add(new Fragment(this.numberDisk-1, array[this.numberDisk-1], name, file, true));
             
        //System.out.println(metadata.toString());
        
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
        //System.out.println("saveSplitFile");
        this.listMetadata.add(metadata);       
        
        for (int i = 0; i < this.listNodes.size(); i++) {
            //System.out.println("Nodo: "+metadata.getFragments().get(i).getDisk());
            //System.out.println("Name: "+metadata.getFragments().get(i).getData().getName());
            //System.out.println("Content: "+metadata.getFragments().get(i).getData().getContent());
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
     * @param size : File size
     * @return String with client data book
     * 
     * Method that listen to the clients, but with a dynamic size buffer
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
        buffer = new byte[send.length()];
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
