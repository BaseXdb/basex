package org.basex.core;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class CommandTest extends SandboxTest {
  /** Test file name. */
  private static final String FN = "input.xml";
  /** Test folder. */
  private static final String FOLDER = "src/test/resources/";
  /** Test file. */
  private static final String FILE = FOLDER + FN;
  /** Test name. */
  static final String NAME2 = NAME + '2';
  /** Admin. */
  static final String ADMIN = "admin";
  /** Socket reference. */
  static Session session;

  /** Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void start() throws IOException {
    session = new LocalSession(context);
    cleanUp();
  }

  /**
   * Deletes the potentially already existing DBs.
   * DBs & User {@link #NAME} and {@link #NAME2}
   * @throws IOException I/O exception
   */
  static void cleanUp() throws IOException {
    session.execute(new DropBackup(NAME));
    session.execute(new DropBackup(NAME2));
    session.execute(new DropDB(NAME));
    session.execute(new DropDB(NAME2));
    session.execute(new DropUser(NAME));
    session.execute(new DropUser(NAME2));
  }

  /**
   * Creates the database.
   * @throws IOException I/O exception
   */
  @AfterEach public final void after() throws IOException {
    cleanUp();
  }

  /** Command test. */
  @Test public final void add() {
    // database must be opened to add files
    no(new Add("", FILE));
    ok(new CreateDB(NAME));
    ok(new Add(FN, FILE));
    ok(new Add("target/" + FN, FILE));
    ok(new Add("/", FILE));
    ok(new Add("test.xml", "<x>te est</x>"));
  }

  /** Create Backup Test.
   * Using glob Syntax. */
  @Test public final void alterBackup() {
    no(new AlterBackup("unknown", NAME));

    ok(new CreateDB(NAME));
    ok(new Close());
    ok(new CreateBackup(NAME));

    ok(new AlterBackup(NAME, NAME2));
    ok(new AlterBackup(NAME2, NAME));
    no(new AlterBackup(NAME2, NAME));
    no(new AlterBackup(NAME2, "?"));
    no(new AlterBackup("?", NAME2));
    ok(new DropBackup(NAME));
  }

  /** Command test. */
  @Test public final void alterDB() {
    no(new AlterDB("unknown", NAME));

    ok(new CreateDB(NAME));
    ok(new AlterDB(NAME, NAME2));
    ok(new CreateDB(NAME));
    ok(new Close());
    no(new AlterDB(NAME, NAME2));
    no(new AlterDB(NAME, NAME2));
    no(new AlterDB(NAME2, "?"));
    no(new AlterDB("?", NAME2));
  }

  /** Command test. */
  @Test public final void alterPassword() {
    ok(new CreateUser(NAME, NAME));
    ok(new AlterPassword(NAME, "test"));
    no(new AlterPassword(":", NAME));
    no(new AlterPassword("unknown", NAME));
  }

  /** Command test. */
  @Test public final void alterUser() {
    // admin cannot be renamed
    no(new AlterUser(ADMIN, NAME));
    no(new AlterUser(NAME, ADMIN));
    // invalid name
    no(new AlterUser(":", NAME));
    no(new AlterUser(NAME, ":"));
    // unknown user
    no(new AlterUser("unknown", NAME));

    // create and rename user
    ok(new CreateUser(NAME, NAME));
    ok(new AlterUser(NAME, NAME2));
    // rename to existing user
    ok(new CreateUser(NAME, NAME));
    no(new AlterUser(NAME, NAME2));
  }

  /** Command test. */
  @Test public final void close() {
    // close is successful, even if no database is opened
    ok(new Close());
    ok(new CreateDB(NAME, FILE));
    ok(new Close());
  }

  /** Create Backup Test.
   * Using glob Syntax. */
  @Test public final void createBackup() {
    no(new CreateBackup(NAME));
    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new CreateBackup(NAME));
    ok(new Restore(NAME));
    ok(new Close());
    ok(new Restore(NAME));
    ok(new CreateBackup(NAME));
    ok(new Restore(NAME));
    ok(new DropBackup(NAME));
    ok(new CreateBackup(NAME + '*'));
    ok(new Restore(NAME2));
    ok(new DropBackup(NAME + '*'));
    no(new Restore(":"));
    ok(new CreateBackup(NAME + "?," + NAME));
    ok(new DropBackup(NAME2));
    ok(new Restore(NAME));
    no(new Restore(NAME + '?'));
    ok(new DropBackup(NAME));
  }

  /** Command test. */
  @Test public final void createDB() {
    ok(new CreateDB(NAME, FILE));
    ok(new InfoDB());
    ok(new CreateDB(NAME, FILE));
    ok(new CreateDB(NAME, "<x>te est</x>"));
    ok(new CreateDB("abcde"));
    ok(new DropDB("abcde"));
    ok(new CreateDB("a.b"));
    ok(new DropDB("a.b"));

    // invalid database names
    no(new CreateDB(""));
    no(new CreateDB(" "));
    no(new CreateDB(":"));
    no(new CreateDB("*?"));
    no(new CreateDB("/"));
    no(new CreateDB(".x"));
    no(new CreateDB("x."));
  }

  /** Command test. */
  @Test public final void createIndex() {
    no(new CreateIndex(null));
    for(final CmdIndex cmd : CmdIndex.values()) no(new CreateIndex(cmd));
    ok(new CreateDB(NAME, FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new CreateIndex(cmd));
    no(new CreateIndex("x"));
  }

  /** Command test. */
  @Test public final void createUser() {
    ok(new CreateUser(NAME2, "test"));
    ok(new CreateUser(NAME2, "test"));
    ok(new DropUser(NAME2));
    no(new CreateUser("", ""));
    no(new CreateUser(":", ""));

    ok(new CreateUser(Databases.DBCHARS, ""));
    ok(new DropUser(Databases.DBCHARS));
  }

  /** Command test. */
  @Test public final void delete() {
    // database must be opened to add and delete files
    no(new Delete(FILE));
    ok(new CreateDB(NAME));
    ok(new Delete(FILE));
    ok(new Add("", FILE));
    ok(new Delete(FILE));
    // target need not exist
    ok(new Delete(FILE));
    // delete binary file
    ok(new Store(FN, FILE));
    ok(new Delete(FN));
    ok(new Delete(FN));
  }

  /** Command test. */
  @Test public final void dropDB() {
    ok(new DropDB(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new DropDB(NAME2));
    ok(new DropDB(NAME));
    ok(new DropDB(NAME));
    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new DropDB(NAME + '*'));
    no(new Open(NAME2));
    no(new DropDB(":"));
    no(new DropDB(""));

    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new DropDB(NAME + ',' + NAME2));
    no(new DropDB(NAME + ", " + ':'));
  }

  /** Command test. */
  @Test public final void dropIndex() {
    for(final CmdIndex cmd : CmdIndex.values()) no(new DropIndex(cmd));
    ok(new CreateDB(NAME, FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new DropIndex(cmd));
    no(new DropIndex("x"));
  }

  /** Command test. */
  @Test public final void dropUser() {
    ok(new CreateUser(NAME, NAME));
    ok(new CreateUser(NAME2, NAME));

    ok(new DropUser(NAME));
    ok(new DropUser(NAME2));
    no(new DropUser(""));
    no(new DropUser(NAME2, ":"));

    ok(new CreateDB(NAME));
    ok(new CreateUser(NAME, NAME));
    ok(new CreateUser(NAME2, NAME));
    ok(new DropUser(NAME2, NAME + '*'));
    ok(new DropUser(NAME + ',' + NAME2));
  }

  /** Command test. */
  @Test public final void export() {
    final IOFile io = new IOFile(FN);
    no(new Export(io.path()));
    ok(new CreateDB(NAME, FILE));
    ok(new Export("."));
    ok(new Export("."));
    ok(io.exists());
    ok(io.delete());
  }

  /** Command test. */
  @Test public final void find() {
    no(new Find("1"));
    ok(new CreateDB(NAME, FILE));
    ok(new Find("1"));
  }

  /** Command test. */
  @Test public final void flush() {
    no(new Flush());
    ok(new CreateDB(NAME));
    ok(new Flush());
    ok(new Close());
    no(new Flush());
  }

  /** Command test. */
  @Test public final void get() {
    ok(new Get());
    ok(new Get(MainOptions.CHOP));
    ok(new Get(MainOptions.TOKENINCLUDE));
    no(new Get(NAME2));
  }

  /** Command test. */
  @Test public final void grant() {
    ok(new CreateUser(NAME2, "test"));
    ok(new CreateUser(NAME, "test"));
    no(new Grant("something", NAME2));
    ok(new CreateDB(NAME));
    ok(new Grant("none", NAME + '*', NAME + '*'));
    no(new Grant("all", NAME2));
    no(new Grant("all", ":*?", ":*:"));
    ok(new DropUser(NAME + ',' + NAME2));
    no(new Grant("all", NAME));
    no(new Grant("all", NAME + '*', ":"));
  }

  /** Command test. */
  @Test public final void help() {
    no(new Help("bla"));
    ok(new Help(null));
  }

  /** Command test. */
  @Test public final void info() {
    ok(new Info());
  }

  /** Command test. */
  @Test public final void infoDB() {
    no(new InfoDB());
    ok(new CreateDB(NAME, FILE));
    ok(new InfoDB());
  }

  /** Command test. */
  @Test public final void infoIndex() {
    no(new InfoIndex());
    ok(new CreateDB(NAME, FILE));
    ok(new InfoIndex());
    no(new InfoIndex("x"));
  }

  /** Command test. */
  @Test public final void infoTable() {
    no(new InfoStorage("1", "2"));
    ok(new CreateDB(NAME, FILE));
    ok(new InfoStorage());
    ok(new InfoStorage("1", "2"));
    ok(new InfoStorage("1", null));
  }

  /** Command test. */
  @Test public final void list() {
    ok(new List());
    ok(new CreateDB(NAME, FILE));
    ok(new List());
  }

  /** Command test. */
  @Test public final void listdb() {
    no(new List(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new List(NAME));
  }

  /** Command test. */
  @Test public final void open() {
    no(new Open(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new Open(NAME));
    ok(new Open(NAME));
    ok(new Open(NAME, FILE));
    ok(new Open(NAME, "abc"));
    no(new Open(":"));
  }

  /** Command test. */
  @Test public final void optimize() {
    no(new Optimize());
    no(new OptimizeAll());
    ok(new CreateDB(NAME, FILE));
    ok(new Optimize());
    ok(new Optimize());
    ok(new OptimizeAll());
  }

  /** Command test. */
  @Test public final void password() {
    ok(new Password(UserText.ADMIN));
  }

  /** Command test. */
  @Test public final void rename() {
    // database must be opened to rename paths
    no(new Rename(FILE, "xxx"));
    ok(new CreateDB(NAME, FILE));
    // target path must not be empty
    no(new Rename(FN, "."));
    no(new Rename(FN, ".."));
    no(new Rename(FN, "/"));
    no(new Rename(FN, ""));
    ok(new Rename(FILE, FILE));
    ok(new Rename(FILE, "xxx"));
    // source need not exist
    ok(new Rename(FILE, "xxx"));

    // check leading and trailing slashes
    ok(new CreateDB(NAME));
    ok(new Add("x.xml", "<x/>"));
    ok(new Rename("x.xml", "y.xml"));
    assertEquals('/' + NAME + "/y.xml", ok(new XQuery("base-uri(.)")));
    ok(new Rename("/", "a/"));
    assertEquals('/' + NAME + "/a/y.xml", ok(new XQuery("base-uri(.)")));
    ok(new Rename("a/", "/"));
    assertEquals('/' + NAME + "/y.xml", ok(new XQuery("base-uri(.)")));
    ok(new Rename("/", "a"));
    assertEquals('/' + NAME + "/a/y.xml", ok(new XQuery("base-uri(.)")));
    ok(new Rename("a", "/"));
    assertEquals('/' + NAME + "/y.xml", ok(new XQuery("base-uri(.)")));
  }

  /** Command test. */
  @Test public final void replace() {
    // query to count number of documents
    final String count = COUNT.args(_DB_OPEN.args(NAME));
    // database must be opened to replace resources
    no(new Replace(FILE, "xxx"));
    ok(new CreateDB(NAME, FILE));
    assertEquals("1", ok(new XQuery(count)));
    // replace existing document
    ok(new Replace(FN, "<a/>"));
    assertEquals("1", ok(new XQuery(count)));
    // replace existing document (again)
    ok(new Replace(FN, "<a/>"));
    assertEquals("1", ok(new XQuery(count)));
    // invalid content
    no(new Replace(FN, ""));
    assertEquals("1", ok(new XQuery(count)));
    // invalid paths
    no(new Replace(".", "<a/>"));
    no(new Replace("..", "<a/>"));
    no(new Replace("/", "<a/>"));
    // create and replace binary file
    ok(new XQuery(_DB_STORE.args(NAME, "a", "a")));
    ok(new Replace("a", "<b/>"));
    assertFalse(ok(new XQuery(_DB_OPEN.args(NAME))).isEmpty());
    ok(new XQuery(_DB_RETRIEVE.args(NAME, "a")));
    // a failing replace should not remove existing documents
    no(new Replace(FN, "<a>"));
    assertEquals("1", ok(new XQuery(count)));
  }

  /** Command test. */
  @Test public final void restore() {
    no(new Restore(NAME));
    ok(new CreateDB(NAME));
    ok(new CreateBackup(NAME));
    ok(new Restore(NAME));
    no(new Restore(":"));
    ok(new DropBackup(NAME));
    no(new Restore(NAME));
    ok(new Open(NAME));
    no(new Restore(NAME));
    ok(new XQuery("."));
    ok(new CreateDB("test-1"));
    ok(new CreateBackup("test-1"));
    ok(new Restore("test-1"));
    ok(new DropBackup("test"));
    no(new Restore("test"));
    ok(new DropBackup("test-1"));
    ok(new DropDB("test-1"));

    ok(new CreateDB(Databases.DBCHARS));
    no(new Restore(Databases.DBCHARS));
    ok(new CreateBackup(Databases.DBCHARS));
    ok(new Restore(Databases.DBCHARS));
    ok(new DropBackup(Databases.DBCHARS));
    ok(new DropDB(Databases.DBCHARS));
  }

  /**
   * Dropping backups.
   */
  @Test public final void dropBackup() {
    // dropping a backup with db name as argument
    ok(new CreateDB(NAME));
    ok(new CreateBackup(NAME));
    ok(new DropBackup(NAME));

    /* Creates 2 dbs: one with a short name (1), the other with a
     * longer name (2). (1) is a prefix of (2). Tests then, whether
     * backups of both dbs are deleted, when we drop backups of (1). */
    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new CreateBackup(NAME));
    ok(new CreateBackup(NAME2));
    ok(new DropBackup(NAME));
  }

  /** Retrieves raw data. */
  @Test public final void retrieve() {
    ok(new CreateDB(NAME));
    // retrieve non-existing file
    no(new Retrieve(NAME2));
    // retrieve existing file
    ok(new Store(NAME2, FILE));
    ok(new Retrieve(NAME2));
  }

  /** Stores raw data. */
  @Test public final void store() {
    ok(new CreateDB(NAME));
    ok(new Store(NAME2, FILE));
    // file can be overwritten
    ok(new Store(NAME2, FILE));
    // adopt name from specified file
    ok(new Store("", FILE));
    // reject invalid or missing names
    no(new Store("", "</a>"));
    no(new Store("../x", FILE));
  }

  /**
   * Command test.
   * @throws IOException I/O exception
   */
  @Test public final void run() throws IOException {
    // test xquery
    IOFile io = new IOFile("test.xq");
    no(new Run(io.path()));
    io.write("// li");
    no(new Run(io.path()));
    ok(new CreateDB(NAME, FILE));
    ok(new Run(io.path()));
    io.delete();
    // test command script (1)
    io = new IOFile("test.bxs");
    io.write("<info/>");
    ok(new Run(io.path()));
    // test command script (2)
    io = new IOFile("test.bxs");
    io.write("</>");
    no(new Run(io.path()));
    io.delete();
  }

  /** Command test. */
  @Test public final void execute() {
    ok(new Execute(new CreateDB(NAME, FILE).toString()));
    ok(new XQuery("//li"));
    ok(new Execute("<info/>"));
    ok(new Execute("<commands><info/><drop-db name='" + NAME + "'/></commands>"));
    no(new XQuery("//li"));

    // pass on command instances
    ok(new Execute(new Info()));
    ok(new Execute(new Info(), new Info()));
  }

  /** Command test. */
  @Test public final void set() {
    ok(new Set(MainOptions.CHOP, false));
    ok(new Set(MainOptions.CHOP, true));
    ok(new Set("chop", true));
    ok(new Set("runs", 1));
    ok(new Set(MainOptions.TOKENINCLUDE, "id"));
    ok(new Set(MainOptions.TOKENINCLUDE, ""));
    no(new Set("runs", true));
    no(new Set(NAME2, NAME2));
  }

  /** Command test. */
  @Test public final void jobsList() {
    ok(new JobsList());
  }

  /** Command test. */
  @Test public final void jobsResult() {
    no(new JobsResult("job1"));
  }

  /** Command test. */
  @Test public final void jobsStop() {
    ok(new JobsStop("job1"));
  }

  /** Command test. */
  @Test public final void showUsers() {
    ok(new ShowUsers());
    ok(new ShowUsers(NAME));
    ok(new CreateDB(NAME));
    ok(new ShowUsers(NAME));
    no(new ShowUsers(":"));
  }

  /** Command test. */
  @Test public final void xquery() {
    no(new XQuery("/"));
    ok(new CreateDB(NAME, FILE));
    ok(new XQuery("/"));
    ok(new XQuery("1"));
    no(new XQuery("1+"));
  }

  /** Command test. */
  @Test public final void test() {
    no(new org.basex.core.cmd.Test("sfsdssdf"));
    no(new org.basex.core.cmd.Test(FOLDER + "tests.xqm"));
    ok(new org.basex.core.cmd.Test(FOLDER + "tests-ok.xqm"));
  }

  /**
   * Assumes that the specified flag is successful.
   * @param flag flag
   */
  private static void ok(final boolean flag) {
    assertTrue(flag);
  }

  /**
   * Assumes that this command is successful.
   * @param cmd command reference
   * @return result as string or {@code null}
   */
  static String ok(final Command cmd) {
    try {
      return session.execute(cmd);
    } catch(final IOException ex) {
      fail(Util.message(ex));
      return null;
    }
  }

  /**
   * Assumes that this command fails.
   * @param cmd command reference
   */
  private static void no(final Command cmd) {
    try {
      session.execute(cmd);
      fail("\"" + cmd + "\" was supposed to fail.");
    } catch(final IOException ex) {
      /* expected */
    }
  }
}
