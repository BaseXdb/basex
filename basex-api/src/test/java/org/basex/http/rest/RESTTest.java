package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.http.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class RESTTest extends HTTPTest {
  /** REST URI. */
  static final String URI = Token.string(RESTText.REST_URI);
  /** Input file. */
  static final String FILE = "src/test/resources/input.xml";

  // INITIALIZERS =================================================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_ROOT, true);
  }

  /**
   * Checks if a string starts with another.
   * @param string full string
   * @param prefix prefix
   */
  protected static void assertStartsWith(final String string, final String prefix) {
    assertTrue(string.startsWith(prefix),
      '\'' + string + "' does not start with '" + prefix + '\'');
  }

  /**
   * Checks if a string is contained in another string.
   * @param str string
   * @param sub sub string
   */
  protected static void assertContains(final String str, final String sub) {
    if(!str.contains(sub)) fail('\'' + sub + "' not contained in '" + str + "'.");
  }
}
