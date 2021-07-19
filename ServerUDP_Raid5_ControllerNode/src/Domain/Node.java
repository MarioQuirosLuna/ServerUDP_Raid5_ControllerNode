package Domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
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
    }

    @Override
    public void run() {
        while(this.state){
            try {
                if(this.writeData){
                    //System.out.println("Saving in node: "+this.id);
                    save(this.name, this.content);
                    //System.out.println("Saved by node: "+this.id);
                    this.writeData = false;
                }
                if(this.readData){
                    //System.out.println("Getting from node: "+this.id);
                    this.content = read(this.name);
                    //System.out.println("Obtained by node: "+this.id);
                    this.readData = false;
                }
                
                Node.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void modeWrite(String name, String content){
        //System.out.println("modeWrite node: "+this.id);
        this.name = name;
        this.content = content;
        this.writeData = true;
    }
    
    public void modeRead(String name){  
        //System.out.println("modeRead node: "+this.id);
        this.name = name;
        this.readData = true;
    }
    
    private String read(String name){
        String contents = "";
        FileReader fr = null;
        try {
            fr = new FileReader(new File(path+"\\"+name+".txt"));
            BufferedReader br = new BufferedReader(fr);

            String linea;
            while((linea=br.readLine())!=null){
                contents += linea;
            }
        }catch(IOException e){
            System.out.println("Node.read(): "+e.getMessage());
            ready();
            return "FileNotFound";
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
    
    private void ready(){
        //System.out.println("ready");
        this.ready = true;
    }

    public String getContent() {
        //System.out.println("\ngetContent"+this.id);
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
    
}
