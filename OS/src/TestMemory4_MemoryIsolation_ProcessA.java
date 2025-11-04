public class TestMemory4_MemoryIsolation_ProcessA extends UserlandProcess {

    @Override
    public void main() {
        int addr = OS.AllocateMemory(1024 * 2); // 2 KB
        Hardware.Write(addr, (byte) 11);
        byte value = Hardware.Read(addr);
        System.out.println("Process A wrote/read: " + value + " at addr " + addr);
        OS.Exit();
    }
}
