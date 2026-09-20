import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Clock {

    // Format "HH:mm:ss dd-MM-yyyy"
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
 private volatile String currentTime;

    public Clock() {
        updateTime();
    }

    
     // Updates the internal time value
    public void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        this.currentTime = now.format(FORMATTER);
    }

     // Prints the current time to the console. 
    public void printTime() {
        System.out.println("Current time: " + currentTime);
    }
}

 // Background thread responsible for updating the clock's internal value.
class ClockUpdater extends Thread {

    private final Clock clock;
    private volatile boolean running = true;

    public ClockUpdater(Clock clock) {
        this.clock = clock;
        // Lower priority than the display thread on purpose
        this.setPriority(Thread.MIN_PRIORITY);
        this.setName("ClockUpdater-Thread");
    }

    public void run() {
        while (running) {
            try {
                clock.updateTime();
                Thread.sleep(1000); // update once per second
            } catch (InterruptedException e) {
                // Error handling: if the thread is interrupted while sleeping, we stop the loop instead of letting the exception disappear silently.
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    public void stopUpdating() {
        running = false;
        this.interrupt();
    }
}

// Thread responsible for printing the clock to the console. 
class ClockDisplay extends Thread {

    private final Clock clock;
    private volatile boolean running = true;

    public ClockDisplay(Clock clock) {
        this.clock = clock;
        this.setPriority(Thread.MAX_PRIORITY);
        this.setName("ClockDisplay-Thread");
    }

    public void run() {
        while (running) {
            try {
                clock.printTime();
                Thread.sleep(1000); // print once per second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    public void stopDisplaying() {
        running = false;
        this.interrupt();
    }
}

public class SimpleClockApp {

    public static void main(String[] args) {

        Clock clock = new Clock();

        ClockUpdater updaterThread = new ClockUpdater(clock);
        ClockDisplay displayThread = new ClockDisplay(clock);

        System.out.println("Starting Simple Clock Application...");
        System.out.println("Display thread priority: " + displayThread.getPriority());
        System.out.println("Updater thread priority: " + updaterThread.getPriority());
        System.out.println();

        updaterThread.start();
        displayThread.start();

        // Let the simulation run for 10 seconds, and stop both threads.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        updaterThread.stopUpdating();
        displayThread.stopDisplaying();

        System.out.println();
        System.out.println("Simple Clock Application stopped.");
    }
}