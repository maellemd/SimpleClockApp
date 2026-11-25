class ClockDisplay extends Thread {
    private final Clock clock;

    public ClockDisplay(Clock clock) {
        super("ClockDisplay-Thread");
        this.clock = clock;
        // Keep the maximum priority required by the assignment.
        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                // Wait for a new update instead of reading the same value twice.
                System.out.println("Current time: " + clock.awaitTime());
            }
        } catch (InterruptedException e) {
            // An interruption is used to stop the worker cleanly.
            Thread.currentThread().interrupt();
        }
    }

    public void stopDisplaying() {
        interrupt();
    }
}
