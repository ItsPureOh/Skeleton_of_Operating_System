public class TestMemory1_WriteAndRead extends UserlandProcess{

    @Override
    public void main() {

        int addr = OS.AllocateMemory(1024 * 10);

        for (int i = 0; i < 26; i++) {
            Hardware.Write(addr + i, (byte)(65 + i)); // Write A, B, C...
        }

        // Read them back
        for (int i = 0; i < 26; i++) {
            byte val = Hardware.Read(addr + i);
            System.out.println("Read from " + (addr + i) + ": " + (char)val);
        }

        System.out.println("✅ Basic Read/Write test complete");
        OS.Exit();
    }
}
