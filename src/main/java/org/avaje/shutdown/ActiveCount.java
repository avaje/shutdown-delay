package org.avaje.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Maintain a counter (using AtomicLong) of active requests.
 * <p>
 * Used to delay shutdown to allow in flight requests to complete.
 * </p>
 * <p>
 * An initial delay is useful in cases where we want to allow DNS/Router changes to propagate.
 * After the initial delay we enter a loop to check for the active request count to hit zero
 * with a maximum number of delays before we deem that a shutdown should be allowed anyway.
 * </p>
 */
public class ActiveCount {

  private static final Logger logger = LoggerFactory.getLogger(ActiveCount.class);

  /**
   * Underlying count of active requests.
   */
  private final AtomicLong count = new AtomicLong();

  /**
   * The initial sleep (to allow DNS/Router changes to propagate).
   */
  private final long initialSleepMillis;

  /**
   * Pause time between each check for Idle.
   */
  private final long busyPauseMillis;

  /**
   * Maximum number of times we check for Idle before deeming that we need to shutdown anyway.
   */
  private final int maxPauseCount;

  /**
   * Create with options controlling how to wait for active requests to complete.
   *
   * @param initialSleepMillis This is an initial wait time in millis after which it actively checks the active count.
   * @param busyPauseMillis The pause in millis while waiting for the active count to get to 0.
   * @param maxPauseCount The maximum number of pauses allowed before we deem that we should shutdown anyway.
   */
  public ActiveCount(long initialSleepMillis, long busyPauseMillis, int maxPauseCount) {
    this.initialSleepMillis = initialSleepMillis;
    this.busyPauseMillis = busyPauseMillis;
    this.maxPauseCount = maxPauseCount;
  }

  /**
   * Return the current active request count.
   */
  public long active() {
    return count.get();
  }

  /**
   * Increment the active request count.
   */
  public void increment() {
    count.incrementAndGet();
  }

  /**
   * Decrement the active request count.
   */
  public void decrement() {
    count.decrementAndGet();
  }

  /**
   * Return a runnable.
   */
  public Runnable asRunnable() {
    return new Runnable() {
      @Override
      public void run() {
        shutdownWaitUntilIdle();
      }
    };
  }

  /**
   * Wait until idle with a maximum wait of (busyPauseMillis * maxPauseCount).
   */
  public void shutdownWaitUntilIdle() {

    try {
      // initial sleep to allow IpTables updates
      Thread.sleep(initialSleepMillis);
      untilIdle();
    } catch (Exception e) {
      logger.warn("Error while waiting to go Idle", e);
    }
  }

  void untilIdle() {
    int i = 0;
    do {
      long activeCount = count.get();
      if (activeCount < 1) {
        // server is idle so allowing shutdown to occur
        return;
      }
      i++;
      try {
        logger.info("WAIT for {} active requests", activeCount);
        // wait a bit for active requests to complete
        Thread.sleep(busyPauseMillis);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

    } while (i < maxPauseCount); // max wait 20 seconds - 500*40
    logger.warn("Maximum wait, not idle but shutting down anyway");
  }
}
