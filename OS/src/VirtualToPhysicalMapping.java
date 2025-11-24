/**
 * Represents the per-page mapping information for a virtual page.
 *
 * Fields:
 *   physicalPage  — The physical frame number currently holding this page.
 *                    -1 means the page is not resident in RAM.
 *
 *   diskPage      — The swap-file page number where this virtual page was
 *                    previously saved.
 *                    -1 means the page has never been written to disk.
 *
 * Notes:
 *   • A virtual page begins with both fields set to -1 (not in RAM, not on disk).
 *   • When evicted, physicalPage becomes -1 and diskPage is assigned a slot.
 *   • When reloaded, physicalPage is filled and data is restored from diskPage.
 */

public class VirtualToPhysicalMapping {
    // which frame in physical memory this virtual page is using
    public int physicalPage;

    // which page number in the swap file (disk), -1 = never saved
    public int diskPage;

    public VirtualToPhysicalMapping() {
        this.physicalPage = -1;   // -1 = not in RAM
        this.diskPage = -1;       // -1 = not on disk yet
    }
}
