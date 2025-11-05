/**
 * The Hardware class simulates a simplified hardware layer that provides
 * memory access and TLB (Translation Lookaside Buffer) translation for
 * virtual-to-physical address mapping.
 *
 * Each page is 1 KB (1024 bytes). When a TLB miss occurs, the system
 * invokes OS.GetMapping() to load the virtual-to-physical mapping.
 */
public class Hardware {
    // Translation Lookaside Buffer (TLB) storing up to two entries.
    // Each entry consists of:
    // Index 0: Virtual Page Number (VPN)
    // Index 1: Physical Page Number (PPN)
    public static int[][] tlb = new int [2][2];

    // Simulated physical memory of 1 MB (1,048,576 bytes).
    private static byte[] memory = new byte[1048576];

    /**
     * Reads a single byte from the given virtual address.
     * Performs a TLB lookup and, if necessary, triggers a TLB miss
     * to fetch the mapping via OS.GetMapping().
     *
     * @param address the virtual address to read from
     * @return the byte value stored at the corresponding physical address
     */
    public static byte Read(int address){
        int virtualPageNumber = (address / 1024);

        // Check if the virtual page is present in the TLB
        int virtualPageIndexInTLB = virtualPageInTLB(virtualPageNumber);

        if (virtualPageIndexInTLB == -1){
            // TLB miss — request OS to update TLB with mapping
            OS.GetMapping(virtualPageNumber);
            virtualPageIndexInTLB = virtualPageInTLB(virtualPageNumber);

        }

        // Translate virtual address to physical address and read memory
        int physicalAddress = getPhysicalMemoryAddress(virtualPageIndexInTLB, address);
        return memory[physicalAddress];
    }
    /**
     * Writes a single byte to the given virtual address.
     * Ensures the address is mapped in the TLB before writing to memory.
     *
     * @param address the virtual address to write to
     * @param value   the byte value to write
     */
    public static void Write(int address, byte value){
        int virtualPageNumber = (address / 1024);

        // Check if the virtual page is present in the TLB
        int virtualPageIndexInTLB = virtualPageInTLB(virtualPageNumber);

        if (virtualPageIndexInTLB == -1){
            // TLB miss — fetch mapping from OS
            OS.GetMapping(virtualPageNumber);

            virtualPageIndexInTLB = virtualPageInTLB(virtualPageNumber);
        }
        // TLB hit — translate to physical address and write
        int physicalAddress = getPhysicalMemoryAddress(virtualPageIndexInTLB, address);
        memory[physicalAddress] = value;
    }

    /**
     * Searches the TLB for a given virtual page number.
     *
     * @param virtualPageNumber the VPN to look up
     * @return index of the TLB entry if found; -1 if not present
     */
    private static int virtualPageInTLB(int virtualPageNumber){
        for (int i = 0; i < 2; i++) {
            if (tlb[i][0] == virtualPageNumber) {
                return i;
            }
        }
        // Not found — TLB miss
        return -1;
    }

    /**
     * Translates a virtual address into a physical address
     * using the specified TLB entry index.
     *
     * @param virtualPageIndexInTLB the index of the TLB entry
     * @param address               the original virtual address
     * @return the corresponding physical address
     */
    private static int getPhysicalMemoryAddress(int virtualPageIndexInTLB, int address){
        int offset = address % 1024;
        int physicalPageNumber = tlb[virtualPageIndexInTLB][1];
        return 1024 * physicalPageNumber + offset;
    }

    /**
     * Prints the current state of the TLB for debugging or visualization.
     */
    public static void PrintTLB() {
        System.out.println("\n=== TLB State ===");
        for (int i = 0; i < 2; i++) {
            System.out.printf("Entry %d: VPN=%d  →  PPN=%d\n",
                    i, tlb[i][0], tlb[i][1]);
        }
        System.out.println("=================\n");
    }
}
