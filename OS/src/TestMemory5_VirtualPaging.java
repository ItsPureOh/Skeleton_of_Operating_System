/**
 * TestMemory5_VirtualPaging
 *
 * This test validates the functionality of the virtual paging system
 * and the Translation Lookaside Buffer (TLB) behavior, including
 * TLB misses, mappings, and possible replacements.
 *
 * The test performs the following steps:
 * 1. Allocates three virtual pages (3 KB total).
 * 2. Accesses each page to trigger TLB misses and generate mappings.
 * 3. Prints the TLB after each access to visualize its state.
 * 4. Re-accesses previous pages to test for hits, consistency, and
 *    replacement behavior when the TLB becomes full.
 *
 * Expected behavior:
 * - First access to each new page should cause a TLB miss and a mapping.
 * - Subsequent accesses to recently used pages should result in TLB hits.
 * - Re-accessing earlier pages may show replacement if the TLB size is limited.
 */
public class TestMemory5_VirtualPaging extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("\n--- Test 5: Complex TLB Mapping and Replacement ---");

        // allocate 3 pages (3KB)
        int base = OS.AllocateMemory(1024 * 3);

        // Access first page — expect MISS and mapping to some physical page
        int addr1 = base;
        Hardware.PrintTLB();
        Hardware.Write(addr1, (byte) 10);
        Hardware.PrintTLB();
        byte val1 = Hardware.Read(addr1);
        System.out.println("First access (expected TLB miss): value=" + val1);

        // Access second page — different virtual page, should map to another physical frame
        int addr2 = base + 1024;
        Hardware.Write(addr2, (byte) 20);
        byte val2 = Hardware.Read(addr2);
        System.out.println("Second page access (expected TLB miss): value=" + val2);
        Hardware.PrintTLB();

        // Access third page — another different virtual page
        int addr3 = base + 2048;
        Hardware.Write(addr3, (byte) 30);
        byte val3 = Hardware.Read(addr3);
        System.out.println("Third page access (expected TLB miss): value=" + val3);
        Hardware.PrintTLB();

        // Re-access first page — may or may not still be in TLB depending on replacement
        byte val4 = Hardware.Read(addr1);
        System.out.println("Re-access first page (check replacement): value=" + val4);
        Hardware.PrintTLB();

        // Re-access second page — verify mapping consistency
        byte val5 = Hardware.Read(addr2);
        System.out.println("Re-access second page (check consistency): value=" + val5);
        Hardware.PrintTLB();

        OS.FreeMemory(base, 1024 * 3);
        OS.Exit();
    }
}
