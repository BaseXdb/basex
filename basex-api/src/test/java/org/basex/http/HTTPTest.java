package org.basex.http;

import static org.basex.http.HTTPMethod.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * This class contains common methods for the HTTP services.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class HTTPTest extends SandboxTest {
  /** Database context. */
  private static final Context CONTEXT = HTTPContext.init();
  /** Start servers. */
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
    initContext(CONTEXT);
    assertTrue(new IOFile(CONTEXT.globalopts.get(GlobalOptions.WEBPATH)).md());
    root = rt;

    final StringList sl = new StringList();
    if(local) sl.add("-l");
    sl.add("-p9996", "-e9997", "-h9998", "-s9999", "-z", "-U" + Text.S_ADMIN, "-P" + Text.S_ADMIN);
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
   * Executes the specified GET request and returns the result.
   * @param query request
   * @param method HTTP method
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  private static String request(final String query, final HTTPMethod method)
      throws IOException {

    final URL url = new URL(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    try {
      conn.setRequestMethod(method.name());
      return read(new BufferInput(conn.getInputStream())).replaceAll("\r?\n *", "");
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
   * @param type content type
   * @return string result, or {@code null} for a failure.
   * @throws IOException I/O exception
   */
  protected static String post(final String query, final String request, final String type)
      throws IOException {

    // create connection
    final URL url = new URL(root + query);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod(POST.name());
    conn.setRequestProperty(MimeTypes.CONTENT_TYPE, type);
    // basic authentication
    final String encoded = Base64.encode(Text.S_ADMIN + ':' + Text.S_ADMIN);
    conn.setRequestProperty(HTTPText.AUTHORIZATION, HTTPText.BASIC + ' ' + encoded);
    // send query
    try(final OutputStream out = conn.getOutputStream()) {
      out.write(token(request));
    }

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
    final ArrayOutput ao = new ArrayOutput();
    if(is != null) {
      try(final BufferInput bi = new BufferInput(is)) {
        for(int i; (i = bi.read()) != -1;) ao.write(i);
      }
    }
    return ao.toString();
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
   * @param ctype content type (optional, may be {@code null})
   * @throws IOException I/O exception
   */
  protected static void put(final String u, final InputStream is, final String ctype)
      throws IOException {

    final URL url = new URL(root + u);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod(PUT.name());
    if(ctype != null) conn.setRequestProperty(MimeTypes.CONTENT_TYPE, ctype);
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
