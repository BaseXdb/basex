package org.basex.test.server;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Text;
import org.basex.core.cmd.*;
import org.basex.core.Commands.*;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.basex.util.Token.*;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class CmdTest {
  /** Database context. */
  protected static final Context CONTEXT = new Context();
  /** Test file name. */
  private static final String FN = "input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/xml";
  /** Test file. */
  private static final String FILE = FLDR + '/' + FN;
  /** Test name. */
  private static final String NAME = Util.name(CmdTest.class);
  /** Test name. */
  private static final String NAME2 = NAME + "2";
  /** Socket reference. */
  static Session session;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    session = new LocalSession(CONTEXT);
  }

  /** Removes test databases and closes the database context. */
  @AfterClass
  public static void finish() {
    CONTEXT.close();
  }

  /**
   * Creates the database.
   * @throws BaseXException database exception
   */
  @Before
  public final void before() throws BaseXException {
    session.execute(new DropDB(NAME));
    session.execute(new DropDB(NAME2));
    session.execute(new DropUser(NAME));
    session.execute(new DropUser(NAME2));
  }

  /**
   * Creates the database.
   * @throws BaseXException database exception
   */
  @After
  public final void after() throws BaseXException {
    before();
  }

  /** Command test. */
  @Test
  public final void add() {
    // database must be opened to add files
    no(new Add(FILE));
    ok(new CreateDB(NAME));
    ok(new Add(FILE, FN));
    ok(new Add(FILE, FN, "target"));
    ok(new Add(FLDR, "xml"));
    no(new Add(FILE, "\\"));
    no(new Add(FILE, "/"));
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
    ok(new CreateUser(NAME2, NAME2));
    ok(new AlterUser(NAME2, "test"));
    no(new AlterUser(":", NAME2));
  }

  /** Command test. */
  @Test
  public final void close() {
    // close is successful, even if no database is opened
    ok(new Close());
    ok(new CreateDB(NAME, FILE));
    ok(new Close());
  }

  /** Command test. */
  @Test
  public final void createBackup() {
    no(new CreateBackup(NAME));
    ok(new CreateDB(NAME));
    ok(new CreateDB(NAME2));
    ok(new CreateBackup(NAME));
    ok(new Close());
    ok(new Restore(NAME));
    ok(new CreateBackup(NAME));
    ok(new DropBackup(NAME));
    ok(new CreateBackup(NAME + "*"));
    ok(new Restore(NAME2));
    ok(new DropBackup(NAME + "*"));
    no(new Restore(":"));
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
    ok(new CreateUser(NAME2, "test"));
    no(new CreateUser(NAME2, "test"));
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
    ok(CONTEXT.current, 2);
    ok(new Cs("."));
    ok(CONTEXT.current, 2);
    ok(new Cs("/"));
    ok(CONTEXT.current, 1);
  }

  /** Command test. */
  @Test
  public final void delete() {
    // database must be opened to add files
    no(new Delete(FILE));
    ok(new CreateDB(NAME));
    // target need not exist
    ok(new Delete(FILE));
    ok(new Add(FILE));
    ok(new Delete(FILE));
    // target need not exist
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
    ok(new CreateUser(NAME, "test"));
    ok(new CreateUser(NAME2, "test"));
    ok(new DropUser(NAME));
    ok(new DropUser(NAME + "*"));
    no(new DropUser(""));
    no(new DropUser(NAME2, ":"));
  }

  /** Command test. */
  @Test
  public final void export() {
    final IO io = IO.get(FN);
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
  public final void get() {
    ok(new Get(CmdSet.CHOP));
    no(new Get(NAME2));
  }

  /** Command test. */
  @Test
  public final void grant() {
    ok(new CreateUser(NAME2, "test"));
    no(new Grant("something", NAME2));
    ok(new Grant("none", NAME2));
    no(new Grant("all", NAME2));
    ok(new DropUser(NAME2));
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
  public final void kill() {
    no(new Kill("admin"));
    ok(new Kill("hans"));
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
    ok(new Password("admin"));
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
    ok(new Rename(FILE, "xxx"));
    // target need not exist
    ok(new Rename(FILE, "xxx"));
  }

  /** Command test. */
  @Test
  public final void replace() {
    // database must be opened to rename paths
    no(new Replace(FILE, "xxx"));
    ok(new CreateDB(NAME, FILE));
    ok(new Replace(FN, "<a/>"));
    ok(new Replace(FN, "<a/>"));
    no(new Replace(FN, ""));
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
    ok(new Close());
  }

  /** Command test. */
  @Test
  public final void run() {
    final IO io = IO.get("test.xq");
    no(new Run(io.path()));
    try {
      io.write(token("// li"));
    } catch(final Exception ex) {
      fail(ex.toString());
    }
    no(new Run(io.path()));
    ok(new CreateDB(NAME, FILE));
    ok(new Run(io.path()));
    io.delete();
  }

  /** Command test. */
  @Test
  public final void set() {
    ok(new Set(CmdSet.CHOP, Text.ON));
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
  public final void xQuery() {
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
   */
  private void ok(final Command cmd) {
    try {
      session.execute(cmd);
    } catch(final BaseXException ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Assumes that this command fails.
   * @param cmd command reference
   */
  private void no(final Command cmd) {
    try {
      session.execute(cmd);
      fail("\"" + cmd + "\" was supposed to fail.");
    } catch(final BaseXException ex) {
    }
  }
}
