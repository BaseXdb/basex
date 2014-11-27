package org.basex.http.restxq;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class contains RESTXQ tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class RestXqTest extends HTTPTest {
  /** Root path. */
  private static final String ROOT = "http://" + S_LOCALHOST + ":9998/";
  /** Query header. */
  private static final String HEADER =
    "module  namespace m = 'http://basex.org/modules/restxq/test';" + Prop.NL +
    "declare namespace R = 'http://exquery.org/ns/restxq';" + Prop.NL;
  /** Counter. */
  private static int count;

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(ROOT, true);
  }

  /**
   * Executes the specified GET request and tests the result.
   * @param function function to test
   * @param query request
   * @param exp expected result
   * @throws IOException I/O exception
   */
  protected static void get(final String function, final String query, final String exp)
      throws IOException {
    install(function);
    assertEquals(exp, get(query));
  }

  /**
   * Executes the specified GET request and tests for an error.
   * @param function function to test
   * @param query request
   * @throws IOException I/O exception
   */
  protected static void getE(final String function, final String query) throws IOException {
    install(function);
    try {
      get(query);
      fail("Error expected: " + query);
    } catch(final BaseXException ignored) {
    }
  }

  /**
   * Installs a new module and removes all others.
   * @param function function to be tested
   * @throws IOException I/O exception
   */
  protected static void install(final String function) throws IOException {
    // delete old module
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    for(final IOFile f : new IOFile(path).children()) assertTrue(f.delete());
    // create new module
    module().write(new TokenBuilder(HEADER).add(function).finish());
  }

  /**
   * Returns the XQuery test module.
   * @return test module
   */
  private static IOFile module() {
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    return new IOFile(path, NAME + count++ + IO.XQMSUFFIX);
  }
}
