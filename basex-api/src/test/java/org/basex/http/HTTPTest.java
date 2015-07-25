package org.basex.http;

import static org.basex.core.users.UserText.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpMethod.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.StaticOptions.AuthMethod;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * This class contains common methods for the HTTP services.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class HTTPTest extends SandboxTest {
  /** HTTP stop port. */
  protected static final int STOP_PORT = 9999;
  /** HTTP port. */
  protected static final int HTTP_PORT = 9998;
  /** REST identifier. */
  protected static final String REST = "rest";
  /** Root path. */
  protected static final String RESTXQ_ROOT = "http://" + Text.S_LOCALHOST + ':' + HTTP_PORT + '/';
  /** Root path. */
  protected static final String REST_ROOT = RESTXQ_ROOT + REST + '/';

  /** Database context. */
  private static final Context CONTEXT = HTTPContext.init();
  /** HTTP server. */
  private static BaseXHTTP http;
  /** Root path. */
  private static String root;

  // INITIALIZATION =====================================================================

  /**
   * Initializes the test.
   * @param rt root path
   * @param local local flag
   * @throws Exception exception
   */
  protected static void init(final String rt, final boolean local) throws Exception {
    assertTrue(new IOFile(CONTEXT.soptions.get(StaticOptions.WEBPATH)).md());
    root = rt;

    final StringList sl = new StringList();
    if(local) sl.add("-l");
    sl.add("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT, "-z");
    sl.add("-U" + ADMIN, "-P" + ADMIN);
    System.setOut(NULL);
    try {
      http = new BaseXHTTP(sl.toArray());
    } finally {
      System.setOut(OUT);
    }
  }

  /**
   * Finish test.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    System.setOut(NULL);
    try {
      http.stop();
    } finally {
      System.setOut(OUT);
    }

    // cleanup: remove project specific system properties
    final StringList sl = new StringList();
    final Properties pr = System.getProperties();
    for(final Object s : pr.keySet()) {
      final String st = s.toString();
      if(st.startsWith(Prop.DBPREFIX)) sl.add(st);
    }
    for(final String s : sl) pr.remove(s);
  }

  // PROTECTED METHODS ==================================================================

  /**
   * Executes the specified GET request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String get(final String query) throws IOException {
    return request(query, GET);
  }

  /**
   * Executes the specified DELETE request.
   * @param query request
   * @return response code
   * @throws IOException I/O exception
   */
  protected static String delete(final String query) throws IOException {
    return request(query, DELETE);
  }

  /**
   * Executes the specified HEAD request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String head(final String query) throws IOException {
    return request(query, HEAD);
  }

  /**
   * Executes the specified HTTP request and returns the result.
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private static String request(final String query, final HttpMethod method)
      throws IOException {
    return request(query, method.name());
  }

  /**
   * Executes the specified HTTP request and returns the result.
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String request(final String query, final String method) throws IOException {
    final IOUrl url = new IOUrl(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.connection();
    try {
      conn.setRequestMethod(method);
      return read(new BufferInput(conn.getInputStream()));
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }

  /**
   * Executes the specified PUT request.
   * @param query path
   * @param request request
   * @param type media type
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String post(final String query, final String request, final MediaType type)
      throws IOException {

    // create connection
    final IOUrl url = new IOUrl(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.connection();
    conn.setDoOutput(true);
    conn.setRequestMethod(POST.name());
    conn.setRequestProperty(HttpText.CONTENT_TYPE, type.toString());
    // basic authentication
    final String encoded = org.basex.util.Base64.encode(ADMIN + ':' + ADMIN);
    conn.setRequestProperty(HttpText.AUTHORIZATION, AuthMethod.BASIC + " " + encoded);
    // send query
    try(final OutputStream out = conn.getOutputStream()) {
      out.write(token(request));
    }

    try {
      return read(conn.getInputStream());
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
  protected static IOException error(final HttpURLConnection conn, final IOException ex)
      throws IOException {
    final String msg = read(conn.getErrorStream());
    throw new BaseXException(msg.isEmpty() ? ex.getMessage() : msg);
  }

  /**
   * Returns a string result from the specified input stream.
   * @param is input stream
   * @return string
   * @throws IOException I/O exception
   */
  protected static String read(final InputStream is) throws IOException {
    return is == null ? "" : normNL(string(new BufferInput(is).content()));
  }

  /**
   * Executes the specified PUT request.
   * @param u url
   * @param is input stream
   * @throws IOException I/O exception
   */
  protected static void put(final String u, final InputStream is) throws IOException {
    put(u, is, null);
  }

  /**
   * Executes the specified PUT request.
   * @param u url
   * @param is input stream
   * @param type media type (optional, may be {@code null})
   * @throws IOException I/O exception
   */
  protected static void put(final String u, final InputStream is, final MediaType type)
      throws IOException {

    final IOUrl url = new IOUrl(root + u);
    final HttpURLConnection conn = (HttpURLConnection) url.connection();
    conn.setDoOutput(true);
    conn.setRequestMethod(PUT.name());
    if(type != null) conn.setRequestProperty(HttpText.CONTENT_TYPE, type.toString());
    try(final OutputStream bos = new BufferedOutputStream(conn.getOutputStream())) {
      if(is != null) {
        // send input stream if it not empty
        try(final BufferedInputStream bis = new BufferedInputStream(is)) {
          for(int i; (i = bis.read()) != -1;) bos.write(i);
        }
      }
    }
    try {
      read(conn.getInputStream());
    } catch(final IOException ex) {
      throw error(conn, ex);
    } finally {
      conn.disconnect();
    }
  }
}
