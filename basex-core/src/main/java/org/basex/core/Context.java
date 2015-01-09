package org.basex.core;

import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.random.*;
import org.basex.query.util.pkg.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serves as a central database context.
 * It references the currently opened database, options, client sessions,
 * users and other meta data. Next, the instance of this class will be passed on to
 * all operations, as it organizes concurrent data access, ensuring that no
 * process will concurrently write to the same data instances.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Context {
  /** Client listener. Set to {@code null} in standalone/server mode. */
  public final ClientListener listener;
  /** Blocked clients. */
  public final ClientBlocker blocker;
  /** Options. */
  public final MainOptions options = new MainOptions();
  /** Static options. */
  public final StaticOptions soptions;
  /** Client sessions. */
  public final Sessions sessions;
  /** Event pool. */
  public final Events events;
  /** Opened databases. */
  public final Datas dbs;
  /** Users. */
  public final Users users;
  /** Package repository. */
  public final Repo repo;
  /** Databases list. */
  public final Databases databases;

  /** Log. */
  public final Log log;

  /** Current node context. Set if it does not contain all documents of the current database. */
  private DBNodes current;
  /** Process locking. */
  private final Locking locks;
  /** User reference. */
  private User user;
  /** Data reference. */
  private Data data;

  // GUI references

  /** Marked nodes. */
  public DBNodes marked;
  /** Copied nodes. */
  public DBNodes copied;
  /** Focused node. */
  public int focused = -1;

  /**
   * Default constructor, which is usually called once in the lifetime of a project.
   */
  public Context() {
    this(true);
  }

  /**
   * Default constructor, which is usually called once in the lifetime of a project.
   * @param file retrieve options from disk
   */
  public Context(final boolean file) {
    this(new StaticOptions(file));
  }

  /**
   * Constructor, called by clients, and adopting the variables of the main process.
   * The {@link #user} reference must be set after calling this method.
   * @param ctx context of the main process
   * @param listener client listener
   */
  public Context(final Context ctx, final ClientListener listener) {
    this.listener = listener;
    soptions = ctx.soptions;
    dbs = ctx.dbs;
    events = ctx.events;
    sessions = ctx.sessions;
    databases = ctx.databases;
    blocker = ctx.blocker;
    locks = ctx.locks;
    users = ctx.users;
    repo = ctx.repo;
    log = ctx.log;
  }

  /**
   * Private constructor.
   * @param soptions static options
   */
  private Context(final StaticOptions soptions) {
    this.soptions = soptions;
    dbs = new Datas();
    events = new Events();
    sessions = new Sessions();
    blocker = new ClientBlocker();
    databases = new Databases(soptions);
    locks = soptions.get(StaticOptions.GLOBALLOCK) ? new ProcLocking(soptions) :
      new DBLocking(soptions);
    users = new Users(soptions);
    repo = new Repo(soptions);
    log = new Log(soptions);
    user = users.get(UserText.ADMIN);
    listener = null;
  }

  /**
   * Returns the user of this context.
   * @return user
   */
  public User user() {
    return user;
  }

  /**
   * Sets the user of this context. This method can only be called once.
   * @param us user
   */
  public void user(final User us) {
    if(user != null) throw Util.notExpected("User has already been assigned.");
    user = us;
  }

  /**
   * Closes the database context. Must only be called on the global database context,
   * and not on client instances.
   */
  public synchronized void close() {
    while(!sessions.isEmpty()) sessions.get(0).quit();
    dbs.close();
    log.close();
  }

  /**
   * Returns {@code true} if a data reference exists and if the current node set contains
   * all documents.
   * @return result of check
   */
  public boolean root() {
    return data != null && current == null;
  }

  /**
   * Returns the current data reference.
   * @return data reference
   */
  public Data data() {
    return data;
  }

  /**
   * Returns the current node context.
   * @return node set
   */
  public DBNodes current() {
    if(data == null) return null;
    if(current != null) return current;
    final DBNodes nodes = new DBNodes(data, data.resources.docs().toArray());
    nodes.all = true;
    return nodes;
  }

  /**
   * Sets the current node context. Discards the input if it contains all document
   * nodes of the currently opened database.
   * @param curr node set
   */
  public void current(final DBNodes curr) {
    current = curr.discardDocs();
  }

  /**
   * Sets the specified data instance as current database.
   * @param dt data reference
   */
  public void openDB(final Data dt) {
    data = dt;
    copied = null;
    set(null, new DBNodes(dt));
  }

  /**
   * Closes the current database context.
   */
  public void closeDB() {
    data = null;
    copied = null;
    set(null, null);
  }

  /**
   * Sets the current context and marked node set and resets the focus.
   * @param curr context set
   * @param mark marked nodes
   */
  public void set(final DBNodes curr, final DBNodes mark) {
    current = curr;
    marked = mark;
    focused = -1;
  }

  /**
   * Invalidates the current node set.
   */
  public void invalidate() {
    current = null;
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of database
   * @return result of check
   */
  public boolean pinned(final String db) {
    return dbs.pinned(db) || TableDiskAccess.locked(db, this);
  }

  /**
   * Checks if the current user has the specified permission.
   * @param perm requested permission
   * @param db database (can be {@code null})
   * @return result of check
   */
  public boolean perm(final Perm perm, final String db) {
    return user.has(perm, db);
  }

  /**
   * Locks the specified process and starts a timeout thread.
   * @param pr process
   */
  public void register(final Proc pr) {
    assert !pr.registered() : "Already registered:" + pr;
    pr.registered(true);

    // administrators will not be affected by the timeout
    if(!user.has(Perm.ADMIN)) pr.startTimeout(soptions.get(StaticOptions.TIMEOUT) * 1000L);

    // get touched databases
    final LockResult lr = new LockResult();
    pr.databases(lr);
    final StringList read = prepareLock(lr.read, lr.readAll);
    final StringList write = prepareLock(lr.write, lr.writeAll);
    locks.acquire(pr, read, write);
  }

  /**
   * Unlocks the process and stops the timeout.
   * @param pr process
   */
  public void unregister(final Proc pr) {
    assert pr.registered() : "Not registered:" + pr;
    pr.registered(false);
    locks.release(pr);
    pr.stopTimeout();
  }

  /**
   * Prepares the string list for locking.
   * @param sl string list
   * @param all lock all databases
   * @return string list or {@code null}
   */
  private StringList prepareLock(final StringList sl, final boolean all) {
    if(all) return null;

    // replace empty string with currently opened database and return array
    for(int d = 0; d < sl.size(); d++) {
      if(Strings.eq(sl.get(d), DBLocking.CTX, DBLocking.COLL)) {
        if(data == null) sl.remove(d);
        else sl.set(d, data.meta.name);
      }
    }
    return sl.sort().unique();
  }
}
