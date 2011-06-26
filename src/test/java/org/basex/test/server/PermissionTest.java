package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdSet;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Find;
import org.basex.core.cmd.Get;
import org.basex.core.cmd.Grant;
import org.basex.core.cmd.Help;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.InfoStorage;
import org.basex.core.cmd.Kill;
import org.basex.core.cmd.List;
import org.basex.core.cmd.ListDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.Password;
import org.basex.core.cmd.Rename;
import org.basex.core.cmd.Replace;
import org.basex.core.cmd.RepoDelete;
import org.basex.core.cmd.RepoInstall;
import org.basex.core.cmd.RepoList;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.ShowUsers;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests user permissions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class PermissionTest {
  /** Name of test database and user. */
  static final String NAME = Util.name(PermissionTest.class);
  /** Name of the database to be renamed. */
  private static final String RENAMED = Util.name(PermissionTest.class) + "r";
  /** Test repository. **/
  protected static final String REPO = "etc/test/repo/";

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session adminSession;
  /** Socket reference. */
  static Session testSession;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");

    try {
      adminSession = new ClientSession(server.context, ADMIN, ADMIN);
      if(server.context.users.get(NAME) != null) {
        ok(new DropUser(NAME), adminSession);
      }

      ok(new CreateUser(NAME, NAME), adminSession);
      ok(new CreateDB(RENAMED), adminSession);
      server.context.repo.init(REPO);
      testSession = new ClientSession(server.context, NAME, NAME);
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }

  /** Tests all commands where no permission is needed. */
  @Test
  public void noPermsNeeded() {
    ok(new CreateDB(NAME, "<xml/>"), adminSession);
    ok(new Close(), adminSession);
    ok(new Grant("none", NAME), adminSession);

    ok(new Password(NAME), testSession);
    ok(new Help("list"), testSession);
    ok(new Close(), testSession);
    no(new ListDB(NAME), testSession);
    ok(new List(), testSession);
    no(new Open(NAME), testSession);
    no(new InfoDB(), testSession);
    no(new InfoIndex(), testSession);
    no(new InfoStorage(), testSession);
    no(new Get("DBPATH"), testSession);
    no(new Set(CmdSet.QUERYINFO, false), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    // XQuery read
    no(new XQuery("//xml"), testSession);
    no(new Find(NAME), testSession);
    no(new Optimize(), testSession);
    // XQuery update
    no(new XQuery("for $item in doc('" + NAME + "')//xml return rename" +
      " node $item as 'null'"), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new Rename(RENAMED, RENAMED + "2"), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB(NAME), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, NAME), testSession);
  }

  /** Tests all commands where read permission is needed. */
  @Test
  public void readPermsNeeded() {
    ok(new Grant("read", NAME), adminSession);

    ok(new Open(NAME), testSession);
    ok(new ListDB(NAME), testSession);
    ok(new InfoDB(), testSession);
    ok(new InfoStorage("1", "2"), testSession);
    ok(new Get("DBPATH"), testSession);
    ok(new Set(CmdSet.QUERYINFO, false), testSession);
    // XQuery read
    ok(new XQuery("//xml"), testSession);
    ok(new Find(NAME), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    // XQuery update
    no(new XQuery("for $item in doc('" + NAME + "')//xml return rename" +
      " node $item as 'null'"), testSession);
    no(new Optimize(), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new Replace(RENAMED, "<xml />"), testSession);
    no(new Rename(RENAMED, RENAMED + "2"), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB(NAME), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, NAME), testSession);
  }

  /** Tests all commands where write permission is needed. */
  @Test
  public void writePermsNeeded() {
    ok(new Grant("write", NAME), adminSession);
    ok(new Rename(RENAMED, RENAMED + "2"), testSession);
    ok(new Rename(RENAMED + "2", RENAMED), testSession);

    // replace Test
    ok(new Close(), testSession);
    ok(new Open(RENAMED), testSession);
    ok(new Add("<xml>1</xml>", NAME + ".xml"), testSession);
    ok(new Optimize(), testSession);
    ok(new Replace(NAME + ".xml", "<xmlr>2</xmlr>"), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    // XQuery Update
    ok(new XQuery("for $item in doc('" + NAME + "')//xml return rename" +
        " node $item as 'null'"), testSession);
    ok(new Optimize(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new InfoIndex(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new DropIndex(cmd), testSession);
    }
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new DropDB(NAME), testSession);
    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, NAME), testSession);
  }

  /** Tests all commands where create permission is needed. */
  @Test
  public void createPermsNeeded() {
    ok(new Grant("create", NAME), adminSession);

    ok(new Close(), testSession);
    ok(new CreateDB(NAME, "<xml/>"), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new DropDB(NAME), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, NAME), testSession);
  }

  /** Tests all commands where admin permission is needed. */
  @Test
  public void adminPermsNeeded() {
    ok(new Grant("admin", NAME), adminSession);
    if(server.context.users.get("test2") != null) {
      ok(new DropUser("test2"), testSession);
    }
    ok(new CreateUser("test2", NAME), testSession);
    ok(new CreateDB(NAME, "<xml/>"), testSession);
    ok(new ShowUsers(), testSession);
    ok(new Grant("admin", "test2"), testSession);
    ok(new Grant("create", "test2"), testSession);
    ok(new AlterUser(NAME, NAME), testSession);
    ok(new DropUser("test2"), testSession);
    ok(new Close(), testSession);
    ok(new Close(), adminSession);
    ok(new DropDB(NAME), adminSession);

    // repo Stuff
    ok(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    ok(new RepoDelete("pkg3", null), testSession);

  }

  /** Tests some usability stuff. */
  @Test
  public void use() {
    no(new DropUser(NAME), testSession);
    no(new DropUser(NAME), adminSession);
    ok(new Exit(), testSession);
    ok(new DropUser(NAME), adminSession);
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
   * [MS] does not check whether it fails *really* due to permissions
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
      adminSession.execute(new DropDB(RENAMED));
      adminSession.close();
    } catch(final Exception ex) {
      fail(ex.toString());
    }
    // stop server instance
    server.stop();
  }
}
