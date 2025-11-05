/**
 * TestMemory3_FreeAndReuseMemory
 *
 * This test verifies that the operating system correctly frees and reuses
 * physical memory after it has been released by a process.
 *
 * The test allocates a block of memory, frees it, and then performs another
 * allocation of the same size. If the allocator reuses freed pages, both
 * addresses should be identical. Otherwise, the result depends on the
 * memory allocation policy (e.g., first-fit, next-fit, etc.).
 */
public class TestMemory3_FreeAndReuseMemory extends UserlandProcess{

    @Override
    public void main() {
        int addr1 = OS.AllocateMemory(1024 * 2);
        OS.FreeMemory(addr1, 1024 * 2);

        int addr2 = OS.AllocateMemory(1024 * 2);

        System.out.println("\n--- Test 3: Free and Reuse ---");
        System.out.println("Addr1: " + addr1 + " | Addr2: " + addr2);

        if (addr1 == addr2)
            System.out.println("Passed: Freed memory reused successfully.");
        else
            System.out.println("Info: Freed memory not reused (depends on allocator policy).");
        OS.Exit();
    }
}
