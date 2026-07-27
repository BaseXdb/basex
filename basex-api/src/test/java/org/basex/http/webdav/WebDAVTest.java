package org.basex.http.webdav;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Grant;
import org.basex.http.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the WebDAV web application.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebDAVTest extends WebappTest {
  /** Name of the database that is created by the tests. */
  private static final String DB = "webdav-test";
  /** Name of the second database that is created by the tests. */
  private static final String DB2 = DB + "-2";
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
    send("DELETE", DB2, null, null);
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
   * Tests that a non-admin user may browse but not perform operations beyond its permissions.
   * @throws Exception exception
   */
  @Test public void nonAdmin() throws Exception {
    final Context ctx = HTTPContext.get().context();
    new CreateUser("reader", "readerpw").execute(ctx);
    new Grant("read", "reader").execute(ctx);
    try {
      // a read user may browse the root collection
      assertEquals(207, dav("PROPFIND", "webdav", "reader", "readerpw"));
      // a read user may not create a database (create permission is required): 403, not 500
      assertEquals(403, dav("MKCOL", "webdav/" + DB, "reader", "readerpw"));
    } finally {
      new DropUser("reader").execute(ctx);
    }
  }

  /**
   * Sends a WebDAV request with the given credentials and returns the status code.
   * @param method HTTP method
   * @param path path relative to the server root
   * @param user username
   * @param pass password
   * @return status code
   * @throws Exception exception
   */
  private static int dav(final String method, final String path, final String user,
      final String pass) throws Exception {
    final String auth = "Basic " + Base64.getEncoder().
        encodeToString((user + ':' + pass).getBytes(StandardCharsets.UTF_8));
    final HttpRequest request = HttpRequest.newBuilder(URI.create(HTTP_ROOT + path)).
        header("Authorization", auth).
        method(method, HttpRequest.BodyPublishers.noBody()).build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).
        statusCode();
  }

  /**
   * Sends a request to the WebDAV root, addressed without a trailing slash.
   * @param method HTTP method
   * @return status code
   * @throws Exception exception
   */
  private static int dav(final String method) throws Exception {
    return dav(method, "webdav", "admin", NAME);
  }

  /**
   * Tests that the root collection is also addressed without a trailing slash.
   * @throws Exception exception
   */
  @Test public void root() throws Exception {
    assertEquals(200, dav("OPTIONS"));
    assertEquals(200, dav("GET"));
    assertEquals(207, dav("PROPFIND"));
    // the root collection is no database: rejected by the WebDAV service, not by RESTXQ
    assertEquals(405, dav("PUT"));
    assertEquals(405, dav("MKCOL"));
    assertEquals(404, dav("DELETE"));
    assertEquals(404, dav("MOVE"));
    assertEquals(404, dav("COPY"));
    assertEquals(409, dav("UNLOCK"));
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
   * Tests that a zero-byte PUT stores an empty resource, also at the root level.
   * @throws IOException I/O exception
   */
  @Test public void emptyResource() throws IOException {
    // a resource at the root level becomes a database that holds it
    send(201, "PUT", DB + ".txt", "", "text/plain");
    assertEquals("", send(200, "GET", DB + "/" + DB + ".txt", null, null).body());

    send(201, "PUT", DB + "/empty.txt", "", "text/plain");
    assertEquals("", send(200, "GET", DB + "/empty.txt", null, null).body());
  }

  /**
   * Tests that PROPFIND honours the requested depth.
   * @throws IOException I/O exception
   */
  @Test public void propfind() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    send(201, "MKCOL", DB + "/sub", null, null);
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
   * Tests that PUT and DELETE honour entity tag conditions.
   * @throws IOException I/O exception
   */
  @Test public void conditions() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    final String etag = header(send("GET", DB + "/a.xml", null, null), "ETag");

    // the resource exists, so a create-only request is rejected
    send(412, "PUT", DB + "/a.xml", "<new/>", XML, Map.of("If-None-Match", "*"));
    // a stale entity tag is rejected; a list that contains the current one is accepted
    send(412, "PUT", DB + "/a.xml", "<new/>", XML, Map.of("If-Match", "\"0-0\""));
    send(204, "PUT", DB + "/a.xml", "<new/>", XML, Map.of("If-Match", "\"0-0\", " + etag));

    // a weak entity tag never satisfies If-Match
    send(412, "DELETE", DB + "/a.xml", null, null, Map.of("If-Match", "W/" + etag));
    send(204, "DELETE", DB + "/a.xml", null, null, Map.of("If-Match", "*"));
  }

  /**
   * Tests that COPY and MOVE honour the Overwrite header.
   * @throws IOException I/O exception
   */
  @Test public void overwrite() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    send(201, "PUT", DB + "/b.xml", "<old/>", XML);

    // an existing target is kept if the client forbids overwriting
    send(412, "COPY", DB + "/a.xml", null, null, destination(DB + "/b.xml", "Overwrite", "F"));
    send(412, "MOVE", DB + "/a.xml", null, null, destination(DB + "/b.xml", "Overwrite", "F"));
    assertEquals("<old/>", send("GET", DB + "/b.xml", null, null).body());

    // a replaced target is reported with 204, a new one with 201
    send(204, "COPY", DB + "/a.xml", null, null, destination(DB + "/b.xml"));
    assertEquals("<doc/>", send("GET", DB + "/b.xml", null, null).body());
    send(201, "MOVE", DB + "/b.xml", null, null, destination(DB + "/c.xml"));
    send(204, "MOVE", DB + "/c.xml", null, null, destination(DB + "/a.xml"));
  }

  /**
   * Tests that COPY with depth 0 copies a collection without its members.
   * @throws IOException I/O exception
   */
  @Test public void copyDepth() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/folder", null, null);
    send(201, "PUT", DB + "/folder/a.xml", "<doc/>", XML);

    send(201, "COPY", DB + "/folder", null, null, destination(DB + "/shallow", "Depth", "0"));
    final List<String> hrefs = hrefs(propfind(DB + "/shallow", "1"));
    assertTrue(hrefs.contains("/webdav/" + DB + "/shallow/"), hrefs.toString());
    assertFalse(hrefs.contains("/webdav/" + DB + "/shallow/a.xml"), hrefs.toString());
  }

  /**
   * Tests that resources and collections can only be created below an existing collection.
   * @throws IOException I/O exception
   */
  @Test public void missingParent() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    send(409, "PUT", DB + "/nonesuch/a.xml", "<doc/>", XML);
    send(409, "MKCOL", DB + "/nonesuch/sub", null, null);
    send(409, "COPY", DB + "/a.xml", null, null, destination(DB + "/nonesuch/copy.xml"));
    send(409, "MOVE", DB + "/a.xml", null, null, destination(DB + "/nonesuch/moved.xml"));
  }

  /**
   * Tests that COPY onto an existing collection replaces its members.
   * @throws IOException I/O exception
   */
  @Test public void copyOntoCollection() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/a", null, null);
    send(201, "PUT", DB + "/a/one.xml", "<one/>", XML);
    send(201, "MKCOL", DB + "/b", null, null);
    send(201, "PUT", DB + "/b/two.xml", "<two/>", XML);

    send(204, "COPY", DB + "/a", null, null, destination(DB + "/b"));
    assertEquals("<one/>", send(200, "GET", DB + "/b/one.xml", null, null).body());
    send(404, "GET", DB + "/b/two.xml", null, null);
  }

  /**
   * Tests that MOVE onto an existing collection replaces it and removes the source.
   * @throws IOException I/O exception
   */
  @Test public void moveOntoCollection() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/a", null, null);
    send(201, "PUT", DB + "/a/one.xml", "<one/>", XML);
    send(201, "MKCOL", DB + "/b", null, null);
    send(201, "PUT", DB + "/b/two.xml", "<two/>", XML);

    send(204, "MOVE", DB + "/a", null, null, destination(DB + "/b"));
    assertEquals("<one/>", send(200, "GET", DB + "/b/one.xml", null, null).body());
    send(404, "GET", DB + "/b/two.xml", null, null);
    send(404, "GET", DB + "/a/one.xml", null, null);
  }

  /**
   * Tests that a collection cannot replace a resource of the same name.
   * @throws IOException I/O exception
   */
  @Test public void moveOntoResource() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/a", null, null);
    send(201, "PUT", DB + "/a/one.xml", "<one/>", XML);
    send(201, "PUT", DB + "/file", "plain", "text/plain");

    send(409, "MOVE", DB + "/a", null, null, destination(DB + "/file"));
    send(409, "COPY", DB + "/a", null, null, destination(DB + "/file"));
  }

  /**
   * Tests that an empty collection is preserved by COPY.
   * @throws IOException I/O exception
   */
  @Test public void copyEmptyCollection() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/a", null, null);
    send(201, "PUT", DB + "/a/one.xml", "<one/>", XML);
    send(201, "MKCOL", DB + "/a/empty", null, null);
    // a collection whose only member is another empty collection
    send(201, "MKCOL", DB + "/a/outer", null, null);
    send(201, "MKCOL", DB + "/a/outer/inner", null, null);

    send(201, "COPY", DB + "/a", null, null, destination(DB + "/c"));
    final List<String> hrefs = hrefs(propfind(DB + "/c", "1"));
    assertTrue(hrefs.contains("/webdav/" + DB + "/c/empty/"), hrefs.toString());
    assertTrue(hrefs.contains("/webdav/" + DB + "/c/one.xml"), hrefs.toString());

    // both levels are kept alive, so removing the inner one leaves the outer in place
    send(204, "DELETE", DB + "/c/outer/inner", null, null);
    final List<String> rest = hrefs(propfind(DB + "/c", "1"));
    assertTrue(rest.contains("/webdav/" + DB + "/c/outer/"), rest.toString());
  }

  /**
   * Tests that malformed and unexpected request bodies are rejected.
   * @throws IOException I/O exception
   */
  @Test public void invalidBody() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(400, "PROPFIND", DB, "<D:propfind", XML, Map.of("Depth", "0"));
    send(415, "MKCOL", DB + "/sub", "<D:mkcol xmlns:D='DAV:'/>", XML);
  }

  /**
   * Tests that a body which is not XML is stored as binary data, even if XML is announced.
   * @throws IOException I/O exception
   */
  @Test public void binaryBody() throws IOException {
    send(201, "MKCOL", DB, null, null);
    // clients announce the content type of the file extension, even for a one-byte probe
    send(201, "PUT", DB + "/a.xml", " ", "application/xml");
    assertEquals(" ", send(200, "GET", DB + "/a.xml", null, null).body());
    // XML input is still parsed
    send(204, "PUT", DB + "/a.xml", "<doc/>", "application/xml");
    assertEquals("<doc/>", send(200, "GET", DB + "/a.xml", null, null).body());
  }

  /**
   * Tests that unsupported methods are rejected with an Allow header.
   * @throws IOException I/O exception
   */
  @Test public void unsupportedMethod() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    final HttpResponse<String> response = send(405, "POST", DB + "/a.xml", "<doc/>", XML);
    final String allow = header(response, "Allow");
    assertTrue(allow.contains("PROPFIND"), allow);
  }

  /**
   * Tests that PROPPATCH rejects all properties, but is answered with a multistatus.
   * @throws IOException I/O exception
   */
  @Test public void proppatch() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    // clients rely on PROPPATCH: a failing request makes Windows Explorer delete its upload
    final String body = send(207, "PROPPATCH", DB + "/a.xml",
        "<D:propertyupdate xmlns:D='DAV:'><D:set><D:prop>"
        + "<Z:Win32FileAttributes xmlns:Z='urn:schemas-microsoft-com:'>00000020"
        + "</Z:Win32FileAttributes>"
        + "</D:prop></D:set></D:propertyupdate>", XML).body();
    assertTrue(body.contains("Win32FileAttributes"), body);
    assertTrue(body.contains("403 Forbidden"), body);

    // no properties, missing and malformed request bodies
    send(207, "PROPPATCH", DB + "/a.xml", "<D:propertyupdate xmlns:D='DAV:'/>", XML);
    send(400, "PROPPATCH", DB + "/a.xml", null, null);
    send(400, "PROPPATCH", DB + "/a.xml", "<D:propertyupdate", XML);
    send(404, "PROPPATCH", DB + "/missing.xml", "<D:propertyupdate xmlns:D='DAV:'/>", XML);
  }

  /**
   * Tests that a lock token which applies to no lock is rejected.
   * @throws IOException I/O exception
   */
  @Test public void unknownLockToken() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    send(412, "PUT", DB + "/a.xml", "<doc/>", XML,
        ifHeader("urn:uuid:00000000-0000-0000-0000-000000000000"));
  }

  /**
   * Tests that a list of requested lock timeouts is accepted.
   * @throws IOException I/O exception
   */
  @Test public void timeoutList() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    final HttpResponse<String> response = send(200, "LOCK", DB + "/a.xml", LOCKINFO, XML,
        Map.of("Timeout", "Second-300, Infinite"));
    assertTrue(response.body().contains("Second-300"), response.body());
  }

  /**
   * Tests that a user with write permissions may modify a database but not create one.
   * @throws Exception exception
   */
  @Test public void writeUser() throws Exception {
    final Context ctx = HTTPContext.get().context();
    new CreateUser("writer", "writerpw").execute(ctx);
    new Grant("write", "writer").execute(ctx);
    try {
      // creating and deleting a database requires create permissions
      assertEquals(403, dav("MKCOL", "webdav/" + DB, "writer", "writerpw"));
      send(201, "MKCOL", DB, null, null);
      assertEquals(403, dav("DELETE", "webdav/" + DB, "writer", "writerpw"));
      // collections and resources within an existing database can be modified
      assertEquals(201, dav("MKCOL", "webdav/" + DB + "/sub", "writer", "writerpw"));
      assertEquals(204, dav("DELETE", "webdav/" + DB + "/sub", "writer", "writerpw"));
    } finally {
      new DropUser("writer").execute(ctx);
    }
  }

  /**
   * Tests that a whole database can be copied into a collection of another database.
   * @throws IOException I/O exception
   */
  @Test public void copyDatabase() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    send(201, "MKCOL", DB2, null, null);

    send(201, "COPY", DB, null, null, destination(DB2 + "/sub"));
    final List<String> hrefs = hrefs(propfind(DB2, "infinity"));
    assertTrue(hrefs.contains("/webdav/" + DB2 + "/sub/a.xml"), hrefs.toString());
  }

  /**
   * Tests that COPY and MOVE reject a target database that does not exist.
   * @throws IOException I/O exception
   */
  @Test public void missingTargetDatabase() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    send(409, "COPY", DB + "/a.xml", null, null, destination(DB2 + "/a.xml"));
    send(409, "MOVE", DB + "/a.xml", null, null, destination(DB2 + "/a.xml"));
  }

  /**
   * Tests that a collection survives the removal of its only member.
   * @throws IOException I/O exception
   */
  @Test public void deleteOnlyMember() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/x", null, null);
    send(201, "MKCOL", DB + "/x/y", null, null);

    send(204, "DELETE", DB + "/x/y", null, null);
    send(207, "PROPFIND", DB + "/x", null, null, Map.of("Depth", "0"));

    // the same holds for a resource that is moved away
    send(201, "PUT", DB + "/x/a.xml", "<doc/>", XML);
    send(201, "MOVE", DB + "/x/a.xml", null, null, destination(DB + "/moved.xml"));
    send(207, "PROPFIND", DB + "/x", null, null, Map.of("Depth", "0"));
  }

  /**
   * Tests that a database is renamed and copied as a whole.
   * @throws IOException I/O exception
   */
  @Test public void renameDatabase() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    // a database is copied to a new name
    send(201, "COPY", DB, null, null, destination(DB2));
    assertEquals("<doc/>", send(200, "GET", DB2 + "/a.xml", null, null).body());
    send(204, "DELETE", DB2, null, null);

    // a database is renamed
    send(201, "MOVE", DB, null, null, destination(DB2));
    send(404, "GET", DB + "/a.xml", null, null);
    assertEquals("<doc/>", send(200, "GET", DB2 + "/a.xml", null, null).body());
  }

  /**
   * Tests that a lock inside the destination collection blocks COPY and MOVE.
   * @throws IOException I/O exception
   */
  @Test public void lockedDestination() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "MKCOL", DB + "/target", null, null);
    send(201, "PUT", DB + "/target/child.xml", "<doc/>", XML);
    send(201, "MKCOL", DB + "/source", null, null);
    send(201, "PUT", DB + "/source/other.xml", "<doc/>", XML);

    // the lock is on a member of the destination, so it is invisible to a covering check
    final String token = lock(DB + "/target/child.xml", "0", 200);
    send(423, "COPY", DB + "/source", null, null, destination(DB + "/target"));
    send(423, "MOVE", DB + "/source", null, null, destination(DB + "/target"));
    unlock(DB + "/target/child.xml", token);
  }

  /**
   * Tests that shared locks can be held by several clients at the same time.
   * @throws IOException I/O exception
   */
  @Test public void sharedLock() throws IOException {
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);

    final String first = share(DB + "/a.xml"), second = share(DB + "/a.xml");
    assertNotEquals(first, second);
    // an exclusive lock is still refused
    send(423, "LOCK", DB + "/a.xml", LOCKINFO, XML, Map.of("Depth", "0"));
    // a write is allowed if it supplies one of the tokens, and refused otherwise
    send(204, "PUT", DB + "/a.xml", "<doc/>", XML, ifHeader(first));
    send(423, "PUT", DB + "/a.xml", "<doc/>", XML);

    unlock(DB + "/a.xml", first);
    unlock(DB + "/a.xml", second);
  }

  /**
   * Acquires a shared lock and returns its token.
   * @param path path below the WebDAV root
   * @return lock token
   * @throws IOException I/O exception
   */
  private static String share(final String path) throws IOException {
    final String info = "<D:lockinfo xmlns:D='DAV:'>" +
        "<D:lockscope><D:shared/></D:lockscope><D:locktype><D:write/></D:locktype>" +
        "<D:owner><D:href>mailto:test@basex.org</D:href></D:owner></D:lockinfo>";
    final HttpResponse<String> response =
        send(200, "LOCK", path, info, XML, Map.of("Depth", "0"));
    return header(response, "Lock-Token").replaceAll("^<|>$", "");
  }

  /**
   * Tests that a user with permissions for a single database can browse it.
   * @throws Exception exception
   */
  @Test public void localPermissions() throws Exception {
    final Context ctx = HTTPContext.get().context();
    send(201, "MKCOL", DB, null, null);
    new CreateUser("local", "localpw").execute(ctx);
    new Grant("read", "local", DB).execute(ctx);
    try {
      assertEquals(207, dav("PROPFIND", "webdav/" + DB, "local", "localpw"));
    } finally {
      new DropUser("local").execute(ctx);
    }
  }

  /**
   * Tests that a user with permissions for a single database can modify it.
   * @throws Exception exception
   */
  @Test public void localWritePermissions() throws Exception {
    final Context ctx = HTTPContext.get().context();
    send(201, "MKCOL", DB, null, null);
    send(201, "PUT", DB + "/a.xml", "<doc/>", XML);
    new CreateUser("localw", "localwpw").execute(ctx);
    new Grant("write", "localw", DB).execute(ctx);
    try {
      // DELETE releases the locks of the resource, which updates the lock store
      assertEquals(204, dav("DELETE", "webdav/" + DB + "/a.xml", "localw", "localwpw"));
    } finally {
      new DropUser("localw").execute(ctx);
    }
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
    send(201, "MKCOL", DB + "/sub", null, null);
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
   * Returns a Destination header, combined with a second header.
   * @param path target path
   * @param name name of the additional header
   * @param value value of the additional header
   * @return headers
   */
  private static Map<String, String> destination(final String path, final String name,
      final String value) {
    return Map.of("Destination", HTTP_ROOT + "webdav/" + path, name, value);
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
