public class VirtualToPhysicalMapping {
    // which frame in physical memory this virtual page is using
    public int physicalPage;

    // which page number in the swap file (disk), -1 = never saved
    public int diskPage;

    public VirtualToPhysicalMapping() {
        this.physicalPage = -1;   // -1 = not in RAM
        this.diskPage = -1;       // -1 = not on disk yet
    }
}
