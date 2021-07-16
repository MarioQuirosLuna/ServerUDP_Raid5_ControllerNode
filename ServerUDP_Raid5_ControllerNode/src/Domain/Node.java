package Domain;

import java.io.File;

/**
 *
 * @author mario
 */
public class Node extends Thread{
    private int id;
    private File file;
    private String path;
    private boolean state;

    public Node(int id) {
        this.id = id;
        this.path = "Raid5\\Disk_"+id;
        this.file = new File(path);  
        this.file.mkdir();
        this.state = true;
    }

    @Override
    public void run() {
        while(this.state){
            
        }
    }
    
    
    
}
