package org.basex.test.server;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdSet;
import org.basex.core.Context;
import org.basex.core.Text;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.AlterDB;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateBackup;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Cs;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropBackup;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Find;
import org.basex.core.cmd.Flush;
import org.basex.core.cmd.Get;
import org.basex.core.cmd.Grant;
import org.basex.core.cmd.Help;
import org.basex.core.cmd.Info;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.InfoStorage;
import org.basex.core.cmd.List;
import org.basex.core.cmd.ListDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.OptimizeAll;
import org.basex.core.cmd.Password;
import org.basex.core.cmd.Rename;
import org.basex.core.cmd.Replace;
import org.basex.core.cmd.Restore;
import org.basex.core.cmd.Retrieve;
import org.basex.core.cmd.Run;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.ShowBackups;
import org.basex.core.cmd.ShowUsers;
import org.basex.core.cmd.Store;
import org.basex.core.cmd.XQuery;
import org.basex.data.Nodes;
import org.basex.io.IOFile;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class CommandTest {
  /** Database context. */
  protected static final Context CONTEXT = new Context();
  /** Test file name. */
  private static final String FN = "input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/test";
  /** Test file. */
  private static final String FILE = FLDR + '/' + FN;
  /** Test name. */
  private static final String NAME = Util.name(CommandTest.class);
  /** Test name. */
  protected static final String NAME2 = NAME + '2';
  /** Socket reference. */
  static Session session;

  /** Starts the server.
   * @throws IOException I/O exception
  */
  @BeforeClass
  public static void start() throws IOException {
    session = new LocalSession(CONTEXT);
    cleanUp();
  }

  /**
   * Deletes the potentially already existing DBs.
   * DBs & User {@link #NAME} and {@link #NAME2}
   * @throws IOException I/O exception
   */
  protected static void cleanUp() throws IOException {
    session.execute(new DropBackup(NAME));
    session.execute(new DropBackup(NAME2));
    session.execute(new DropDB(NAME));
    session.execute(new DropDB(NAME2));
    session.execute(new DropUser(NAME));
    session.execute(new DropUser(NAME2));
  }

  /** Removes test databases and closes the database context. */
  @AfterClass
  public static void finish() {
    CONTEXT.close();
  }

  /**
   * Creates the database.
   * @throws IOException I/O exception
   */
  @After
  public final void after() throws IOException {
    cleanUp();
  }

  /** Command test. */
  @Test
  public final void add() {
    // database must be opened to add files
    no(new Add("", FILE));
    ok(new CreateDB(NAME));
    ok(new Add(FN, FILE));
    ok(new Add("target/" + FN, FILE));
    ok(new Add("/", FILE));
  }

  /** Command test. */
  @Test
  public final void alterDB() {
    ok(new CreateDB(NAME));
    ok(new AlterDB(NAME, NAME2));
    ok(new Close());
    no(new AlterDB(NAME, NAME2));
    no(new AlterDB(NAME2, "!"));
    no(new AlterDB("!", NAME2));
  }

  /** Command test. */
  @Test
  public final void alterUser() {
    ok(new CreateUser(NAME2, md5(NAME2)));
    ok(new AlterUser(NAME2, md5("test")));
    no(new AlterUser(":", md5(NAME2)));
  }

  /** Command test. */
  @Test
  public final void close() {
    // close is successful, even if no database is opened
    ok(new Close());
    ok(new CreateDB(NAME, FILE));
    ok(new Close());
  }

  /** Create Backup Test.
   * Using glob Syntax. */
  @Test
  public final void createBackup() {
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
    ok(new CreateBackup(NAME + "*"));
    ok(new Restore(NAME2));
    ok(new DropBackup(NAME + "*"));
    no(new Restore(":"));
    ok(new CreateBackup(NAME + "?," + NAME));
    ok(new DropBackup(NAME2));
    ok(new Restore(NAME));
    no(new Restore(NAME + "?"));
    ok(new DropBackup(NAME));
  }

  /** Command test. */
  @Test
  public final void createDB() {
    ok(new CreateDB(NAME, FILE));
    ok(new InfoDB());
    ok(new CreateDB(NAME, FILE));
    ok(new CreateDB("abcde"));
    ok(new DropDB("abcde"));
    // invalid database names
    no(new CreateDB(""));
    no(new CreateDB(" "));
    no(new CreateDB(":"));
    no(new CreateDB("*?"));
    no(new CreateDB("/"));
  }

  /** Command test. */
  @Test
  public final void createIndex() {
    no(new CreateIndex(null));
    for(final CmdIndex cmd : CmdIndex.values()) no(new CreateIndex(cmd));
    ok(new CreateDB(NAME, FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new CreateIndex(cmd));
    no(new CreateIndex("x"));
  }

  /** Command test. */
  @Test
  public final void createUser() {
    ok(new CreateUser(NAME2, md5("test")));
    no(new CreateUser(NAME2, md5("test")));
    ok(new DropUser(NAME2));
    no(new CreateUser("", ""));
    no(new CreateUser(":", ""));
  }

  /** Command test. */
  @Test
  public final void cs() {
    no(new Cs("//li"));
    ok(new CreateDB(NAME, FILE));
    ok(new Cs("//  li"));
    ok(CONTEXT.current(), 2);
    ok(new Cs("."));
    ok(CONTEXT.current(), 2);
    ok(new Cs("/"));
    ok(CONTEXT.current(), 1);
  }

  /** Command test. */
  @Test
  public final void delete() {
    // database must be opened to add files
    no(new Delete(FILE));
    ok(new CreateDB(NAME));
    // target need not exist
    ok(new Delete(FILE));
    ok(new Add("", FILE));
    ok(new Delete(FILE));
    ok(new Delete(FILE));
  }

  /** Command test. */
  @Test
  public final void dropDB() {
    ok(new DropDB(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new DropDB(NAME2));
    ok(new DropDB(NAME));
    ok(new DropDB(NAME));
    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new DropDB(NAME + "*"));
    no(new Open(NAME2));
    no(new DropDB(":"));
    no(new DropDB(""));

    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new DropDB(NAME + "," + NAME2));
    no(new DropDB(NAME + ", " + ":"));
  }

  /** Command test. */
  @Test
  public final void dropIndex() {
    for(final CmdIndex cmd : CmdIndex.values()) no(new DropIndex(cmd));
    ok(new CreateDB(NAME, FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new DropIndex(cmd));
    no(new DropIndex("x"));
  }

  /** Command test. */
  @Test
  public final void dropUser() {
    ok(new CreateUser(NAME, md5(NAME)));
    ok(new CreateUser(NAME2, md5(NAME)));

    ok(new DropUser(NAME));
    ok(new DropUser(NAME2));
    no(new DropUser(""));
    no(new DropUser(NAME2, ":"));

    ok(new CreateDB(NAME));
    ok(new CreateUser(NAME, md5(NAME)));
    ok(new CreateUser(NAME2, md5(NAME)));
    ok(new DropUser(NAME2, NAME + "*"));
    ok(new DropUser(NAME + "," + NAME2));
  }

  /** Command test. */
  @Test
  public final void export() {
    final IOFile io = new IOFile(FN);
    no(new Export(io.path()));
    ok(new CreateDB(NAME, FILE));
    ok(new Export("."));
    ok(io.exists());
    ok(io.delete());
  }

  /** Command test. */
  @Test
  public final void find() {
    no(new Find("1"));
    ok(new CreateDB(NAME, FILE));
    ok(new Find("1"));
  }

  /** Command test. */
  @Test
  public final void flush() {
    no(new Flush());
    ok(new CreateDB(NAME));
    ok(new Flush());
    ok(new Close());
    no(new Flush());
  }

  /** Command test. */
  @Test
  public final void get() {
    ok(new Get(CmdSet.CHOP));
    no(new Get(NAME2));
  }

  /** Command test. */
  @Test
  public final void grant() {
    ok(new CreateUser(NAME2, md5("test")));
    ok(new CreateUser(NAME, md5("test")));
    no(new Grant("something", NAME2));
    ok(new CreateDB(NAME));
    ok(new Grant("none", NAME + "*", NAME + "*"));
    no(new Grant("all", NAME2));
    no(new Grant("all", ":*?", ":*:"));
    ok(new DropUser(NAME + "," + NAME2));
    no(new Grant("all", NAME));
    no(new Grant("all", NAME + "*", ":"));
  }

  /** Command test. */
  @Test
  public final void help() {
    no(new Help("bla"));
    ok(new Help(null));
  }

  /** Command test. */
  @Test
  public final void info() {
    ok(new Info());
  }

  /** Command test. */
  @Test
  public final void infoDB() {
    no(new InfoDB());
    ok(new CreateDB(NAME, FILE));
    ok(new InfoDB());
  }

  /** Command test. */
  @Test
  public final void infoIndex() {
    no(new InfoIndex());
    ok(new CreateDB(NAME, FILE));
    ok(new InfoIndex());
    no(new InfoIndex("x"));
  }

  /** Command test. */
  @Test
  public final void infoTable() {
    no(new InfoStorage("1", "2"));
    ok(new CreateDB(NAME, FILE));
    ok(new InfoStorage("1", "2"));
    ok(new InfoStorage("1", null));
    ok(new InfoStorage("// li", null));
  }

  /** Command test. */
  @Test
  public final void list() {
    ok(new List());
    ok(new CreateDB(NAME, FILE));
    ok(new List());
  }

  /** Command test. */
  @Test
  public final void listdb() {
    no(new ListDB(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new ListDB(NAME));
  }

  /** Command test. */
  @Test
  public final void open() {
    no(new Open(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new Open(NAME));
    ok(new Open(NAME));
    no(new Open(":"));
  }

  /** Command test. */
  @Test
  public final void optimize() {
    no(new Optimize());
    no(new OptimizeAll());
    ok(new CreateDB(NAME, FILE));
    ok(new Optimize());
    ok(new OptimizeAll());
  }

  /** Command test. */
  @Test
  public final void password() {
    ok(new Password(md5(Text.ADMIN)));
    no(new Password(""));
  }

  /** Command test. */
  @Test
  public final void rename() {
    // database must be opened to rename paths
    no(new Rename(FILE, "xxx"));
    ok(new CreateDB(NAME, FILE));
    // target path must not be empty
    no(new Rename(FN, "/"));
    no(new Rename(FN, ""));
    ok(new Rename(FILE, FILE));
    ok(new Rename(FILE, "xxx"));
    // source need not exist
    ok(new Rename(FILE, "xxx"));
  }

  /** Command test. */
  @Test
  public final void replace() {
    // query to count number of documents
    final String count = "count(db:open('" + NAME + "'))";
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
    // create and replace binary file
    ok(new XQuery("db:store('" + NAME + "', 'a', 'a')"));
    ok(new Replace("a", "<b/>"));
    assertTrue(ok(new XQuery("db:open('" + NAME + "')")).length() != 0);
    ok(new XQuery("db:retrieve('" + NAME + "', 'a')"));
    // a failing replace should not remove existing documents
    no(new Replace(FN, "<a>"));
    assertEquals("1", ok(new XQuery(count)));
  }

  /** Command test. */
  @Test
  public final void restore() {
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
    // deleting a backup passing the exact backup name as argument
    ok(new CreateDB(NAME));
    ok(new CreateBackup(NAME));
    ok(new DropBackup(ShowBackups.findBackups(NAME, CONTEXT).get(0)));
    assertEquals(0, ShowBackups.findBackups(NAME, CONTEXT).size());
  }

  /**
   * Dropping backups.
   */
  @Test
  public final void dropBackup() {
    // dropping a backup with db name as argument
    ok(new CreateDB(NAME));
    ok(new CreateBackup(NAME));
    ok(new DropBackup(NAME));

    // dropping a specific backup (database name + time stamp)
    // how to get my hands on the created backup name?
    ok(new CreateDB(NAME));
    ok(new CreateBackup(NAME));
    final String[] b = ShowBackups.findBackups(NAME, CONTEXT).toArray();
    ok(new DropBackup(b[0]));
    assertEquals(0, ShowBackups.findBackups(NAME, CONTEXT).size());

    /* Creates 2 dbs: one with a short name (1), the other with a
     * longer name (2). (1) is a prefix of (2). Tests then, whether
     * backups of both dbs are deleted, when we drop backups of (1).
     */
    System.out.println("now");
    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new CreateBackup(NAME));
    ok(new CreateBackup(NAME2));
    ok(new DropBackup(NAME));
    assertEquals(1, ShowBackups.findBackups(NAME2, CONTEXT).size());
  }

  /** Retrieves raw data. */
  @Test
  public final void retrieve() {
    ok(new CreateDB(NAME));
    // retrieve non-existing file
    no(new Retrieve(NAME2));
    // retrieve existing file
    ok(new Store(NAME2, FILE));
    ok(new Retrieve(NAME2));
  }

  /** Stores raw data. */
  @Test
  public final void store() {
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

  /** Command test. */
  @Test
  public final void run() {
    final IOFile io = new IOFile("test.xq");
    no(new Run(io.path()));
    try {
      io.write(token("// li"));
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
    no(new Run(io.path()));
    ok(new CreateDB(NAME, FILE));
    ok(new Run(io.path()));
    io.delete();
  }

  /** Command test. */
  @Test
  public final void set() {
    ok(new Set(CmdSet.CHOP, false));
    ok(new Set(CmdSet.CHOP, true));
    ok(new Set("chop", true));
    ok(new Set("runs", 1));
    no(new Set("runs", true));
    no(new Set(NAME2, NAME2));
  }

  /** Command test. */
  @Test
  public final void showUsers() {
    ok(new ShowUsers());
    no(new ShowUsers(NAME));
    ok(new CreateDB(NAME));
    ok(new ShowUsers(NAME));
    no(new ShowUsers(":"));
  }

  /** Command test. */
  @Test
  public final void xquery() {
    no(new XQuery("/"));
    ok(new CreateDB(NAME, FILE));
    ok(new XQuery("/"));
    ok(new XQuery("1"));
    no(new XQuery("1+"));
  }

  /**
   * Assumes that the specified flag is successful.
   * @param flag flag
   */
  private static void ok(final boolean flag) {
    assertTrue(flag);
  }

  /**
   * Assumes that the nodes have the specified number of nodes.
   * @param nodes context nodes
   * @param size expected size
   */
  private static void ok(final Nodes nodes, final int size) {
    if(nodes != null) assertEquals(size, nodes.size());
  }

  /**
   * Assumes that this command is successful.
   * @param cmd command reference
   * @return result as string
   */
  protected final String ok(final Command cmd) {
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
  protected final void no(final Command cmd) {
    try {
      session.execute(cmd);
      fail("\"" + cmd + "\" was supposed to fail.");
    } catch(final IOException ex) {
    }
  }
}
