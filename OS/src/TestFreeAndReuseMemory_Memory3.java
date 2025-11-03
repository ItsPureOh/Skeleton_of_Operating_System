public class TestFreeAndReuseMemory_Memory3 extends UserlandProcess{

    @Override
    public void main() {
        int addr1 = OS.AllocateMemory(1024 * 2);
        OS.FreeMemory(addr1, 1024 * 2);

        int addr2 = OS.AllocateMemory(1024 * 2);

        System.out.println("\n--- Test 3: Free and Reuse ---");
        System.out.println("Addr1: " + addr1 + " | Addr2: " + addr2);

        if (addr1 == addr2)
            System.out.println("✅ Passed: Freed memory reused successfully.");
        else
            System.out.println("ℹ️ Info: Freed memory not reused (depends on allocator policy).");
        OS.Exit();
    }
}
