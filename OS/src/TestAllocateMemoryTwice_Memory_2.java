public class TestAllocateMemoryTwice_Memory_2 extends UserlandProcess {

    @Override
    public void main() {
        int addr1 = OS.AllocateMemory(1024 * 2); // 2 pages
        int addr2 = OS.AllocateMemory(1024 * 3); // 3 pages

        Hardware.Write(addr1, (byte)10);
        Hardware.Write(addr2, (byte)20);

        byte v1 = Hardware.Read(addr1);
        byte v2 = Hardware.Read(addr2);

        System.out.println("\n--- Test 2: Multiple Allocations ---");
        System.out.println("Value at addr1: " + v1);
        System.out.println("Value at addr2: " + v2);

        if (v1 == 10 && v2 == 20)
            System.out.println("✅ Passed: Memory isolated correctly.");
        else
            System.out.println("❌ Failed: Memory overlap or mapping error.");
        OS.Exit();
    }
}
