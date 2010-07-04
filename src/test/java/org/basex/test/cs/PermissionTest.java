package org.basex.test.cs;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Session;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdSet;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateFS;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Find;
import org.basex.core.cmd.Grant;
import org.basex.core.cmd.Help;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.InfoTable;
import org.basex.core.cmd.Kill;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.Password;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.ShowUsers;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests user permissions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class PermissionTest {
  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session adminSession;
  /** Socket reference. */
  static Session testSession;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer();

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
    ok(new CreateDB("test", "<xml>This is a test</xml>"), adminSession);
    ok(new Close(), adminSession);
    ok(new Grant("none", "test"), adminSession);

    ok(new Set(CmdSet.QUERYINFO, false), testSession);
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
    no(new CreateDB("test", "<xml>This is a test</xml>"), testSession);
    no(new CreateFS("test", "c:/test"), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB("test"), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Grant("none", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where read permission is needed. */
  @Test
  public void readPermsNeeded() {
    ok(new Grant("read", "test"), adminSession);

    ok(new Open("test"), testSession);
    ok(new InfoDB(), testSession);
    ok(new InfoTable("1", "2"), testSession);
    // XQuery read
    ok(new XQuery("//xml"), testSession);
    ok(new Find("test"), testSession);
    // XQuery update
    no(new XQuery("for $item in fn:doc('test')//xml return rename" +
    " node $item as 'null'"), testSession);
    no(new Optimize(), testSession);
    no(new CreateDB("test", "<xml>This is a test</xml>"), testSession);
    no(new CreateFS("test", "c:/test"), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB("test"), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Grant("none", "test"), testSession);
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
    ok(new InfoIndex(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new DropIndex(cmd), testSession);
    }
    no(new CreateDB("test", "<xml>This is a test</xml>"), testSession);
    no(new CreateFS("test", "c:/test"), testSession);
    no(new DropDB("test"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Grant("none", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where create permission is needed. */
  @Test
  public void createPermsNeeded() {
    ok(new Grant("create", "test"), adminSession);

    ok(new Close(), testSession);
    ok(new CreateDB("test", "<xml>This is a test</xml>"), testSession);
    //ok(new CreateFS("bin", "fs"), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new DropDB("test"), testSession);
    no(new CreateUser("test", "test"), testSession);
    no(new DropUser("test"), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", "test"), testSession);
    no(new Grant("none", "test"), testSession);
    no(new AlterUser("test", "test"), testSession);
  }

  /** Tests all commands where admin permission is needed. */
  @Test
  public void adminPermsNeeded() {
    ok(new Grant("admin", "test"), adminSession);
    if(server.context.users.get("test2") != null) {
      ok(new DropUser("test2"), testSession);
    }
    ok(new CreateUser("test2", "test"), testSession);
    ok(new CreateDB("test", "<xml>This is a test</xml>"), testSession);
    ok(new ShowUsers(), testSession);
    ok(new Grant("admin", "test2"), testSession);
    ok(new Grant("create", "test2"), testSession);
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
   * @param cmd command reference
   * @param s session
   */
  private static void ok(final Command cmd, final Session s) {
    try {
      s.execute(cmd);
    } catch(final BaseXException ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Assumes that this command fails.
   * @param cmd command reference
   * @param s session
   */
  private static void no(final Command cmd, final Session s) {
    try {
      s.execute(cmd);
      fail("\"" + cmd + "\" was supposed to fail.");
    } catch(final BaseXException ex) {
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
    // stop server instance
    new BaseXServer("stop");
  }
}
