import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Clock {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    // A separate time source lets us use a fixed time in tests.
    private final java.time.Clock timeSource;
    private String currentTime;
    // The first value is prepared in the constructor.
    private boolean updateAvailable = true;

    public Clock() {
        this(java.time.Clock.systemDefaultZone());
    }

    Clock(java.time.Clock timeSource) {
        this.timeSource = timeSource;
        currentTime = readTime();
    }

    private String readTime() {
        return LocalDateTime.now(timeSource).format(FORMATTER);
    }

    public synchronized void updateTime() throws InterruptedException {
        // Wait until the display has read the previous update.
        // wait() releases the lock so the display can access the clock.
        while (updateAvailable) {
            wait();
        }
        currentTime = readTime();
        updateAvailable = true;
        // Let the waiting display know that a new value is ready.
        notifyAll();
    }

    public synchronized String awaitTime() throws InterruptedException {
        // Check again after waking up in case no update is ready yet.
        while (!updateAvailable) {
            wait();
        }
        String time = currentTime;
        // Mark this update as consumed so the updater can continue.
        updateAvailable = false;
        notifyAll();
        return time;
    }
}
