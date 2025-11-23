/**
 * TestMemory8_MultiProcessSwap
 *
 * Forces swapping across multiple processes.
 * Creates many helper processes that each allocate/touch 100 pages.
 * Then main process allocates pages, writes values, and re-reads them
 * under swap pressure to verify correctness.
 */
public class TestMemory8_MultiProcessSwap extends UserlandProcess {

    // helper process to consume lots of physical frames
    public static class MemoryHog extends UserlandProcess {
        private final int id;
        public MemoryHog(int id) { this.id = id; }

        @Override
        public void main() {
            int pages = 100; // PCB limit
            int base = OS.AllocateMemory(1024 * pages);

            // touch all pages so they get mapped into RAM
            for (int i = 0; i < pages; i++) {
                int addr = base + i * 1024;
                Hardware.Write(addr, (byte)(id)); // tag with hog id
            }

            // keep process alive to maintain memory pressure
            OS.Sleep(5000);
            OS.Exit();
        }
    }

    @Override
    public void main() {
        System.out.println("\n===== Test 8: Multi-Process Swap Stress =====");

        // 1) Spawn enough hogs to exceed 1024 physical pages
        int hogCount = 12; // 12*100 = 1200 pages > 1024 frames
        for (int i = 0; i < hogCount; i++) {
            OS.CreateProcess(new MemoryHog(i + 1), OS.PriorityType.background);
        }

        // Give hogs time to allocate/touch
        OS.Sleep(200);

        // 2) Main allocates some pages and writes known values
        int myPages = 20;
        int myBase = OS.AllocateMemory(1024 * myPages);

        for (int i = 0; i < myPages; i++) {
            int addr = myBase + i * 1024;
            Hardware.Write(addr, (byte)(50 + i));
        }
        System.out.println("Main wrote values to its pages.");

        // 3) Re-read under pressure: should trigger swap-ins
        boolean allCorrect = true;
        for (int i = 0; i < myPages; i++) {
            int addr = myBase + i * 1024;
            byte expected = (byte)(50 + i);
            byte actual = Hardware.Read(addr);

            if (actual != expected) {
                System.out.println("✘ FAIL page " + i + ": expected " + expected + " got " + actual);
                allCorrect = false;
            }
        }

        // 4) Summary
        System.out.println("\n===== Test 8 Summary =====");
        if (allCorrect) {
            System.out.println("✔ TEST PASSED — multi-process swap preserved main data.");
        } else {
            System.out.println("✘ TEST FAILED — data corruption under multi-process swap.");
        }

        OS.FreeMemory(myBase, 1024 * myPages);
        OS.Exit();
    }
}
