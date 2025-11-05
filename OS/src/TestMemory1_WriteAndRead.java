/**
 * TestMemory1_WriteAndRead
 *
 * This test verifies the basic functionality of the simulated memory system.
 * It allocates a block of memory, writes a sequence of bytes (A–Z) to it,
 * and then reads them back to confirm that memory read/write operations
 * work correctly through the OS and Hardware layers.
 */
public class TestMemory1_WriteAndRead extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("\n--- Test 1: Write and Read ---");
        int addr = OS.AllocateMemory(1024 * 10);

        for (int i = 0; i < 26; i++) {
            Hardware.Write(addr + i, (byte)(65 + i)); // Write A, B, C...
        }

        // Read them back
        for (int i = 0; i < 26; i++) {
            byte val = Hardware.Read(addr + i);
            System.out.println("Read from " + (addr + i) + ": " + (char)val);
        }

        System.out.println("Basic Read/Write test complete");
        OS.Exit();
    }
}
