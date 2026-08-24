package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.http.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

import org.basex.core.cmd.XQuery;
import org.basex.core.jobs.*;
import org.basex.http.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the DBA: deploys the application into the sandbox webapp, holds a login session, and
 * connects to its WebSocket endpoints. All views share a single deployment; each nested class
 * covers one of them.
 * Naming note: the JDK client type {@link java.net.http.WebSocket} collides with
 * BaseX's {@link org.basex.http.ws.WebSocket}; we therefore use the fully-qualified
 * name {@code java.net.http.WebSocket} throughout this file.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBATest extends WebappTest {
  /** Messages pushed by the server. */
  private static final BlockingQueue<String> MESSAGES = new LinkedBlockingQueue<>();

  /** Connection to a WebSocket endpoint ({@code null} if none was opened). */
  private static java.net.http.WebSocket ws;

  /**
   * Deploys the DBA, starts the server and logs in.
   * @throws Exception exception
   */
  @BeforeAll public static void startDBA() throws Exception {
    init("dba");
    final String page = post("login", Map.of("_name", "admin", "_pass", NAME));
    assertFalse(page.contains("_pass"), "DBA login failed");
  }

  /**
   * Closes the connection.
   * @throws Exception exception
   */
  @AfterEach public void disconnect() throws Exception {
    if(ws != null) {
      ws.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "bye").get(5, TimeUnit.SECONDS);
      ws = null;
    }
  }

  // PROTECTED METHODS ============================================================================

  /**
   * Opens a connection to a WebSocket endpoint of the application.
   * @param path path, relative to the WebSocket root of the DBA
   * @throws Exception exception
   */
  static void connect(final String path) throws Exception {
    MESSAGES.clear();
    ws = socket("/dba" + path, new java.net.http.WebSocket.Listener() {
      /** Accumulator for text frame parts. */
      private final StringBuilder buffer = new StringBuilder();

      @Override
      public CompletionStage<?> onText(final java.net.http.WebSocket socket,
          final CharSequence data, final boolean last) {
        buffer.append(data);
        if(last) {
          MESSAGES.add(buffer.toString());
          buffer.setLength(0);
        }
        socket.request(1);
        return null;
      }
    });
  }

  /**
   * Sends a message to the server.
   * @param message message
   * @throws Exception exception
   */
  static void sendMessage(final String message) throws Exception {
    ws.sendText(message, true).get(5, TimeUnit.SECONDS);
  }

  /**
   * Returns the next message pushed by the server.
   * @return message
   * @throws Exception exception
   */
  static String pollMessage() throws Exception {
    final String message = MESSAGES.poll(15, TimeUnit.SECONDS);
    assertNotNull(message, "No message received within timeout.");
    return message;
  }

  /**
   * Runs a query in the context of the HTTP server.
   * @param query query
   * @throws Exception exception
   */
  static void execute(final String query) throws Exception {
    new XQuery(query).execute(HTTPContext.get().context());
  }

  /**
   * Runs a query that cleans up after a test, ignoring what does not exist any more.
   * @param query query
   * @throws Exception exception
   */
  static void discard(final String query) throws Exception {
    execute("try { " + query + " } catch * { }");
  }

  // NESTED TEST CLASSES ==========================================================================

  /**
   * Smoke tests for the top-level DBA pages.
   */
  @Nested final class Pages {
    /**
     * Requests every top-level page and checks for an authenticated HTML response.
     * @throws IOException I/O exception
     */
    @Test public void pages() throws IOException {
      for(final String page : new String[] { "databases", "users", "workspace", "logs", "activity",
          "settings" }) {
        final String html = get(page);
        assertTrue(html.contains("<title>DBA"), page + ": not an authenticated DBA page:\n" + html);
      }
    }

    /**
     * Serves a static resource from the file system, and rejects an unknown one.
     * @throws IOException I/O exception
     */
    @Test public void staticResource() throws IOException {
      final HttpResponse<String> response = send(200, "GET", ".static/style.css", null, null);
      final String body = response.body();
      assertTrue(body.contains("{"), body);
      assertEquals(String.valueOf(body.getBytes(StandardCharsets.UTF_8).length),
          response.headers().firstValue("Content-Length").orElse(null));
      send(404, "GET", ".static/unknown.css", null, null);
    }

    /**
     * The views that were merged into others are gone; their addresses must not resolve any more.
     * @throws IOException I/O exception
     */
    @Test public void mergedPages() throws IOException {
      for(final String page : new String[] { "database", "user", "jobs", "sessions", "editor",
          "files" }) {
        send(404, "GET", page, null, null);
      }
    }
  }

  /**
   * Tests for the DBA databases view.
   */
  @Nested final class Databases {
    /** Test database. */
    private static final String DB = "dba-junit-test";
    /** Name the test database is renamed or copied to. */
    private static final String OTHER = DB + "-other";

    /**
     * Drops the test databases after each test.
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      discard("db:drop('" + DB + "')");
      discard("db:drop('" + OTHER + "')");
    }

    /**
     * Tests that the databases overview page is served.
     * @throws IOException I/O exception
     */
    @Test public void listPage() throws IOException {
      assertTrue(get("databases").contains("<html"), "expected an HTML page");
    }

    /**
     * Tests a create/list/drop round-trip.
     * @throws IOException I/O exception
     */
    @Test public void createAndDrop() throws IOException {
      assertTrue(create().contains("was created"), "new database not reported");
      assertTrue(get("databases").contains(DB), "database missing from list");
      post("databases/drop", Map.of("name", DB));
      assertFalse(get("databases").contains(DB + "<"), "database still listed after drop");
    }

    /**
     * Tests that creating an existing database reports an error.
     * @throws IOException I/O exception
     */
    @Test public void duplicate() throws IOException {
      create();
      assertTrue(create().contains("Database already exists."), "duplicate not rejected");
    }

    /**
     * The index options of the create dialog reach the new database: the optimize dialog offers
     * what the database really has, which is not what the create dialog defaults to.
     * @throws IOException I/O exception
     */
    @Test public void createWithOptions() throws IOException {
      post("databases/create", Map.of("name", DB, "opts", "textindex", "lang", "en"));
      final String dialog = dialog(get("databases?name=" + DB), "optimize");
      assertTrue(dialog.contains("value=\"textindex\" checked"), "text index not enabled");
      assertFalse(dialog.contains("value=\"attrindex\" checked"), "attribute index not disabled");
    }

    /**
     * Without a selection, only the lists are shown.
     * @throws IOException I/O exception
     */
    @Test public void noSelection() throws IOException {
      final String page = get("databases");
      assertTrue(page.contains("hidden\" data-label=\"Database\""), "database panel not hidden");
      assertTrue(page.contains("hidden\" data-label=\"Resource\""), "resource panel not hidden");
    }

    /**
     * Renames a database; the selection follows the new name.
     * @throws IOException I/O exception
     */
    @Test public void rename() throws IOException {
      create();
      assertTrue(post("databases/rename", Map.of("name", DB, "newname", OTHER)).
          contains("was renamed"), "database not renamed");
      assertTrue(get("databases?name=" + OTHER).contains("Database: "),
          "renamed database not shown");
      assertFalse(get("databases").contains(DB + "<"), "old name still listed");
    }

    /**
     * A name that is taken is rejected, and the selection stays on the edited database.
     * @throws IOException I/O exception
     */
    @Test public void renameToExistingName() throws IOException {
      create();
      post("databases/create", Map.of("name", OTHER));
      final String page = post("databases/rename", Map.of("name", DB, "newname", OTHER));
      assertTrue(page.contains("Database already exists."), "duplicate name not rejected");
      assertTrue(page.contains(">" + DB + "</a>"), "selection did not stay on the database");
    }

    /**
     * Copies a database; both names remain.
     * @throws IOException I/O exception
     */
    @Test public void copy() throws IOException {
      create();
      assertTrue(post("databases/copy", Map.of("name", DB, "newname", OTHER)).
          contains("was copied"), "database not copied");
      final String page = get("databases");
      assertTrue(page.contains(DB), "source missing");
      assertTrue(page.contains(OTHER), "copy missing");
    }

    /**
     * Creates a backup, finds it in the panel and drops it again.
     * @throws IOException I/O exception
     */
    @Test public void backups() throws IOException {
      create();
      assertTrue(post("databases/backup-create",
          Map.of("name", DB, "comment", "junit", "compress", "true")).
          contains("was backed up"), "backup not created");

      final String page = get("databases?name=" + DB);
      assertTrue(page.contains("<td>junit</td>"), "comment of the backup not listed");
      final Matcher m = Pattern.compile("name=\"backup\" value=\"([^\"]+)\"").matcher(page);
      assertTrue(m.find(), "backup not listed");

      assertTrue(post("databases/backup-drop", Map.of("name", DB, "backup", m.group(1))).
          contains("was dropped"), "backup not dropped");
    }

    /**
     * An unknown action is rejected.
     * @throws IOException I/O exception
     */
    @Test public void unknownAction() throws IOException {
      send(404, "POST", "databases/no-such-action", "name=" + DB,
          "application/x-www-form-urlencoded");
    }

    /**
     * The properties of a database are listed with stable column widths: a long value is
     * truncated instead of widening the panel.
     * @throws IOException I/O exception
     */
    @Test public void informationPanel() throws IOException {
      create();
      final String page = get("databases?name=" + DB);
      final int index = page.indexOf("id=\"information-panel\"");
      assertTrue(index != -1, "information panel missing");
      assertTrue(page.substring(index).contains("<h2>Information</h2><table class=\"fixed\">"),
          "properties are not listed in a table of fixed width");
    }

    /**
     * Creates the test database with the default options.
     * @return response body
     * @throws IOException I/O exception
     */
    private static String create() throws IOException {
      return post("databases/create", Map.of("name", DB));
    }

    /**
     * Returns the markup of a dialog of a page.
     * @param page page
     * @param id id of the dialog, without its suffix
     * @return markup
     */
    private static String dialog(final String page, final String id) {
      final String start = "<dialog id=\"" + id + "-dialog\">";
      final int s = page.indexOf(start);
      assertTrue(s != -1, id + " dialog not found");
      return page.substring(s, page.indexOf("</dialog>", s));
    }
  }

  /**
   * Tests for the resources of the DBA databases view: the document the editor shows, and the
   * actions that apply to it.
   */
  @Nested final class Resources {
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
      discard("db:drop('" + DB + "')");
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
  }

  /**
   * Tests for the WebSocket endpoint of the DBA databases view, which serves both its panels
   * and the queries on a resource.
   */
  @Nested final class DbQuery {
    /** Test database. */
    private static final String DB = "dba-junit-query";
    /** Test resource. */
    private static final String RESOURCE = "doc.xml";

    /**
     * Creates a database with one resource and opens the connection.
     * @throws Exception exception
     */
    @BeforeEach public void open() throws Exception {
      execute("db:create('" + DB + "', <x><y>1</y><y>2</y></x>, '" + RESOURCE + "')");
      connect("/databases");
    }

    /**
     * Drops the test database.
     * @throws Exception exception
     */
    @AfterEach public void drop() throws Exception {
      discard("db:drop('" + DB + "')");
    }

    /**
     * Requests the resource itself.
     * @throws Exception exception
     */
    @Test public void resource() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1," +
          "\"result\":\"<x><y>1<\\/y><y>2<\\/y><\\/x>\"}", evaluate("."));
    }

    /**
     * Runs a query on the resource.
     * @throws Exception exception
     */
    @Test public void context() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"1\\n2\"}",
          evaluate("//y/string()"));
    }

    /**
     * A failing query is reported with its position.
     * @throws Exception exception
     */
    @Test public void error() throws Exception {
      final String message = evaluate("1 +");
      assertTrue(message.startsWith("{\"type\":\"error\",\"run\":1,"), message);
      assertTrue(message.contains("\"line\":1,\"column\":4"), message);
    }

    /**
     * The database list is pushed as the markup of its panel.
     * @throws Exception exception
     */
    @Test public void databasesPanel() throws Exception {
      final String message = panel("{ \"type\": \"databases\", \"name\": \"" + DB +
          "\", \"sort\": \"\", \"page\": 1 }", "databases");
      assertTrue(message.contains(DB), "database missing from the panel: " + message);
    }

    /**
     * The panel of a database lists its resources.
     * @throws Exception exception
     */
    @Test public void databasePanel() throws Exception {
      final String message = panel("{ \"type\": \"database\", \"name\": \"" + DB +
          "\", \"resource\": \"\", \"sort\": \"\", \"page\": 1 }", "database");
      assertTrue(message.contains(RESOURCE), "resource missing from the panel: " + message);
    }

    /**
     * A panel with nothing to show answers with empty contents, which hides it.
     * @throws Exception exception
     */
    @Test public void emptyPanel() throws Exception {
      assertEquals("{\"type\":\"database\",\"html\":\"\"}",
          panel("{ \"type\": \"database\", \"name\": \"\", \"resource\": \"\"," +
              " \"sort\": \"\", \"page\": 1 }", "database"));
    }

    /**
     * The resource message carries the panel, the document and its edit state.
     * @throws Exception exception
     */
    @Test public void resourcePanel() throws Exception {
      final String message = panel("{ \"type\": \"resource\", \"name\": \"" + DB +
          "\", \"resource\": \"" + RESOURCE + "\" }", "resource");
      assertTrue(message.contains("\"editable\":true"), "document not editable: " + message);
      assertTrue(message.contains("<x><y>1<\\/y><y>2<\\/y><\\/x>"), "document missing: " + message);
    }

    /**
     * An unknown message type is reported.
     * @throws Exception exception
     */
    @Test public void unknownType() throws Exception {
      sendMessage("{ \"type\": \"nonsense\" }");
      final String message = pollMessage();
      assertTrue(message.contains("Unknown message type: nonsense"), message);
    }

    /**
     * Sends a message and returns the pushed panel.
     * @param message message
     * @param type expected type of the answer
     * @return message
     * @throws Exception exception
     */
    private static String panel(final String message, final String type) throws Exception {
      sendMessage(message);
      final String answer = pollMessage();
      assertTrue(answer.startsWith("{\"type\":\"" + type + "\""), answer);
      return answer;
    }

    /**
     * Sends a query and returns the pushed message.
     * @param string query
     * @return message
     * @throws Exception exception
     */
    private static String evaluate(final String string) throws Exception {
      sendMessage("{ \"type\": \"query\", \"run\": 1, \"name\": \"" + DB + "\"," +
          " \"resource\": \"" + RESOURCE + "\", \"query\": \"" +
          string.replace("\\", "\\\\").replace("\"", "\\\"") + "\", \"indent\": false }");
      return pollMessage();
    }
  }

  /**
   * Tests for the DBA users view.
   */
  @Nested final class Users {
    /** Test user. */
    private static final String USER = "dba-junit-user";
    /** Name the test user is renamed to. */
    private static final String OTHER = USER + "-renamed";

    /**
     * Drops the test users after each test.
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      discard("user:drop('" + USER + "')");
      discard("user:drop('" + OTHER + "')");
    }

    /**
     * Tests a create/list/inspect/drop round-trip.
     * @throws IOException I/O exception
     */
    @Test public void createAndDrop() throws IOException {
      assertTrue(create("read").contains("was created"), "user not created");
      assertTrue(get("users").contains(USER), "user missing from list");
      assertTrue(get("users?name=" + USER).contains("User: " + USER), "user panel not served");
      post("users/drop", Map.of("name", USER));
      assertFalse(get("users").contains(USER), "user still listed after drop");
    }

    /**
     * Tests that creating an existing user reports an error instead of a redirect.
     * @throws IOException I/O exception
     */
    @Test public void duplicate() throws IOException {
      create("read");
      assertTrue(create("read").contains("User already exists."), "duplicate not rejected");
    }

    /**
     * The user panel is only shown for a user that exists.
     * @throws IOException I/O exception
     */
    @Test public void noSelection() throws IOException {
      final String page = get("users");
      assertTrue(page.contains("class=\"panel hidden\" data-label=\"User\""),
          "user panel not hidden");
      assertTrue(page.contains("class=\"panel hidden\" data-label=\"Permissions\""),
          "permissions panel not hidden");
    }

    /**
     * Renames a user and changes its permission; the selection follows the new name.
     * @throws IOException I/O exception
     */
    @Test public void update() throws IOException {
      create("read");
      assertTrue(update(USER, OTHER, "write").contains("was updated"), "user not updated");
      final String page = get("users?name=" + OTHER);
      assertTrue(page.contains("User: " + OTHER), "renamed user not shown");
      assertTrue(page.contains("<option selected=\"\">write</option>"), "permission not changed");
    }

    /**
     * A name that is taken is rejected, and the panel keeps showing the edited user.
     * @throws IOException I/O exception
     */
    @Test public void updateToExistingName() throws IOException {
      create("read");
      final String page = update(USER, "admin", "read");
      assertTrue(page.contains("User already exists."), "duplicate name not rejected");
      assertTrue(page.contains("User: " + USER), "selection did not stay on the edited user");
    }

    /**
     * The password is not carried back into the address, the history and the log.
     * @throws IOException I/O exception
     */
    @Test public void passwordIsNotEchoed() throws IOException {
      create("read");
      // an update that fails is the case that used to carry the entered values back
      final String page = update(USER, "admin", "read", "topsecret");
      assertTrue(page.contains("User already exists."), "test needs an update that fails");
      assertFalse(page.contains("topsecret"), "password was echoed into the response");
    }

    /**
     * Adds and drops a local permission.
     * @throws IOException I/O exception
     */
    @Test public void patterns() throws IOException {
      create("read");
      assertTrue(post("users/pattern-add",
          Map.of("name", USER, "pattern", "unit*", "perm", "write")).contains("was created"),
          "pattern not added");
      assertTrue(get("users?name=" + USER).contains("unit*"), "pattern not listed");
      assertTrue(post("users/pattern-drop", Map.of("name", USER, "pattern", "unit*")).
          contains("was dropped"), "pattern not dropped");
    }

    /**
     * The admin has no local permissions to assign.
     * @throws IOException I/O exception
     */
    @Test public void adminHasNoPatterns() throws IOException {
      assertFalse(get("users?name=admin").contains("Local Permissions"), "panel shown for admin");
    }

    /**
     * Updates the information that belongs to no user in particular.
     * @throws IOException I/O exception
     */
    @Test public void globalInformation() throws IOException {
      assertTrue(post("users/info", Map.of("info", "<info><junit/></info>")).
          contains("User information was updated."), "information not updated");
      assertTrue(get("users").contains("junit"), "information not shown");
      post("users/info", Map.of("info", ""));
    }

    /**
     * Information that is not an {@code info} element is rejected.
     * @throws IOException I/O exception
     */
    @Test public void invalidInformation() throws IOException {
      assertTrue(post("users/info", Map.of("info", "<other/>")).
          contains("\"info\" root element"), "invalid information not rejected");
    }

    /**
     * Creates the test user with the given permission.
     * @param perm permission
     * @return response body
     * @throws IOException I/O exception
     */
    private static String create(final String perm) throws IOException {
      return post("users/create", Map.of("name", USER, "pw", "secret", "perm", perm));
    }

    /**
     * Updates the test user, leaving its password as it is.
     * @param name current name
     * @param newname new name
     * @param perm permission
     * @return response body
     * @throws IOException I/O exception
     */
    private static String update(final String name, final String newname, final String perm)
        throws IOException {
      return update(name, newname, perm, "");
    }

    /**
     * Updates the test user.
     * @param name current name
     * @param newname new name
     * @param perm permission
     * @param pw password (empty string: unchanged)
     * @return response body
     * @throws IOException I/O exception
     */
    private static String update(final String name, final String newname, final String perm,
        final String pw) throws IOException {
      return post("users/update", Map.of("name", name, "newname", newname, "pw", pw,
          "perm", perm, "info", ""));
    }
  }

  /**
   * Tests for the DBA settings page.
   */
  @Nested final class Settings {
    /** Default option values (a save persists every field, so all must be supplied). */
    private static final Map<String, String> DEFAULTS = Map.of(
        "timeout", "60", "memory", "8000", "maxchars", "1000000",
        "permission", "admin", "maxrows", "100");

    /**
     * Restores the default settings after each test.
     * @throws IOException I/O exception
     */
    @AfterEach public void restore() throws IOException {
      save(Map.of());
    }

    /**
     * Tests that the settings page is served with the current option values.
     * @throws IOException I/O exception
     */
    @Test public void settingsPage() throws IOException {
      assertTrue(get("settings").contains("<title>DBA"), "not a settings page");
    }

    /**
     * Tests that a changed option is saved and shown on reload.
     * @throws IOException I/O exception
     */
    @Test public void saveRoundTrip() throws IOException {
      assertTrue(save(Map.of("maxrows", "7")).contains("Settings were saved."),
          "settings not saved");
      assertTrue(get("settings").contains("value=\"7\""), "changed value not persisted");
    }

    /**
     * The options, the environment variables and the system properties are listed with stable
     * column widths: a long value is truncated instead of widening the panel.
     * @throws IOException I/O exception
     */
    @Test public void fixedTables() throws IOException {
      final String page = get("settings");
      assertTrue(page.contains("<table class=\"fixed\"><colgroup><col style=\"width: 40%\">"),
          "properties are not listed in tables of fixed width");
      assertFalse(page.contains("<table><colgroup>"), "table without stable column widths");
    }

    /**
     * Saves the settings, overriding individual defaults.
     * @param overrides option values to override
     * @return response body
     * @throws IOException I/O exception
     */
    private static String save(final Map<String, String> overrides) throws IOException {
      final Map<String, String> form = new HashMap<>(DEFAULTS);
      form.putAll(overrides);
      return post("settings/save", form);
    }
  }

  /**
   * Tests for the WebSocket endpoint of the DBA log view.
   * The sandbox server is started with suppressed logging, so no log file exists to be queried:
   * the tests cover the routing and the error paths of the endpoint.
   */
  @Nested final class Logs {
    /**
     * Opens the connection.
     * @throws Exception exception
     */
    @BeforeEach public void open() throws Exception {
      connect("/logs");
    }

    /**
     * An incomplete search input is reported without raising an error (which would be logged).
     * @throws Exception exception
     */
    @Test public void invalidInput() throws Exception {
      sendMessage(entries("(", ""));
      assertEquals("{\"type\":\"error\",\"run\":1," +
          "\"message\":\"Invalid regular expression: (.\"}", pollMessage());
    }

    /**
     * An incomplete column filter is reported in the same way.
     * @throws Exception exception
     */
    @Test public void invalidFilter() throws Exception {
      sendMessage(entries("", "["));
      assertEquals("{\"type\":\"error\",\"run\":1," +
          "\"message\":\"Invalid regular expression: [.\"}", pollMessage());
    }

    /**
     * An unknown log file is reported by the searching job, which returns the number of the run.
     * @throws Exception exception
     */
    @Test public void unknownDate() throws Exception {
      sendMessage(entries("", ""));
      final String message = pollMessage();
      assertTrue(message.startsWith("{\"type\":\"error\",\"run\":1,"), message);
      assertTrue(message.contains("Resource '1999-01-01' not found."), message);
    }

    /**
     * Returns a request for the log entries of a date that has no log file.
     * @param input search input
     * @param filter filter for the text column
     * @return message
     */
    private static String entries(final String input, final String filter) {
      return "{ \"type\": \"entries\", \"run\": 1, \"date\": \"1999-01-01\", \"sort\": \"\"," +
          " \"page\": 1, \"time\": \"\", \"ignore\": \"\", \"input\": \"" + input + "\"," +
          " \"filters\": { \"text\": \"" + filter + "\" } }";
    }
  }

  /**
   * Tests for the WebSocket endpoint of the DBA workspace view.
   */
  @Nested final class Query {
    /** Number of the last run. */
    private int run;

    /**
     * Opens the connection.
     * @throws Exception exception
     */
    @BeforeEach public void open() throws Exception {
      connect("/workspace");
    }

    /**
     * Tests query evaluation.
     * @throws Exception exception
     */
    @Test public void query() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"2\"}", evaluate("1 + 1"));
      assertEquals("{\"type\":\"result\",\"run\":2,\"result\":\"ok\"}", evaluate("'ok'"));
    }

    /**
     * An updating query is evaluated by the job itself; its output is the result.
     * @throws Exception exception
     */
    @Test public void update() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"<a\\/>\"}",
          evaluate("copy $a := <a/> modify delete node $a/@* return $a"));
    }

    /**
     * An empty result is pushed as an empty string.
     * @throws Exception exception
     */
    @Test public void emptyResult() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"\"}", evaluate("()"));
    }

    /**
     * A static error is reported with its position.
     * @throws Exception exception
     */
    @Test public void staticError() throws Exception {
      final String message = evaluate("1 +");
      assertTrue(message.startsWith("{\"type\":\"error\",\"run\":1,"), message);
      assertTrue(message.contains("\"line\":1,\"column\":4"), message);
    }

    /**
     * A dynamic error is reported with its position.
     * @throws Exception exception
     */
    @Test public void dynamicError() throws Exception {
      assertEquals("{\"type\":\"error\",\"run\":1," +
          "\"message\":\"1 cannot be divided by zero.\",\"line\":1,\"column\":7}",
          evaluate("1 div 0"));
    }

    /**
     * A result beyond the default WebSocket frame limit is pushed as a single message.
     * @throws Exception exception
     */
    @Test public void largeResult() throws Exception {
      final String message = evaluate("string-join((1 to 100000) ! 'x')");
      assertTrue(message.length() > 100000, "Truncated message: " + message.length() + " chars.");
    }

    /**
     * A query beyond the default WebSocket frame limit is accepted.
     * @throws Exception exception
     */
    @Test public void largeQuery() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"1\"}",
          evaluate("1 (: " + "x".repeat(100000) + " :)"));
    }

    /**
     * A stop request is confirmed. The job that waits for the query result is stopped as well,
     * so no result is pushed.
     * @throws Exception exception
     */
    @Test public void stopped() throws Exception {
      sendMessage("{ \"type\": \"run\", \"run\": 1, \"query\": \"prof:sleep(10000)\"," +
          " \"indent\": false }");
      sendMessage("{ \"type\": \"stop\" }");
      assertEquals("{\"type\":\"stopped\"}", outcome());
    }

    /**
     * Evaluates a query and returns the pushed message.
     * @param query query
     * @return message
     * @throws Exception exception
     */
    private String evaluate(final String query) throws Exception {
      sendMessage("{ \"type\": \"run\", \"run\": " + ++run + ", \"query\": \"" +
          query.replace("\\", "\\\\").replace("\"", "\\\"") + "\", \"indent\": false }");
      return outcome();
    }

    /**
     * Returns the outcome of a request, skipping the notifications that precede it. The endpoint
     * announces the job it started before the job has an outcome; the client tells the two apart
     * by the number of the run, which only an outcome carries.
     * @return message
     * @throws Exception exception
     */
    private static String outcome() throws Exception {
      String message = pollMessage();
      while(message.startsWith("{\"type\":\"job\"")) message = pollMessage();
      return message;
    }
  }

  /**
   * Tests for the jobs panel of the Activity view.
   */
  @Nested final class Activity {
    /** ID of the registered service. */
    private static final String ID = "DORMANT";

    /**
     * Drops the test service after each test.
     * @throws IOException I/O exception
     */
    @AfterEach public void cleanup() throws IOException {
      services().unregister(ID);
    }

    /**
     * A service without a job is listed in the jobs table, and its definition is shown.
     * @throws IOException I/O exception
     */
    @Test public void dormantService() throws IOException {
      register();
      assertTrue(get("activity").contains(ID), "service missing from jobs table");
      final String page = get("activity?job=" + ID);
      assertTrue(page.contains("Service: " + ID), "service panel not served");
      assertTrue(page.contains(">Unregister<"), "registered service cannot be unregistered");
    }

    /**
     * A service is unregistered via the jobs panel.
     * @throws IOException I/O exception
     */
    @Test public void unregisterService() throws IOException {
      register();
      final String page = post("jobs/unregister", Map.of("id", ID));
      assertTrue(page.contains("was unregistered"), "service not unregistered");
      assertFalse(page.contains(ID + "<"), "service still listed");
    }

    /**
     * Registers a service, without scheduling a job for it.
     * @throws IOException I/O exception
     */
    private static void register() throws IOException {
      final JobOptions options = new JobOptions();
      options.set(JobOptions.ID, ID);
      options.set(JobOptions.INTERVAL, "P1D");
      services().register(new QueryJobSpec(options, new HashMap<>(), new IOContent("1"), null));
    }

    /**
     * Returns the services of the server.
     * @return services
     */
    private static Services services() {
      return HTTPContext.get().context().services;
    }
  }

  /**
   * Tests for the caches panel of the Activity view.
   */
  @Nested final class Caches {
    /** Test cache. */
    private static final String CACHE = "dba-junit-cache";

    /**
     * Fills a cache before each test.
     * @throws Exception exception
     */
    @BeforeEach public void fill() throws Exception {
      execute("cache:put('key', 'value', '" + CACHE + "')");
    }

    /**
     * Deletes the caches after each test.
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      execute("cache:clear()");
    }

    /**
     * A cache is listed with what it holds.
     * @throws Exception exception
     */
    @Test public void listed() throws Exception {
      final String page = get("activity");
      assertTrue(page.contains(CACHE), "cache missing from the panel");
      assertTrue(page.contains("(default)"), "default cache not listed");
    }

    /**
     * A cache is deleted.
     * @throws Exception exception
     */
    @Test public void delete() throws Exception {
      assertTrue(post("caches/delete", Map.of("cache", CACHE)).
          contains("Cache \"" + CACHE + "\" was deleted."), "cache not deleted");
      assertFalse(get("activity").contains(CACHE), "cache still listed");
    }

    /**
     * All caches are cleared.
     * @throws Exception exception
     */
    @Test public void clear() throws Exception {
      assertTrue(post("caches/clear", Map.of()).contains("All caches were cleared."),
          "caches not cleared");
      assertFalse(get("activity").contains(CACHE), "cache still listed");
    }
  }

  /**
   * Tests for the DBA stores view.
   */
  @Nested final class Stores {
    /** Test store. */
    private static final String STORE = "dba-junit-store";
    /** Test store whose name follows the first one, ignoring case. */
    private static final String OTHER = "DBA-JUNIT-ZOO";
    /** Link of the child that is looked at. */
    private static final Pattern SELECTED =
        Pattern.compile("data-step=\"([^\"]*)\"[^>]*class=\"selected\"");

    /**
     * Deletes the test stores after each test.
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      discard("store:delete('" + STORE + "')");
      discard("store:delete('" + OTHER + "')");
    }

    /**
     * The view opens on the default store and offers to add an entry.
     * @throws IOException I/O exception
     */
    @Test public void storesPage() throws IOException {
      final String page = get("stores");
      assertTrue(page.contains("(default)"), "default store not listed");
      assertTrue(page.contains(">Add…<"), "add button not offered");
    }

    /**
     * An entry is added to a store and removed again.
     * @throws IOException I/O exception
     */
    @Test public void addAndRemove() throws IOException {
      assertTrue(add("", false, "'key'", "'value'").contains("Entry \"key\" was added."),
          "entry not added");
      assertTrue(get("stores?name=" + STORE).contains("data-step=\"key\""),
          "entry missing from the list");
      assertTrue(post("stores/remove", Map.of("name", STORE, "path", "", "step", "key")).
          contains("Entry \"key\" was removed."), "entry not removed");
      assertFalse(get("stores?name=" + STORE).contains("data-step=\"key\""),
          "entry still listed after removal");
    }

    /**
     * An entry that exists is not replaced.
     * @throws IOException I/O exception
     */
    @Test public void rejectExisting() throws IOException {
      add("", false, "'key'", "'value'");
      assertTrue(add("", false, "'key'", "'other'").contains("Entry already exists: key."),
          "existing entry not rejected");
      assertTrue(get("stores?name=" + STORE + "&key=key").contains(">\"value\"</textarea>"),
          "value of the existing entry was replaced");
    }

    /**
     * A map key is stored as a string: a key of another type could only be addressed by its
     * position, which would make its value read-only.
     * @throws IOException I/O exception
     */
    @Test public void stringKey() throws IOException {
      add("", false, "'map'", "{ 'a': 1 }");
      assertTrue(add("map", false, "'abcde'", "'text'").contains("Entry \"abcde\" was added."),
          "child not added");

      final String level = get("stores?name=" + STORE + "&path=map");
      assertTrue(level.contains("data-step=\"abcde\""), "key not addressed by itself");
      assertFalse(level.contains("{&quot;pos&quot;"), "key addressed by its position");

      final String value = get("stores?name=" + STORE + "&path=map.abcde");
      assertTrue(value.contains(">\"text\"</textarea>"), "value not shown in the editor");
      assertFalse(value.contains("Read-only"), "value reported as read-only");
    }

    /**
     * A child of a sequence is appended: its position is not asked for.
     * @throws IOException I/O exception
     */
    @Test public void appendToSequence() throws IOException {
      add("", false, "'seq'", "(1, 2, 3)");
      final String level = get("stores?name=" + STORE + "&path=seq");
      assertFalse(level.contains("Index:"), "position asked for");
      assertTrue(level.contains("name=\"step\" value=\"4\""), "position to append at not offered");

      assertTrue(add("seq", true, "4", "4").contains("Entry \"4\" was added."), "child not added");
      assertTrue(get("stores?name=" + STORE + "&path=seq").
          contains("name=\"step\" value=\"5\""), "child not appended");
    }

    /**
     * A member of an array is appended.
     * @throws IOException I/O exception
     */
    @Test public void appendToArray() throws IOException {
      add("", false, "'array'", "[ 'a' ]");
      assertTrue(get("stores?name=" + STORE + "&path=array").
          contains("name=\"step\" value=\"2\""), "position to append at not offered");

      assertTrue(add("array", true, "2", "'b'").contains("Entry \"2\" was added."),
          "member not added");
      assertTrue(get("stores?name=" + STORE + "&path=array").
          contains("name=\"step\" value=\"3\""), "member not appended");
    }

    /**
     * The store opens on the entry that its first row shows, which is sorted ignoring case.
     * @throws IOException I/O exception
     */
    @Test public void firstEntry() throws IOException {
      add("", false, "'Zoo'", "'z'");
      add("", false, "'apple'", "'a'");

      final String page = get("stores?name=" + STORE);
      assertTrue(page.indexOf("data-step=\"apple\"") < page.indexOf("data-step=\"Zoo\""),
          "entries not sorted, ignoring case");
      final Matcher matcher = SELECTED.matcher(page);
      assertTrue(matcher.find(), "no entry is looked at");
      assertEquals("apple", matcher.group(1), "entry of the first row is not the one looked at");
    }

    /**
     * The stores are listed in the order the table sorts them, ignoring case.
     * @throws IOException I/O exception
     */
    @Test public void storeOrder() throws IOException {
      add("", false, "'key'", "1");
      post("stores/add", Map.of("name", OTHER, "path", "", "index", "false", "step", "'key'",
          "value", "1"));

      final String page = get("stores");
      assertTrue(page.indexOf(STORE) < page.indexOf(OTHER), "stores not sorted, ignoring case");
    }

    /**
     * A store is written to disk, read again and closed.
     * @throws IOException I/O exception
     */
    @Test public void writeReadClose() throws IOException {
      add("", false, "'key'", "'value'");
      final Map<String, String> store = Map.of("name", STORE);
      assertTrue(post("stores/write", store).contains("was written to disk"), "store not written");
      assertTrue(post("stores/read", store).contains("was read from disk"), "store not read");
      assertTrue(post("stores/close", store).contains("was closed"), "store not closed");
    }

    /**
     * A key is an expression: what it yields is the key, with its type.
     * @throws IOException I/O exception
     */
    @Test public void typedKey() throws IOException {
      add("", false, "'map'", "{}");
      assertTrue(add("map", false, "1", "'int'").contains("Entry \"1\" was added."),
          "child not added");
      assertTrue(get("stores?name=" + STORE + "&path=map.1").contains(">\"int\"</textarea>"),
          "integer key not addressed by its value");

      assertTrue(add("map", false, "'1'", "'string'").contains("was added"),
          "string key rejected as the integer one");
      assertTrue(add("map", false, "1", "'other'").contains("Entry already exists: 1."),
          "integer key not rejected as existing");
    }

    /**
     * A key that is not a single atomic value is rejected, and an entry of a store is named by
     * a string.
     * @throws IOException I/O exception
     */
    @Test public void invalidKey() throws IOException {
      assertTrue(add("", false, "(1, 2)", "'x'").contains("must be a single atomic value"),
          "sequence accepted as a key");
      assertTrue(add("", false, "1", "'x'").contains("named by a string"),
          "integer accepted as the name of an entry");
    }

    /**
     * Adds a value to a level of the test store.
     * @param path path of the level
     * @param index whether the children of the level are addressed by position
     * @param step key or position of the new child
     * @param value expression that yields the value
     * @return response body
     * @throws IOException I/O exception
     */
    private static String add(final String path, final boolean index, final String step,
        final String value) throws IOException {
      return post("stores/add", Map.of("name", STORE, "path", path, "index",
          String.valueOf(index), "step", step, "value", value));
    }
  }

  /**
   * Tests for the WebSocket endpoint of the DBA stores view, which serves its panels and the
   * value that is looked at.
   */
  @Nested final class StoresSocket {
    /** Test store. */
    private static final String STORE = "dba-junit-socket";

    /**
     * Fills a store with one entry and opens the connection.
     * @throws Exception exception
     */
    @BeforeEach public void open() throws Exception {
      execute("store:put('key', 'text', '" + STORE + "')");
      connect("/stores");
    }

    /**
     * Deletes the test store.
     * @throws Exception exception
     */
    @AfterEach public void delete() throws Exception {
      discard("store:delete('" + STORE + "')");
    }

    /**
     * The store list is pushed as the markup of its panel.
     * @throws Exception exception
     */
    @Test public void storesPanel() throws Exception {
      final String message = panel("{ \"type\": \"stores\", \"name\": \"" + STORE +
          "\", \"sort\": \"\", \"page\": 1 }", "stores");
      assertTrue(message.contains(STORE), "store missing from the panel: " + message);
    }

    /**
     * The panel of a store lists its entries.
     * @throws Exception exception
     */
    @Test public void entriesPanel() throws Exception {
      final String message = panel("{ \"type\": \"entries\", \"name\": \"" + STORE +
          "\", \"path\": \"\", \"selected\": \"key\", \"sort\": \"\", \"page\": 1 }", "entries");
      assertTrue(message.contains("key"), "entry missing from the panel: " + message);
    }

    /**
     * The value message carries the panel, the value and its edit state.
     * @throws Exception exception
     */
    @Test public void value() throws Exception {
      final String message = panel("{ \"type\": \"value\", \"name\": \"" + STORE +
          "\", \"path\": \"key\" }", "value");
      assertTrue(message.contains("\"editable\":true"), "value not editable: " + message);
      assertTrue(message.contains("\\\"text\\\""), "value missing: " + message);
    }

    /**
     * A path that leads to no value answers with empty contents, which hides the panel.
     * @throws Exception exception
     */
    @Test public void emptyValue() throws Exception {
      final String message = panel("{ \"type\": \"value\", \"name\": \"" + STORE +
          "\", \"path\": \"none\" }", "value");
      assertTrue(message.contains("\"html\":\"\""), "panel not hidden: " + message);
      assertTrue(message.contains("\"editable\":false"), "value reported as editable: " + message);
    }

    /**
     * An unknown message type is reported.
     * @throws Exception exception
     */
    @Test public void unknownType() throws Exception {
      sendMessage("{ \"type\": \"nonsense\" }");
      final String message = pollMessage();
      assertTrue(message.contains("Unknown message type: nonsense"), message);
    }

    /**
     * Sends a message and returns the pushed panel.
     * @param message message
     * @param type expected type of the answer
     * @return message
     * @throws Exception exception
     */
    private static String panel(final String message, final String type) throws Exception {
      sendMessage(message);
      final String answer = pollMessage();
      assertTrue(answer.startsWith("{\"type\":\"" + type + "\""), answer);
      return answer;
    }
  }
}
