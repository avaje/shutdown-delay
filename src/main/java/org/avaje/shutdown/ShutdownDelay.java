package org.avaje.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides a mechanism to delay JVM shutdown.
 */
public class ShutdownDelay {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownDelay.class);

  private static ShutdownDelay instance = new ShutdownDelay();

  private Set<Thread> registeredHooks;

  private Runnable delayCallback;

  /**
   * Register a shutdown delay callback.
   * <p>
   * This callback will be executed prior to running all the usual/registered shutdown hooks.
   * </p>
   * <p>
   * This registration should not occur until after the usual shutdown hooks have all been
   * registered to the runtime.
   * </p>
   *
   * @param registerInMillis A delay in millis to allow the usual shutdown hooks to all be registered
   * @param delayCallback The callback to execute prior to running normal shutdown hooks
   */
  public static void register(final int registerInMillis, final Runnable delayCallback) {

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        instance.swap(delayCallback);
      }
    }, registerInMillis);
  }

  /**
   * Get all the registered shutdown hooks and replace them with a 'master' shutdown hook.
   */
  private void swap(Runnable delayCallback) {

    synchronized (ShutdownDelay.class) {

      this.delayCallback = delayCallback;
      try {

        registeredHooks = new HashSet<>(existingHooks().keySet());
        logger.debug("de-register {} existing shutdown hooks ", registeredHooks.size());

        for (Thread hook : registeredHooks) {
          Runtime.getRuntime().removeShutdownHook(hook);
        }

        logger.info("Register shutdown hook delay.  registered hooks [{}] ", registeredHooks.size());
        Runtime.getRuntime().addShutdownHook(new MasterHook());

      } catch (Exception e) {
        logger.error("Error getting shutdown hooks", e);
      }
    }
  }

  /**
   * Return the registered shutdown hooks using reflection.
   */
  private Map<Thread, Thread> existingHooks() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

    Class clazz = Class.forName("java.lang.ApplicationShutdownHooks");
    Field field = clazz.getDeclaredField("hooks");
    field.setAccessible(true);
    Map<Thread, Thread> hooks = (Map<Thread, Thread>) field.get(null);
    return hooks;
  }

  /**
   * Run all the shutdown hooks.
   * <p>
   * This is based on java.lang.ApplicationShutdownHooks.runHooks()
   * </p>
   */
  private void shutdownAll() {
    synchronized (ShutdownDelay.class) {
      if (registeredHooks != null) {
        logger.info("executing normal shutdown hooks");
        for (Thread hook : registeredHooks) {
          hook.start();
        }
        for (Thread hook : registeredHooks) {
          try {
            hook.join();
          } catch (InterruptedException x) {
            // ignore like ApplicationShutdownHooks.runHooks()
          }
        }
        registeredHooks = null;
      }
    }
  }

  /**
   * Our 'Master' shutdown hook.
   * <p>
   * It should be the ONLY shutdown hook registered with the Runtime.
   * </p>
   */
  private class MasterHook extends Thread {
    @Override
    public void run() {
      logger.info("shutdown requested");
      try {
        if (delayCallback != null) {
          delayCallback.run();
        }
      } catch (Exception e) {
        logger.warn("Error executing shutdown delayCallback ", e);
      } finally {
        shutdownAll();
      }
    }

  }
}
