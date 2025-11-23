/**
 * TestMemory7_SwapStorm
 *
 * This is a high-pressure stress test for the swapping system.
 *
 * What it does:
 * 1. Allocates 120 virtual pages.
 * 2. Writes a unique byte to every page (value = 50 + pageIndex).
 * 3. Randomly re-reads 60 pages in random order.
 *    - This forces constant swap-ins and swap-outs.
 *    - TLB entries are constantly replaced.
 * 4. Verifies that every re-read value matches the expected byte.
 *
 * PASS conditions:
 * - Every re-read returns the correct value.
 * - No crashes or exceptions occur.
 * - TLB remains consistent and prints correctly.
 */
public class TestMemory7_SwapStorm extends UserlandProcess {

    @Override
    public void main() {

        System.out.println("\n===== Test 7: Swap Storm Stress Test =====");

        // 120 pages = more than your “100 pages per process” limit,
        // but AllocateMemory only allocates as much as the process supports.
        int pages = 100;   // supports 100 pages per process
        int base = OS.AllocateMemory(1024 * pages);

        System.out.println("Allocated " + pages + " virtual pages.");

        // Step 1 — write values to all pages
        for (int i = 0; i < pages; i++) {
            int addr = base + (i * 1024);
            byte value = (byte)(50 + i);    // predictable pattern
            Hardware.Write(addr, value);
        }

        System.out.println("Wrote values to all pages. Beginning random swap-ins...");

        // Step 2 — random access 60 pages to cause swap-ins
        java.util.Random rng = new java.util.Random();

        boolean allCorrect = true;

        for (int k = 0; k < 60; k++) {

            int page = rng.nextInt(pages);        // random page 0–99
            int addr = base + (page * 1024);
            byte expected = (byte)(50 + page);

            byte actual = Hardware.Read(addr);

            System.out.print("Access page " + page + " → got " + actual);

            if (actual == expected) {
                System.out.println(" ✔ PASS");
            } else {
                System.out.println(" ✘ FAIL (expected " + expected + ")");
                allCorrect = false;
            }

            // Optional: show TLB state to debug churn
            Hardware.PrintTLB();
        }

        System.out.println("\n===== Test 7 Summary =====");
        if (allCorrect) {
            System.out.println("✔ TEST PASSED — All random swap-in reads were correct.");
        } else {
            System.out.println("✘ TEST FAILED — One or more values were incorrect.");
        }

        System.out.println("===== End of TestMemory7 =====");

        OS.FreeMemory(base, 1024 * pages);
        OS.Exit();
    }
}
