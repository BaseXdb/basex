package org.basex.http;

import static org.basex.core.users.UserText.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.Base64;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * This class contains common methods for the HTTP services.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class HTTPTest extends SandboxTest {
  /** HTTP server. */
  private static BaseXHTTP http;
  /** Root path. */
  private static String rootUrl;

  // INITIALIZATION ===============================================================================

  /**
   * Initializes the test.
   * @param rt root path
   * @param local local flag
   * @throws Exception exception
   */
  protected static void init(final String rt, final boolean local) throws Exception {
    init(rt, local, false);
  }

  /**
   * Initializes the test.
   * @param url root path
   * @param local local flag
   * @param auth enforce authentication
   * @throws Exception exception
   */
  protected static void init(final String url, final boolean local, final boolean auth)
      throws Exception {

    final StringList sl = new StringList();
    sl.add("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT, "-z", "-q");
    if(local) sl.add("-l");
    if(!auth) sl.add("-U" + ADMIN);
    http = new BaseXHTTP(sl.toArray());
    rootUrl = url;

    final StaticOptions sopts = HTTPContext.get().context().soptions;
    assertTrue(new IOFile(sopts.get(StaticOptions.WEBPATH)).md());
  }

  /**
   * Finishes the test.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    http.stop();

    // cleanup: remove project specific system properties
    final StringList keys = new StringList();
    final Properties props = System.getProperties();
    for(final Object key : props.keySet()) {
      final String path = key.toString();
      if(path.startsWith(Prop.DBPREFIX)) keys.add(path);
    }
    for(final String key : keys) props.remove(key);
  }

  // PROTECTED METHODS ============================================================================

  /**
   * Executes the specified GET request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure
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
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String head(final String query) throws IOException {
    return request(query, HEAD);
  }

  /**
   * Executes the specified OPTIONS request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String options(final String query) throws IOException {
    return request(query, OPTIONS);
  }

  /**
   * Executes the specified HTTP request and returns the result.
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  private static String request(final String query, final HttpMethod method)throws IOException {
    return request(query, method.name());
  }

  /**
   * Executes the specified HTTP request and returns the result.
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String request(final String query, final String method) throws IOException {
    return request(rootUrl, query, method);
  }

  /**
   * Executes the specified HTTP request and returns the result.
   * @param root root URL
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String request(final String root, final String query, final String method)
      throws IOException {

    final IOUrl url = new IOUrl(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.connection();
    try {
      conn.setRequestMethod(method);
      return read(conn.getInputStream());
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
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String post(final String query, final String request, final MediaType type)
      throws IOException {

    // create connection
    final IOUrl url = new IOUrl(rootUrl + query);
    final HttpURLConnection conn = (HttpURLConnection) url.connection();
    conn.setDoOutput(true);
    conn.setRequestMethod(POST.name());
    conn.setRequestProperty(HttpText.CONTENT_TYPE, type.toString());
    // basic authentication
    final String encoded = Base64.encode(ADMIN + ':' + ADMIN);
    conn.setRequestProperty(HttpText.AUTHORIZATION, AuthMethod.BASIC + " " + encoded);
    // send query
    try(OutputStream out = conn.getOutputStream()) {
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
    return is == null ? "" : string(BufferInput.get(is).content());
  }

  /**
   * Executes the specified PUT request.
   * @param url url
   * @param is input stream
   * @throws IOException I/O exception
   */
  protected static void put(final String url, final InputStream is) throws IOException {
    put(url, is, null);
  }

  /**
   * Executes the specified PUT request.
   * @param url url
   * @param is input stream
   * @param type media type (optional, may be {@code null})
   * @throws IOException I/O exception
   */
  protected static void put(final String url, final InputStream is, final MediaType type)
      throws IOException {

    final IOUrl io = new IOUrl(rootUrl + url);
    final HttpURLConnection conn = (HttpURLConnection) io.connection();
    conn.setDoOutput(true);
    conn.setRequestMethod(PUT.name());
    if(type != null) conn.setRequestProperty(HttpText.CONTENT_TYPE, type.toString());
    try(OutputStream bos = new BufferedOutputStream(conn.getOutputStream())) {
      if(is != null) {
        // send input stream if it not empty
        try(BufferedInputStream bis = new BufferedInputStream(is)) {
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
