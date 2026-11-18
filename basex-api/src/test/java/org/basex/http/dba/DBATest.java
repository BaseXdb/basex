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
import org.basex.http.ws.WsPool;
import org.basex.io.*;
import org.basex.util.Performance;
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
  /** Content type of URL-encoded forms. */
  private static final String FORM = "application/x-www-form-urlencoded";
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
   * Runs a query in the context of the HTTP server and returns its result.
   * @param query query
   * @return result
   * @throws Exception exception
   */
  static String evaluate(final String query) throws Exception {
    return new XQuery(query).execute(HTTPContext.get().context());
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
     * The New buttons of the rendered pages open dialogs with headings.
     * @throws IOException I/O exception
     */
    @Test public void newDialogs() throws IOException {
      for(final String[] page : new String[][] { { "databases", "Database" }, { "users", "User" },
          { "stores", "Store" }, { "activity", "Job" } }) {
        assertTrue(get(page[0]).contains("<h2>New " + page[1] + "</h2>"),
            page[0] + ": no dialog with a heading");
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

    /**
     * Redirects to relative pages only after a login.
     * @throws IOException I/O exception
     */
    @Test public void loginRedirect() throws IOException {
      final String login = "_name=admin&_pass=" + NAME + "&_page=";
      assertTrue(send(200, "POST", "login", login + "settings", FORM).uri().getPath().
          endsWith("/dba/settings"));
      final String authority = java.net.URI.create(HTTP_ROOT).getAuthority();
      for(final String page : new String[] { "http://example.org/x", "//example.org/x" }) {
        final java.net.URI uri = send("POST", "login", login + enc(page), FORM, Map.of()).uri();
        assertEquals(authority, uri.getAuthority(), page);
        assertTrue(uri.getPath().startsWith("/dba/"), page);
      }
    }

    /**
     * Rejects form bodies that exceed the maximum form size.
     * @throws IOException I/O exception
     */
    @Test public void largeForm() throws IOException {
      send(413, "POST", "login", "_name=admin&x=" + "x".repeat(300_000), FORM);
    }

    /**
     * Forwards query parameters, but no form parameters, to the login page.
     * @throws IOException I/O exception
     */
    @Test public void anonymousRedirect() throws IOException {
      post("logout", Map.of());
      try {
        final String page = post("settings?query=1", Map.of("form", "2"));
        assertTrue(page.contains("name=\"query\""), page);
        assertFalse(page.contains("name=\"form\""), page);
      } finally {
        post("login", Map.of("_name", "admin", "_pass", NAME));
      }
    }

    /**
     * Closes the session of a dropped user.
     * @throws Exception exception
     */
    @Test public void droppedUser() throws Exception {
      final String user = NAME + "dba";
      execute("user:create('" + user + "', 'pw', 'admin')");
      try {
        assertFalse(post("login", Map.of("_name", user, "_pass", "pw")).contains("_pass"));
        post("users/drop", Map.of("name", user));
        assertTrue(get("settings").contains("_pass"), "session was not closed");
      } finally {
        execute("if(user:exists('" + user + "')) { user:drop('" + user + "') }");
        post("login", Map.of("_name", "admin", "_pass", NAME));
      }
    }

    /**
     * Closes the WebSocket connections of a session on logout.
     * @throws Throwable exception
     */
    @Test public void logoutClosesSockets() throws Throwable {
      socketClosed(() -> post("logout", Map.of()));
    }

    /**
     * Closes the WebSocket connections of a session when the session is closed.
     * @throws Throwable exception
     */
    @Test public void sessionClosesSockets() throws Throwable {
      socketClosed(() -> execute("sessions:ids() ! sessions:close(.)"));
    }

    /**
     * Checks if a WebSocket connection is closed by the specified action.
     * @param action action
     * @throws Throwable exception
     */
    private void socketClosed(final org.junit.jupiter.api.function.Executable action)
        throws Throwable {
      final CompletableFuture<Integer> closed = new CompletableFuture<>();
      final java.net.http.WebSocket socket = socket("/dba/logs",
          new java.net.http.WebSocket.Listener() {
        @Override
        public CompletionStage<?> onClose(final java.net.http.WebSocket wsc, final int status,
            final String reason) {
          closed.complete(status);
          return null;
        }
      });
      try {
        // the server may register the socket after the client handshake has completed
        final long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while(WsPool.ids().isEmpty() && System.nanoTime() < end) Performance.sleep(10);
        action.execute();
        assertNotNull(closed.get(10, TimeUnit.SECONDS));
      } finally {
        socket.abort();
        post("login", Map.of("_name", "admin", "_pass", NAME));
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
     * An invalid name is reported, and the page it is redirected to is served.
     * @throws IOException I/O exception
     */
    @Test public void invalidName() throws IOException {
      assertTrue(post("databases/create", Map.of("name", "bad name")).contains("Invalid name"),
          "invalid name not reported");
      assertTrue(get("databases?name=bad%20name").contains("<html"), "page not served");
    }

    /**
     * The index options of the create dialog reach the new database: the properties dialog
     * offers what the database really has, which is not what the create dialog defaults to.
     * @throws IOException I/O exception
     */
    @Test public void createWithOptions() throws IOException {
      post("databases/create", Map.of("name", DB, "opts", "textindex", "lang", "en"));
      final String dialog = dialog(get("databases?name=" + DB), "properties");
      assertTrue(dialog.contains("value=\"textindex\" checked"), "text index not enabled");
      assertFalse(dialog.contains("value=\"attrindex\" checked"), "attribute index not disabled");
    }

    /**
     * Mixed content and the element names it refers to reach the new database, and are offered
     * again by the properties dialog.
     * @throws IOException I/O exception
     */
    @Test public void createWithMixedContent() throws IOException {
      post("databases/create", Map.of("name", DB, "opts", "ftmixed", "ftinclude", "p"));
      final String dialog = dialog(get("databases?name=" + DB), "properties");
      assertTrue(dialog.contains("value=\"ftmixed\" checked"), "mixed content not enabled");
      assertTrue(dialog.contains("name=\"ftinclude\" value=\"p\""), "element names not offered");
    }

    /**
     * Automatic optimization and the names of the value indexes reach the new database, and are
     * offered again by the properties dialog.
     * @throws IOException I/O exception
     */
    @Test public void createWithNames() throws IOException {
      post("databases/create", Map.of("name", DB, "opts", "autooptimize",
          "textinclude", "a", "attrinclude", "b", "tokeninclude", "c"));
      final String dialog = dialog(get("databases?name=" + DB), "properties");
      assertTrue(dialog.contains("value=\"autooptimize\" checked"), "optimization not enabled");
      assertTrue(dialog.contains("name=\"textinclude\" value=\"a\""), "text names not offered");
      assertTrue(dialog.contains("name=\"attrinclude\" value=\"b\""), "attribute names lost");
      assertTrue(dialog.contains("name=\"tokeninclude\" value=\"c\""), "token names lost");
    }

    /**
     * Incremental indexing can be changed by the properties dialog: the optimization is then
     * full.
     * @throws IOException I/O exception
     */
    @Test public void optimizeIncremental() throws IOException {
      post("databases/create", Map.of("name", DB, "opts", "textindex"));
      final String page = post("databases/optimize-db", Map.of("name", DB, "opts", "updindex"));
      assertTrue(page.contains("optimized"), "optimization failed");
      final String dialog = dialog(get("databases?name=" + DB), "properties");
      assertTrue(dialog.contains("value=\"updindex\" checked"), "incremental indexing not enabled");
    }

    /**
     * Creates a database from an input that the server reads, and adds the same input again:
     * a target path is what an input is stored under, and what a second run replaces.
     * @throws Exception exception
     */
    @Test public void createWithInput() throws Exception {
      final IOFile dir = new IOFile(sandbox(), "import");
      final IOFile sub = new IOFile(dir, "sub");
      sub.md();
      new IOFile(dir, "a.xml").write("<a/>");
      new IOFile(sub, "b.xml").write("<b/>");
      try {
        assertTrue(post("databases/create", Map.of("name", DB, "input", dir.path())).
            contains("was created"), "database not created");
        assertEquals("a.xml sub/b.xml", resources(), "unexpected resources");

        post("databases/put", Map.of("name", DB, "input", dir.path(), "target", "import"));
        post("databases/put", Map.of("name", DB, "input", dir.path(), "target", "import"));
        assertEquals("a.xml import/a.xml import/sub/b.xml sub/b.xml", resources(),
            "input not replaced by the second run");
      } finally {
        dir.delete();
      }
    }

    /**
     * An input that is neither a file nor a URL is reported as such.
     * @throws Exception exception
     */
    @Test public void unknownInput() throws Exception {
      assertTrue(post("databases/create", Map.of("name", DB, "input", "<a/>")).
          contains("No file, directory, archive or URL"), "unknown input not reported");
    }

    /**
     * Restore is only offered for databases with a backup.
     * @throws Exception exception
     */
    @Test public void restoreRequiresBackup() throws Exception {
      assertTrue(get("databases").contains("data-require=\"a[href*=&quot;backups-panel&quot;]\""),
          "restore does not require a backup");
    }

    /**
     * The parser of the create dialog is applied to the input.
     * @throws Exception exception
     */
    @Test public void createWithParser() throws Exception {
      final IOFile file = new IOFile(sandbox(), "input.json");
      file.write("{ \"city\": \"Konstanz\" }");
      try {
        post("databases/create", Map.of("name", DB, "input", file.path(), "parser", "json",
            "filter", "*.json"));
        assertEquals("<json type=\"object\"><city>Konstanz</city></json>",
            evaluate("db:get('" + DB + "')/*"), "input not parsed as JSON");
      } finally {
        file.delete();
      }
    }

    /**
     * An input without a target path is rejected: an empty one would address the database as
     * a whole, and delete what the input does not supply.
     * @throws Exception exception
     */
    @Test public void inputWithoutTarget() throws Exception {
      create();
      assertTrue(post("databases/put", Map.of("name", DB, "input", "x.xml")).
          contains("Target path is required"), "missing target not reported");
    }

    /**
     * Returns the resources of the test database, separated by spaces.
     * @return resources
     * @throws Exception exception
     */
    private String resources() throws Exception {
      return evaluate("string-join(sort(db:list('" + DB + "')), ' ')");
    }

    /**
     * Without a database, only the list is shown.
     * @throws IOException I/O exception
     */
    @Test public void noSelection() throws IOException {
      final String page = get("databases");
      assertTrue(page.contains("hidden\" data-label=\"Database\""), "database panel not hidden");
      assertTrue(page.contains("hidden\" data-label=\"Resource\""), "resource panel not hidden");
    }

    /**
     * Without a selection, the first database is opened.
     * @throws IOException I/O exception
     */
    @Test public void firstDatabase() throws IOException {
      createBoth();
      final String page = get("databases");
      assertTrue(page.contains("Database: " + DB + "<"), "first database not opened");
      assertFalse(page.contains("class=\"dismiss\""), "panel can be closed");
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
      createBoth();
      final String page = post("databases/rename", Map.of("name", DB, "newname", OTHER));
      assertTrue(page.contains("Database already exists."), "duplicate name not rejected");
      assertTrue(page.contains(">" + DB + "</a>"), "selection did not stay on the database");
    }

    /**
     * An invalid name is rejected, and the selection stays on the database.
     * @throws IOException I/O exception
     */
    @Test public void renameToInvalidName() throws IOException {
      // the second database is renamed: the first one is what is opened without a selection
      createBoth();
      final String page = post("databases/rename", Map.of("name", OTHER, "newname", "bad name"));
      assertTrue(page.contains("Invalid name"), "invalid name not rejected");
      assertTrue(page.contains("Database: " + OTHER + "<"),
          "selection did not stay on the database");
    }

    /**
     * A database that is backed up from the list stays selected.
     * @throws Exception exception
     */
    @Test public void backUpKeepsSelection() throws Exception {
      createBoth();
      try {
        assertTrue(post("databases/backups-create", Map.of("name", OTHER)).
            contains("Database: " + OTHER + "<"), "selection did not stay on the database");
      } finally {
        execute("db:backups('" + OTHER + "') ! db:drop-backup(.)");
      }
    }

    /**
     * An invalid option is rejected, and the selection stays on the database.
     * @throws IOException I/O exception
     */
    @Test public void optimizeWithInvalidOption() throws IOException {
      createBoth();
      final String page = post("databases/optimize-db", Map.of("name", OTHER, "lang", "xx"));
      assertTrue(page.contains("Unknown language"), "invalid language not rejected");
      assertTrue(page.contains("Database: " + OTHER + "<"),
          "selection did not stay on the database");
    }

    /**
     * A database without backups is not offered for restoring.
     * @throws IOException I/O exception
     */
    @Test public void missingWithoutBackups() throws IOException {
      final String page = get("databases?name=" + DB);
      assertTrue(page.contains("The database does not exist.<"), "missing database not reported");
      assertFalse(page.contains("Backups…</button>"), "backups are offered");
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
     * The properties dialog of a database that is only known from its backups can be closed.
     * @throws Exception exception
     */
    @Test public void droppedProperties() throws Exception {
      create();
      execute("db:create-backup('" + DB + "')");
      execute("db:drop('" + DB + "')");
      try {
        final String dialog = dialog(get("databases?name=" + DB), "properties");
        assertFalse(dialog.contains("id=\"options-form\""), "options of a dropped database");
        assertFalse(dialog.contains("Rename…"), "a dropped database can be renamed");
        assertFalse(dialog.contains("aria-controls=\"index-panel\""), "indexes are offered");
        assertTrue(dialog.contains("<form method=\"dialog\" class=\"buttons\">"),
            "dialog cannot be closed");
      } finally {
        execute("db:drop-backup('" + DB + "')");
      }
    }

    /**
     * A database that is only known from its backups is sorted by name with the others.
     * @throws Exception exception
     */
    @Test public void droppedSorted() throws Exception {
      execute("db:create('dba-b'), db:create('dba-a')");
      execute("db:create-backup('dba-a')");
      execute("db:drop('dba-a')");
      try {
        final String page = get("databases");
        assertTrue(page.indexOf("dba-a") < page.indexOf("dba-b"), "dropped database not sorted");
      } finally {
        execute("db:drop-backup('dba-a'), db:drop('dba-b')");
      }
    }

    /**
     * Creates a general backup, finds it in the storage dialog, and drops it.
     * @throws Exception exception
     */
    @Test public void generalBackups() throws Exception {
      final String page = post("databases/backup-create",
          Map.of("name", "", "comment", "junit-general"));
      assertTrue(page.contains("Users, services, and stores were backed up."),
          "backup not created");
      // the dialog is filled when it is opened
      connect("/databases");
      sendMessage("{ \"type\": \"storage\" }");
      final String dialog = pollMessage();
      assertTrue(dialog.contains("<td>junit-general<\\/td>"), "comment of the backup not listed");
      final Matcher m = Pattern.compile("name=\\\\\"backup\\\\\" value=\\\\\"([^\\\\\"]+)").
          matcher(dialog);
      assertTrue(m.find(), "backup not listed");

      assertTrue(post("databases/backup-drop", Map.of("name", "", "backup", m.group(1))).
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
      final String dialog = dialog(get("databases?name=" + DB), "properties");
      assertTrue(dialog.contains("\"information-panel-tab\"><table class=\"fixed\">"),
          "properties are not listed in a table of fixed width");
    }

    /**
     * The index block creates and drops the index it browses; the name indexes that every
     * database has are rejected.
     * @throws IOException I/O exception
     */
    @Test public void indexCreateAndDrop() throws IOException {
      create();
      assertTrue(post("databases/index-create", Map.of("name", DB, "index", "text")).
          contains("was created"), "index not created");
      assertTrue(dialog(get("databases?name=" + DB), "properties").
          contains("value=\"textindex\" checked"), "text index not enabled");

      assertTrue(post("databases/index-drop", Map.of("name", DB, "index", "text")).
          contains("was dropped"), "index not dropped");
      assertFalse(dialog(get("databases?name=" + DB), "properties").
          contains("value=\"textindex\" checked"), "text index not disabled");

      assertTrue(post("databases/index-create", Map.of("name", DB, "index", "element-name")).
          contains("Index cannot be created or dropped."), "name index not rejected");
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
     * Creates the test database and the one that follows it in the list.
     * @throws IOException I/O exception
     */
    private static void createBoth() throws IOException {
      create();
      post("databases/create", Map.of("name", OTHER));
    }

    /**
     * Returns the markup of a dialog of a page.
     * @param page page
     * @param id id of the dialog, without its suffix
     * @return markup
     */
    private static String dialog(final String page, final String id) {
      final String start = "<dialog id=\"" + id + "-dialog\"";
      final int s = page.indexOf(start);
      assertTrue(s != -1, id + " dialog not found");
      // a dialog may contain dialogs of its own
      int e = s, depth = 0;
      do {
        final int open = page.indexOf("<dialog", e + 1), close = page.indexOf("</dialog>", e + 1);
        if(open != -1 && open < close) {
          depth++;
          e = open;
        } else {
          depth--;
          e = close;
        }
      } while(depth >= 0);
      return page.substring(s, e);
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
     * Without a selected resource, the resource panel stays in place.
     * @throws Exception exception
     */
    @Test public void noResource() throws Exception {
      final String page = get("databases?name=" + DB);
      assertFalse(page.contains("hidden\" data-label=\"Resource\""), "resource panel hidden");
      assertTrue(page.contains("Choose a resource"), "placeholder missing");
    }

    /**
     * A resource that is gone is reported as such.
     * @throws Exception exception
     */
    @Test public void missingResource() throws Exception {
      final String page = get("databases?name=" + DB + "&resource=gone.xml");
      assertTrue(page.contains("The resource does not exist."), "missing resource not reported");
    }

    /**
     * A document that is not XML is shown read-only, with the reason.
     * @throws Exception exception
     */
    @Test public void binaryIsReadOnly() throws Exception {
      execute("db:put-binary('" + DB + "', xs:base64Binary('SGVsbG8='), 'bin')");
      final String page = page("bin");
      assertTrue(page.contains("initDatabases(false)"), "binary reported as editable");
      assertTrue(page.contains("no XML document"), "reason not given");
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
     * An invalid path is rejected, and the selection stays on the edited resource.
     * @throws Exception exception
     */
    @Test public void renameToInvalidPath() throws Exception {
      final String page = post("databases/resource-rename",
          Map.of("name", DB, "resource", RESOURCE, "target", "dir/"));
      assertTrue(page.contains("Invalid path"), "invalid path not rejected");
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
     * A resource that does not exist is not offered for download, and the failure is reported.
     * @throws Exception exception
     */
    @Test public void downloadUnknown() throws Exception {
      assertTrue(post("db-download", Map.of("name", DB, "resource", "no-such-resource")).
          contains("Download failed"), "failed download not reported");
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
     * The contents of the properties dialog are pushed when they are requested.
     * @throws Exception exception
     */
    @Test public void properties() throws Exception {
      sendMessage("{ \"type\": \"properties\", \"name\": \"" + DB + "\" }");
      final String message = pollMessage();
      assertTrue(message.contains("\"id\":\"properties-dialog\""), "dialog not pushed: " + message);
      assertTrue(message.contains("Properties: " + DB), "database not named: " + message);
      assertTrue(message.contains("options-form"), "options missing: " + message);
    }

    /**
     * The element names of the index tab are listed in the order that their column states.
     * @throws Exception exception
     */
    @Test public void sortedNames() throws Exception {
      execute("db:put('" + DB + "', <z><a/></z>, 'other.xml')");
      sendMessage("{ \"type\": \"index\", \"name\": \"" + DB + "\", \"index\": \"element-name\" }");
      final String message = pollMessage();
      final Matcher m = Pattern.compile("<td>([a-z])<\\\\/td>").matcher(message);
      final StringBuilder names = new StringBuilder();
      while(m.find()) names.append(m.group(1));
      assertEquals("axyz", names.toString(), "names not sorted: " + message);
    }

    /**
     * The contents of the storage dialog are pushed when they are requested.
     * @throws Exception exception
     */
    @Test public void storage() throws Exception {
      sendMessage("{ \"type\": \"storage\" }");
      final String message = pollMessage();
      assertTrue(message.contains("\"id\":\"storage-dialog\""), "dialog not pushed: " + message);
      assertTrue(message.contains("general-backups-panel"), "backups missing: " + message);
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
          "\", \"sort\": \"\", \"page\": 1 }", "panel", "databases-panel");
      assertTrue(message.contains(DB), "database missing from the panel: " + message);
    }

    /**
     * The panel of a database lists its resources.
     * @throws Exception exception
     */
    @Test public void databasePanel() throws Exception {
      final String message = panel("{ \"type\": \"database\", \"name\": \"" + DB +
          "\", \"resource\": \"\", \"sort\": \"\", \"page\": 1 }", "panel", "database-panel");
      assertTrue(message.contains(RESOURCE), "resource missing from the panel: " + message);
    }

    /**
     * A panel with nothing to show answers with empty contents, which hides it.
     * @throws Exception exception
     */
    @Test public void emptyPanel() throws Exception {
      assertEquals("{\"type\":\"panel\",\"id\":\"database-panel\",\"html\":\"\"}",
          panel("{ \"type\": \"database\", \"name\": \"\", \"resource\": \"\"," +
              " \"sort\": \"\", \"page\": 1 }", "panel", "database-panel"));
    }

    /**
     * The resource message carries the panel, the document and its edit state.
     * @throws Exception exception
     */
    @Test public void resourcePanel() throws Exception {
      final String message = panel("{ \"type\": \"resource\", \"name\": \"" + DB +
          "\", \"resource\": \"" + RESOURCE + "\" }", "editor", "resource-panel");
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
     * @param id expected id of the panel
     * @return message
     * @throws Exception exception
     */
    private static String panel(final String message, final String type, final String id)
        throws Exception {
      sendMessage(message);
      final String answer = pollMessage();
      assertTrue(answer.startsWith("{\"type\":\"" + type + "\",\"id\":\"" + id + "\""),
          answer);
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
     * An invalid name is reported, and the page it is redirected to is served.
     * @throws IOException I/O exception
     */
    @Test public void invalidName() throws IOException {
      assertTrue(post("users/create", Map.of("name", "bad name", "pw", "secret", "perm", "read")).
          contains("Invalid username"), "invalid name not reported");
      assertTrue(get("users?name=bad%20name").contains("User: admin"), "first user not opened");
    }

    /**
     * The first user is opened if none is named; the user panel is only shown for a user that
     * exists.
     * @throws IOException I/O exception
     */
    @Test public void noSelection() throws IOException {
      assertTrue(get("users").contains("User: admin"), "first user not opened");
      assertTrue(get("users?name=" + USER).contains("class=\"panel hidden\" data-label=\"User\""),
          "user panel not hidden");
    }

    /**
     * Renames a user and changes its permission; the selection follows the new name.
     * @throws IOException I/O exception
     */
    @Test public void update() throws IOException {
      create("read");
      assertTrue(update(USER, OTHER, "write").contains("was saved"), "user not updated");
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
     * An invalid name is rejected, and the panel keeps showing the edited user.
     * @throws IOException I/O exception
     */
    @Test public void updateToInvalidName() throws IOException {
      create("read");
      final String page = update(USER, "bad name", "read");
      assertTrue(page.contains("Invalid username"), "invalid name not rejected");
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
          Map.of("name", USER, "pattern", "unit*", "perm", "write")).contains("was added"),
          "pattern not added");
      final String page = get("users?name=" + USER);
      assertTrue(page.contains("<code>unit*</code>"), "pattern not listed in the user form");
      assertTrue(page.contains("read +1"), "pattern not counted in the user list");
      assertTrue(post("users/pattern-drop", Map.of("name", USER, "pattern", "unit*")).
          contains("was dropped"), "pattern not dropped");
    }

    /**
     * The admin has no local permissions to assign.
     * @throws IOException I/O exception
     */
    @Test public void adminHasNoPatterns() throws IOException {
      assertFalse(get("users?name=admin").contains("Per database:"), "field shown for admin");
    }

    /**
     * Updates the information that belongs to no user in particular.
     * @throws IOException I/O exception
     */
    @Test public void globalInformation() throws IOException {
      assertTrue(post("users/info", Map.of("info", "<info><junit/></info>")).
          contains("General user data was saved."), "information not updated");
      assertTrue(get("users").contains("junit"), "information not shown");
      post("users/info", Map.of("info", ""));
    }

    /**
     * Information that is not an {@code info} element is rejected.
     * @throws IOException I/O exception
     */
    @Test public void invalidInformation() throws IOException {
      assertTrue(post("users/info", Map.of("info", "<other/>")).
          contains("“info” root element"), "invalid information not rejected");
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
   * Tests for the editor of the workspace.
   */
  @Nested final class Editor {
    /** Test file. */
    private static final String FILE = "dba-junit-save.xq";

    /**
     * Deletes the test file after each test.
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      post("workspace/delete", Map.of("name", FILE));
    }

    /**
     * An existing file is only replaced if this is requested.
     * @throws Exception exception
     */
    @Test public void replace() throws Exception {
      final String path = "editor-save?name=" + FILE + "&replace=";
      send(200, "POST", path + "false", "1", "text/plain");
      send(409, "POST", path + "false", "2", "text/plain");
      send(200, "POST", path + "true", "3", "text/plain");
    }
  }

  /**
   * Tests for the DBA settings page.
   */
  @Nested final class Settings {
    /** Default option values (a save persists every field, so all must be supplied). */
    private static final Map<String, String> DEFAULTS = Map.of(
        "timeout", "60", "memory", "8000", "maxchars", "1000000",
        "permission", "admin");

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
      assertTrue(save(Map.of("timeout", "7")).contains("Settings were saved."),
          "settings not saved");
      assertTrue(get("settings").contains("value=\"7\""), "changed value not persisted");
    }

    /**
     * An invalid value is rejected, and the saved settings are kept.
     * @throws IOException I/O exception
     */
    @Test public void invalidValue() throws IOException {
      save(Map.of("timeout", "7"));
      assertFalse(save(Map.of("memory", "")).contains("Settings were saved."),
          "empty value saved");
      assertFalse(save(Map.of("memory", "-1")).contains("Settings were saved."),
          "negative value saved");
      assertFalse(save(Map.of("interval", "0")).contains("Settings were saved."),
          "interval out of range saved");
      assertTrue(get("settings").contains("value=\"7\""), "saved settings were reset");
    }

    /**
     * The options, the environment variables and the system properties are listed in one
     * searchable table with stable column widths.
     * @throws IOException I/O exception
     */
    @Test public void optionTable() throws IOException {
      final String page = get("settings");
      assertTrue(page.contains("id=\"input\""), "search field is missing");
      assertTrue(page.contains("<table class=\"fixed\""), "table without stable column widths");
      for(final String category : new String[] { "Global Option", "Local Option",
          "System Property", "Environment Variable" }) {
        assertTrue(page.contains("<td>" + category + "</td>"), category + " is missing");
      }
      assertTrue(get("settings?sort=-name").contains("<span class=\"sorted\">"),
          "table is not sorted");
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
     * A download of logs that do not exist is reported, and a path outside the logs rejected.
     * @throws Exception exception
     */
    @Test public void download() throws Exception {
      assertTrue(post("logs-download", Map.of("name", "2000-01-01")).
          contains("No log was selected."), "missing log not reported");
      send(400, "POST", "logs-download", "name=..%2F..%2Fx", FORM);
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
     * Several files are searched, newest first: the error names the file that is reached first.
     * @throws Exception exception
     */
    @Test public void severalDates() throws Exception {
      sendMessage("{ \"type\": \"entries\", \"run\": 1," +
          " \"dates\": [ \"1999-01-01\", \"1999-12-31\" ], \"sort\": \"\"," +
          " \"page\": 1, \"time\": \"\", \"ignore\": \"\", \"input\": \"\"," +
          " \"filters\": { } }");
      final String message = pollMessage();
      assertTrue(message.contains("Resource '1999-12-31' not found."), message);
    }

    /**
     * Returns a request for the log entries of a date that has no log file.
     * @param input search input
     * @param filter filter for the text column
     * @return message
     */
    private static String entries(final String input, final String filter) {
      return "{ \"type\": \"entries\", \"run\": 1, \"dates\": [ \"1999-01-01\" ], \"sort\": \"\"," +
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
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"2\",\"items\":1}",
          evaluate("1 + 1"));
      assertEquals("{\"type\":\"result\",\"run\":2,\"result\":\"ok\",\"items\":1}",
          evaluate("'ok'"));
    }

    /**
     * An updating query is evaluated by the job itself; its output is the result.
     * @throws Exception exception
     */
    @Test public void update() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"<a\\/>\",\"items\":1}",
          evaluate("copy $a := <a/> modify delete node $a/@* return $a"));
    }

    /**
     * The result is serialized by the job, without being materialized.
     * @throws Exception exception
     */
    @Test public void serialized() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"fn:true#0\",\"items\":1}",
          evaluate("true#0"));
    }

    /**
     * The bytes of a binary result are shown as text.
     * @throws Exception exception
     */
    @Test public void binary() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"AB\",\"items\":1}",
          evaluate("xs:base64Binary('QUI=')"));
    }

    /**
     * An empty result is pushed as an empty string.
     * @throws Exception exception
     */
    @Test public void emptyResult() throws Exception {
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"\",\"items\":0}",
          evaluate("()"));
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
      assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"1\",\"items\":1}",
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
     * The outcome carries the query information, rendered as the list that the editor shows
     * below the result.
     * @throws Exception exception
     */
    @Test public void queryInfo() throws Exception {
      sendMessage("{ \"type\": \"run\", \"run\": 1, \"query\": \"1 + 1\", \"indent\": false }");
      final String message = outcome();
      assertTrue(message.contains("\"info\":\"<dl class="), message);
      assertTrue(message.contains("<dt>Optimized Query:<\\/dt>"), message);
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
      return withoutInfo(outcome());
    }

    /**
     * Drops the query information from an outcome: it is checked by its own test, and its
     * contents would make every other comparison unreadable.
     * @param message message
     * @return message without the query information
     */
    private static String withoutInfo(final String message) {
      final int i = message.lastIndexOf(",\"info\":\"");
      return i == -1 ? message : message.substring(0, i) + '}';
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
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      services().unregister(ID);
      discard("('junit-long', 'junit-short') ! job:remove(.)");
    }

    /**
     * The jobs table is sorted by the durations of its jobs.
     * @throws Exception exception
     */
    @Test public void sortByDuration() throws Exception {
      execute("job:eval('prof:sleep(10000)', (), { 'id': 'junit-long' })");
      Thread.sleep(500);
      execute("job:eval('prof:sleep(10000)', (), { 'id': 'junit-short' })");
      connect("/activity");
      sendMessage("{ \"type\": \"panels\", \"open\": [ \"jobs-panel\" ], " +
          "\"sorts\": { \"jobs-panel\": \"duration\" } }");
      final String message = pollMessage();
      final int longer = message.indexOf("junit-long"), shorter = message.indexOf("junit-short");
      assertTrue(longer != -1 && shorter != -1, "jobs missing: " + message);
      assertTrue(longer < shorter, "longer job not listed first: " + message);
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
     * A new job is reported by what was done with it: registered, scheduled, or started.
     * @throws Exception exception
     */
    @Test public void createMessages() throws Exception {
      assertTrue(post("jobs/create", Map.of("query", "1", "id", ID, "start", "PT1H",
          "service", "true")).contains("Service was registered."), "service not reported");
      services().unregister(ID);
      discard("job:remove('" + ID + "')");
      assertTrue(post("jobs/create", Map.of("query", "1", "id", ID, "start", "PT1H")).
          contains("Job was scheduled."), "scheduled job not reported");
      discard("job:remove('" + ID + "')");
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
   * Tests for the web sessions of the clients panel of the Activity view.
   */
  @Nested final class Sessions {
    /** Test attribute. */
    private static final String ATTRIBUTE = "dba-junit-attribute";
    /** Pattern that isolates the id of a session from its checkbox. */
    private static final Pattern SESSION = Pattern.compile("name=\"session\" value=\"(.*?)\"");

    /**
     * Removes the test attribute from all sessions after each test.
     * @throws Exception exception
     */
    @AfterEach public void cleanup() throws Exception {
      discard("sessions:ids() ! sessions:delete(., '" + ATTRIBUTE + "')");
    }

    /**
     * The attribute that holds the login is listed.
     * @throws Exception exception
     */
    @Test public void listed() throws Exception {
      assertTrue(get("activity").contains("data-name=\"dba\""),
          "login attribute missing from the panel");
    }

    /**
     * An attribute is assigned, listed and deleted.
     * @throws Exception exception
     */
    @Test public void assign() throws Exception {
      final String id = session();
      assertTrue(post("sessions/set", Map.of("id", id, "name", ATTRIBUTE, "value", "1 + 1")).
          contains("Attribute “" + ATTRIBUTE + "” was saved."), "attribute not assigned");
      assertTrue(get("activity").contains(ATTRIBUTE), "attribute missing from the panel");

      assertTrue(post("sessions/delete", Map.of("id", id, "name", ATTRIBUTE)).
          contains("Attribute “" + ATTRIBUTE + "” was deleted."), "attribute not deleted");
      assertFalse(get("activity").contains(ATTRIBUTE), "attribute still listed");
    }

    /**
     * The value of an attribute is served as the expression that yields it again.
     * @throws Exception exception
     */
    @Test public void value() throws Exception {
      final String id = session();
      post("sessions/set", Map.of("id", id, "name", ATTRIBUTE, "value", "'x', 1"));
      assertTrue(get("session-value?id=" + id + "&name=" + ATTRIBUTE).
          contains("(\\\"x\\\", 1)"), "value not served as an expression");
    }

    /**
     * A value that exceeds the maximum output size is left out, with a note.
     * @throws Exception exception
     */
    @Test public void largeValue() throws Exception {
      final String id = session();
      post("sessions/set", Map.of("id", id, "name", ATTRIBUTE,
          "value", "string-join((1 to 1100000) ! 'x')"));
      final String answer = get("session-value?id=" + id + "&name=" + ATTRIBUTE);
      assertTrue(answer.contains("\"text\":\"\""), "large value was served");
      assertTrue(answer.contains("too large"), "large value was served without a note");
    }

    /**
     * A session that holds no attribute is listed while it is in use, and can be closed.
     * @throws Exception exception
     */
    @Test public void empty() throws Exception {
      // the login is repeated with fresh cookies, which leaves the first session behind
      final String id = session();
      dropCookies();
      assertFalse(post("login", Map.of("_name", "admin", "_pass", NAME)).contains("_pass"),
          "second login failed");
      execute("sessions:delete('" + id + "', 'dba')");
      // it was accessed just now, so it is not idle
      final String page = get("activity");
      assertTrue(page.contains("value=\"" + id + "\""),
          "session that holds no attribute is not listed");
      assertTrue(page.contains("Idle (0)"), "session in use is counted as idle");
      assertTrue(post("clients/close", Map.of("session", id)).
          contains("Client “" + id + "” was closed."), "session not closed");
      assertFalse(sessions().contains(id), "closed session still listed");
    }

    /**
     * Returns the id of the session of the test client, which is the only one that is listed.
     * @return session id
     * @throws IOException I/O exception
     */
    private String session() throws IOException {
      final Set<String> ids = sessions();
      assertEquals(1, ids.size(), "unexpected number of sessions: " + ids);
      return ids.iterator().next();
    }

    /**
     * Returns the ids of the listed sessions.
     * @return session ids
     * @throws IOException I/O exception
     */
    private Set<String> sessions() throws IOException {
      final Set<String> ids = new HashSet<>();
      final Matcher matcher = SESSION.matcher(get("activity"));
      while(matcher.find()) ids.add(matcher.group(1));
      return ids;
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
     * The keys and values of a cache are shown.
     * @throws Exception exception
     */
    @Test public void entries() throws Exception {
      assertTrue(get("cache-keys?name=" + CACHE).contains("\"key\""), "key missing");
      assertTrue(get("cache-value?name=" + CACHE + "&key=key").contains("value"),
          "value missing");
    }

    /**
     * An entry is assigned, and several entries are removed at once.
     * @throws Exception exception
     */
    @Test public void putRemove() throws Exception {
      assertTrue(post("caches/put", Map.of("name", CACHE, "key", "k2", "value", "1 to 3")).
          contains("Entry “k2” was saved."), "entry not saved");
      assertTrue(get("cache-value?name=" + CACHE + "&key=k2").contains("(1, 2, 3)"),
          "value not assigned");
      assertEquals("2 entries were removed.",
          post("cache-remove?name=" + CACHE + "&key=key&key=k2", ""));
      assertTrue(get("cache-keys?name=" + CACHE).contains("\"count\":0"), "entries still listed");
    }

    /**
     * A cache is deleted.
     * @throws Exception exception
     */
    @Test public void delete() throws Exception {
      assertTrue(post("caches/delete", Map.of("cache", CACHE)).
          contains("Cache “" + CACHE + "” was deleted."), "cache not deleted");
      assertFalse(get("activity").contains(CACHE), "cache still listed");
    }

    /**
     * All caches are cleared.
     * @throws Exception exception
     */
    @Test public void clear() throws Exception {
      assertTrue(post("caches/clear", Map.of()).
          contains("All caches and their options were deleted."), "caches not cleared");
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
        Pattern.compile("data-select=\"([^\"]*)\"[^>]*class=\"selected\"");
    /** Start of the panel that lists the children of a level. */
    private static final String ENTRIES = "id=\"entries-panel\"";

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
     * A page with an invalid store name opens the default store.
     * @throws IOException I/O exception
     */
    @Test public void invalidName() throws IOException {
      assertTrue(get("stores?name=bad%20name").contains("Store: (default)"),
          "default store not opened");
    }

    /**
     * An entry is added to a store and removed again.
     * @throws IOException I/O exception
     */
    @Test public void addAndRemove() throws IOException {
      assertTrue(add("", false, "'key'", "'value'").contains("Entry “key” was added."),
          "entry not added");
      assertTrue(get("stores?name=" + STORE).contains("data-select=\"key\""),
          "entry missing from the list");
      assertTrue(post("stores/remove", Map.of("name", STORE, "path", "", "step", "key")).
          contains("Entry “key” was removed."), "entry not removed");
      assertFalse(get("stores?name=" + STORE).contains("data-select=\"key\""),
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
     * @throws Exception exception
     */
    @Test public void stringKey() throws Exception {
      add("", false, "'map'", "{ 'a': 1 }");
      assertTrue(add("map", false, "'abcde'", "'text'").contains("Entry “abcde” was added."),
          "child not added");

      final String level = get("stores?name=" + STORE + "&path=map");
      assertTrue(level.contains("data-select=\"abcde\""), "key not addressed by itself");
      assertFalse(level.contains("{&quot;pos&quot;"), "key addressed by its position");

      final String value = value("map.abcde");
      assertTrue(value.contains("\\\"text\\\""), "value not shown in the editor");
      assertFalse(value.contains("Read-only"), "value reported as read-only");
    }

    /**
     * An invalid key is rejected, and the selection stays on the level of the store.
     * @throws IOException I/O exception
     */
    @Test public void invalidKeyKeepsLevel() throws IOException {
      add("", false, "'map'", "{ 'a': 1 }");
      final String page = add("map", false, "1 +", "2");
      assertTrue(page.contains("Expecting expression"), "invalid key not rejected");
      assertTrue(page.contains("data-path=\"map\""), "selection did not stay on the level");
    }

    /**
     * An empty value is shown as such; a missing one is reported by the noun it lacks; the
     * members of an array are named by their position.
     * @throws Exception exception
     */
    @Test public void emptyValues() throws Exception {
      add("", false, "'map'", "{ 'none': (), 'list': [ 1 ] }");
      assertTrue(value("map.none").contains("empty sequence"), "empty value not shown");
      assertTrue(add("", false, " ", "1").contains("No key was supplied."),
          "missing key not reported");
      assertTrue(add("map", false, "'x'", " ").contains("No value was supplied."),
          "missing value not reported");
      final String level = get("stores?name=" + STORE + "&path=map.list");
      assertTrue(level.contains(">Position<") && level.contains("1 Member"),
          "members not named as such");
    }

    /**
     * A path that leads to a value without children lists the closest level that has some.
     * @throws IOException I/O exception
     */
    @Test public void scalarLevel() throws IOException {
      add("", false, "'map'", "{ 'a': 1 }");
      assertTrue(get("stores?name=" + STORE + "&path=map.a").contains("data-path=\"map\""),
          "parent level not listed");
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

      assertTrue(add("seq", true, "4", "4").contains("Entry “4” was added."), "child not added");
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

      assertTrue(add("array", true, "2", "'b'").contains("Entry “2” was added."),
          "member not added");
      assertTrue(get("stores?name=" + STORE + "&path=array").
          contains("name=\"step\" value=\"3\""), "member not appended");
    }

    /**
     * The entries are sorted ignoring case, and the first one is looked at if none is chosen.
     * @throws IOException I/O exception
     */
    @Test public void firstEntry() throws IOException {
      add("", false, "'Zoo'", "'z'");
      add("", false, "'apple'", "'a'");

      final String page = get("stores?name=" + STORE);
      assertTrue(page.indexOf("data-select=\"apple\"") < page.indexOf("data-select=\"Zoo\""),
          "entries not sorted, ignoring case");
      // the store list marks the chosen store in the same way, and comes first
      final Matcher matcher = SELECTED.matcher(page.substring(page.indexOf(ENTRIES)));
      assertTrue(matcher.find() && matcher.group(1).equals("apple"), "first entry not shown");
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
     * A store list that exceeds a page offers the entries that follow.
     * @throws Exception exception
     */
    @Test public void storePages() throws Exception {
      final String names = "(1 to 150) ! `" + STORE + "-{ format-integer(., '000') }`";
      execute(names + " ! store:put('key', 1, .)");
      try {
        assertTrue(get("stores").contains("data-more=\"2\""), "no further page offered");
      } finally {
        discard(names + " ! store:delete(.)");
      }
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
     * @throws Exception exception
     */
    @Test public void typedKey() throws Exception {
      add("", false, "'map'", "{}");
      assertTrue(add("map", false, "1", "'int'").contains("Entry “1” was added."),
          "child not added");
      assertTrue(value("map.1").contains("\\\"int\\\""), "integer key not addressed by its value");

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

    /**
     * Requests the value that a path of the test store leads to.
     * @param path path
     * @return message with the panel, the value and its edit state
     * @throws Exception exception
     */
    private static String value(final String path) throws Exception {
      connect("/stores");
      return StoresSocket.panel("{ \"type\": \"value\", \"name\": \"" + STORE +
          "\", \"path\": \"" + path + "\" }", "editor", "value-panel");
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
          "\", \"sort\": \"\", \"page\": 1 }", "panel", "stores-panel");
      assertTrue(message.contains(STORE), "store missing from the panel: " + message);
    }

    /**
     * The panel of a store lists its entries.
     * @throws Exception exception
     */
    @Test public void entriesPanel() throws Exception {
      final String message = panel("{ \"type\": \"entries\", \"name\": \"" + STORE +
          "\", \"path\": \"\", \"selected\": \"key\", \"sort\": \"\", \"page\": 1 }",
          "panel", "entries-panel");
      assertTrue(message.contains("key"), "entry missing from the panel: " + message);
    }

    /**
     * The value message carries the panel, the value and its edit state.
     * @throws Exception exception
     */
    @Test public void value() throws Exception {
      final String message = panel("{ \"type\": \"value\", \"name\": \"" + STORE +
          "\", \"path\": \"key\" }", "editor", "value-panel");
      assertTrue(message.contains("\"editable\":true"), "value not editable: " + message);
      assertTrue(message.contains("\\\"text\\\""), "value missing: " + message);
    }

    /**
     * A path that leads to no value answers with empty contents, which hides the panel.
     * @throws Exception exception
     */
    @Test public void emptyValue() throws Exception {
      final String message = panel("{ \"type\": \"value\", \"name\": \"" + STORE +
          "\", \"path\": \"none\" }", "editor", "value-panel");
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
     * @param id expected id of the panel
     * @return message
     * @throws Exception exception
     */
    private static String panel(final String message, final String type, final String id)
        throws Exception {
      sendMessage(message);
      final String answer = pollMessage();
      assertTrue(answer.startsWith("{\"type\":\"" + type + "\",\"id\":\"" + id + "\""),
          answer);
      return answer;
    }
  }

  /**
   * Tests for the WebSocket endpoint of the DBA users view, which serves the panels of the
   * user that is looked at.
   */
  @Nested final class UsersSocket {
    /** Test user. */
    private static final String USER = "dba-junit-socket";

    /**
     * Creates a user and opens the connection.
     * @throws Exception exception
     */
    @BeforeEach public void open() throws Exception {
      execute("user:create('" + USER + "', '', 'read')");
      connect("/users");
    }

    /**
     * Drops the test user.
     * @throws Exception exception
     */
    @AfterEach public void drop() throws Exception {
      discard("user:drop('" + USER + "')");
    }

    /**
     * The user list is pushed as the markup of its panel.
     * @throws Exception exception
     */
    @Test public void usersPanel() throws Exception {
      final String message = panel("{ \"type\": \"users\", \"name\": \"" + USER +
          "\", \"sort\": \"name\", \"page\": 1 }", "users-panel");
      assertTrue(message.contains(USER), "user missing from the panel: " + message);
    }

    /**
     * The panel of a user carries what is submitted for it.
     * @throws Exception exception
     */
    @Test public void userPanel() throws Exception {
      final String message = panel("{ \"type\": \"user\", \"name\": \"" + USER + "\" }",
          "user-panel");
      assertTrue(message.contains("User: " + USER), "user panel not served: " + message);
      assertTrue(message.contains("newname"), "name field missing: " + message);
    }

    /**
     * A user that is not selected answers with empty contents, which hides the panel.
     * @throws Exception exception
     */
    @Test public void emptyUser() throws Exception {
      assertEquals("{\"type\":\"panel\",\"id\":\"user-panel\",\"html\":\"\"}",
          panel("{ \"type\": \"user\", \"name\": \"\" }", "user-panel"));
    }

    /**
     * The admin has no local permissions to overwrite, so its panel offers none.
     * @throws Exception exception
     */
    @Test public void adminPermissions() throws Exception {
      final String message = panel("{ \"type\": \"user\", \"name\": \"admin\" }", "user-panel");
      assertFalse(message.contains("Per database:"), "local permissions offered: " + message);
    }

    /**
     * A local permission is listed in the user panel.
     * @throws Exception exception
     */
    @Test public void permissionsPanel() throws Exception {
      execute("user:grant('" + USER + "', 'write', 'dba-junit-*')");
      final String message = panel("{ \"type\": \"user\", \"name\": \"" + USER + "\" }",
          "user-panel");
      assertTrue(message.contains("dba-junit-*"), "pattern missing from the panel: " + message);
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
     * @param id expected id of the panel
     * @return message
     * @throws Exception exception
     */
    private static String panel(final String message, final String id) throws Exception {
      sendMessage(message);
      final String answer = pollMessage();
      assertTrue(answer.startsWith("{\"type\":\"panel\",\"id\":\"" + id + "\""), answer);
      return answer;
    }
  }
}
