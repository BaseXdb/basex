package org.basex.examples;

import org.basex.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Runs the XQJ examples, which connect to a running server.
 *
 * @author BaseX Team, BSD License
 */
public final class XQJExamplesTest {
  /** Server instance. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    server = new BaseXServer("-z", "-c", "PASSWORD admin");
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  @AfterAll public static void stop() throws Exception {
    server.stop();
  }

  /**
   * Runs an XQJ example.
   * @param example name of example class
   * @throws Exception exception
   */
  @ParameterizedTest
  @ValueSource(strings = {
    "XQJQuery", "cfoster.Part1", "cfoster.Part2", "cfoster.Part3", "cfoster.Part4", "cfoster.Part5",
    "cfoster.Part6", "tutorial.Part1", "tutorial.Part2", "tutorial.Part3", "tutorial.Part4",
    "tutorial.Part5", "tutorial.Part6", "tutorial.Part7", "tutorial.Part8", "tutorial.Part9",
    "tutorial.Part10", "tutorial.Part11"
  })
  public void xqj(final String example) throws Exception {
    ExamplesTest.runMain(Class.forName("org.basex.examples.xqj." + example));
  }
}
