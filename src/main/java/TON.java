public class TON {
    public boolean Q;

    private boolean lastIN;
    private long startTimeMs;

    public void update(boolean IN, long PT_ms) {
        long now = System.currentTimeMillis();

        if (!IN) {
            Q = false;
            lastIN = false;
            startTimeMs = 0;
            return;
        }

        if (!lastIN) {
            startTimeMs = now;
        }

        Q = (now - startTimeMs) >= PT_ms;
        lastIN = true;
    }
}