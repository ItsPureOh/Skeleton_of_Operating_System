/**
 * TestMemory4_VirtualHoleReuse
 *
 * This test evaluates how the memory allocator manages fragmented
 * (non-contiguous) free regions — also known as “virtual holes” —
 * and whether it can correctly reuse and merge them during subsequent
 * allocations.
 *
 * The test performs the following steps:
 * 1. Allocates multiple memory chunks of different sizes.
 * 2. Frees several blocks to create holes between allocated regions.
 * 3. Requests new allocations that should fit exactly into the freed holes.
 * 4. Frees and reallocates additional blocks to verify merging behavior.
 *
 * Expected behavior:
 * - Freed regions should be reused for new allocations of matching size.
 * - The allocator should properly merge adjacent free regions when possible.
 */
public class TestMemory4_VirtualHoleReuse extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("\n--- Test 4: Complex Virtual Hole Reuse ---");

        // Step 1: allocate several chunks (1–4 KB each)
        int a1 = OS.AllocateMemory(1024 * 1); // page 0
        int a2 = OS.AllocateMemory(1024 * 2); // pages 1–2
        int a3 = OS.AllocateMemory(1024 * 3); // pages 3–5
        int a4 = OS.AllocateMemory(1024 * 1); // page 6
        int a5 = OS.AllocateMemory(1024 * 2); // pages 7–8

        System.out.println("Initial allocations:");
        System.out.println("a1=" + a1 + " a2=" + a2 + " a3=" + a3 + " a4=" + a4 + " a5=" + a5);

        // Step 2: free some blocks, leaving irregular holes
        OS.FreeMemory(a2, 1024 * 2); // free pages 1–2
        OS.FreeMemory(a4, 1024 * 1); // free page 6
        System.out.println("Freed middle blocks (a2, a4)");

        // Step 3: allocate new blocks with different sizes
        int b1 = OS.AllocateMemory(1024 * 2); // should reuse a2 hole (pages 1–2)
        int b2 = OS.AllocateMemory(1024 * 1); // should reuse a4 hole (page 6)
        System.out.println("New allocations:");
        System.out.println("b1=" + b1 + " b2=" + b2);

        // Step 4: free another chunk and allocate bigger block to test merging
        OS.FreeMemory(a3, 1024 * 3); // free pages 3–5
        System.out.println("Freed large block (a3)");

        int c1 = OS.AllocateMemory(1024 * 4); // should occupy pages 9-12 (fills merged hole)
        System.out.println("New large allocation c1=" + c1);

        int c2 = OS.AllocateMemory(1024 * 3); // should occupy pages 3-5
        System.out.println("New large allocation c2=" + c2);

        // Step 5: verify behavior
        boolean reusedA2 = (b1 == a2);
        boolean reusedA4 = (b2 == a4);
        boolean reusedA3 = (c2 == a3);
        boolean merged = (c1 == 1024 * 9);

        if (reusedA2 && reusedA4 && merged && reusedA3)
            System.out.println("Passed: allocator correctly reused and merged free holes.");
        else {
            System.out.println("Partial success:");
            System.out.println("  reusedA2=" + reusedA2 + " reusedA4=" + reusedA4 + " merged=" + merged);
        }

        OS.Exit();
    }
}
