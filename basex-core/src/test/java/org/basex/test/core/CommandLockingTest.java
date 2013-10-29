package org.basex.test.core;

import static org.junit.Assert.*;
import org.basex.index.*;

import org.basex.core.*;
import org.basex.core.LockResult;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * This class tests commands and XQueries for correct identification of databases
 * to lock.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Jens Erat
 */
public class CommandLockingTest extends SandboxTest {
  /** Static dummy context so we do not have to create a new one every time. */
  private static final Context DUMMY_CONTEXT = new Context();
  /** Test file name. */
  private static final String FN = "hello.xq";
  /** Test folder. */
  private static final String FLDR = "src/test/resources";
  /** Test file. */
  private static final String FILE = FLDR + '/' + FN;
  /** Test name. */
  private static final String NAME2 = NAME + '2';
  /** Test repository. **/
  private static final String REPO = "src/test/resources/repo/";
  /** Empty StringList. */
  private static final StringList NONE = new StringList(0);
  /** StringList containing name. */
  private static final StringList NAME_LIST = new StringList(NAME);
  /** StringList containing context. */
  private static final StringList CTX_LIST = new StringList(DBLocking.CTX);
  /** StringList containing collection. */
  private static final StringList COLL_LIST = new StringList(DBLocking.COLL);
  /** StringList containing name and context. */
  private static final StringList NAME_CTX = new StringList(NAME, DBLocking.CTX);
  /** StringList containing ADMIN lock string. */
  private static final StringList ADMIN_LIST = new StringList(DBLocking.ADMIN);
  /** StringList containing ADMIN lock string and name. */
  private static final StringList ADMIN_NAME = new StringList(DBLocking.ADMIN, NAME);
  /** StringList containing REPO lock string. */
  private static final StringList REPO_LIST = new StringList(DBLocking.REPO);
  /** StringList containing BACKUP lock string. */
  private static final StringList BACKUP_LIST = new StringList(DBLocking.BACKUP);
  /** StringList containing BACKUP lock string and name. */
  private static final StringList BACKUP_NAME = new StringList(DBLocking.BACKUP, NAME);
  /** StringList containing EVENT lock string. */
  private static final StringList EVENT_LIST = new StringList(DBLocking.EVENT);

  /**
   * Test commands affecting databases.
   */
  @Test
  public final void databaseCommands() {
    ckDBs(new Add(FILE, FILE), true, CTX_LIST);
    ckDBs(new AlterDB(NAME, NAME2), true, new StringList(NAME, NAME2));
    ckDBs(new AlterUser(NAME, NAME), true, ADMIN_LIST);
    ckDBs(new Check(NAME), false, NAME_CTX);
    ckDBs(new Close(), false, CTX_LIST);
    ckDBs(new Copy(NAME2, NAME), new StringList(NAME2), NAME_LIST);
    ckDBs(new CreateBackup(NAME), NAME_LIST, BACKUP_LIST);
    ckDBs(new CreateDB(NAME), CTX_LIST, NAME_LIST);
    ckDBs(new CreateEvent(NAME), true, EVENT_LIST);
    ckDBs(new CreateIndex(IndexType.TEXT), true, CTX_LIST);
    ckDBs(new CreateUser(NAME, NAME), true, ADMIN_LIST);
    ckDBs(new Delete(FILE), true, CTX_LIST);
    ckDBs(new DropBackup(NAME), true, BACKUP_LIST);
    ckDBs(new DropDB(NAME + "*"), true, null); // Drop using globbing
    ckDBs(new DropDB(NAME), true, NAME_LIST);
    ckDBs(new DropEvent(NAME), true, EVENT_LIST);
    ckDBs(new DropIndex(IndexType.TEXT), true, CTX_LIST);
    ckDBs(new DropUser(NAME), true, ADMIN_LIST);
    ckDBs(new Execute("RUN " + FILE), true, null);
    ckDBs(new Export(FILE), false, CTX_LIST);
    ckDBs(new Find("token"), false, CTX_LIST);
    ckDBs(new Flush(), true, CTX_LIST);
    ckDBs(new Get("DBPATH"), false, NONE);
    ckDBs(new Grant("all", NAME), true, ADMIN_LIST);
    ckDBs(new Grant("all", NAME, NAME), true, ADMIN_NAME);
    ckDBs(new Grant("all", NAME, NAME + "*"), true, null);
    ckDBs(new Help("HELP"), false, NONE);
    ckDBs(new Info(), false, NONE);
    ckDBs(new InfoDB(), false, CTX_LIST);
    ckDBs(new InfoIndex(), false, CTX_LIST);
    ckDBs(new InfoStorage(), false, CTX_LIST);
    ckDBs(new Inspect(), false, CTX_LIST);
    ckDBs(new Kill(NAME), true, ADMIN_LIST);
    ckDBs(new List(), false, null);
    ckDBs(new List(NAME), false, NAME_LIST);
    ckDBs(new Open(NAME), false, NAME_CTX);
    ckDBs(new Optimize(), true, CTX_LIST);
    ckDBs(new OptimizeAll(), true, CTX_LIST);
    ckDBs(new Password(NAME), true, ADMIN_LIST);
    ckDBs(new Rename(FILE, FILE), true, CTX_LIST);
    ckDBs(new Replace(FILE, FILE), true, CTX_LIST);
    ckDBs(new RepoInstall(REPO + "/pkg3.xar", null), true, REPO_LIST);
    ckDBs(new RepoList(), false, REPO_LIST);
    ckDBs(new RepoDelete("http://www.pkg3.com", null), true, REPO_LIST);
    ckDBs(new Restore(NAME), true, BACKUP_NAME);
    ckDBs(new Retrieve(FILE), false, CTX_LIST);
    ckDBs(new Run(FILE), true, null);
    ckDBs(new Set(NAME, NAME), false, NONE);
    ckDBs(new ShowBackups(), false, BACKUP_LIST);
    ckDBs(new ShowEvents(), false, EVENT_LIST);
    ckDBs(new ShowSessions(), false, NONE);
    ckDBs(new ShowUsers(), false, ADMIN_LIST);
    ckDBs(new ShowUsers(NAME), false, ADMIN_NAME);
    ckDBs(new Store(FILE), true, CTX_LIST);
  }

  /** Tests locked databases in XQuery queries. */
  @Test
  public void xquery() {
    // Basic document access
    ckDBs(new XQuery("collection('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("collection()"), false, COLL_LIST);
    // fn:collection() always accesses the global context, no matter what local context is
    ckDBs(new XQuery("<a/>/count(collection())"), false, COLL_LIST);
    ckDBs(new XQuery("doc('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("doc-available('" + NAME + "/foo.xml')"), false, NAME_LIST, null);
    ckDBs(new XQuery("parse-xml('<foo/>')"), true, NONE);
    ckDBs(new XQuery("parse-xml-fragment('<foo/>')"), true, NONE);
    ckDBs(new XQuery("put(<foo/>, '" + NAME + "')"), true, NONE);
    ckDBs(new XQuery("put(., '" + NAME + "')"), true, CTX_LIST);
    ckDBs(new XQuery("root()"), false, CTX_LIST);
    ckDBs(new XQuery("root(.)"), false, CTX_LIST);
    ckDBs(new XQuery("root(./test)"), false, CTX_LIST);
    ckDBs(new XQuery("serialize('<foo/>')"), true, NONE);
    ckDBs(new XQuery("unparsed-text('" + FILE + "')"), false, NONE);
    ckDBs(new XQuery("unparsed-text-available('" + FILE + "')"), false, NONE);
    ckDBs(new XQuery("unparsed-text-lines('" + FILE + "')"), false, NONE);
    ckDBs(new XQuery("uri-collection('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("uri-collection()"), false, COLL_LIST);

    // Accessor and node functions
    for(final String fn : new String[] { "data", "string", "number", "string-length",
        "normalize-space", "document-uri", "nilled", "node-name", "local-name", "name",
        "namespace-uri", "root", "base-uri", "generate-id", "has-children", "path"}) {
      ckDBs(new XQuery(fn + "()"), false, CTX_LIST);
      ckDBs(new XQuery("doc('" + NAME + "')/" + fn + "()"), false, NAME_LIST, NAME_CTX);
    }
    for(final String fn : new String[] { "data", "string", "number", "string-length",
        "normalize-space", "document-uri", "nilled", "node-name", "local-name", "name",
        "namespace-uri", "root", "base-uri", "generate-id", "has-children", "path"}) {
      ckDBs(new XQuery(fn + "(doc('" + NAME + "'))"), false, NAME_LIST);
    }

    // Others
    ckDBs(new XQuery("."), false, CTX_LIST);
    ckDBs(new XQuery("error()"), false, NONE);
    ckDBs(new XQuery("error(xs:QName('foo'))"), false, NONE);
    ckDBs(new XQuery("error(xs:QName('foo'), 'bar')"), false, NONE);
    ckDBs(new XQuery("error(xs:QName('foo'), 'bar', <batz/>)"), false, NONE);
    ckDBs(new XQuery("random:integer()"), false, NONE);

    // User defined functions
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then $a else local:a($a idiv 2) };" +
        "local:a(5)"), false, NONE);
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then collection() else local:a($a idiv 2) };" +
        "local:a(5)"), false, COLL_LIST);
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then doc('" + NAME + "') else local:a($a idiv 2) };" +
        "local:a(5)"), false, NAME_LIST);
  }

  /** Test admin module. */
  @Test
  public void admin() {
    ckDBs(new XQuery("admin:users()"), false, ADMIN_LIST);
    ckDBs(new XQuery("admin:users('" + NAME + "')"), false, ADMIN_NAME);
    ckDBs(new XQuery("admin:sessions()"), false, ADMIN_LIST);
    ckDBs(new XQuery("admin:logs()"), false, NONE);
  }

  /** Test database module. */
  @Test
  public void db() {
    // General Functions
    ckDBs(new XQuery("db:info('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:list('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:list()"), false, null);
    ckDBs(new XQuery("db:list-details('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:list-details()"), false, null);
    ckDBs(new XQuery("db:open('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:open-id('" + NAME + "', 0)"), false, NAME_LIST);
    ckDBs(new XQuery("db:open-pre('" + NAME + "', 0)"), false, NAME_LIST);
    ckDBs(new XQuery("db:system()"), false, NONE);

    // Read Operations
    ckDBs(new XQuery("db:attribute('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("db:attribute-range('" + NAME + "', '23', '42')"), false, NAME_LIST);
    ckDBs(new XQuery("db:node-id(.)"), false, CTX_LIST);
    ckDBs(new XQuery("db:node-pre(.)"), false, CTX_LIST);
    ckDBs(new XQuery("db:retrieve('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("db:text('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("db:text-range('" + NAME + "', '23', '42')"), false, NAME_LIST);

    // Updates
    ckDBs(new XQuery("db:create('" + NAME + "')"), true, NAME_LIST);
    ckDBs(new XQuery("db:create('" + NAME + "', '" + FILE + "')"), true, NAME_LIST);
    ckDBs(new XQuery("db:create('" + NAME + "', '<foo/>', '" + FILE + "')"), true,
        NAME_LIST);
    ckDBs(new XQuery("db:create('" + NAME + "', '" + FILE + "', '" + FILE + "')"), true,
        NAME_LIST);
    ckDBs(new XQuery("db:drop('" + NAME + "')"), true, NAME_LIST);
    ckDBs(new XQuery("db:add('" + NAME + "', '" + FILE + "')"), true, NAME_LIST);
    ckDBs(new XQuery("db:add('" + NAME + "', '<foo/>', '" + FILE + "')"), true,
        NAME_LIST);
    ckDBs(new XQuery("db:add('" + NAME + "', '" + FILE + "', '" + FILE + "')"), true,
        NAME_LIST);
    ckDBs(new XQuery("db:delete('" + NAME + "', '" + FILE + "')"), true, NAME_LIST);
    ckDBs(new XQuery("db:optimize('" + NAME + "')"), true, NAME_LIST);
    ckDBs(new XQuery("db:optimize('" + NAME + "', true())"), true, NAME_LIST);
    ckDBs(new XQuery("db:rename('" + NAME + "', '" + FILE + "', '" + FILE + "2')"), true,
        NAME_LIST);
    ckDBs(new XQuery("db:replace('" + NAME + "', '" + FILE + "', '" + FILE + "2')"), true,
        NAME_LIST);
    ckDBs(new XQuery("db:store('" + NAME + "', '" + FILE + "', 'foo')"), true, NAME_LIST);
    ckDBs(new XQuery("db:output('foo')"), true, NONE);
    ckDBs(new XQuery("db:flush('" + NAME + "')"), true, NAME_LIST);

    // Helper Functions
    ckDBs(new XQuery("db:exists('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:is-raw('" + NAME + "', '" + FILE + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:is-xml('" + NAME + "', '" + FILE + "')"), false, NAME_LIST);
    ckDBs(new XQuery("db:content-type('" + NAME + "', '" + FILE + "')"), false,
        NAME_LIST);
    ckDBs(new XQuery("db:event('foo', 'bar')"), false, NONE);
    ckDBs(new XQuery("db:event('foo', doc('" + NAME + "'))"), false, NAME_LIST);
  }

  /** Test ft module. */
  @Test
  public void ft() {
    ckDBs(new XQuery("ft:search('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("ft:tokens('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("ft:tokens('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("ft:tokenize('foo')"), false, NONE);
  }

  /** Test index module. */
  @Test
  public void index() {
    ckDBs(new XQuery("index:facets('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("index:texts('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("index:texts('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("index:texts('" + NAME + "', 'foo', true())"), false, NAME_LIST);
    ckDBs(new XQuery("index:attributes('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("index:attributes('" + NAME + "', 'foo')"), false, NAME_LIST);
    ckDBs(new XQuery("index:attributes('" + NAME + "', 'foo', true())"), false,
        NAME_LIST);
    ckDBs(new XQuery("index:element-names('" + NAME + "')"), false, NAME_LIST);
    ckDBs(new XQuery("index:attribute-names('" + NAME + "')"), false, NAME_LIST);
  }

  /** Test repository module. */
  @Test
  public void repository() {
//    ckDBs(new XQuery("repo:install('" + FILE + "')"), true, REPO_LIST);
//    ckDBs(new XQuery("repo:install('foo')"), true, REPO_LIST);
    ckDBs(new XQuery("repo:list()"), false, REPO_LIST);
  }

  /** Test XQuery module. */
  @Test
  public void xqueryModule() {
    ckDBs(new XQuery("xquery:eval('1')"), false, null);
    ckDBs(new XQuery("xquery:eval('" + FILE + "')"), false, null);
    ckDBs(new XQuery("xquery:type(doc('" + NAME + "'))"), false, NAME_LIST);
  }

  /**
   * Test if the right databases are identified for locking. Required databases are exact,
   * no additional ones allowed.
   *
   * Pass empty string for currently opened database, {@code null} for all.
   *
   * @param cmd Command to test
   * @param up Updating command?
   * @param dbs Required and allowed databases
   */
  private void ckDBs(final Command cmd, final boolean up, final StringList dbs) {
    ckDBs(cmd, up, dbs, dbs);
  }

  /**
   * Test if the right databases are identified for locking. Required databases are exact,
   * no additional ones allowed.
   *
   * Pass empty string for currently opened database, {@code null} for all.
   *
   * @param cmd Command to test
   * @param read Required and allowed databases for read lock
   * @param write Required and allowed databases for write lock
   */
  private void ckDBs(final Command cmd, final StringList read, final StringList write) {
    ckDBs(cmd, read, read, write, write);
  }

  /**
   * Test if the right databases are identified for locking.
   *
   * Pass empty string for currently opened database, {@code null} for all.
   *
   * @param cmd Command to test
   * @param up Updating command?
   * @param req Required databases
   * @param allow Allowed databases
   */
  private void ckDBs(final Command cmd, final boolean up, final StringList req,
      final StringList allow) {
    ckDBs(cmd, up ? NONE : req, up ? NONE : allow, up ? req : NONE, up ? allow : NONE);
  }

  /**
   * Test if the right databases are identified for locking.
   *
   * Pass empty string for currently opened database, {@code null} for all.
   *
   * @param cmd Command to test
   * @param reqRd Required databases for read locks
   * @param allowRd Allowed databases for read locks
   * @param reqWt Required databases for write locks
   * @param allowWt Allowed databases for write locks
   */
  private void ckDBs(final Command cmd, final StringList reqRd, final StringList allowRd,
      final StringList reqWt, final StringList allowWt) {
    // Fetch databases BaseX thinks it needs to lock
    final LockResult lr = new LockResult();
    // [CG] cmd.updating needed because of some side-effects (instantiate QueryProcessor?)
    cmd.updating(DUMMY_CONTEXT);
    cmd.databases(lr);
    // Need sorted lists for compareAll
    for (final StringList list : new StringList[]
        {reqRd, allowRd, reqWt, allowWt, lr.read, lr.write})
      if (null != list) list.sort(false).unique();

    // Test if read locking too much or less databases
    if(null == reqRd && !lr.readAll) fail("Should read lock all databases, didn't.");
    if(null != reqRd && null != allowRd && !lr.read.containsAll(reqRd))
      fail("Didn't read lock all necessary databases.");
    if(null != allowRd && lr.readAll) fail("Read locked all databases, may not.");
    if(null != allowRd && !allowRd.containsAll(lr.read))
      fail("Read locked more databases than I should.");
    // Test if write locking too much or less databases
    if(null == reqWt && !lr.writeAll) fail("Should write lock all databases, didn't.");
    if(null != reqWt && null != allowWt && !lr.write.containsAll(reqWt))
      fail("Didn't write lock all necessary databases.");
    if(null != allowWt && lr.writeAll) fail("Write locked all databases, may not.");
    if(null != allowWt && !allowWt.containsAll(lr.write))
      fail("Write locked more databases than I should.");
  }
}
