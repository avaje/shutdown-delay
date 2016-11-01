package org.avaje.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ShutdownDelayTest {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownDelayTest.class);

  @Test
  public void testWithActiveCount_hitMaxWait() throws Exception {

    ActiveCount activeCount = new ActiveCount(100, 10, 5);

    // delay to allow DI container to start
    // and for all runtime hooks to be registered
    ShutdownDelay.register(100, activeCount.asRunnable());

    // simulate container starting with a shutdown hook
    Runtime.getRuntime().addShutdownHook(new PoolShutdown());
    Thread.sleep(200);

    // make an active request that doesn't complete
    // so we end up hitting max wait
    activeCount.increment();
    logger.info("test completed" );
  }

  @Test
  public void testWithActiveCount() throws Exception {


    ActiveCount activeCount = new ActiveCount(100, 10, 10);

    // delay by 1 second to allow DI container to start
    // and for all runtime hooks to be registered
    ShutdownDelay.register(1000, activeCount.asRunnable());

    // simulate container starting with a shutdown hook
    Runtime.getRuntime().addShutdownHook(new PoolShutdown());
    Thread.sleep(1200);

    activeCount.increment();
    activeCount.decrement();

    logger.info("test completed" );
  }

  @Test
  public void testWithCustomDelay() throws Exception {

    // delay to allow DI container to start
    // and for all runtime hooks to be registered
    ShutdownDelay.register(100, new Delay());

    // simulate container starting with a shutdown hook
    Runtime.getRuntime().addShutdownHook(new PoolShutdown());
    Thread.sleep(200);

    logger.info("test completed" );
  }

  class PoolShutdown extends Thread {
    @Override
    public void run() {
      try {
        logger.info("Doing pool shutdown");
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  class Delay implements Runnable {

    @Override
    public void run() {
      try {
        // normally we try and delay to allow in-flight requests to process etc
        logger.info("our shutdown delay allowing in-flight requests etc");
        Thread.sleep(4000);
        logger.info("our shutdown delay has completed, normal shutdown can occur");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}