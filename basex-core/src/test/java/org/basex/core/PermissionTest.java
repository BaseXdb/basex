package org.basex.core;

import static org.basex.core.users.UserText.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests user permissions.
 *
 * @author BaseX Team, BSD License
 * @author Andreas Weiler
 */
public final class PermissionTest extends SandboxTest {
  /** Name of the database to be renamed. */
  private static final String NAME2 = NAME + '2';
  /** Test folder. */
  private static final String FOLDER = "src/test/resources/";
  /** Test repository. **/
  private static final String REPO = FOLDER + "repo/";

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
  @BeforeAll public static void start() throws IOException {
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
    new IOFile(Prop.TEMPDIR, NAME + "-export").delete();
  }

  /** Set up method. */
  @BeforeEach public void setUp() {
    try {
      adminSession = createClient();
      if(server.context.users.get(NAME) != null) {
        ok(new DropUser(NAME), adminSession);
      }

      ok(new CreateUser(NAME, NAME), adminSession);
      ok(new CreateDB(NAME2), adminSession);
      server.context.soptions.set(StaticOptions.REPOPATH, REPO);
      testSession = createClient(NAME, NAME);

      ok(new CreateDB(NAME, "<xml/>"), adminSession);
      ok(new XQuery(_DB_PUT_BINARY.args(NAME, " xs:hexBinary('62')", "binary")), adminSession);
      ok(new XQuery(_DB_PUT_VALUE.args(NAME, "v", "value")), adminSession);
      ok(new XQuery(_FILE_WRITE.args(sandbox() + "file", "file")), adminSession);
      ok(new XQuery(_FILE_WRITE_TEXT.args(sandbox() + "doc.xml", "<x/>")), adminSession);
      ok(new Close(), adminSession);
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
  }

  /** Clean up method. */
  @AfterEach public void cleanUp() {
    try {
      testSession.close();
      adminSession.execute(new DropDB(NAME2));
      adminSession.execute(new DropDB(NAME));
      adminSession.close();
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
  }

  /** Tests all commands where no permission is needed. */
  @Test public void noPermsNeeded() {
    ok(new Grant("none", NAME), adminSession);

    ok(new Password(NAME), testSession);
    ok(new Help("list"), testSession);
    ok(new List(), testSession);
    no(new List(NAME), testSession);
    no(new Open(NAME), testSession);
    no(new InfoDB(), testSession);
    no(new InfoIndex(), testSession);
    no(new InfoStorage(), testSession);
    no(new ShowOptions("DBPATH"), testSession);
    ok(new ShowOptions(MainOptions.QUERYINFO), testSession);
    ok(new Set(MainOptions.QUERYINFO, false), testSession);
    no(new Get(NAME + ".xml"), testSession);
    no(new Find(NAME), testSession);
    no(new Optimize(), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new Rename(NAME2, NAME2 + '2'), testSession);
    no(new CreateIndex("token"), testSession);
    no(new DropDB(NAME), testSession);
    no(new DropIndex("token"), testSession);
    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Kill("dada"), testSession);
    ok(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterPassword(NAME, NAME), testSession);
    no(new AlterUser(NAME, "test2"), testSession);
    no(new Flush(), testSession);
    ok(new Close(), testSession);
    no(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

    // XQuery
    ok(new XQuery("1"), testSession);
    ok(new XQuery("delete node <a/>"), testSession);
    // higher-order standard functions: no permission required
    ok(new XQuery("fn:apply(fn($x) { $x + 1 }, [1])"), testSession);
    ok(new XQuery("fn:function-lookup(xs:QName('fn:abs'), 1)(-1)"), testSession);
    no(new XQuery("for $n in " + _DB_GET.args(NAME) + "//xml return delete node $n"), testSession);

    no(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    no(new XQuery(_DB_ATTRIBUTE.args(NAME, "x")), testSession);
    no(new XQuery(_DB_ATTRIBUTE_RANGE.args(NAME, "x", "y")), testSession);
    no(new XQuery(_DB_TEXT.args(NAME, "x")), testSession);
    no(new XQuery(_DB_TEXT_RANGE.args(NAME, "x", "y")), testSession);
    no(new XQuery(_DB_FLUSH.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST.args()), testSession);
    no(new XQuery(_DB_LIST.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args()), testSession);
    no(new XQuery(_DB_LIST_DETAILS.args(NAME)), testSession);
    no(new XQuery(_DB_DIR.args(NAME, "path")), testSession);
    no(new XQuery(_DB_CONTENT_TYPE.args(NAME, "Sandbox.xml")), testSession);
    no(new XQuery(_DB_TYPE.args(NAME, "Sandbox.xml")), testSession);
    no(new XQuery(_DB_EXISTS.args(NAME, "Sandbox.xml")), testSession);
    no(new XQuery(_DB_GET.args(NAME, "Sandbox.xml")), testSession);
    no(new XQuery(_DB_GET_BINARY.args(NAME, "binary")), testSession);
    no(new XQuery(_DB_GET_VALUE.args(NAME, "value")), testSession);
    no(new XQuery(_DB_INFO.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTION_MAP.args()), testSession);
    no(new XQuery(_DB_PROPERTY.args(NAME, "textindex")), testSession);
    no(new XQuery(_DB_PROPERTY_MAP.args(NAME)), testSession);
    ok(new XQuery(_DB_SYSTEM.args()), testSession);
    ok(new XQuery(_DB_OPTION.args("mainmem")), testSession);
    no(new XQuery(_DB_OPTION.args("dbpath")), testSession);

    no(new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")), testSession);
    no(new XQuery(_DB_PUT.args(NAME, "<a/>", "a.xml")), testSession);
    no(new XQuery(_DB_PUT_BINARY.args(NAME, "b", "binary")), testSession);
    no(new XQuery(_DB_PUT_VALUE.args(NAME, "v", "value")), testSession);
    no(new XQuery(_DB_RENAME.args(NAME, "a.xml", "b.xml")), testSession);
    no(new XQuery(_DB_DELETE.args(NAME, "b.xml")), testSession);
    no(new XQuery(_DB_CREATE.args(NAME)), testSession);
    no(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    no(new XQuery(_DB_CREATE_BACKUP.args(NAME)), testSession);
    no(new XQuery(_DB_EXPORT.args(NAME, sandbox() + NAME + "-export")), testSession);
    no(new XQuery(_DB_BACKUPS.args()), testSession);
    no(new XQuery(_DB_BACKUPS.args(NAME)), testSession);
    no(new XQuery(_DB_RESTORE.args(NAME)), testSession);
    no(new XQuery(_DB_ALTER_BACKUP.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_DROP_BACKUP.args(NAME2)), testSession);
    no(new XQuery(_DB_COPY.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_ALTER.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_DROP.args(NAME2)), testSession);
    no(new XQuery("Q{java.lang.String}new('x')"), testSession);

    ok(new XQuery(_FILE_PATH_SEPARATOR.args()), testSession);
    ok(new XQuery(_FILE_NAME.args("name")), testSession);
    no(new XQuery(_FILE_PATH_TO_NATIVE.args(".")), testSession);
    no(new XQuery(_FILE_BASE_DIR.args()), testSession);
    no(new XQuery(_FILE_LIST.args(sandbox())), testSession);
    no(new XQuery(_FILE_READ_BINARY.args(sandbox() + "file")), testSession);
    no(new XQuery(_FILE_WRITE.args(sandbox() + "file2", "file2")), testSession);
    no(new XQuery(_FILE_DELETE.args(sandbox() + "file2")), testSession);

    // fn:doc, fn:doc-available, fn:collection: external resources require CREATE permission
    no(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession);
  }

  /** Tests all commands where read permission is needed. */
  @Test public void readPermsNeeded() {
    ok(new Grant("read", NAME), adminSession);

    ok(new List(), testSession);
    ok(new List(NAME), testSession);
    ok(new List(NAME, "path"), testSession);
    ok(new Open(NAME), testSession);
    ok(new Dir("path"), testSession);
    ok(new InfoDB(), testSession);
    ok(new InfoStorage("1", "2"), testSession);
    no(new ShowOptions("DBPATH"), testSession);
    ok(new ShowOptions(MainOptions.QUERYINFO), testSession);
    ok(new Set(MainOptions.QUERYINFO, false), testSession);
    ok(new Find(NAME), testSession);
    ok(new Get(NAME + ".xml"), testSession);
    no(new Optimize(), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new Put(NAME2, "<xml />"), testSession);
    no(new Rename(NAME2, NAME2 + '2'), testSession);
    no(new CreateIndex("token"), testSession);
    no(new DropDB(NAME), testSession);
    no(new DropIndex("token"), testSession);
    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export(sandbox() + "-export"), testSession);
    no(new Kill("dada"), testSession);
    ok(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterPassword(NAME, NAME), testSession);
    no(new AlterUser(NAME, "test2"), testSession);
    no(new Flush(), testSession);
    ok(new Close(), testSession);
    no(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

    // XQuery
    ok(new XQuery("1"), testSession);
    ok(new XQuery("delete node <a/>"), testSession);
    no(new XQuery("for $n in " + _DB_GET.args(NAME) + "//xml return delete node $n"), testSession);

    no(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    ok(new XQuery(_DB_ATTRIBUTE.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_ATTRIBUTE_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_TEXT.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_TEXT_RANGE.args(NAME, "x", "y")), testSession);
    no(new XQuery(_DB_FLUSH.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST.args()), testSession);
    ok(new XQuery(_DB_LIST.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args()), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args(NAME)), testSession);
    ok(new XQuery(_DB_DIR.args(NAME, "path")), testSession);
    ok(new XQuery(_DB_CONTENT_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_EXISTS.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET_BINARY.args(NAME, "binary")), testSession);
    ok(new XQuery(_DB_GET_VALUE.args(NAME, "value")), testSession);
    ok(new XQuery(_DB_INFO.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTION_MAP.args()), testSession);
    ok(new XQuery(_DB_PROPERTY.args(NAME, "textindex")), testSession);
    ok(new XQuery(_DB_PROPERTY_MAP.args(NAME)), testSession);
    ok(new XQuery(_DB_SYSTEM.args()), testSession);
    ok(new XQuery(_DB_OPTION.args("mainmem")), testSession);
    no(new XQuery(_DB_OPTION.args("dbpath")), testSession);

    no(new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")), testSession);
    no(new XQuery(_DB_PUT.args(NAME, "<a/>", "a.xml")), testSession);
    no(new XQuery(_DB_PUT_BINARY.args(NAME, "b", "binary")), testSession);
    no(new XQuery(_DB_PUT_VALUE.args(NAME, "v", "value")), testSession);
    no(new XQuery(_DB_RENAME.args(NAME, "a.xml", "b.xml")), testSession);
    no(new XQuery(_DB_DELETE.args(NAME, "b.xml")), testSession);
    no(new XQuery(_DB_CREATE.args(NAME)), testSession);
    no(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    no(new XQuery(_DB_CREATE_BACKUP.args(NAME)), testSession);
    no(new XQuery(_DB_EXPORT.args(NAME, sandbox() + "-export")), testSession);
    no(new XQuery(_DB_BACKUPS.args()), testSession);
    no(new XQuery(_DB_BACKUPS.args(NAME)), testSession);
    no(new XQuery(_DB_RESTORE.args(NAME)), testSession);
    no(new XQuery(_DB_ALTER_BACKUP.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_DROP_BACKUP.args(NAME2)), testSession);
    no(new XQuery(_DB_COPY.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_ALTER.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_DROP.args(NAME2)), testSession);
    no(new XQuery("Q{java.lang.String}new('x')"), testSession);

    ok(new XQuery(_FILE_PATH_SEPARATOR.args()), testSession);
    ok(new XQuery(_FILE_NAME.args("name")), testSession);
    no(new XQuery(_FILE_PATH_TO_NATIVE.args(".")), testSession);
    no(new XQuery(_FILE_BASE_DIR.args()), testSession);
    no(new XQuery(_FILE_LIST.args(sandbox())), testSession);
    no(new XQuery(_FILE_READ_BINARY.args(sandbox() + "file")), testSession);
    no(new XQuery(_FILE_WRITE.args(sandbox() + "file2", "file2")), testSession);
    no(new XQuery(_FILE_DELETE.args(sandbox() + "file2")), testSession);

    // fn:doc, fn:doc-available, fn:collection: external resources require CREATE permission
    no(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession);
  }

  /** Tests all commands where write permission is needed. */
  @Test public void writePermsNeeded() {
    ok(new Grant("write", NAME), adminSession);
    ok(new Open(NAME2), testSession);
    ok(new Rename(NAME2, NAME2 + '2'), testSession);
    ok(new Rename(NAME2 + '2', NAME2), testSession);

    // replace Test
    ok(new Open(NAME2), testSession);
    ok(new Add(NAME + ".xml", "<xml>1</xml>"), testSession);
    ok(new Optimize(), testSession);
    ok(new Put(NAME + ".xml", "<xmlr>2</xmlr>"), testSession);

    ok(new InfoIndex(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new DropIndex(cmd), testSession);
    }
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new Flush(), testSession);
    ok(new Close(), testSession);
    no(new CreateDB(NAME, "<xml/>"), testSession);
    no(new DropDB(NAME), testSession);
    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Export(sandbox() + "-export"), testSession);
    no(new Kill("dada"), testSession);
    ok(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterPassword(NAME, NAME), testSession);
    no(new AlterUser(NAME, "test2"), testSession);
    no(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

    // XQuery
    ok(new XQuery("1"), testSession);
    ok(new XQuery("delete node <a/>"), testSession);
    ok(new XQuery("for $n in " + _DB_GET.args(NAME) + "//xml return delete node $n"), testSession);

    ok(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    ok(new XQuery(_DB_ATTRIBUTE.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_ATTRIBUTE_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_TEXT.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_TEXT_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_FLUSH.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST.args()), testSession);
    ok(new XQuery(_DB_LIST.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args()), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args(NAME)), testSession);
    ok(new XQuery(_DB_DIR.args(NAME, "path")), testSession);
    ok(new XQuery(_DB_CONTENT_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_EXISTS.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET_BINARY.args(NAME, "binary")), testSession);
    ok(new XQuery(_DB_GET_VALUE.args(NAME, "value")), testSession);
    ok(new XQuery(_DB_INFO.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTION_MAP.args()), testSession);
    ok(new XQuery(_DB_PROPERTY.args(NAME, "textindex")), testSession);
    ok(new XQuery(_DB_PROPERTY_MAP.args(NAME)), testSession);
    ok(new XQuery(_DB_SYSTEM.args()), testSession);
    ok(new XQuery(_DB_OPTION.args("mainmem")), testSession);
    no(new XQuery(_DB_OPTION.args("dbpath")), testSession);

    ok(new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")), testSession);
    ok(new XQuery(_DB_PUT.args(NAME, "<a/>", "a.xml")), testSession);
    ok(new XQuery(_DB_PUT_BINARY.args(NAME, " xs:hexBinary('62')", "binary")), testSession);
    ok(new XQuery(_DB_PUT_VALUE.args(NAME, "v", "value")), testSession);
    ok(new XQuery(_DB_RENAME.args(NAME, "a.xml", "b.xml")), testSession);
    ok(new XQuery(_DB_DELETE.args(NAME, "b.xml")), testSession);
    no(new XQuery(_DB_CREATE.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    no(new XQuery(_DB_CREATE_BACKUP.args(NAME)), testSession);
    no(new XQuery(_DB_EXPORT.args(NAME, sandbox() + "-export")), testSession);
    no(new XQuery(_DB_BACKUPS.args()), testSession);
    no(new XQuery(_DB_BACKUPS.args(NAME)), testSession);
    no(new XQuery(_DB_RESTORE.args(NAME)), testSession);
    no(new XQuery(_DB_ALTER_BACKUP.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_DROP_BACKUP.args(NAME2)), testSession);
    no(new XQuery(_DB_COPY.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_ALTER.args(NAME, NAME2)), testSession);
    no(new XQuery(_DB_DROP.args(NAME2)), testSession);
    no(new XQuery("Q{java.lang.String}new('x')"), testSession);

    ok(new XQuery(_FILE_PATH_SEPARATOR.args()), testSession);
    ok(new XQuery(_FILE_NAME.args("name")), testSession);
    no(new XQuery(_FILE_PATH_TO_NATIVE.args(".")), testSession);
    no(new XQuery(_FILE_BASE_DIR.args()), testSession);
    no(new XQuery(_FILE_LIST.args(sandbox())), testSession);
    no(new XQuery(_FILE_READ_BINARY.args(sandbox() + "file")), testSession);
    no(new XQuery(_FILE_WRITE.args(sandbox() + "file2", "file2")), testSession);
    no(new XQuery(_FILE_DELETE.args(sandbox() + "file2")), testSession);

    // fn:doc, fn:doc-available, fn:collection: external resources require CREATE permission
    no(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession);
  }

  /** Tests all commands where create permission is needed. */
  @Test public void createPermsNeeded() {
    ok(new Grant("create", NAME), adminSession);

    ok(new CreateDB(NAME2, "<xml/>"), testSession);
    ok(new InfoIndex(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new DropIndex(cmd), testSession);
    }
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new Export(sandbox() + "-export"), testSession);
    ok(new Close(), testSession);
    ok(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    ok(new RepoDelete("http://www.pkg3.com", null), testSession);

    no(new CreateUser(NAME, NAME), testSession);
    no(new DropUser(NAME), testSession);
    no(new Kill("dada"), testSession);
    ok(new ShowUsers("Users"), testSession);
    no(new Grant("read", NAME), testSession);
    no(new Grant("none", NAME), testSession);
    no(new AlterPassword(NAME, NAME), testSession);
    no(new org.basex.core.cmd.Test(FOLDER + "tests-ok.xqm"), testSession);

    // XQuery
    ok(new XQuery("1"), testSession);
    ok(new XQuery("delete node <a/>"), testSession);
    ok(new XQuery("for $n in " + _DB_GET.args(NAME) + "//xml return delete node $n"), testSession);

    ok(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    ok(new XQuery(_DB_ATTRIBUTE.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_ATTRIBUTE_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_TEXT.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_TEXT_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_FLUSH.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST.args()), testSession);
    ok(new XQuery(_DB_LIST.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args()), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args(NAME)), testSession);
    ok(new XQuery(_DB_DIR.args(NAME, "path")), testSession);
    ok(new XQuery(_DB_CONTENT_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_EXISTS.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET_BINARY.args(NAME, "binary")), testSession);
    ok(new XQuery(_DB_GET_VALUE.args(NAME, "value")), testSession);
    ok(new XQuery(_DB_INFO.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTION_MAP.args()), testSession);
    ok(new XQuery(_DB_PROPERTY.args(NAME, "textindex")), testSession);
    ok(new XQuery(_DB_PROPERTY_MAP.args(NAME)), testSession);
    ok(new XQuery(_DB_SYSTEM.args()), testSession);
    ok(new XQuery(_DB_OPTION.args("mainmem")), testSession);
    no(new XQuery(_DB_OPTION.args("dbpath")), testSession);

    ok(new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")), testSession);
    ok(new XQuery(_DB_PUT.args(NAME, "<a/>", "a.xml")), testSession);
    ok(new XQuery(_DB_PUT_BINARY.args(NAME, " xs:hexBinary('62')", "binary")), testSession);
    ok(new XQuery(_DB_PUT_VALUE.args(NAME, "v", "value")), testSession);
    ok(new XQuery(_DB_RENAME.args(NAME, "a.xml", "b.xml")), testSession);
    ok(new XQuery(_DB_DELETE.args(NAME, "b.xml")), testSession);
    ok(new XQuery(_DB_CREATE.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    ok(new XQuery(_DB_CREATE_BACKUP.args(NAME)), testSession);
    no(new XQuery(_DB_EXPORT.args(NAME, sandbox() + "-export")), testSession);
    ok(new XQuery(_DB_BACKUPS.args()), testSession);
    ok(new XQuery(_DB_BACKUPS.args(NAME)), testSession);
    ok(new XQuery(_DB_RESTORE.args(NAME)), testSession);
    ok(new XQuery(_DB_ALTER_BACKUP.args(NAME, NAME2)), testSession);
    ok(new XQuery(_DB_DROP_BACKUP.args(NAME2)), testSession);
    ok(new XQuery(_DB_COPY.args(NAME, NAME2)), testSession);
    ok(new XQuery(_DB_ALTER.args(NAME, NAME2)), testSession);
    ok(new XQuery(_DB_DROP.args(NAME2)), testSession);
    no(new XQuery("Q{java.lang.String}new('x')"), testSession);

    ok(new XQuery(_FILE_PATH_SEPARATOR.args()), testSession);
    ok(new XQuery(_FILE_NAME.args("name")), testSession);
    ok(new XQuery(_FILE_PATH_TO_NATIVE.args(".")), testSession);
    ok(new XQuery(_FILE_BASE_DIR.args()), testSession);
    ok(new XQuery(_FILE_LIST.args(sandbox())), testSession);
    ok(new XQuery(_FILE_READ_BINARY.args(sandbox() + "file")), testSession);
    no(new XQuery(_FILE_WRITE.args(sandbox() + "file2", "file2")), testSession);
    no(new XQuery(_FILE_DELETE.args(sandbox() + "file2")), testSession);

    // fn:doc, fn:doc-available: external resources accessible with CREATE permission
    ok(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    ok(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);

    // xquery:parse and xquery:eval: load from URI now permitted with CREATE
    ok(new XQuery(_XQUERY_PARSE.args(" xs:anyURI('" + sandbox() + "doc.xml')")), testSession);
    ok(new XQuery(_XQUERY_EVAL.args(" xs:anyURI('" + sandbox() + "doc.xml')")), testSession);
  }

  /** Tests all commands where admin permission is needed. */
  @Test public void adminPermsNeeded() {
    ok(new Grant(ADMIN, NAME), adminSession);
    if(server.context.users.get("test2") != null) {
      ok(new DropUser("test2"), testSession);
    }
    ok(new CreateUser("test2", NAME), testSession);
    ok(new CreateDB(NAME2, "<xml/>"), testSession);
    ok(new InfoIndex(), testSession);
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new DropIndex(cmd), testSession);
    }
    for(final CmdIndex cmd : CmdIndex.values()) {
      ok(new CreateIndex(cmd), testSession);
    }
    ok(new Close(), testSession);
    ok(new DropDB(NAME2), testSession);
    ok(new ShowUsers(), testSession);
    ok(new Grant(ADMIN, "test2"), testSession);
    ok(new Grant("create", "test2"), testSession);
    ok(new AlterPassword(NAME, NAME), testSession);
    ok(new AlterUser("test2", "test4"), testSession);
    ok(new DropUser("test3"), testSession);
    ok(new RepoInstall(REPO + "/pkg3.xar", null), testSession);
    ok(new RepoList(), testSession);
    ok(new RepoDelete("http://www.pkg3.com", null), testSession);
    ok(new org.basex.core.cmd.Test(FOLDER + "tests-ok.xqm"), testSession);

    // XQuery
    ok(new XQuery("1"), testSession);
    ok(new XQuery("delete node <a/>"), testSession);
    ok(new XQuery("for $n in " + _DB_GET.args(NAME) + "//xml return delete node $n"), testSession);

    ok(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    ok(new XQuery(_DB_ATTRIBUTE.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_ATTRIBUTE_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_TEXT.args(NAME, "x")), testSession);
    ok(new XQuery(_DB_TEXT_RANGE.args(NAME, "x", "y")), testSession);
    ok(new XQuery(_DB_FLUSH.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST.args()), testSession);
    ok(new XQuery(_DB_LIST.args(NAME)), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args()), testSession);
    ok(new XQuery(_DB_LIST_DETAILS.args(NAME)), testSession);
    ok(new XQuery(_DB_DIR.args(NAME, "path")), testSession);
    ok(new XQuery(_DB_CONTENT_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_TYPE.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_EXISTS.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET.args(NAME, "Sandbox.xml")), testSession);
    ok(new XQuery(_DB_GET_BINARY.args(NAME, "binary")), testSession);
    ok(new XQuery(_DB_GET_VALUE.args(NAME, "value")), testSession);
    ok(new XQuery(_DB_INFO.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTION_MAP.args()), testSession);
    ok(new XQuery(_DB_PROPERTY.args(NAME, "textindex")), testSession);
    ok(new XQuery(_DB_PROPERTY_MAP.args(NAME)), testSession);
    ok(new XQuery(_DB_SYSTEM.args()), testSession);
    ok(new XQuery(_DB_OPTION.args("mainmem")), testSession);
    ok(new XQuery(_DB_OPTION.args("dbpath")), testSession);

    ok(new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")), testSession);
    ok(new XQuery(_DB_PUT.args(NAME, "<a/>", "a.xml")), testSession);
    ok(new XQuery(_DB_PUT_BINARY.args(NAME, " xs:hexBinary('62')", "binary")), testSession);
    ok(new XQuery(_DB_PUT_VALUE.args(NAME, "v", "value")), testSession);
    ok(new XQuery(_DB_RENAME.args(NAME, "a.xml", "b.xml")), testSession);
    ok(new XQuery(_DB_DELETE.args(NAME, "b.xml")), testSession);
    ok(new XQuery(_DB_CREATE.args(NAME)), testSession);
    ok(new XQuery(_DB_OPTIMIZE.args(NAME)), testSession);
    ok(new XQuery(_DB_CREATE_BACKUP.args(NAME)), testSession);
    ok(new XQuery(_DB_EXPORT.args(NAME, sandbox() + "-export")), testSession);
    ok(new XQuery(_DB_BACKUPS.args()), testSession);
    ok(new XQuery(_DB_BACKUPS.args(NAME)), testSession);
    ok(new XQuery(_DB_RESTORE.args(NAME)), testSession);
    ok(new XQuery(_DB_ALTER_BACKUP.args(NAME, NAME2)), testSession);
    ok(new XQuery(_DB_DROP_BACKUP.args(NAME2)), testSession);
    ok(new XQuery(_DB_COPY.args(NAME, NAME2)), testSession);
    ok(new XQuery(_DB_ALTER.args(NAME, NAME2)), testSession);
    ok(new XQuery(_DB_DROP.args(NAME2)), testSession);
    ok(new XQuery("Q{java.lang.String}new('x')"), testSession);

    ok(new XQuery(_FILE_PATH_SEPARATOR.args()), testSession);
    ok(new XQuery(_FILE_NAME.args("name")), testSession);
    ok(new XQuery(_FILE_PATH_TO_NATIVE.args(".")), testSession);
    ok(new XQuery(_FILE_BASE_DIR.args()), testSession);
    ok(new XQuery(_FILE_LIST.args(sandbox())), testSession);
    ok(new XQuery(_FILE_READ_BINARY.args(sandbox() + "file")), testSession);
    ok(new XQuery(_FILE_WRITE.args(sandbox() + "file2", "file2")), testSession);
    ok(new XQuery(_FILE_DELETE.args(sandbox() + "file2")), testSession);

    // xquery:eval from URI: admin may load external code
    ok(new XQuery(_XQUERY_EVAL.args(" xs:anyURI('" + sandbox() + "doc.xml')")), testSession);
  }

  /** Tests the XQUERY_NOPERM_X error raised by xquery:eval. */
  @Test public void xqueryNoPerm() {
    // CREATE user cannot raise the inner query's permission to admin
    ok(new Grant("create", NAME), adminSession);
    error(new XQuery(_XQUERY_EVAL.args("1", " ()", " { 'permission': 'admin' }")),
        testSession, "[xquery:permission]", "admin");
    // READ user cannot raise the inner query's permission to create
    ok(new Grant("read", NAME), adminSession);
    error(new XQuery(_XQUERY_EVAL.args("1", " ()", " { 'permission': 'create' }")),
        testSession, "[xquery:permission]", "create");
  }

  /** Tests the permission error raised when accessing external resources. */
  @Test public void permRequired() {
    // fn:collection bypasses the DTD-related permission check in Docs.check
    // and reaches QueryResources.create, which raises BASEX_PERMISSION_X_X

    // READ user: missing CREATE for an external file
    ok(new Grant("read", NAME), adminSession);
    error(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession,
        "[basex:permission]", "create");
    // NONE user: same code; CREATE is still the missing permission for files
    ok(new Grant("none", NAME), adminSession);
    error(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession,
        "[basex:permission]", "create");
  }

  /** Drops users. */
  @Test public void dropUsers() {
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
   * Assumes that the command fails and that the error message contains all given markers.
   * @param cmd command reference
   * @param s session
   * @param markers substrings expected in the error message
   */
  private static void error(final Command cmd, final Session s, final String... markers) {
    try {
      s.execute(cmd);
      fail("\"" + cmd + "\" was supposed to fail.");
    } catch(final IOException ex) {
      final String msg = ex.getMessage();
      for(final String marker : markers) {
        assertTrue(msg.contains(marker), "Expected '" + marker + "' in: " + msg);
      }
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
      Util.debug(ex);
    }
  }
}
