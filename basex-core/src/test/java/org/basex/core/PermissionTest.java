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
  /** Query that parses a document with a catalog-mapped DTD. */
  private static final String CATALOG_QUERY = "declare option db:catalog '" + FOLDER
      + "catalog/catalog.xml'; parse-xml(\"<!DOCTYPE xml SYSTEM 'http://dtd.dtd'><doc/>\")";

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
      ok(new XQuery(_FILE_WRITE_TEXT.args(sandbox() + "module.xqm",
          "module namespace m = 'm'; declare function m:f() { 1 };")), adminSession);
      ok(new XQuery(_FILE_WRITE_TEXT.args(sandbox() + "params.xml",
          "<serialization-parameters xmlns='http://www.w3.org/2010/xslt-xquery-serialization'/>")),
          adminSession);
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
    ok(new Info(), testSession);
    no(new ShowOptions("DBPATH"), testSession);
    ok(new ShowOptions(MainOptions.QUERYINFO), testSession);
    ok(new Set(MainOptions.QUERYINFO, false), testSession);
    ok(new Set(MainOptions.BINDINGS, "a=1"), testSession);
    ok(new Set(MainOptions.SERIALIZER.name(), "indent=yes"), testSession);
    ok(new Set(MainOptions.STRIPWS, true), testSession);
    no(new Set(StaticOptions.DEBUG, true), testSession);
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
    no(new RepoList(), testSession);
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

    // fn:doc, fn:doc-available, fn:collection: external resources require ADMIN permission
    no(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession);

    // files referenced in the query prolog or body, and query files, require ADMIN permission
    for(final String query : externalQueries()) no(new XQuery(query), testSession);
    // thesaurus databases require READ permission
    no(new XQuery("'a' contains text 'a' using thesaurus at '" + NAME + "'"), testSession);
    no(new Run(sandbox() + "doc.xml"), testSession);

    // fn:parse-xml: the trust-external option is ignored for users without ADMIN permission
    ok(new XQuery("parse-xml('<x/>')"), testSession);
    ok(new XQuery("parse-xml('<x/>', { 'trust-external': true() })"), testSession);
    ok(new XQuery("parse-xml('<x/>', { 'intparse': true() })"), testSession);
    no(new XQuery(CATALOG_QUERY), testSession);
  }

  /**
   * Returns queries that access external files.
   * @return queries
   */
  private static String[] externalQueries() {
    return new String[] {
      "import module namespace m = 'm' at '" + sandbox() + "module.xqm'; m:f()",
      "declare option output:parameter-document '" + sandbox() + "params.xml'; 1",
      "'a' contains text 'a' using stop words at '" + sandbox() + "file'",
      "'a' contains text 'a' using thesaurus at '" + sandbox() + "doc.xml'"
    };
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
    ok(new Set(MainOptions.STRIPWS, true), testSession);
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
    no(new RepoList(), testSession);
    no(new RepoDelete("http://www.pkg3.com", null), testSession);

    // XQuery
    ok(new XQuery("1"), testSession);
    ok(new XQuery("delete node <a/>"), testSession);
    no(new XQuery("for $n in " + _DB_GET.args(NAME) + "//xml return delete node $n"), testSession);
    ok(new XQuery("'a' contains text 'a' using thesaurus at '" + NAME + "'"), testSession);

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

    // fn:doc, fn:doc-available, fn:collection: external resources require ADMIN permission
    no(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession);
  }

  /** Tests all commands where write permission is needed. */
  @Test public void writePermsNeeded() {
    ok(new Grant("write", NAME), adminSession);
    ok(new Open(NAME2), testSession);
    // local resources require ADMIN permission
    no(new Add("file.xml", sandbox() + "doc.xml"), testSession);
    no(new Put("file.xml", sandbox() + "doc.xml"), testSession);
    no(new BinaryPut("file.bin", sandbox() + "file"), testSession);
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
    no(new RepoList(), testSession);
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

    // fn:doc, fn:doc-available, fn:collection: external resources require ADMIN permission
    no(new XQuery(DOC.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(DOC_AVAILABLE.args(sandbox() + "doc.xml")), testSession);
    no(new XQuery(COLLECTION.args(sandbox() + "doc.xml")), testSession);

    // db:add, db:put, db:put-binary: file paths require ADMIN permission
    no(new XQuery(_DB_ADD.args(NAME, sandbox() + "doc.xml", "doc.xml")), testSession);
    no(new XQuery(_DB_PUT.args(NAME, sandbox() + "doc.xml", "doc.xml")), testSession);
    no(new XQuery(_DB_PUT_BINARY.args(NAME, sandbox() + "file", "file")), testSession);
    // crypto:validate-signature: external references require ADMIN permission
    error(new XQuery(signature()), testSession, "basex:permission");
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
    // WRITE user cannot raise the inner query's permission to admin
    ok(new Grant("write", NAME), adminSession);
    error(new XQuery(_XQUERY_EVAL.args("1", " ()", " { 'permission': 'admin' }")),
        testSession, "[xquery:permission]", "admin");
    // READ user cannot raise the inner query's permission to write
    ok(new Grant("read", NAME), adminSession);
    error(new XQuery(_XQUERY_EVAL.args("1", " ()", " { 'permission': 'write' }")),
        testSession, "[xquery:permission]", "write");
  }

  /** Tests that a nested xquery:eval inherits the outer permission restriction. */
  @Test public void xqueryNestedPerm() {
    // outer eval restricts permissions to READ; inner eval must inherit this
    // and reject unparsed-text (function-level ADMIN permission).
    ok(new Grant("admin", NAME), adminSession);
    error(new XQuery(_XQUERY_EVAL.args(
        "xquery:eval(`unparsed-text('x.xq')`)",
        " ()", " { 'permission': 'read' }")),
        testSession, "[xquery:permission]", "admin");
  }

  /** Tests permissions of dynamically loaded code. */
  @Test public void dynamicCode() {
    ok(new Grant("read", NAME), adminSession);
    // modules supplied as content require no permissions, external locations require create
    ok(new XQuery(LOAD_XQUERY_MODULE.args("m", " { 'content': "
        + "'module namespace m = \"m\"; declare function m:f() { 1 };' }") + "?functions?*?*()"),
        testSession);
    final String load = LOAD_XQUERY_MODULE.args("m",
        " { 'location-hints': '" + sandbox() + "module.xqm' }");
    error(new XQuery(load), testSession, "[basex:permission]", "admin");
    // modules installed in the repository require no permissions
    ok(new XQuery(LOAD_XQUERY_MODULE.args("urn:isbn:12345") + "?variables?* = '123' or error()"),
        testSession);
    // functions looked up at runtime are only available with sufficient permissions
    final String lookup = "for $n in 'file:list' where random:double() >= 0 "
        + "return function-lookup(xs:QName($n), 1)";
    ok(new XQuery("empty(" + lookup + ") or error()"), testSession);

    ok(new Grant("admin", NAME), adminSession);
    ok(new XQuery(load + "?functions?*?*()"), testSession);
    ok(new XQuery("exists(" + lookup + ") or error()"), testSession);
  }

  /** Tests permissions of imported Java classes. */
  @Test public void javaImport() {
    final String timer = "import module namespace t = 'java:java.util.Timer'; 1";
    final String module = "import module namespace qm = "
        + "'java:org.basex.query.func.QueryModuleTest'; qm:convenient(1)";
    ok(new Grant("read", NAME), adminSession);
    error(new XQuery(timer), testSession, "[basex:permission]", "admin");
    ok(new XQuery(module), testSession);
    error(new XQuery(_XQUERY_EVAL.args(timer, " ()", " { 'permission': 'write' }")),
        adminSession, "[FODC0016]");

    ok(new Grant("admin", NAME), adminSession);
    ok(new XQuery(timer), testSession);
  }

  /** Tests the permission error raised when accessing external resources. */
  @Test public void permRequired() {
    // fn:collection bypasses the DTD-related permission check in Docs.check
    // and reaches QueryResources.create, which raises BASEX_PERMISSION_X_X.
    // Use a file:// URL so the path is not parsed as a database reference
    // (the first path segment of an absolute UNIX path would otherwise be
    // interpreted as the database name).
    final String url = sandbox().url() + "doc.xml";

    // READ user: missing ADMIN for an external file
    ok(new Grant("read", NAME), adminSession);
    error(new XQuery(COLLECTION.args(url)), testSession,
        "[basex:permission]", "admin");
    // NONE user: same code; ADMIN is still the missing permission for files
    ok(new Grant("none", NAME), adminSession);
    error(new XQuery(COLLECTION.args(url)), testSession,
        "[basex:permission]", "admin");
  }

  /** Tests that external entities and XIncludes in input strings require ADMIN permission. */
  @Test public void externalEntities() {
    final String url = new IOFile(sandbox() + "file").url();
    final String xxe = "<!DOCTYPE x [<!ENTITY e SYSTEM '" + url + "'>]><x>&e;</x>";
    final String xi = "<x xmlns:xi='http://www.w3.org/2001/XInclude'>"
        + "<xi:include href='" + url + "' parse='text'/></x>";
    final String string = '"' + xxe.replace("&", "&amp;") + '"';
    final String add = "db:add('" + NAME2 + "', " + string + ", 'x.xml', { 'dtd': true() })";
    final String addXi = "db:add('" + NAME2 + "', \"" + xi + "\", 'x.xml', { 'xinclude': true() })";
    final String fetch = "fetch:binary-doc(bin:encode-string(" + string + "), { 'dtd': true() })";

    ok(new Grant("write", NAME), adminSession);
    error(new XQuery(add), testSession, "trust-external");
    no(new XQuery(addXi), testSession);
    // trust-external is ignored: entity expansion remains limited
    final StringBuilder dtd = new StringBuilder("<!ENTITY e0 'xxxxxxxxxx'>");
    for(int e = 1; e <= 6; e++) {
      dtd.append("<!ENTITY e").append(e).append(" '").append(("&e" + (e - 1) + ';').repeat(10));
      dtd.append("'>");
    }
    final String laughs = "<!DOCTYPE x [" + dtd + "]><x>&e6;</x>";
    error(new XQuery("parse-xml(\"" + laughs.replace("&", "&amp;") + "\", "
        + "{ 'trust-external': true(), 'dtd': false() })"), testSession, "JAXP00010001");

    ok(new Grant("admin", NAME), adminSession);
    ok(new Set(MainOptions.XINCLUDE, true), testSession);
    ok(new Open(NAME2), testSession);
    ok(new Add("x.xml", xi), testSession);
    ok(new Set(MainOptions.DTD, true), testSession);
    ok(new Add("x.xml", xxe), testSession);
    ok(new XQuery(add), testSession);
    ok(new XQuery(fetch), testSession);
    ok(new Close(), testSession);
  }

  /** Tests that external entities in serialized XML of map-to-element require ADMIN permission. */
  @Test public void mapToElementEntities() {
    final String url = new IOFile(sandbox() + "file").url();
    final String query = "map-to-element({ 'a': "
        + "\"<!DOCTYPE x [<!ENTITY e SYSTEM '" + url + "'>]><x>&amp;e;</x>\" }, "
        + "{ 'plan': { 'a': { 'layout': 'xml' } } })";
    // code with reduced permissions ignores the trust-external option of the admin
    error(new XQuery("declare option db:trustexternal 'true'; " + _XQUERY_EVAL.args(
        " ``[" + query + "]``", " ()", " { 'permission': 'write' }")), adminSession,
        "trust-external");
  }

  /** Tests that options and functions accessing files require ADMIN permission. */
  @Test public void fileOptions() {
    final IOFile xsd = new IOFile(sandbox(), "x.xsd");
    write(xsd, "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
        + "<xs:element name='a'/></xs:schema>");
    final String xml = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
        + "xsi:noNamespaceSchemaLocation='" + xsd.url() + "'/>";
    final String add = "db:add('" + NAME2 + "', \"" + xml + "\", 'a.xml', "
        + "{ 'xsdvalidation': 'strict' })";

    ok(new Grant("write", NAME), adminSession);
    // schema locations in the input are not followed
    no(new XQuery(add), testSession);
    // options pointing to files
    no(new Set(MainOptions.STOPWORDS, sandbox() + "file"), testSession);
    no(new Set(MainOptions.WRITEBACK, true), testSession);
    no(new XQuery("declare option db:stopwords '" + sandbox() + "file'; 1"), testSession);
    no(new XQuery("(# db:writeback true #) { 1 }"), testSession);
    no(new XQuery(_DB_ADD.args(NAME2, " <a/>", "b.xml", " { 'stopwords': 'x' }")),
        testSession);
    // own local permissions and keystores
    error(new XQuery(_USER_DROP.args(NAME, NAME2)), testSession, "[basex:permission]");
    error(new XQuery(_CRYPTO_GENERATE_SIGNATURE.args(" <a/>", "", "", "", "", "",
        " <digital-certificate><keystore-uri>" + sandbox() + "file</keystore-uri>"
        + "</digital-certificate>")), testSession, "[basex:permission]");

    ok(new Grant("admin", NAME), adminSession);
    ok(new XQuery(add), testSession);
    ok(new Set(MainOptions.STOPWORDS, ""), testSession);
  }

  /** Tests that resources mapped by catalogs require ADMIN permission. */
  @Test public void catalogs() {
    final String catalog = FOLDER + "catalog/catalog.xml";
    final String xml = "<!DOCTYPE doc SYSTEM 'http://dtd.dtd'><doc>&x;</doc>";
    final String add = "db:add('" + NAME2 + "', \"" + xml.replace("&", "&amp;") + "\", 'c.xml', "
        + "{ 'catalog': '" + catalog + "', 'dtd': true() })";
    final String xsd = "declare option db:catalog '" + catalog + "'; xsd-validator({ 'schema': "
        + "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
        + "<xs:include schemaLocation='http://import.xsd'/></xs:schema> })(<a/>)?is-valid";

    ok(new Grant("write", NAME), adminSession);
    // catalogs can be assigned, but they are ignored
    no(new XQuery(add), testSession);
    no(new XQuery(xsd), testSession);

    ok(new Grant("admin", NAME), adminSession);
    ok(new Set(MainOptions.CATALOG, catalog), testSession);
    ok(new Set(MainOptions.DTD, true), testSession);
    ok(new Open(NAME2), testSession);
    ok(new Add("c.xml", xml), testSession);
    ok(new XQuery(add), testSession);
    ok(new Close(), testSession);
  }

  /** Tests that copying, renaming and restoring databases respects local permissions. */
  @Test public void localPermissions() {
    ok(new Grant(ADMIN, NAME), adminSession);
    ok(new CreateBackup(NAME2), adminSession);
    ok(new Grant("read", NAME, NAME2), adminSession);
    final String mine = NAME + "mine";
    ok(new CreateDB(mine), testSession);

    no(new Copy(NAME2, mine), testSession);
    no(new Copy(mine, NAME2), testSession);
    no(new AlterDB(NAME2, mine), testSession);
    no(new AlterDB(mine, NAME2), testSession);
    no(new Restore(NAME2), testSession);
    no(new AlterBackup(NAME2, mine), testSession);
    no(new CreateDB(NAME2), testSession);
    no(new CreateBackup(NAME2), testSession);
    ok(new DropDB(NAME2), testSession);
    no(new XQuery(_DB_COPY.args(NAME2, mine)), testSession);
    no(new XQuery(_DB_COPY.args(mine, NAME2)), testSession);
    no(new XQuery(_DB_ALTER.args(mine, NAME2)), testSession);
    no(new XQuery(_DB_ALTER_BACKUP.args(NAME2, mine)), testSession);
    ok(new XQuery(_DB_EXISTS.args(NAME2) + " or error()"), adminSession);

    // backups addressed by their full name
    final String backup = ok(new XQuery(_DB_BACKUPS.args(NAME2) + "[1] ! string()"), adminSession);
    no(new AlterBackup(backup, mine), testSession);
    ok(new DropBackup(backup), testSession);
    no(new XQuery(_DB_ALTER_BACKUP.args(backup, mine)), testSession);
    no(new XQuery(_DB_DROP_BACKUP.args(backup)), testSession);
    ok(new XQuery("exists(" + _DB_BACKUPS.args(NAME2) + ") or error()"), adminSession);
    // backups of inaccessible databases are not listed
    ok(new Grant("none", NAME, NAME2), adminSession);
    assertFalse(ok(new ShowBackups(), testSession).contains(NAME2), "backup listed");
    ok(new XQuery("empty(" + _DB_BACKUPS.args() + "[. = '" + backup + "']) or error()"),
        testSession);
    ok(new DropDB(mine), testSession);
    // general data requires global admin permissions
    ok(new Grant("write", NAME), adminSession);
    no(new CreateBackup(null), testSession);
    no(new DropBackup(null), testSession);
    no(new XQuery(_DB_CREATE_BACKUP.args("")), testSession);

    ok(new DropBackup(NAME2), adminSession);
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
   * @return result
   */
  private static String ok(final Command cmd, final Session s) {
    try {
      return s.execute(cmd);
    } catch(final IOException ex) {
      return fail(Util.message(ex));
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
    } catch(final IOException expected) {
      Util.debug(expected);
    }
  }

  /**
   * Returns a query that validates a signature with a reference to an external file.
   * @return query
   */
  private static String signature() {
    return _CRYPTO_VALIDATE_SIGNATURE.args(" <x><Signature "
      + "xmlns='http://www.w3.org/2000/09/xmldsig#'><SignedInfo>"
      + "<CanonicalizationMethod Algorithm='http://www.w3.org/2001/10/xml-exc-c14n#'/>"
      + "<SignatureMethod Algorithm='http://www.w3.org/2001/04/xmldsig-more#rsa-sha256'/>"
      + "<Reference URI='" + new IOFile(sandbox() + "file").url() + "'>"
      + "<DigestMethod Algorithm='http://www.w3.org/2001/04/xmlenc#sha256'/>"
      + "<DigestValue>AA==</DigestValue></Reference></SignedInfo>"
      + "<SignatureValue>AA==</SignatureValue></Signature></x>");
  }
}
