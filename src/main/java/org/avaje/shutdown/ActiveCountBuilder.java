package org.avaje.shutdown;

/**
 * A fluent builder for ActiveCount.
 *
 * <pre>{@code
 *
 * ActiveCount count = new ActiveCountBuilder()
 *    .initialDelay(500)
 *    .pauseMillis(500)
 *    .maxPause(10).build();
 *
 * }</pre>
 */
public class ActiveCountBuilder {

  /**
   * The initial sleep (to allow DNS/Router changes to propagate).
   */
  private long initialSleepMillis = 1000;

  /**
   * Pause time between each check for Idle.
   */
  private long busyPauseMillis = 500;

  /**
   * Maximum number of times we check for Idle before deeming that we need to shutdown anyway.
   */
  private int maxPauseCount = 40;

  /**
   * Set the initial delay before we start checking for Idle.
   *
   * @param initialSleepMillis The time in millis we wait before starting the check for Idle
   * @return The ActiveCount builder
   */
  public ActiveCountBuilder initialDelay(long initialSleepMillis) {
    this.initialSleepMillis = initialSleepMillis;
    return this;
  }


  /**
   * The duration we pause between each check for Idle.
   *
   * @param busyPauseMillis The time to pause between each check for Idle
   * @return The ActiveCount builder
   */
  public ActiveCountBuilder pauseMillis(long busyPauseMillis) {
    this.busyPauseMillis = busyPauseMillis;
    return this;
  }

  /**
   * Set the maximum number of times to pause and check for Idle.
   * <p>
   * After we exceed the maximum pause count we returning allowing a shutdown to occur anyway.
   * </p>
   *
   * @param maxPauseCount The maximum number of times we check for Idle before deeming we should shutdown anyway.
   * @return The ActiveCount builder
   */
  public ActiveCountBuilder maxPause(int maxPauseCount) {
    this.maxPauseCount = maxPauseCount;
    return this;
  }

  /**
   * Build and return the ActiveCount.
   */
  public ActiveCount build() {
    return new ActiveCount(initialSleepMillis, busyPauseMillis, maxPauseCount);
  }
}
