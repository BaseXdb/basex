package org.basex.http;

import static org.basex.core.users.UserText.*;
import static org.basex.util.http.Method.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * This class contains common methods for the HTTP services.
 *
 * @author BaseX Team 2005-22, BSD License
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
   * @param url root path
   * @param local local flag
   * @throws Exception exception
   */
  protected static void init(final String url, final boolean local) throws Exception {
    init(url, local, false);
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
    return get(query, 200);
  }

  /**
   * Executes the specified GET request and returns the result.
   * @param query request
   * @param status status code to check
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String get(final String query, final int status) throws IOException {
    return send(query, GET.name(), null, null, status);
  }

  /**
   * Executes the specified DELETE request.
   * @param query request
   * @param status status code to check
   * @return response code
   * @throws IOException I/O exception
   */
  protected static String delete(final String query, final int status) throws IOException {
    return send(query, DELETE.name(), null, null, status);
  }

  /**
   * Executes the specified HEAD request and returns the result.
   * @param query request
   * @param status status code to check
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String head(final String query, final int status) throws IOException {
    return send(query, HEAD.name(), null, null, status);
  }

  /**
   * Executes the specified OPTIONS request and returns the result.
   * @param query request
   * @return string result, or {@code null} for a failure
   * @throws IOException I/O exception
   */
  protected static String options(final String query) throws IOException {
    return send(query, OPTIONS.name(), null, null, 200);
  }

  /**
   * Executes the specified POST request.
   * @param query path
   * @param payload payload
   * @param type media type
   * @return string result
   * @throws IOException I/O exception
   */
  protected static String post(final String query, final String payload, final MediaType type)
      throws IOException {
    return post(query, payload, type, 200);
  }

  /**
   * Executes the specified POST request.
   * @param query path
   * @param payload payload
   * @param type media type
   * @param status status code to check
   * @return string result
   * @throws IOException I/O exception
   */
  protected static String post(final String query, final String payload, final MediaType type,
      final int status) throws IOException {
    return send(query, Method.POST.name(), new ArrayInput(payload), type, status);
  }

  /**
   * Executes the specified PUT request.
   * @param query query
   * @param is input stream (may be {@code null})
   * @throws IOException I/O exception
   */
  protected static void put(final String query, final InputStream is) throws IOException {
    put(query, is, 201);
  }

  /**
   * Executes the specified PUT request.
   * @param query query
   * @param is input stream (may be {@code null})
   * @param status status code to check
   * @throws IOException I/O exception
   */
  protected static void put(final String query, final InputStream is, final int status)
      throws IOException {
    send(query, Method.PUT.name(), is, null, status);
  }

  /**
   * Executes the specified PUT request.
   * @param query query
   * @param method HTTP method
   * @param is input stream (may be {@code null})
   * @param type media type (optional, may be {@code null})
   * @param status status code to check
   * @return string result
   * @throws IOException I/O exception
   */
  protected static String send(final String query, final String method, final InputStream is,
      final MediaType type, final int status) throws IOException {

    final BodyPublisher pub = is != null ? HttpRequest.BodyPublishers.ofInputStream(() -> is) :
      HttpRequest.BodyPublishers.noBody();
    final URI uri = URI.create(rootUrl + query.replace("<", "%3C").replace(">", "%3E"));
    final HttpRequest.Builder builder = HttpRequest.newBuilder(uri).method(method, pub);
    if(type != null) builder.setHeader(HTTPText.CONTENT_TYPE, type.toString());

    try {
      final HttpClient client = IOUrl.clientBuilder(true).build();
      final HttpResponse<String> response = client.send(builder.build(),
          HttpResponse.BodyHandlers.ofString());
      final String body = response.body();
      assertEquals(status, response.statusCode(), body);
      return body;
    } catch(final InterruptedException ex) {
      throw new IOException(ex);
    }
  }
}
