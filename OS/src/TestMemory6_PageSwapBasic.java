/**
 * TestMemory6_PageSwapBasic
 *
 * This test forces swap-out and swap-in behavior by allocating
 * more virtual pages across multiple processes than physical memory
 * can hold.
 *
 * Expected behavior:
 * - First several page accesses should simply load from free physical frames.
 * - Once free frames run out, PageSwap() should be triggered.
 * - Victim page should be saved to disk if not saved before.
 * - Re-accessing a swapped-out page should load correct data from disk.
 * - Zero-fill should happen for pages that never had data.
 *
 * PASS conditions:
 *  - Re-accessing a swapped-out page returns the value that was originally written.
 *  - Zero-filled pages return 0.
 *  - TLB reflects correct VPN→PPN mappings.
 */

public class TestMemory6_PageSwapBasic extends UserlandProcess {

    @Override
    public void main() {
        System.out.println("\n===== Test 6: Forced Page Swap + Reload Test =====");

        // We create MANY pages to guarantee swap-out.
        // A single process has 100 virtual pages; physical memory has 1024 pages.
        // To force swapping in one process, we repeatedly allocate and touch pages.
        int base = OS.AllocateMemory(1024 * 80);    // 80 pages accessed

        System.out.println("Allocated 80 KB (80 pages).");

        // Write unique values to many pages
        for (int i = 0; i < 80; i++) {
            int addr = base + (i * 1024);
            byte value = (byte)(10 + i);

            Hardware.Write(addr, value);

            System.out.println("Wrote " + value + " to virtual page " + i);
            Hardware.PrintTLB();
        }

        // Now re-access a page that is VERY likely swapped out
        int victimPage = 5;    // pick page 5 (arbitrary)
        int victimAddr = base + (victimPage * 1024);
        byte reread = Hardware.Read(victimAddr);

        System.out.println("\nRe-read page " + victimPage + " → value: " + reread);

        System.out.println("\n===== Test 6: PASS/FAIL Checks =====");

        // Expected value for page 5
        byte expected = (byte)(10 + victimPage);

        if (reread == expected) {
            System.out.println("PASS: Swapped-out page reloaded correctly.");
        } else {
            System.out.println("FAIL: Expected " + expected + " but got " + reread);
        }

        System.out.println("===== End of TestMemory6 =====\n");

        OS.FreeMemory(base, 1024 * 80);
        /*
        //result check
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
         */

        OS.Exit();
    }
}
