package org.avaje.shutdown;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class ActiveCountTest {

  ActiveCount activeCount = new ActiveCount(300, 10, 10);

  @Test
  public void testActive() throws Exception {

    assertThat(activeCount.active()).isEqualTo(0);

    activeCount.increment();
    assertThat(activeCount.active()).isEqualTo(1);

    activeCount.decrement();
    assertThat(activeCount.active()).isEqualTo(0);
  }

  @Test
  public void testAsRunnable() throws Exception {

    ActiveCount active = new ActiveCountBuilder()
        .initialDelay(300)
        .maxPause(10)
        .pauseMillis(10).build();

    Runnable runnable = active.asRunnable();
    runnable.run();
  }

  @Test
  public void testShutdownWaitUntilIdle() throws Exception {

    ActiveCount active = new ActiveCount(300, 10, 10);
    active.shutdownWaitUntilIdle();
  }

  @Test
  public void testUntilIdle() throws Exception {

    ActiveCount active = new ActiveCount(300, 10, 10);
    active.untilIdle();
  }

  @Test
  public void testUntilIdle_notIdle() throws Exception {

    ActiveCount active = new ActiveCount(300, 10, 10);
    active.increment();
    active.untilIdle();
  }

}