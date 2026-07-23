package org.basex.http.webdav;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the WebDAV web application.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebDAVTest extends WebappTest {
  /** Name of the database that is created by the tests. */
  private static final String DB = "webdav-test";
  /** Lock request body. */
  private static final String LOCKINFO = "<D:lockinfo xmlns:D='DAV:'>" +
      "<D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype>" +
      "<D:owner><D:href>mailto:test@basex.org</D:href></D:owner></D:lockinfo>";
  /** XML media type. */
  private static final String XML = "text/xml; charset=utf-8";
  /** Href of a multistatus response. */
  private static final Pattern HREF = Pattern.compile("<DAV:href>([^<]*)</DAV:href>");
  /** Lock token and lock root of a multistatus response. */
  private static final Pattern LOCK = Pattern.compile("<DAV:locktoken><DAV:href>([^<]*)</DAV:href>"
      + "</DAV:locktoken><DAV:lockroot><DAV:href>([^<]*)</DAV:href>");

  /**
   * Deploys the WebDAV application and starts the server.
   * @throws Exception exception
   */
  @BeforeAll public static void startWebDAV() throws Exception {
    init("webdav");
  }

  /**
   * Releases the locks that a failing test may have left behind, and removes the database.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    final HttpResponse<String> response =
        send("PROPFIND", DB, null, null, Map.of("Depth", "infinity"));
    if(response.statusCode() == 207) {
      final Matcher m = LOCK.matcher(response.body());
      while(m.find()) {
        final String path = m.group(2).replaceAll("^/webdav/|/$", "");
        send("UNLOCK", path, null, null, Map.of("Lock-Token", '<' + m.group(1) + '>'));
      }
    }
    send("DELETE", DB, null, null);
  }

  /**
   * Tests that OPTIONS announces the compliance classes and the supported methods.
   * @throws IOException I/O exception
   */
  @Test public void options() throws IOException {
    final HttpResponse<String> response = send(200, "OPTIONS", "", null, null);
    assertEquals("1, 2", header(response, "DAV"));
    assertEquals("DAV", header(response, "MS-Author-Via"));
    final String allow = header(response, "Allow");
    for(final String method : new String[] { "PROPFIND", "MKCOL", "LOCK", "UNLOCK", "COPY" }) {
      assertTrue(allow.contains(method), allow);
    }
  }

  /**
   * Tests that requests without credentials are rejected.
   * @throws Exception exception
   */
  @Test public void unauthorized() throws Exception {
    final HttpRequest request = HttpRequest.newBuilder(URI.create(HTTP_ROOT + "webdav")).
        method("PROPFIND", HttpRequest.BodyPublishers.noBody()).build();
    final HttpResponse<String> response = HttpClient.newHttpClient().
        send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(401, response.statusCode());
    assertTrue(response.headers().firstValue("WWW-Authenticate").orElse("").startsWith("Basic"));
  }

  /**
   * Tests that MKCOL creates databases and folders, and that PUT and GET round-trip contents.
   * @throws IOException I/O exception
   */
  @Test public void createAndRead() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(405, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/folder", null, null);

    send(201, "PUT", DB + "/folder/a.xml", "<doc>text</doc>", XML);
    send(204, "PUT", DB + "/folder/a.xml", "<doc>more</doc>", XML);
    send(201, "PUT", DB + "/folder/b.txt", "plain", "text/plain");

    assertEquals("<doc>more</doc>", send("GET", DB + "/folder/a.xml", null, null).body());
    assertEquals("plain", send("GET", DB + "/folder/b.txt", null, null).body());
    send(404, "GET", DB + "/folder/missing.xml", null, null);
  }

  /**
   * Tests that PROPFIND honours the requested depth.
   * @throws IOException I/O exception
   */
  @Test public void propfind() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    send(201, "PUT", DB + "/sub/b.xml", "<doc/>", XML);

    assertEquals(1, hrefs(propfind(DB, "0")).size());
    final List<String> depth1 = hrefs(propfind(DB, "1"));
    assertTrue(depth1.contains("/webdav/" + DB + "/a.xml"), depth1.toString());
    assertTrue(depth1.contains("/webdav/" + DB + "/sub/"), depth1.toString());
    assertFalse(depth1.contains("/webdav/" + DB + "/sub/b.xml"), depth1.toString());
    assertTrue(hrefs(propfind(DB, "infinity")).contains("/webdav/" + DB + "/sub/b.xml"));

    // the root collection lists the database
    assertTrue(hrefs(propfind("", "1")).contains("/webdav/" + DB + "/"));
  }

  /**
   * Tests that properties which are not supported are reported as missing.
   * @throws IOException I/O exception
   */
  @Test public void properties() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/b.txt", "plain", "text/plain");

    final String body = send(207, "PROPFIND", DB + "/b.txt", "<D:propfind xmlns:D='DAV:'>" +
        "<D:prop><D:getcontentlength/><D:nosuchprop/></D:prop></D:propfind>", XML,
        Map.of("Depth", "0")).body();
    assertTrue(body.contains("<DAV:getcontentlength>5</DAV:getcontentlength>"), body);
    assertTrue(body.contains("HTTP/1.1 404 Not Found"), body);

    // for XML resources, the size is a node count, so the property is omitted
    send(201, "PUT", DB + "/a.xml", "<doc>text</doc>", XML);
    assertFalse(propfind(DB + "/a.xml", "0").contains("getcontentlength"));
  }

  /**
   * Tests that COPY and MOVE relocate resources, also across databases.
   * @throws IOException I/O exception
   */
  @Test public void copyAndMove() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    send(201, "COPY", DB + "/a.xml", null, null, destination(DB + "/copy.xml"));
    assertEquals("<doc/>", send("GET", DB + "/copy.xml", null, null).body());
    send(201, "MOVE", DB + "/copy.xml", null, null, destination(DB + "/moved.xml"));
    send(404, "GET", DB + "/copy.xml", null, null);
    assertEquals("<doc/>", send("GET", DB + "/moved.xml", null, null).body());
  }

  /**
   * Tests that a plus sign in a path is a literal character, not a space.
   * @throws IOException I/O exception
   */
  @Test public void plusSign() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    send(201, "MOVE", DB + "/a.xml", null, null, destination(DB + "/a%2Bb.xml"));
    assertTrue(hrefs(propfind(DB, "1")).contains("/webdav/" + DB + "/a%2Bb.xml"));
  }

  /**
   * Tests that a lock blocks writes that do not carry its token.
   * @throws IOException I/O exception
   */
  @Test public void lock() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    final String token = lock(DB + "/a.xml", "0", 200);
    assertTrue(token.startsWith("urn:uuid:"), token);

    send(423, "PUT", DB + "/a.xml", "<doc/>", XML);
    send(423, "DELETE", DB + "/a.xml", null, null);
    send(204, "PUT", DB + "/a.xml", "<doc/>", XML, ifHeader(token));

    // the lock is reported by PROPFIND
    assertTrue(propfind(DB + "/a.xml", "0").contains(token));

    // refreshing succeeds, an unknown token does not
    send(200, "LOCK", DB + "/a.xml", null, null, ifHeader(token));
    send(412, "LOCK", DB + "/a.xml", null, null,
        ifHeader("urn:uuid:00000000-0000-0000-0000-000000000000"));

    unlock(DB + "/a.xml", token);
    send(409, "UNLOCK", DB + "/a.xml", null, null, Map.of("Lock-Token", '<' + token + '>'));
    send(204, "PUT", DB + "/a.xml", "<doc/>", XML);
  }

  /**
   * Tests that locking an unmapped path reserves the name.
   * @throws IOException I/O exception
   */
  @Test public void lockNull() throws IOException {
    send(201, "MKCOL", DB, null, null);
    final String token = lock(DB + "/new.xml", "0", 201);
    send(207, "PROPFIND", DB + "/new.xml", null, null, Map.of("Depth", "0"));
    send(423, "PUT", DB + "/new.xml", "<doc/>", XML);
    send(204, "PUT", DB + "/new.xml", "<doc/>", XML, ifHeader(token));
    unlock(DB + "/new.xml", token);
  }

  /**
   * Tests that a lock on a collection applies to its members.
   * @throws IOException I/O exception
   */
  @Test public void lockCollection() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/sub/a.xml", "<doc/>", XML);

    final String token = lock(DB, "infinity", 200);
    send(423, "PUT", DB + "/sub/a.xml", "<doc/>", XML);
    send(423, "PUT", DB + "/deep.xml", "<doc/>", XML);
    send(423, "DELETE", DB + "/sub/a.xml", null, null);
    // a second lock inside the locked tree is refused
    send(423, "LOCK", DB + "/sub/a.xml", LOCKINFO, XML, Map.of("Depth", "0"));

    send(201, "PUT", DB + "/deep.xml", "<doc/>", XML, ifHeader(token));
    unlock(DB, token);
    send(204, "PUT", DB + "/deep.xml", "<doc/>", XML);
  }

  /**
   * Tests that deleting a resource releases its locks.
   * @throws IOException I/O exception
   */
  @Test public void lockReleasedOnDelete() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    final String token = lock(DB + "/a.xml", "0", 200);
    send(204, "DELETE", DB + "/a.xml", null, null, ifHeader(token));
    // the name is free again, and no lock is left behind
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Acquires a lock and returns its token.
   * @param path path below the WebDAV root
   * @param depth requested depth
   * @param status expected status code
   * @return lock token
   * @throws IOException I/O exception
   */
  private static String lock(final String path, final String depth, final int status)
      throws IOException {
    final HttpResponse<String> response =
        send(status, "LOCK", path, LOCKINFO, XML, Map.of("Depth", depth));
    return header(response, "Lock-Token").replaceAll("^<|>$", "");
  }

  /**
   * Releases a lock.
   * @param path path below the WebDAV root
   * @param token lock token
   * @throws IOException I/O exception
   */
  private static void unlock(final String path, final String token) throws IOException {
    send(204, "UNLOCK", path, null, null, Map.of("Lock-Token", '<' + token + '>'));
  }

  /**
   * Sends a PROPFIND request and returns the response body.
   * @param path path below the WebDAV root
   * @param depth requested depth
   * @return response body
   * @throws IOException I/O exception
   */
  private static String propfind(final String path, final String depth) throws IOException {
    return send(207, "PROPFIND", path, null, null, Map.of("Depth", depth)).body();
  }

  /**
   * Extracts the hrefs of a multistatus response.
   * @param body response body
   * @return hrefs
   */
  private static List<String> hrefs(final String body) {
    final List<String> hrefs = new ArrayList<>();
    final Matcher m = HREF.matcher(body);
    while(m.find()) hrefs.add(m.group(1));
    return hrefs;
  }

  /**
   * Returns a Destination header for a path below the WebDAV root.
   * @param path target path
   * @return header
   */
  private static Map<String, String> destination(final String path) {
    return Map.of("Destination", HTTP_ROOT + "webdav/" + path);
  }

  /**
   * Returns an If header with a lock token.
   * @param token lock token
   * @return header
   */
  private static Map<String, String> ifHeader(final String token) {
    return Map.of("If", "(<" + token + ">)");
  }

  /**
   * Sends a request without additional headers.
   * @param method HTTP method
   * @param path path relative to the application root
   * @param body request body ({@code null} to omit)
   * @param type content type ({@code null} to omit)
   * @return response
   * @throws IOException I/O exception
   */
  private static HttpResponse<String> send(final String method, final String path,
      final String body, final String type) throws IOException {
    return send(method, path, body, type, Map.of());
  }

  /**
   * Returns a response header.
   * @param response response
   * @param name header name
   * @return value (empty string if the header is missing)
   */
  private static String header(final HttpResponse<String> response, final String name) {
    return response.headers().firstValue(name).orElse("");
  }
}
