/**
 * TestMemory9_ModifyAfterSwap
 *
 * PURPOSE:
 * This test checks that when:
 *   1. A page is written,
 *   2. Swapped out,
 *   3. Later swapped back in,
 *   4. Modified again,
 *   5. Swapped out again,
 *   6. Swapped back in again,
 * The most recent modification must survive all swap cycles.
 *
 * WHAT IT DOES:
 * 1. Allocates 100 virtual pages.
 * 2. Writes a unique byte to each page: (30 + pageIndex).
 * 3. Forces swap-outs by scanning all pages twice.
 * 4. Modifies **ONE** page after it has definitely been swapped out.
 * 5. Forces more swap activity.
 * 6. Re-reads the modified page.
 *
 * PASS CONDITION:
 * - The modified page returns the value 99.
 * - No exceptions or crashes occur.
 */
public class TestMemory9_ModifyAfterSwap extends UserlandProcess {

    @Override
    public void main() {

        System.out.println("\n===== Test 9: Modify-After-Swap =====");

        int pages = 100;
        int base = OS.AllocateMemory(1024 * pages);

        // Step 1 — write predictable values
        for (int i = 0; i < pages; i++) {
            Hardware.Write(base + i * 1024, (byte)(30 + i));
        }

        // Step 2 — force swaps by touching every page twice
        for (int r = 0; r < 2; r++) {
            for (int i = 0; i < pages; i++) {
                Hardware.Read(base + i * 1024);
            }
        }

        // Step 3 — pick a page and modify it AFTER swap-outs
        int target = 37;
        Hardware.Write(base + target * 1024, (byte)99);

        // Step 4 — cause more swapping
        for (int i = 0; i < pages; i++) {
            Hardware.Read(base + i * 1024);
        }

        // Step 5 — verify the modified page persisted
        byte actual = Hardware.Read(base + target * 1024);

        if (actual == 99) {
            System.out.println("✔ TEST PASSED — modified page survived swapping.");
        } else {
            System.out.println("✘ TEST FAILED — expected 99, got " + actual);
        }

        OS.FreeMemory(base, 1024 * pages);
        OS.Exit();
    }
}
