import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

public class ClockTest {
    public static void main(String[] args) throws Exception {
        // A fixed time makes the expected result predictable.
        Clock clock = fixedClock();
        check(clock.awaitTime().equals("14:35:08 25-11-2026"), "Initial formatting");
        // Store the reader result safely so the test thread can check it.
        AtomicReference<String> result = new AtomicReference<>();
        Thread reader = new Thread(() -> {
            try { result.set(clock.awaitTime()); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        reader.setDaemon(true);
        reader.start();
        awaitWaiting(reader);
        check(result.get() == null, "Reader waits for a new update");
        clock.updateTime();
        reader.join(2000);
        check(!reader.isAlive() && "14:35:08 25-11-2026".equals(result.get()),
                "New update wakes reader even with identical formatted time");

        // Check that an unread value cannot be overwritten.
        Clock full = fixedClock();
        Thread writer = new Thread(() -> {
            try { full.updateTime(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        writer.setDaemon(true);
        writer.start();
        awaitWaiting(writer);
        check(writer.isAlive(), "Writer waits while update pending");
        full.awaitTime();
        writer.join(2000);
        check(!writer.isAlive(), "Consuming update wakes writer");
        check(full.awaitTime().equals("14:35:08 25-11-2026"), "Next update is available");

        // Check that interrupting a waiting display stops it.
        Clock empty = fixedClock();
        empty.awaitTime();
        ClockDisplay display = new ClockDisplay(empty);
        display.setDaemon(true);
        display.start();
        awaitWaiting(display);
        display.stopDisplaying();
        display.join(2000);
        check(!display.isAlive(), "Waiting display responds to interruption");

        ClockUpdater updater = new ClockUpdater(fixedClock());
        updater.setDaemon(true);
        check(updater.getPriority() == Thread.MIN_PRIORITY &&
                display.getPriority() == Thread.MAX_PRIORITY, "Required priorities");
        updater.start();
        updater.stopUpdating();
        updater.join(2000);
        check(!updater.isAlive(), "Updater responds to interruption");
        System.out.println("All checks passed.");
    }

    private static Clock fixedClock() {
        return new Clock(java.time.Clock.fixed(
                Instant.parse("2026-11-25T14:35:08Z"), ZoneOffset.UTC));
    }

    private static void awaitWaiting(Thread thread) throws InterruptedException {
        // Limit how long the test waits so a broken worker cannot stall this check.
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (thread.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        if (thread.getState() != Thread.State.WAITING) {
            throw new AssertionError("Thread did not enter WAITING state");
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        System.out.println("PASS: " + description);
    }
}
