package org.basex.test.restxq;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * This class tests the embedded REST implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RestXqTest {
  /** Class name. */
  private static final String DB = Util.name(RestXqTest.class);
  /** Test module. */
  private static final String MODULE = "src/test/resources/" + DB + IO.XQMSUFFIX;
  /** Root path. */
  private static final String ROOT = "http://" + LOCALHOST + ":9998/restxq/";
  /** Start servers. */
  private static BaseXHTTP http;

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(true);
  }

  /**
   * Initializes the test.
   * @param local local flag
   * @throws Exception exception
   */
  protected static void init(final boolean local) throws Exception {
    final Context ctx = HTTPSession.context();
    final IOFile tmp = sandbox();
    assertTrue(tmp.md());
    ctx.mprop.set(MainProp.HTTPPATH, tmp.path());

    final StringList sl = new StringList();
    if(local) sl.add("-l");
    sl.add(new String[] { "-p9996", "-e9997", "-h9998", "-s9999", "-z",
        "-U" + ADMIN, "-P" + ADMIN });
    http = new BaseXHTTP(sl.toArray());

    final TokenBuilder tb = new TokenBuilder();
    tb.add(new IOFile(MODULE).read());
    module().write(tb.finish());
  }

  /**
   * Returns the XQuery test module.
   * @return test module
   */
  private static IOFile module() {
    final Context ctx = HTTPSession.context();
    final String path = ctx.mprop.get(MainProp.HTTPPATH);
    return new IOFile(path, DB + IO.XQMSUFFIX);
  }

  /**
   * Finish test.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    http.stop();
    assertTrue(sandbox().delete());
  }

  /**
   * Returns the temporary database path.
   * @return database path
   */
  static IOFile sandbox() {
    return new IOFile(Prop.TMP, DB);
  }

  // TEST METHODS =============================================================

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get() throws Exception {
    assertEquals("root", get(""));
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get2() throws Exception {
    assertEquals("one", get("one"));
  }

  /**
   * GET Test.
   * @throws Exception exception
   */
  @Test
  public void get3() throws Exception {
    assertEquals("x", get("one/x"));
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Executes the specified GET request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private static String get(final String query) throws IOException {
    final URL url = new URL(ROOT + query);

    // create connection
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      return read(conn.getInputStream()).replaceAll("\r?\n *", "");
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Returns an exception with improved error message.
   * @param conn connection reference
   * @param ex exception
   * @return exception
   * @throws IOException I/O exception
   */
  private static IOException error(final HttpURLConnection conn,
      final IOException ex) throws IOException {
    final String msg = read(conn.getErrorStream());
    throw new BaseXException(msg.isEmpty() ? ex.getMessage() : msg);

  }

  /**
   * Returns a string result from the specified input stream.
   * @param is input stream
   * @return string
   * @throws IOException I/O exception
   */
  private static String read(final InputStream is) throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    final BufferInput bi = new BufferInput(is);
    for(int i; (i = bi.read()) != -1;) ao.write(i);
    bi.close();
    return ao.toString();
  }
}
