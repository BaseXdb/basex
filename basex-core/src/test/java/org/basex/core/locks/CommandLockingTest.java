package org.basex.core.locks;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.query.func.*;
import org.junit.Test;

/**
 * This class tests commands and XQueries for correct identification of databases to lock.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Jens Erat
 */
public final class CommandLockingTest extends SandboxTest {
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
  private static final LockList NONE = new LockList();
  /** StringList containing name. */
  private static final LockList NAME_LIST = new LockList().add(NAME);
  /** StringList containing context. */
  private static final LockList CTX_LIST = new LockList().add(Locking.CONTEXT);
  /** StringList containing name and context. */
  private static final LockList NAME_CTX = new LockList().add(NAME).add(Locking.CONTEXT);
  /** StringList containing USER lock string. */
  private static final LockList USER_LIST = new LockList().add(Locking.USER);
  /** StringList containing REPO lock string. */
  private static final LockList REPO_LIST = new LockList().add(Locking.REPO);
  /** StringList containing BACKUP lock string. */
  private static final LockList BACKUP_LIST = new LockList().add(Locking.BACKUP);
  /** StringList containing BACKUP lock string and name. */
  private static final LockList BACKUP_NAME =
      new LockList().add(Locking.BACKUP).add(NAME);
  /** StringList containing java module test lock strings. */
  private static final LockList MODULE_LIST = new LockList().add(Locking.JAVA_PREFIX
      + QueryModuleTest.LOCK1).add(Locking.JAVA_PREFIX + QueryModuleTest.LOCK2);

  /**
   * Test commands affecting databases.
   */
  @Test
  public void databaseCommands() {
    ckDBs(new Add(FILE, FILE), true, CTX_LIST);
    ckDBs(new AlterDB(NAME, NAME2), true, new LockList().add(NAME).add(NAME2));
    ckDBs(new AlterPassword(NAME, NAME), true, USER_LIST);
    ckDBs(new AlterUser(NAME, NAME), true, USER_LIST);
    ckDBs(new Check(NAME), false, NAME_CTX);
    ckDBs(new Close(), false, CTX_LIST);
    ckDBs(new Copy(NAME2, NAME), new LockList().add(NAME2), NAME_LIST);
    ckDBs(new CreateBackup(NAME), NAME_LIST, BACKUP_LIST);
    ckDBs(new CreateDB(NAME), CTX_LIST, NAME_LIST);
    ckDBs(new CreateIndex(IndexType.TEXT), true, CTX_LIST);
    ckDBs(new CreateUser(NAME, NAME), true, USER_LIST);
    ckDBs(new Delete(FILE), true, CTX_LIST);
    ckDBs(new DropBackup(NAME), true, BACKUP_LIST);
    ckDBs(new DropDB(NAME + '*'), true, null); // Drop using globbing
    ckDBs(new DropDB(NAME), true, NAME_LIST);
    ckDBs(new DropIndex(IndexType.TEXT), true, CTX_LIST);
    ckDBs(new DropUser(NAME), true, USER_LIST);
    ckDBs(new Execute("RUN " + FILE), false, null);
    ckDBs(new Export(FILE), false, CTX_LIST);
    ckDBs(new Find("token"), false, CTX_LIST);
    ckDBs(new Flush(), true, CTX_LIST);
    ckDBs(new Get("DBPATH"), false, NONE);
    ckDBs(new Grant("all", NAME), true, USER_LIST);
    ckDBs(new Grant("all", NAME, NAME), true, USER_LIST);
    ckDBs(new Grant("all", NAME, NAME + '*'), true, USER_LIST);
    ckDBs(new Help("HELP"), false, NONE);
    ckDBs(new Info(), false, NONE);
    ckDBs(new InfoDB(), false, CTX_LIST);
    ckDBs(new InfoIndex(), false, CTX_LIST);
    ckDBs(new InfoStorage(), false, CTX_LIST);
    ckDBs(new Inspect(), false, CTX_LIST);
    ckDBs(new JobsList(), false, NONE);
    ckDBs(new JobsResult("job0"), false, NONE);
    ckDBs(new JobsStop("job0"), false, NONE);
    ckDBs(new Kill(NAME), true, USER_LIST);
    ckDBs(new List(), false, null);
    ckDBs(new List(NAME), false, NAME_LIST);
    ckDBs(new Open(NAME), false, NAME_CTX);
    ckDBs(new Optimize(), true, CTX_LIST);
    ckDBs(new OptimizeAll(), true, CTX_LIST);
    ckDBs(new Password(NAME), true, USER_LIST);
    ckDBs(new Rename(FILE, FILE), true, CTX_LIST);
    ckDBs(new Replace(FILE, FILE), true, CTX_LIST);
    ckDBs(new RepoInstall(REPO + "/pkg3.xar", null), true, REPO_LIST);
    ckDBs(new RepoList(), false, REPO_LIST);
    ckDBs(new RepoDelete("http://www.pkg3.com", null), true, REPO_LIST);
    ckDBs(new Restore(NAME), true, BACKUP_NAME);
    ckDBs(new Retrieve(FILE), false, CTX_LIST);
    ckDBs(new Run(FILE), false, null);
    ckDBs(new Set(NAME, NAME), false, NONE);
    ckDBs(new ShowBackups(), false, BACKUP_LIST);
    ckDBs(new ShowSessions(), false, NONE);
    ckDBs(new ShowUsers(), false, NONE);
    ckDBs(new ShowUsers(NAME), false, NONE);
    ckDBs(new Store(FILE), true, CTX_LIST);
  }

  /** Tests locked databases in XQuery queries. */
  @Test
  public void xquery() {
    // Basic document access
    ckDBs(new XQuery(COLLECTION.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(COLLECTION.args()), false, CTX_LIST);
    // fn:collection() always accesses the global context, no matter what local context is
    ckDBs(new XQuery("<a/>/" + COUNT.args(COLLECTION.args())), false, CTX_LIST);
    ckDBs(new XQuery(DOC.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(DOC.args("http://abc.de/")), false, NONE);
    ckDBs(new XQuery(ID.args(NAME)), false, CTX_LIST);
    ckDBs(new XQuery(IDREF.args(NAME)), false, CTX_LIST);
    ckDBs(new XQuery(ELEMENT_WITH_ID.args(NAME)), false, CTX_LIST);
    ckDBs(new XQuery(LANG.args(NAME)), false, CTX_LIST);
    ckDBs(new XQuery(ID.args(NAME, DOC.args(NAME))), false, NAME_LIST);
    ckDBs(new XQuery(IDREF.args(NAME, DOC.args(NAME))), false, NAME_LIST);
    ckDBs(new XQuery(ELEMENT_WITH_ID.args(NAME, DOC.args(NAME))), false, NAME_LIST);
    ckDBs(new XQuery(LANG.args(NAME, DOC.args(NAME))), false, NAME_LIST);
    ckDBs(new XQuery(DOC_AVAILABLE.args(NAME + "/foo.xml")), false, NAME_LIST, null);
    ckDBs(new XQuery(PARSE_XML.args(" <foo/>")), true, NONE);
    ckDBs(new XQuery(PARSE_XML_FRAGMENT.args(" <foo/>")), true, NONE);
    ckDBs(new XQuery(PUT.args(" <foo/>", NAME)), true, NONE);
    ckDBs(new XQuery(PUT.args(" .", NAME)), true, CTX_LIST);
    ckDBs(new XQuery(ROOT.args()), false, CTX_LIST);
    ckDBs(new XQuery(ROOT.args(" .")), false, CTX_LIST);
    ckDBs(new XQuery(ROOT.args(" ./test")), false, CTX_LIST);
    ckDBs(new XQuery(ROOT.args(" <foo/>")), true, NONE);
    ckDBs(new XQuery(UNPARSED_TEXT.args(FILE)), false, NONE);
    ckDBs(new XQuery(UNPARSED_TEXT_AVAILABLE.args(FILE)), false, NONE);
    ckDBs(new XQuery(UNPARSED_TEXT_LINES.args(FILE)), false, NONE);
    ckDBs(new XQuery(URI_COLLECTION.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(URI_COLLECTION.args()), false, CTX_LIST);

    // Accessor and node functions
    for(final Function fn : new Function[] { DATA, STRING, NUMBER, STRING_LENGTH, NORMALIZE_SPACE,
        DOCUMENT_URI, NILLED, NODE_NAME, LOCAL_NAME, Function.NAME, NAMESPACE_URI, ROOT, BASE_URI,
        GENERATE_ID, HAS_CHILDREN, PATH }) {
      ckDBs(new XQuery(fn.args()), false, CTX_LIST);
      ckDBs(new XQuery(DOC.args(NAME) + '/' + fn.args()), false, NAME_LIST, NAME_CTX);
    }
    for(final Function fn : new Function[] { DATA, STRING, NUMBER, STRING_LENGTH, NORMALIZE_SPACE,
        DOCUMENT_URI, NILLED, NODE_NAME, LOCAL_NAME, Function.NAME, NAMESPACE_URI, ROOT, BASE_URI,
        GENERATE_ID, HAS_CHILDREN, PATH }) {
      ckDBs(new XQuery(fn.args(DOC.args(NAME))), false, NAME_LIST);
    }

    // Others
    ckDBs(new XQuery("."), false, CTX_LIST);
    ckDBs(new XQuery(ERROR.args()), false, NONE);
    ckDBs(new XQuery(ERROR.args(" xs:QName('foo')")), false, NONE);
    ckDBs(new XQuery(ERROR.args(" xs:QName('foo')", "bar")), false, NONE);
    ckDBs(new XQuery(ERROR.args(" xs:QName('foo')", "bar", " <batz/>")), false, NONE);
    ckDBs(new XQuery(_RANDOM_INTEGER.args()), false, NONE);

    // User defined functions
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then $a else local:a($a idiv 2) };" +
        "local:a(5)"), false, NONE);
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then " + COLLECTION.args() + " else local:a($a idiv 2) };" +
        "local:a(5)"), false, CTX_LIST);
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then " + DOC.args(NAME) + " else local:a($a idiv 2) };" +
        "local:a(5)"), false, NAME_LIST);

    // Java module function test. Locks are added at compilation, so execute each query
    // to get it compiled.
    XQuery query = new XQuery(
        "import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:readLock()");
    execute(query);
    ckDBs(query, false, MODULE_LIST);
    query = new XQuery(
        "import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:writeLock()");
    execute(query);
    ckDBs(query, true, MODULE_LIST);
  }

  /** Test admin module. */
  @Test
  public void admin() {
    ckDBs(new XQuery(_ADMIN_SESSIONS.args()), false, NONE);
    ckDBs(new XQuery(_ADMIN_LOGS.args()), false, NONE);
  }

  /** Test user module. */
  @Test
  public void user() {
    ckDBs(new XQuery(_USER_LIST.args()), false, NONE);
    ckDBs(new XQuery(_USER_LIST_DETAILS.args()), false, NONE);
  }

  /** Test database module. */
  @Test
  public void db() {
    // General Functions
    ckDBs(new XQuery(_DB_INFO.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_LIST.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_LIST_DETAILS.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_LIST_DETAILS.args()), false, null);
    ckDBs(new XQuery(_DB_OPEN.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_OPEN_ID.args(NAME, 0)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_OPEN_PRE.args(NAME, 0)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_SYSTEM.args()), false, NONE);

    // Read Operations
    ckDBs(new XQuery(_DB_ATTRIBUTE.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_DB_ATTRIBUTE.args(NAME, 23, 42)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_NODE_ID.args(" .")), false, CTX_LIST);
    ckDBs(new XQuery(_DB_NODE_PRE.args(" .")), false, CTX_LIST);
    ckDBs(new XQuery(_DB_RETRIEVE.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_DB_TEXT.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_DB_TEXT_RANGE.args(NAME, 23, 42)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_TOKEN.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_DB_TOKEN.args(NAME, 23, 42)), false, NAME_LIST);

    // Updates
    ckDBs(new XQuery(_DB_CREATE.args(NAME)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_CREATE.args(NAME, FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_CREATE.args(NAME, " <foo/>", FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_CREATE.args(NAME, FILE, FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_DROP.args(NAME)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_ADD.args(NAME, FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_ADD.args(NAME, " <foo/>", FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_ADD.args(NAME, FILE, FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_DELETE.args(NAME, FILE)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_OPTIMIZE.args(NAME)), true, NAME_LIST);
    ckDBs(new XQuery(_DB_OPTIMIZE.args(NAME, "true()")), true, NAME_LIST);
    ckDBs(new XQuery(_DB_RENAME.args(NAME, FILE, FILE + '2')), true, NAME_LIST);
    ckDBs(new XQuery(_DB_REPLACE.args(NAME, FILE, FILE + '2')), true, NAME_LIST);
    ckDBs(new XQuery(_DB_STORE.args(NAME, FILE, "foo")), true, NAME_LIST);
    ckDBs(new XQuery(_DB_FLUSH.args(NAME)), true, NAME_LIST);

    // Helper Functions
    ckDBs(new XQuery(_DB_EXISTS.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_IS_RAW.args(NAME, FILE)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_IS_XML.args(NAME, FILE)), false, NAME_LIST);
    ckDBs(new XQuery(_DB_CONTENT_TYPE.args(NAME, FILE)), false, NAME_LIST);
  }

  /** Test ft module. */
  @Test
  public void ft() {
    ckDBs(new XQuery(_FT_SEARCH.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_FT_TOKENS.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_FT_TOKENS.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_FT_TOKENIZE.args("foo")), false, NONE);
  }

  /** Test index module. */
  @Test
  public void index() {
    ckDBs(new XQuery(_INDEX_FACETS.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_TEXTS.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_TEXTS.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_TEXTS.args(NAME, "foo", "true()")), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_ATTRIBUTES.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_ATTRIBUTES.args(NAME, "foo")), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_ATTRIBUTES.args(NAME, "foo", "true()")), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_ELEMENT_NAMES.args(NAME)), false, NAME_LIST);
    ckDBs(new XQuery(_INDEX_ATTRIBUTE_NAMES.args(NAME)), false, NAME_LIST);
  }

  /** Update module. */
  @Test
  public void update() {
    ckDBs(new XQuery(_UPDATE_OUTPUT.args("foo")), true, NONE);
  }

  /** Test repository module. */
  @Test
  public void repository() {
    ckDBs(new XQuery(_REPO_LIST.args()), false, REPO_LIST);
  }

  /** Test XQuery module. */
  @Test
  public void xqueryModule() {
    ckDBs(new XQuery(_XQUERY_EVAL.args("1")), false, null);
    ckDBs(new XQuery(_XQUERY_EVAL.args(FILE)), false, null);
  }

  /**
   * Test if the right databases are identified for locking. Required databases are exact,
   * no additional ones allowed.
   * Pass empty string for currently opened database, {@code null} for all.
   * @param cmd command to test
   * @param up updating command?
   * @param dbs required and allowed databases (can be {@code null})
   */
  private static void ckDBs(final Command cmd, final boolean up, final LockList dbs) {
    ckDBs(cmd, up, dbs, dbs);
  }

  /**
   * Test if the right databases are identified for locking. Required databases are exact,
   * no additional ones allowed.
   * Pass empty string for currently opened database, {@code null} for all.
   * @param cmd command to test
   * @param read required and allowed databases for read lock (can be {@code null})
   * @param write required and allowed databases for write lock (can be {@code null})
   */
  private static void ckDBs(final Command cmd, final LockList read, final LockList write) {
    ckDBs(cmd, read, read, write, write);
  }

  /**
   * Test if the right databases are identified for locking.
   * Pass empty string for currently opened database, {@code null} for all.
   * @param cmd command to test
   * @param up updating command?
   * @param req required databases (can be {@code null})
   * @param allow allowed databases (can be {@code null})
   */
  private static void ckDBs(final Command cmd, final boolean up, final LockList req,
      final LockList allow) {
    ckDBs(cmd, up ? NONE : req, up ? NONE : allow, up ? req : NONE, up ? allow : NONE);
  }

  /**
   * Test if the right databases are identified for locking.
   * Pass empty string for currently opened database, {@code null} for all.
   * @param cmd command to test
   * @param reqRd required databases for read locks (can be {@code null})
   * @param allowRd allowed databases for read locks (can be {@code null})
   * @param reqWt required databases for write locks (can be {@code null})
   * @param allowWt allowed databases for write locks (can be {@code null})
   */
  private static void ckDBs(final Command cmd, final LockList reqRd, final LockList allowRd,
      final LockList reqWt, final LockList allowWt) {

    // Fetch databases BaseX thinks it needs to lock
    cmd.updating(DUMMY_CONTEXT);
    cmd.addLocks();

    final Locks locks = cmd.jc().locks;
    cmd.jc().locks.finish(context);

    for(final LockList list : new LockList[] { reqRd, allowRd, reqWt, allowWt }) {
      if(list != null) list.finish(null);
    }

    // Test if read locking too much or less databases
    if(reqRd == null && !locks.reads.global()) fail("Should read lock all databases, didn't.");
    if(reqRd != null && allowRd != null && !containsAll(locks.reads, reqRd))
      fail("Didn't read lock all necessary databases.");
    if(allowRd != null && locks.reads.global()) fail("Read locked all databases, may not.");
    if(allowRd != null && !containsAll(allowRd, locks.reads))
      fail("Read locked more databases than I should.");
    // Test if write locking too much or less databases
    if(reqWt == null && !locks.writes.global()) fail("Should write lock all databases, didn't.");
    if(reqWt != null && allowWt != null && !containsAll(locks.writes, reqWt))
      fail("Didn't write lock all necessary databases.");
    if(allowWt != null && locks.writes.global()) fail("Write locked all databases, may not.");
    if(allowWt != null && !containsAll(allowWt, locks.writes))
      fail("Write locked more databases than I should.");
  }

  /**
   * Check if all elements of the second list are contained in the first.
   * @param list1 first list
   * @param list2 second list
   * @return result of check
   */
  private static boolean containsAll(final LockList list1, final LockList list2) {
    for(final String lock : list2) {
      if(!list1.contains(lock)) return false;
    }
    return true;
  }

}
