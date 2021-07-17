package Domain;

/**
 *
 * @author mario
 */
public class Fragment {
    private int id;
    private int disk;
    private Data data;

    public Fragment(int id, int disk,String name, String data) {
        this.id = id;
        this.disk = disk;
        this.data = new Data(name, data);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
    
    
}
