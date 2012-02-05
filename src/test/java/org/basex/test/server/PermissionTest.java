package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.Command;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Prop;
import org.basex.core.Text;
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
import org.basex.core.cmd.Flush;
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
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests user permissions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class PermissionTest {
  /** Name of test database and user. */
  private static final String NAME = Util.name(PermissionTest.class);
  /** Name of the database to be renamed. */
  private static final String RENAMED = Util.name(PermissionTest.class) + 'r';
  /** Test repository. **/
  private static final String REPO = "src/test/resources/repo/";

  /** Server reference. */
  private static BaseXServer server;
  /** Admin session. */
  private Session adminSession;
  /** Test session. */
  private Session testSession;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = new BaseXServer("-z", "-p9999", "-e9998");
  }

  /** Set up method. */
  @Before
  public void setUp() {
    try {
      adminSession = new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN);
      if(server.context.users.get(NAME) != null) {
        ok(new DropUser(NAME), adminSession);
      }

      ok(new CreateUser(NAME, Token.md5(NAME)), adminSession);
      ok(new CreateDB(RENAMED), adminSession);
      server.context.repo.init(REPO);
      testSession = new ClientSession(LOCALHOST, 9999, NAME, NAME);

      ok(new CreateDB(NAME, "<xml/>"), adminSession);
      ok(new Close(), adminSession);
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
  }

  /** Tests all commands where no permission is needed. */
  @Test
  public void noPermsNeeded() {
    ok(new Grant("none", NAME), adminSession);

    ok(new Password(Token.md5(NAME)), testSession);
    ok(new Help("list"), testSession);
    ok(new Close(), testSession);
    no(new ListDB(NAME), testSession);
    ok(new List(), testSession);
    no(new Open(NAME), testSession);
    no(new InfoDB(), testSession);
    no(new InfoIndex(), testSession);
    no(new InfoStorage(), testSession);
    no(new Get("DBPATH"), testSession);
    no(new Set(Prop.QUERYINFO, false), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    // XQuery read
    no(new XQuery("//xml"), testSession);
    no(new Find(NAME), testSession);
    no(new Optimize(), testSession);
    // XQuery update
    no(new XQuery("for $item in doc('" + NAME + "')//xml " +
      "return rename node $item as 'null'"), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new Rename(RENAMED, RENAMED + '2'), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB(NAME), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser(NAME, Token.md5(NAME)), testSession);
    no(new DropUser(NAME), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, Token.md5(NAME)), testSession);
    no(new Flush(), testSession);
  }

  /** Tests all commands where read permission is needed. */
  @Test
  public void readPermsNeeded() {
    ok(new Grant("read", NAME), adminSession);

    ok(new Open(NAME), testSession);
    ok(new ListDB(NAME), testSession);
    ok(new InfoDB(), testSession);
    ok(new InfoStorage("1", "2"), testSession);
    ok(new Get(Prop.QUERYINFO), testSession);
    ok(new Set(Prop.QUERYINFO, false), testSession);
    // XQuery read
    ok(new XQuery("//xml"), testSession);
    ok(new Find(NAME), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    // XQuery update
    no(new XQuery("for $item in doc('" + NAME + "')//xml " +
      "return rename node $item as 'null'"), testSession);
    no(new Optimize(), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new Replace(RENAMED, "<xml />"), testSession);
    no(new Rename(RENAMED, RENAMED + '2'), testSession);
    no(new CreateIndex("SUMMARY"), testSession);
    no(new DropDB(NAME), testSession);
    no(new DropIndex("SUMMARY"), testSession);
    no(new CreateUser(NAME, Token.md5(NAME)), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, Token.md5(NAME)), testSession);
    no(new Flush(), testSession);
    ok(new Close(), testSession);
  }

  /** Tests all commands where write permission is needed. */
  @Test
  public void writePermsNeeded() {
    ok(new Grant("write", NAME), adminSession);
    ok(new Open(RENAMED), testSession);
    ok(new Rename(RENAMED, RENAMED + '2'), testSession);
    ok(new Rename(RENAMED + '2', RENAMED), testSession);

    // replace Test
    ok(new Close(), testSession);
    ok(new Open(RENAMED), testSession);
    ok(new Add(NAME + ".xml", "<xml>1</xml>"), testSession);
    ok(new Optimize(), testSession);
    ok(new Replace(NAME + ".xml", "<xmlr>2</xmlr>"), testSession);

    // repo Stuff
    no(new RepoInstall(REPO, null), testSession);
    no(new RepoDelete(REPO, null), testSession);
    no(new RepoList(), testSession);

    // XQuery Update
    ok(new XQuery("for $item in doc('" + NAME + "')//xml " +
        "return rename node $item as 'null'"), testSession);
    ok(new Optimize(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new InfoIndex(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new DropIndex(cmd), testSession);
    }
    ok(new Flush(), testSession);
    ok(new Close(), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new DropDB(NAME), testSession);
    no(new CreateUser(NAME, Token.md5(NAME)), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, Token.md5(NAME)), testSession);
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

    no(new CreateUser(NAME, Token.md5(NAME)), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export("."), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, Token.md5(NAME)), testSession);
  }

  /** Tests all commands where admin permission is needed. */
  @Test
  public void adminPermsNeeded() {
    ok(new Grant(Text.ADMIN, NAME), adminSession);
    if(server.context.users.get("test2") != null) {
      ok(new DropUser("test2"), testSession);
    }
    ok(new CreateUser("test2", Token.md5(NAME)), testSession);
    ok(new CreateDB(NAME, "<xml/>"), testSession);
    ok(new ShowUsers(), testSession);
    ok(new Grant(Text.ADMIN, "test2"), testSession);
    ok(new Grant("create", "test2"), testSession);
    ok(new AlterUser(NAME, Token.md5(NAME)), testSession);
    ok(new DropUser("test2"), testSession);
    ok(new Close(), testSession);
    ok(new Close(), adminSession);
    ok(new DropDB(NAME), adminSession);

    // repo Stuff
    ok(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    ok(new RepoDelete("http://www.pkg3.com", null), testSession);
  }

  /** Drops users. */
  @Test
  public void dropUsers() {
    no(new DropUser(NAME), testSession);
    no(new DropUser(NAME), adminSession);
    ok(new Exit(), testSession);
    // give the server some time to close the client session
    Performance.sleep(50);
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
    } catch(final IOException ex) {
      fail(Util.message(ex));
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
    } catch(final IOException ex) {
    }
  }

  /** Clean up method. */
  @After
  public void cleanUp() {
    try {
      testSession.close();
      adminSession.execute(new DropDB(RENAMED));
      adminSession.execute(new DropDB(NAME));
      adminSession.close();
      // give the server some time to clean up the sessions before next test
      Performance.sleep(50);
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    // stop server instance
    server.stop();
  }
}
