public class Hardware {
    // [virtual page, physical page]
    public static int[][] tlb = new int [2][2];
    private static byte[] memory = new byte[1048576];

    public static byte Read(int address){
        int virtualPageNumber = (address / 1024);
        // check whether virtual page number contained in TLB
        int virtualPageIndexInTLB = virtualPageInTLB(virtualPageNumber);
        // if tlb contained current virtual page number
        if (virtualPageIndexInTLB != -1){
            int physicalAddress = getPhysicalMemoryAddress(virtualPageIndexInTLB, virtualPageNumber);
            return memory[physicalAddress];
        }
        OS.GetMapping(address);
        int physicalAddress = getPhysicalMemoryAddress(virtualPageIndexInTLB, virtualPageNumber);
        return memory[physicalAddress];
    }
    public static void Write(int address, byte value){
        int virtualPageNumber = (address / 1024);
        // check whether virtual page number contained in TLB
        int virtualPageIndexInTLB = virtualPageInTLB(virtualPageNumber);
        // if tlb contained current virtual page number
        if (virtualPageIndexInTLB != -1){
            int physicalAddress = getPhysicalMemoryAddress(virtualPageIndexInTLB, virtualPageNumber);
            memory[physicalAddress] = value;
        }
    }

    private static int virtualPageInTLB(int virtualPageNumber){
        for (int i = 0; i < 2; i++) {
            if (tlb[i][0] == virtualPageNumber) {
                return i;
            }
        }
        return -1;
    }

    private static int getPhysicalMemoryAddress(int virtualPageIndexInTLB, int virtualPageNumber){
        int offset = virtualPageNumber % 1024;
        int physicalPageNumber = tlb[virtualPageIndexInTLB][1];
        return 1024 * physicalPageNumber + offset;
    }
}
