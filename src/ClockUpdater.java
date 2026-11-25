class ClockUpdater extends Thread {
    private static final long UPDATE_INTERVAL_MS = 1000;
    private final Clock clock;

    public ClockUpdater(Clock clock) {
        super("ClockUpdater-Thread");
        this.clock = clock;
        // The assignment requires minimum priority for the updater.
        // Priority does not guarantee which thread runs first.
        setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                // The clock already has an initial value, so wait first.
                Thread.sleep(UPDATE_INTERVAL_MS);
                clock.updateTime();
            }
        } catch (InterruptedException e) {
            // Restore the interrupt flag before leaving the thread.
            Thread.currentThread().interrupt();
        }
    }

    public void stopUpdating() {
        interrupt();
    }
}
