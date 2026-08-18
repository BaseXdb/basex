package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.*;
import java.util.*;

import org.basex.core.cmd.XQuery;
import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the resources of the DBA databases view: the document the editor shows, and the
 * actions that apply to it.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ResourcesTest extends DBATest {
  /** Test database. */
  private static final String DB = "dba-junit-resources";
  /** Test resource. */
  private static final String RESOURCE = "doc.xml";

  /**
   * Creates a database with one resource.
   * @throws Exception exception
   */
  @BeforeEach public void create() throws Exception {
    execute("db:create('" + DB + "', <x>one</x>, '" + RESOURCE + "')");
  }

  /**
   * Drops the test database.
   * @throws Exception exception
   */
  @AfterEach public void drop() throws Exception {
    execute("db:drop('" + DB + "')");
  }

  /**
   * The selected document is served with the page, so that the editor needs no second request.
   * @throws Exception exception
   */
  @Test public void shown() throws Exception {
    final String page = page(RESOURCE);
    assertTrue(page.contains("&lt;x&gt;one&lt;/x&gt;"), "document not in the editor");
    assertTrue(page.contains("initDatabases(true)"), "document not editable");
    assertTrue(page.contains("Resource: " + RESOURCE), "resource not named");
  }

  /**
   * A document that is not XML is shown read-only, with the reason.
   * @throws Exception exception
   */
  @Test public void binaryIsReadOnly() throws Exception {
    execute("db:put-binary('" + DB + "', xs:base64Binary('SGVsbG8='), 'bin')");
    final String page = page("bin");
    assertTrue(page.contains("initDatabases(false)"), "binary reported as editable");
    assertTrue(page.contains("only XML can be edited"), "reason not given");
  }

  /**
   * Saves the edited document.
   * @throws Exception exception
   */
  @Test public void save() throws Exception {
    post("db-save?name=" + DB + "&resource=" + RESOURCE, "<x>two</x>");
    assertTrue(page(RESOURCE).contains("&lt;x&gt;two&lt;/x&gt;"), "document not saved");
  }

  /**
   * Renames a resource; the selection follows the new path.
   * @throws Exception exception
   */
  @Test public void rename() throws Exception {
    assertTrue(post("databases/resource-rename",
        Map.of("name", DB, "resource", RESOURCE, "target", "sub/moved.xml")).
        contains("was renamed"), "resource not renamed");
    assertTrue(page("sub/moved.xml").contains("Resource: sub/moved.xml"), "new path not shown");
  }

  /**
   * A path that is taken is rejected, and the selection stays on the edited resource.
   * @throws Exception exception
   */
  @Test public void renameToExistingPath() throws Exception {
    execute("db:put('" + DB + "', <y/>, 'other.xml')");
    final String page = post("databases/resource-rename",
        Map.of("name", DB, "resource", RESOURCE, "target", "other.xml"));
    assertTrue(page.contains("Resource already exists."), "duplicate path not rejected");
    assertTrue(page.contains("Resource: " + RESOURCE), "selection did not stay on the resource");
  }

  /**
   * Deletes a resource.
   * @throws Exception exception
   */
  @Test public void delete() throws Exception {
    assertTrue(post("databases/resource-delete", Map.of("name", DB, "resource", RESOURCE)).
        contains("was deleted"), "resource not deleted");
    assertFalse(get("databases?name=" + DB).contains(RESOURCE), "resource still listed");
  }

  /**
   * A download names the file in a header that survives spaces and non-ASCII characters.
   * @throws Exception exception
   */
  @Test public void download() throws Exception {
    final HttpResponse<String> response = send(200, "POST", "db-download",
        "name=" + DB + "&resource=" + RESOURCE, "application/x-www-form-urlencoded");
    assertEquals("attachment; filename*=UTF-8''" + RESOURCE,
        response.headers().firstValue("Content-Disposition").orElse(null));
    assertEquals("<x>one</x>", response.body());
  }

  /**
   * A resource that does not exist is not offered for download.
   * @throws Exception exception
   */
  @Test public void downloadUnknown() throws Exception {
    send(404, "POST", "db-download", "name=" + DB + "&resource=no-such-resource",
        "application/x-www-form-urlencoded");
  }

  /**
   * Returns the page that shows a resource.
   * @param resource resource
   * @return page
   * @throws Exception exception
   */
  private static String page(final String resource) throws Exception {
    return get("databases?name=" + DB + "&resource=" + resource.replace("/", "%2F"));
  }

  /**
   * Runs a query in the context of the HTTP server.
   * @param query query
   * @throws Exception exception
   */
  private static void execute(final String query) throws Exception {
    new XQuery(query).execute(HTTPContext.get().context());
  }
}
