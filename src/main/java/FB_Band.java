public class FB_Band {
    public boolean bLaeuft;

    public void update(boolean bStart, boolean bStop) {
        if (bStart) {
            bLaeuft = true;
        }

        if (bStop) {
            bLaeuft = false;
        }
    }
}