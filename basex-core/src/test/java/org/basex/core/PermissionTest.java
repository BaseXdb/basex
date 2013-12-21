package org.basex.core;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.server.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests user permissions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Andreas Weiler
 */
public final class PermissionTest extends SandboxTest {
  /** Name of the database to be renamed. */
  private static final String RENAMED = Util.className(PermissionTest.class) + 'r';
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
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    stopServer(server);
  }

  /** Set up method. */
  @Before
  public void setUp() {
    try {
      adminSession = createClient();
      if(server.context.users.get(NAME) != null) {
        ok(new DropUser(NAME), adminSession);
      }

      ok(new CreateUser(NAME, Token.md5(NAME)), adminSession);
      ok(new CreateDB(RENAMED), adminSession);
      server.context.globalopts.set(GlobalOptions.REPOPATH, REPO);
      testSession = createClient(NAME, NAME);

      ok(new CreateDB(NAME, "<xml/>"), adminSession);
      ok(new Close(), adminSession);
    } catch(final Exception ex) {
      fail(Util.message(ex));
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

  /** Tests all commands where no permission is needed. */
  @Test
  public void noPermsNeeded() {
    ok(new Grant("none", NAME), adminSession);

    ok(new Password(Token.md5(NAME)), testSession);
    ok(new Help("list"), testSession);
    ok(new Close(), testSession);
    no(new List(NAME), testSession);
    ok(new List(), testSession);
    no(new Open(NAME), testSession);
    no(new InfoDB(), testSession);
    no(new InfoIndex(), testSession);
    no(new InfoStorage(), testSession);
    no(new Get("DBPATH"), testSession);
    ok(new Get(MainOptions.QUERYINFO), testSession);
    ok(new Set(MainOptions.QUERYINFO, false), testSession);

    // repo Stuff
    no(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

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
    ok(new List(NAME), testSession);
    ok(new InfoDB(), testSession);
    ok(new InfoStorage("1", "2"), testSession);
    no(new Get("DBPATH"), testSession);
    ok(new Get(MainOptions.QUERYINFO), testSession);
    ok(new Set(MainOptions.QUERYINFO, false), testSession);
    // XQuery read
    ok(new XQuery("//xml"), testSession);
    ok(new Find(NAME), testSession);

    // repo Stuff
    no(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

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
    no(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

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
    ok(new Export("."), testSession);

    // repo Stuff
    ok(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    ok(new RepoDelete("http://www.pkg3.com", null), testSession);

    no(new CreateUser(NAME, Token.md5(NAME)), testSession);
    no(new DropUser(NAME), testSession);
    no(new Kill("dada"), testSession);
    no(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterUser(NAME, Token.md5(NAME)), testSession);
  }

  /** Tests all commands where admin permission is needed. */
  @Test
  public void adminPermsNeeded() {
    ok(new Grant(S_ADMIN, NAME), adminSession);
    if(server.context.users.get("test2") != null) {
      ok(new DropUser("test2"), testSession);
    }
    ok(new CreateUser("test2", Token.md5(NAME)), testSession);
    ok(new CreateDB(NAME, "<xml/>"), testSession);
    ok(new ShowUsers(), testSession);
    ok(new Grant(S_ADMIN, "test2"), testSession);
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
}
