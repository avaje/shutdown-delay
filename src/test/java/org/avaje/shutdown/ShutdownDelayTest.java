package org.avaje.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ShutdownDelayTest {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownDelayTest.class);

  @Test
  public void testRegister() throws Exception {


    // delay by 1 second to allow DI container to start etc
    ShutdownDelay.register(1000, new Delay());

    // simulate container starting with a shutdown hook
    Thread.sleep(500);
    Runtime.getRuntime().addShutdownHook(new PoolShutdown());
    Thread.sleep(800);

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