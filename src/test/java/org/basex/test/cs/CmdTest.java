package org.basex.test.cs;

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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.basex.util.Token.*;

/**
 * This class tests the database commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class CmdTest {
  /** Database context. */
  protected static final Context CONTEXT = new Context();
  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/xml";
  /* Test url.
  private static final String URL =
    "http://www.inf.uni-konstanz.de/dbis/basex/dl/xml.xml";  */
  /** Test name. */
  private static final String NAME = "input";
  /** Test name. */
  private static final String USER = "cmdtest";
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
   */
  @After
  public final void setUp() {
    try {
      session.execute(new DropDB(NAME));
      session.execute(new DropUser(USER));
    } catch(final BaseXException ex) {
    }
  }

  /** Command test. */
  @Test
  public final void add() {
    // database must be opened to add files
    no(new Add(FILE));
    ok(new CreateDB(NAME));
    ok(new Add(FILE, "input"));
    ok(new Add(FILE, "input"));
    //ok(new Add(URL, "xml"));
    ok(new Add(FLDR, "xml"));
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
  public final void createDB() {
    ok(new CreateDB(NAME, FILE));
    ok(new InfoDB());
    ok(new CreateDB(NAME, FILE));
    ok(new CreateDB("abcde"));
  }

  /** Command test. */
  @Test
  public final void createFS() {
    no(new CreateFS("test", ".s"));
    ok(new CreateFS("test", ".settings"));
    ok(new DropDB("test"));
  }

  /** Command test. */
  @Test
  public final void createIndex() {
    no(new CreateIndex(null));
    for(final CmdIndex cmd : CmdIndex.values()) no(new CreateIndex(cmd));
    ok(new CreateDB(NAME, FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new CreateIndex(cmd));
  }

  /** Command test. */
  @Test
  public final void createMAB() {
    // no test file available
    no(new CreateMAB("abcde", "abcde"));
  }

  /** Command test. */
  @Test
  public final void createUser() {
    ok(new CreateUser(USER, "test"));
    no(new CreateUser(USER, "test"));
    ok(new DropUser(USER));
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
    ok(new DropDB(USER));
    ok(new DropDB(NAME));
    ok(new DropDB(NAME));
  }

  /** Command test. */
  @Test
  public final void dropIndex() {
    for(final CmdIndex cmd : CmdIndex.values()) no(new DropIndex(cmd));
    ok(new CreateDB(NAME, FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new DropIndex(cmd));
  }

  /** Command test. */
  @Test
  public final void dropUser() {
    ok(new CreateUser(USER, "test"));
    ok(new DropUser(USER));
    no(new DropUser(USER));
  }

  /** Command test. */
  @Test
  public final void export() {
    final IO io = IO.get("input.xml");
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
  public final void grant() {
    ok(new CreateUser(USER, "test"));
    no(new Grant("something", USER));
    ok(new Grant("none", USER));
    no(new Grant("all", USER));
    ok(new DropUser(USER));
  }

  /** Command test. */
  @Test
  public final void help() {
    ok(new Help(""));
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
  }

  /** Command test. */
  @Test
  public final void infoTable() {
    no(new InfoTable("1", "2"));
    ok(new CreateDB(NAME, FILE));
    ok(new InfoTable("1", "2"));
    ok(new InfoTable("1", null));
    ok(new InfoTable("// li", null));
  }

  /** Command test. */
  @Test
  public final void kill() {
    ok(new Kill("hans"));
    no(new Kill("admin"));
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
  public final void open() {
    no(new Open(NAME));
    ok(new CreateDB(NAME, FILE));
    ok(new Open(NAME));
    ok(new Open(NAME));
  }

  /** Command test. */
  @Test
  public final void optimize() {
    no(new Optimize());
    ok(new CreateDB(NAME, FILE));
    ok(new Optimize());
  }

  /** Command test. */
  @Test
  public final void password() {
    ok(new Password("admin"));
    no(new Password(""));
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
    no(new Set(USER, USER));
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
