package Domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mario
 */
public class Node extends Thread{
    private int id;
    private final File file;
    private final String path;
    private final boolean state;
    private boolean writeData;
    private boolean readData;
    private boolean ready;
    private boolean damaged;
    
    private String name="";
    private String content="";
    
    public Node(int id) {
        this.id = id;
        this.path = "Raid5\\Disk_"+id;
        this.file = new File(path);  
        this.file.mkdir();
        this.state = true;
        this.writeData = false;
        this.readData = false;
        this.ready = false;
        this.damaged = false;
    }

    /**
     * Start node
     */
    @Override
    public void run() {
        while(this.state){
            try {
                if(this.writeData){
                    save(this.name, this.content);
                    this.writeData = false;
                }
                if(this.readData){
                    this.content = read(this.name);
                    this.readData = false;
                }
                
                Node.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @param name
     * @param content 
     * 
     * Activate writing mode
     */
    public void modeWrite(String name, String content){
        this.name = name;
        this.content = content;
        this.writeData = true;
    }
    
    /**
     * @param name 
     * 
     * Activate reading mode
     */
    public void modeRead(String name){  
        this.name = name;
        this.readData = true;
    }
    
    /**
     * @param name : Name fragment file
     * @return Fragment file
     * 
     * Check if the file exists
     * If it exists, it returns
     * else return file not found
     */
    private String read(String name){
        String contents = "";
        FileReader fr = null;
        File folder = new File(path);
        File fileContent = new File(path+"\\"+name+".txt");
        if (!folder.exists() || !fileContent.exists()) {
            this.damaged = true;
            ready(); 
            return "ErrorFileNotFoundError";          
        }
        try {
            fr = new FileReader(fileContent);
            BufferedReader br = new BufferedReader(fr);

            String linea;
            while((linea=br.readLine())!=null){
                contents += linea;
            }
        }catch(IOException e){
            System.out.println("Node.read(): "+e.getMessage());          
        }finally{
            try{                    
                if( null != fr ){   
                    fr.close();     
                }                  
            }catch (IOException e2){ 
                System.out.println("Node.read(): "+e2.getMessage());
            }
        }
        ready();
        return contents;
    }
    
    /**
     * @param name : File name
     * @param content : File content
     * 
     * Save the file fragment
     */
    public void save(String name, String content){
        FileWriter fw = null;
        try {
            File fileContent = new File(path+"\\"+name+".txt");
        
            if (!fileContent.exists()) {
                fileContent.createNewFile();
            }
            
            fw = new FileWriter(fileContent);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(content);
            }
        } catch (IOException e) {
            System.err.println("Node.Save() "+e.getMessage());
        }finally{
            try{                    
                if( null != fw ){   
                    fw.close();     
                }                  
            }catch (IOException e2){ 
                System.out.println("Node.Save(): "+e2.getMessage());
            }
        }
        
    }
    
    /**
     * Notifies that the node is ready to be read
     */
    private void ready(){
        this.ready = true;
    }

    /**
     * @return : File fragment content
     */
    public String getContent() {
        this.ready = false;
        return content;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getIdentification() {
        return id;
    }

    /**
     * @return Notify if the disk is damaged
     */
    public boolean isDamaged() {
        return damaged;
    } 
}
