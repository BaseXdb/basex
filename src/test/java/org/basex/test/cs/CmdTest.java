package org.basex.test.cs;

import org.basex.core.Session;
import org.basex.core.Context;
import org.basex.core.LocalSession;
import org.basex.core.Proc;
import org.basex.core.Text;
import org.basex.core.proc.*;
import org.basex.core.Commands.*;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.io.NullOutput;
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
    process(new DropDB(NAME));
    process(new DropUser(USER));
  }

  /** Command Test. */
  @Test
  public final void add() {
    no(new Add(FILE));
    ok(new CreateColl(NAME));
    ok(new Add(FILE));
    no(new Add(FILE));
  }

  /** Command test. */
  @Test
  public final void close() {
    ok(new Close());
    ok(new CreateDB(FILE));
    ok(new Close());
    no(new InfoDB());
  }

  /** Command Test. */
  @Test
  public final void createColl() {
    ok(new CreateColl(NAME));
    ok(new CreateColl(NAME));
    ok(new DropDB(NAME));
  }

  /** Command Test. */
  @Test
  public final void createDB() {
    ok(new CreateDB(FILE));
    ok(new InfoDB());
    ok(new CreateDB(FILE, FILE));
    no(new CreateDB("abcde"));
    no(new CreateDB(""));
  }

  /** Command Test. */
  @Test
  public final void createFS() {
    no(new CreateFS(".s", "test"));
    ok(new CreateFS("src/test", "test"));
    ok(new DropDB("test"));
  }

  /** Command Test. */
  @Test
  public final void createIndex() {
    no(new CreateIndex(null));
    for(final CmdIndex cmd : CmdIndex.values()) no(new CreateIndex(cmd));
    ok(new CreateDB(FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new CreateIndex(cmd));
  }

  /** Command Test. */
  @Test
  public final void createMAB() {
    // no test file available
    no(new CreateMAB("abcde", "abcde"));
  }

  /** Command Test. */
  @Test
  public final void createUser() {
    ok(new CreateUser(USER, "test"));
    no(new CreateUser(USER, "test"));
    ok(new DropUser(USER));
  }

  /** Command Test. */
  @Test
  public final void cs() {
    no(new Cs("//li"));
    ok(new CreateDB(FILE));
    ok(new Cs("//  li"));
    ok(CONTEXT.current, 2);
    ok(new Cs("."));
    ok(CONTEXT.current, 2);
    ok(new Cs("/"));
    ok(CONTEXT.current, 1);
  }

  /** Command Test. */
  @Test
  public final void delete() {
    no(new Delete(FILE));
    ok(new CreateColl(NAME));
    no(new Delete(FILE));
    ok(new Add(FILE));
    ok(new Delete(FILE));
    no(new Delete(FILE));
  }

  /** Command Test. */
  @Test
  public final void dropDB() {
    no(new DropDB(NAME));
    ok(new CreateDB(FILE));
    no(new DropDB(FILE));
    ok(new DropDB(NAME));
    no(new DropDB(NAME));
  }

  /** Command Test. */
  @Test
  public final void dropIndex() {
    for(final CmdIndex cmd : CmdIndex.values()) no(new DropIndex(cmd));
    ok(new CreateDB(FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new DropIndex(cmd));
  }

  /** Command Test. */
  @Test
  public final void dropUser() {
    ok(new CreateUser(USER, "test"));
    ok(new DropUser(USER));
    no(new DropUser(USER));
  }

  /** Command Test. */
  @Test
  public final void export() {
    final IO io = IO.get("export.xml");
    no(new Export(io.path()));
    ok(new CreateDB(FILE));
    ok(new Export(".", io.name()));
    ok(io.exists());
    ok(io.delete());
  }

  /** Command Test. */
  @Test
  public final void find() {
    no(new Find("1"));
    ok(new CreateDB(FILE));
    ok(new Find("1"));
  }

  /** Command Test. */
  @Test
  public final void grant() {
    ok(new CreateUser(USER, "test"));
    no(new Grant("something", USER));
    ok(new Grant("none", USER));
    no(new Grant("all", USER));
    ok(new DropUser(USER));
  }

  /** Command Test. */
  @Test
  public final void help() {
    ok(new Help(""));
    ok(new Help(null));
  }

  /** Command Test. */
  @Test
  public final void info() {
    ok(new Info());
  }

  /** Command Test. */
  @Test
  public final void infoDB() {
    no(new InfoDB());
    ok(new CreateDB(FILE));
    ok(new InfoDB());
  }

  /** Command Test. */
  @Test
  public final void infoIndex() {
    no(new InfoIndex());
    ok(new CreateDB(FILE));
    ok(new InfoIndex());
  }

  /** Command Test. */
  @Test
  public final void infoTable() {
    no(new InfoTable("1", "2"));
    ok(new CreateDB(FILE));
    ok(new InfoTable("1", "2"));
    ok(new InfoTable("1", null));
    ok(new InfoTable("// li", null));
  }

  /** Command Test. */
  @Test
  public final void kill() {
    ok(new Kill("hans"));
    no(new Kill("admin"));
  }

  /** Command Test. */
  @Test
  public final void list() {
    ok(new List());
    ok(new CreateDB(FILE));
    ok(new List());
  }

  /** Command Test. */
  @Test
  public final void open() {
    no(new Open(NAME));
    ok(new CreateDB(FILE));
    ok(new Open(NAME));
    ok(new Open(NAME));
  }

  /** Command Test. */
  @Test
  public final void optimize() {
    no(new Optimize());
    ok(new CreateDB(FILE));
    ok(new Optimize());
  }

  /** Command Test. */
  @Test
  public final void password() {
    ok(new Password("admin"));
    no(new Password(""));
  }

  /** Command Test. */
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
    ok(new CreateDB(FILE));
    ok(new Run(io.path()));
    io.delete();
  }

  /** Command Test. */
  @Test
  public final void set() {
    ok(new Set(CmdSet.INFO, Text.ON));
    ok(new Set(CmdSet.INFO, false));
    ok(new Set(CmdSet.CHOP, true));
    ok(new Set("runs", 1));
    no(new Set("runs", true));
    no(new Set(USER, USER));
  }

  /** Command Test. */
  @Test
  public final void xQuery() {
    no(new XQuery("/"));
    ok(new CreateDB(FILE));
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
   * @param pr process reference
   */
  private void ok(final Proc pr) {
    final String msg = process(pr);
    if(msg != null) fail(msg);
  }

  /**
   * Assumes that this command fails.
   * @param pr process reference
   */
  private void no(final Proc pr) {
    ok(process(pr) != null);
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @return success flag
   */
  private String process(final Proc pr) {
    try {
      return session.execute(pr, new NullOutput()) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }
}
