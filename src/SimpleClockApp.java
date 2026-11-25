public class SimpleClockApp {
    private static final long RUN_DURATION_MS = 10_000;

    public static void main(String[] args) {
        // Both worker threads use the same clock object.
        Clock clock = new Clock();
        ClockUpdater updaterThread = new ClockUpdater(clock);
        ClockDisplay displayThread = new ClockDisplay(clock);

        System.out.println("Starting Simple Clock Application...");
        System.out.println("Display thread priority: " + displayThread.getPriority());
        System.out.println("Updater thread priority: " + updaterThread.getPriority());
        System.out.println();

        // start() runs the workers in separate threads.
        updaterThread.start();
        displayThread.start();

        boolean interrupted = false;
        try {
            Thread.sleep(RUN_DURATION_MS);
        } catch (InterruptedException e) {
            interrupted = true;
        } finally {
            // Request shutdown even if the main thread was interrupted.
            updaterThread.stopUpdating();
            displayThread.stopDisplaying();

            // Wait for both workers before printing the final message.
            // Use |= so both join calls run even if interrupted is already true.
            interrupted |= joinWorker(updaterThread);
            interrupted |= joinWorker(displayThread);
            // Restore the main thread's interrupt flag after cleanup.
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println();
        System.out.println("Simple Clock Application stopped.");
    }

    private static boolean joinWorker(Thread worker) {
        boolean interrupted = false;
        // If join is interrupted, remember it and keep waiting.
        while (true) {
            try {
                worker.join();
                return interrupted;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }
}
