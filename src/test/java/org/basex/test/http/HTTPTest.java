package org.basex.test.http;

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
 * This class tests the RESTful Annotations for XQuery implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class HTTPTest {
  /** Class name. */
  protected static final String DB = Util.name(HTTPTest.class);
  /** Start servers. */
  private static BaseXHTTP http;

  // INITIALIZATION =====================================================================

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

  // PRIVATE METHODS ====================================================================

  /**
   * Executes the specified GET request and returns the result.
   * @param root root node
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String get(final String root, final String query) throws IOException {
    final URL url = new URL(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      return read(new BufferInput(conn.getInputStream())).replaceAll("\r?\n *", "");
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

  /**
   * Returns the temporary database path.
   * @return database path
   */
  private static IOFile sandbox() {
    return new IOFile(Prop.TMP, DB);
  }
}
