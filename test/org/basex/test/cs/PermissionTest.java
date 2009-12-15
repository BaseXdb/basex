package org.basex.test.cs;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;
import org.basex.BaseXServer;
import org.basex.core.Proc;
import org.basex.core.Session;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdSet;
import org.basex.core.proc.AlterUser;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateFS;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.CreateUser;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.DropUser;
import org.basex.core.proc.Exit;
import org.basex.core.proc.Export;
import org.basex.core.proc.Find;
import org.basex.core.proc.Grant;
import org.basex.core.proc.Help;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.InfoIndex;
import org.basex.core.proc.InfoTable;
import org.basex.core.proc.Kill;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.Password;
import org.basex.core.proc.Revoke;
import org.basex.core.proc.Set;
import org.basex.core.proc.Show;
import org.basex.core.proc.XQuery;
import org.basex.io.NullOutput;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests user permissions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class PermissionTest {
  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session adminSession;
  /** Socket reference. */
  static Session testSession;
  /** Export file. */
  final String export = "export.xml";

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    new Thread() {
      @Override
      public void run() {
        server = new BaseXServer();
      }
    }.start();

    // wait for server to be started
    Performance.sleep(200);

    try {
      adminSession = new ClientSession(server.context, ADMIN, ADMIN);
      if(server.context.users.get("test") != null) {
        ok(new DropUser("test"), adminSession);
      }
      ok(new CreateUser("test", "test"), adminSession);
      testSession = new ClientSession(server.context, "test", "test");
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }

  /** Tests all commands where no permission is needed. */
  @Test
  public void noPermsNeeded() {
    ok(new CreateDB("<xml>This is a test</xml>", "test"), adminSession);
    ok(new Close(), adminSession);
    ok(new Revoke("all", "test"), adminSession);

    ok(new Set(CmdSet.INFO, false), testSession);
    ok(new Password("test"), testSession);
    ok(new Help("list"), testSession);
    ok(new Close(), testSession);
    ok(new List(), testSession);
    no(new Open("test"), testSession);
    no(new InfoDB(), testSession);
    no(new InfoIndex(), testSession);
    no(new InfoTable(), testSession);
    // XQuery read
    no(new XQuery("//xml"), testSession);
    no(new Find("test"), testSession);
    no(new Optimize(), testSession);
    // XQuery update
    no(new XQuery("for $item in fn:doc('test')//xml return rename" +
    " node $item as 'null'"), testSession);
    no(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    no(new CreateFS("c:/test", "test"), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB("test"), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export("c:/test"), testSession);
    no(new Kill("dada"), testSession);
    no(new Show("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Revoke("read", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where read permission is needed. */
  @Test
  public void readPermsNeeded() {
    ok(new Grant("read", "test"), adminSession);
    ok(new Open("test"), testSession);
    ok(new InfoDB(), testSession);
    ok(new InfoIndex(), testSession);
    ok(new InfoTable("1", "2"), testSession);
    // XQuery read
    ok(new XQuery("//xml"), testSession);
    ok(new Find("test"), testSession);
    // XQuery update
    no(new XQuery("for $item in fn:doc('test')//xml return rename" +
    " node $item as 'null'"), testSession);
    no(new Optimize(), testSession);
    no(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    no(new CreateFS("c:/test", "test"), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB("test"), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export(".", export), testSession);
    no(new Kill("dada"), testSession);
    no(new Show("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Revoke("read", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where write permission is needed. */
  @Test
  public void writePermsNeeded() {
    ok(new Grant("write", "test"), adminSession);

    // XQuery Update
    ok(new XQuery("for $item in fn:doc('test')//xml return rename" +
        " node $item as 'null'"), testSession);
    ok(new Optimize(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
    ok(new CreateIndex(cmd), testSession);
    }
    for(final CmdIndex cmd : CmdIndex.values()) {
    ok(new DropIndex(cmd), testSession);
    }
    no(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    no(new CreateFS("c:/test", "test"), testSession);
    no(new DropDB("test"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export(".", export), testSession);
    no(new Kill("dada"), testSession);
    no(new Show("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Revoke("read", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where create permission is needed. */
  @Test
  public void createPermsNeeded() {
    ok(new Grant("create", "test"), adminSession);
    ok(new Close(), testSession);
    ok(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    //ok(new CreateFS("bin", "fs"), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new DropDB("test"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export(".", export), testSession);
    no(new Kill("dada"), testSession);
    no(new Show("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Revoke("read", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where admin permission is needed. */
  @Test
  public void adminPermsNeeded() {
    ok(new Grant("admin", "test"), adminSession);
    ok(new CreateUser("test2", "test"), testSession);
    ok(new CreateDB("<xml>This is a test</xml>", "test"), testSession);
    ok(new Export(".", export), testSession);
    ok(new Show("Users"), testSession);
    ok(new Grant("admin", "test2"), testSession);
    ok(new Revoke("admin", "test2"), testSession);
    ok(new AlterUser("test", "test"), testSession);
    ok(new DropUser("test2"), testSession);
    ok(new Close(), testSession);
    ok(new Close(), adminSession);
    ok(new DropDB("test"), adminSession);
  }

  /** Tests some usability stuff. */
  @Test
  public void use() {
    no(new DropUser("test"), testSession);
    no(new DropUser("test"), adminSession);
    ok(new Exit(), testSession);
    ok(new DropUser("test"), adminSession);
  }

  /**
   * Assumes that this command is successful.
   * @param pr process reference
   * @param s Session
   */
  static void ok(final Proc pr, final Session s) {
    final String msg = process(pr, s);
    if(msg != null) fail(msg);
  }

  /**
   * Assumes that this command fails.
   * @param pr process reference
   * @param s Session
   */
  private void no(final Proc pr, final Session s) {
    ok(process(pr, s) != null);
  }

  /**
   * Assumes that the specified flag is successful.
   * @param flag flag
   */
  private static void ok(final boolean flag) {
    assertTrue(flag);
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @param session Session
   * @return success flag
   */
  private static String process(final Proc pr, final Session session) {
    try {
      return session.execute(pr, new NullOutput()) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    try {
      adminSession.close();
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }

    // Stop server instance.
    new BaseXServer("stop");
  }
}
