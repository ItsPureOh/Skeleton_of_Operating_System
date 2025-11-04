public class TestMemory5_VirtualPaging extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("\n--- Test 5: GetMapping() and TLB Miss ---");

        // allocate 3 pages (3KB)
        int base = OS.AllocateMemory(1024 * 3);

        // 1️⃣ first access: should MISS TLB and trigger GetMapping()
        int addr1 = base; // first page
        Hardware.Write(addr1, (byte) 10);
        byte val1 = Hardware.Read(addr1);
        System.out.println("First access (expected TLB miss): value=" + val1);

        // 2️⃣ second access to same page: should HIT TLB (no GetMapping)
        byte val2 = Hardware.Read(addr1);
        System.out.println("Second access (expected TLB hit): value=" + val2);

        // 3️⃣ access a different page: should MISS again (new GetMapping)
        int addr2 = base + 1024; // next virtual page
        Hardware.Write(addr2, (byte) 20);
        byte val3 = Hardware.Read(addr2);
        System.out.println("Access new page (expected TLB miss): value=" + val3);

        // 4️⃣ access first page again: if TLB size = 2, should still be cached or replaced
        byte val4 = Hardware.Read(addr1);
        System.out.println("Re-access first page (depends on replacement): value=" + val4);

        OS.FreeMemory(base, 1024 * 3);
        OS.Exit();
    }
}
