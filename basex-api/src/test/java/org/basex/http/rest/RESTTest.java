package org.basex.http.rest;

import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.basex.http.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.junit.*;

/**
 * This class tests the embedded REST API.
 *
 * @author BaseX Team 2005-19, BSD License
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
  @BeforeClass public static void start() throws Exception {
    init(REST_ROOT, true);
  }

  /**
   * Checks if a string starts with another.
   * @param string full string
   * @param prefix prefix
   */
  protected static void assertStartsWith(final String string, final String prefix) {
    assertTrue('\'' + string + "' does not start with '" + prefix + '\'',
        string.startsWith(prefix));
  }

  /**
   * Compares media types.
   * @param ret returned media type
   * @param exp expected media type
   */
  protected static void assertMediaType(final MediaType ret, final MediaType exp) {
    if(!ret.is(exp)) fail("Wrong media type: " + ret + " returned, " + exp + " expected.");
  }

  /**
   * Checks if a string is contained in another string.
   * @param str string
   * @param sub sub string
   */
  protected static void assertContains(final String str, final String sub) {
    if(!str.contains(sub)) fail('\'' + sub + "' not contained in '" + str + "'.");
  }

  /**
   * Executes the specified GET request and returns the media type.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static MediaType mediaType(final String query) throws IOException {
    final IOUrl url = new IOUrl(REST_ROOT + query);
    final HttpURLConnection conn = (HttpURLConnection) url.connection();
    try {
      read(conn.getInputStream());
      return new MediaType(conn.getContentType());
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }
}
