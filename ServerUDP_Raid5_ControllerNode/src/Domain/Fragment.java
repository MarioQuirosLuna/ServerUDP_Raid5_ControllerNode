package Domain;

/**
 *
 * @author mario
 */
public class Fragment {
    
    /**
     * position : Position of the book fragment
     * disk : Disk identifier
     * data : Book fragment data
     */
    private int position;
    private int disk;
    private Data data;

    public Fragment(int position, int disk, String name, String data) {
        this.position = position;
        this.disk = disk;
        this.data = new Data(position, name, data);
    }

    public int getId() {
        return position;
    }

    public void setId(int position) {
        this.position = position;
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

    @Override
    public String toString() {
        return "Fragment{" + "position=" + position + ", disk=" + disk + ", data=" + data + '}';
    }
    
    
}
