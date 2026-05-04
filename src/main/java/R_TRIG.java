public class R_TRIG {
    public boolean Q;

    private boolean lastCLK;

    public void update(boolean CLK) {
        Q = CLK && !lastCLK;
        lastCLK = CLK;
    }
}