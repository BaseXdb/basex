package org.basex.test.core;

import static org.junit.Assert.*;
import org.basex.index.*;

import org.basex.core.*;
import org.basex.core.Progress.LockResult;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * This class tests commands and XQueries for correct identification of databases
 * to lock.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
public class CommandLockingTest extends SandboxTest {
  /** Test file name. */
  private static final String FN = "hello.xq";
  /** Test folder. */
  private static final String FLDR = "src/test/resources";
  /** Test file. */
  private static final String FILE = FLDR + '/' + FN;
  /** Test name. */
  private static final String NAME2 = NAME + '2';
  /** Context (empty string). */
  private static final String CTX = ""; // [JE] Replace empty string
  /** Test repository. **/
  private static final String REPO = "src/test/resources/repo/";
  /** Empty StringList. */
  private static final StringList NONE = new StringList(0);
  /** StringList containing name. */
  private static final StringList NAME_LIST = new StringList(NAME);
  /** StringList containing context. */
  private static final StringList CTX_LIST = new StringList(CTX);
  /** StringList containing name and context. */
  private static final StringList NAME_CTX = new StringList(NAME, CTX);
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
    ckDBs(new Add(FILE), true, CTX_LIST);
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
    ckDBs(new List(NAME), false, null);
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
    ckDBs(new ShowUsers(), false, null);
    ckDBs(new ShowUsers(NAME), false, ADMIN_NAME);
    ckDBs(new Store(FILE), true, CTX_LIST);
  }

  /** Tests locked databases in XQuery queries. */
  @Test public void xquery() {
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then $a else local:a($a idiv 2) };" +
        "local:a(5)"), false, NONE);
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then collection() else local:a($a idiv 2) };" +
        "local:a(5)"), false, null);
    ckDBs(new XQuery("declare function local:a($a) {" +
        "if($a = 0) then doc('" + NAME + "') else local:a($a idiv 2) };" +
        "local:a(5)"), false, NAME_LIST);
  }

  /**
   * Test if the right databases are identified for locking. Required databases are exact,
   * no additional ones allowed.
   *
   * [JE] Replace empty string
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
   * [JE] Replace empty string
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
   * [JE] Replace empty string
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
   * [JE] Replace empty string
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
    cmd.databases(lr);
    // Need sorted lists for compareAll
    for (StringList list : new StringList[]
        {reqRd, allowRd, reqWt, allowWt, lr.read, lr.write})
      if (null != list) list.sort(false, true).unique();

    // Test if read locking too much or less databases
    if(null == reqRd && !lr.readAll) fail("Should read lock all databases, didn't.");
    if(null != reqRd && !lr.read.containsAll(reqRd))
      fail("Didn't read lock all necessary databases.");
    if(null != allowRd && lr.readAll) fail("Read locked all databases, may not.");
    if(null != allowRd && !allowRd.containsAll(lr.read))
      fail("Read locked more databases than I should.");
    // Test if write locking too much or less databases
    if(null == reqWt && !lr.writeAll) fail("Should write lock all databases, didn't.");
    if(null != reqWt && !lr.write.containsAll(reqWt))
      fail("Didn't write lock all necessary databases.");
    if(null != allowWt && lr.writeAll) fail("write locked all databases, may not.");
    if(null != allowWt && !allowWt.containsAll(lr.write))
      fail("write locked more databases than I should.");
  }

}
