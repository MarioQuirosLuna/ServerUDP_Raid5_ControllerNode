package Domain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mario
 */
public class Node extends Thread{
    private int id;
    private File file;
    private String path;
    private boolean state;
    private boolean saveData;
    
    private String name="";
    private String content="";

    public Node(int id) {
        this.id = id;
        this.path = "Raid5\\Disk_"+id;
        this.file = new File(path);  
        this.file.mkdir();
        this.state = true;
        this.saveData = false;
    }

    @Override
    public void run() {
        while(this.state){
            
            try {
                if(saveData){
                    System.out.println("Saving in node: "+this.id);
                    save(this.name, this.content);
                    System.out.println("Saved by node: "+this.id);                  
                }
                
                this.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void modeWrite(String name, String content){       
        this.name = name;
        this.content = content;
        this.saveData = true;
    }
    
    public void save(String name, String content){
        try {
            File file = new File(path+"\\"+name+".txt");
        
            if (!file.exists()) {
                file.createNewFile();
            }
            
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (Exception e) {
            System.err.println("Node.Save() "+e.getMessage());
        }
        
        this.saveData = false;
    }
}
