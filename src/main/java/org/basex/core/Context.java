package org.basex.core;

import static org.basex.core.Text.*;

import org.basex.data.*;
import org.basex.io.random.*;
import org.basex.query.util.pkg.*;
import org.basex.server.*;
import org.basex.util.list.*;

/**
 * This class serves as a central database context.
 * It references the currently opened database, properties, client sessions,
 * users and other meta data. Next, the instance of this class will be passed on to
 * all operations, as it organizes concurrent data access, ensuring that no
 * process will concurrently write to the same data instances.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Context {
  /** Client listener. Set to {@code null} in standalone/server mode. */
  public final ClientListener listener;
  /** Blocked clients. */
  public final ClientBlocker blocker;
  /** Client-related properties. */
  public final Prop prop = new Prop();
  /** Main properties. */
  public final MainProp mprop;
  /** Client connections. */
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

  /** User reference. */
  public User user;
  /** Log. */
  public final Log log;

  // GUI references
  /** Marked nodes. */
  public Nodes marked;
  /** Copied nodes. */
  public Nodes copied;
  /** Focused node. */
  public int focused = -1;

  /** Node context. Set if it does not contain all documents of the current database. */
  private Nodes current;
  /** Process locking. */
  private final Locking locks;
  /** Indicates if a process is currently registered. */
  private boolean registered;
  /** Data reference. */
  private Data data;

  /**
   * Default constructor, which is usually called once in the lifetime of a project.
   */
  public Context() {
    this(true);
  }

  /**
   * Default constructor, which is usually called once in the lifetime of a project.
   * @param file retrieve properties from disk
   */
  public Context(final boolean file) {
    this(new MainProp(file));
  }

  /**
   * Constructor, called by clients, and adopting the variables of the main process.
   * The {@link #user} reference must be set after calling this method.
   * @param ctx context of the main process
   * @param cl client listener
   */
  public Context(final Context ctx, final ClientListener cl) {
    listener = cl;
    mprop = ctx.mprop;
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
   * @param mp main properties
   */
  private Context(final MainProp mp) {
    mprop = mp;
    dbs = new Datas();
    events = new Events();
    sessions = new Sessions();
    blocker = new ClientBlocker();
    databases = new Databases(this);
    locks = mp.is(MainProp.GLOBALLOCK) || Prop.gui ?
      new ProcessLocking(this) : new DBLocking(mp);
    users = new Users(this);
    repo = new Repo(this);
    log = new Log(this);
    user = users.get(ADMIN);
    listener = null;
  }

  /**
   * Closes the database context. Should only be called from the main context instance.
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
  public Nodes current() {
    if(current != null || data == null) return current;
    final Nodes n = new Nodes(data.resources.docs().toArray(), data);
    n.root = true;
    return n;
  }

  /**
   * Sets the current node context. Discards the input if it contains all document
   * nodes of the currently opened database.
   * @param curr node set
   */
  public void current(final Nodes curr) {
    current = curr.checkRoot();
  }

  /**
   * Sets the specified data instance as current database.
   * @param d data reference
   */
  public void openDB(final Data d) {
    data = d;
    copied = null;
    set(null, new Nodes(d));
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
  public void set(final Nodes curr, final Nodes mark) {
    current = curr;
    marked = mark;
    focused = -1;
  }

  /**
   * Invalidates the current node set.
   */
  public void update() {
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
   * Locks the specified process and starts a timeout thread.
   * @param pr process
   */
  public void register(final Progress pr) {
    // administrators will not be affected by the timeout
    if(!user.has(Perm.ADMIN)) pr.startTimeout(mprop.num(MainProp.TIMEOUT));

    // get touched databases
    StringList sl = new StringList(1);
    if(!pr.databases(sl)) {
      // databases cannot be determined... pass null reference
      sl = null;
    } else {
      // replace empty string with currently opened database and return array
      prepareLock(sl);
    }
    assert !registered : "Already registered";
    registered = true;
    locks.acquire(pr, pr.updating ? new StringList(0) : sl,
                                  pr.updating ? sl : new StringList(0));
  }

  /**
   * Downgrades locks.
   * @param sl string list
   */
  public void downgrade(final StringList sl) {
    if(!registered) return;
    prepareLock(sl);
    //locks.downgrade(sl);
  }

  /**
   * Prepares the string list for locking.
   * @param sl string list
   */
  private void prepareLock(final StringList sl) {
    // replace empty string with currently opened database and return array
    for(int d = 0; d < sl.size(); d++) {
      if(sl.get(d).isEmpty())
        if(null == data) sl.deleteAt(d);
        else sl.set(d, data.meta.name);
    }
  }

  /**
   * Unlocks the process and stops the timeout.
   * @param pr process
   */
  public void unregister(final Progress pr) {
    assert registered : "Not registered";
    registered = false;
    locks.release(pr);
    pr.stopTimeout();
  }

  /**
   * Adds the specified client session.
   * @param s session to be added
   */
  public void add(final ClientListener s) {
    sessions.add(s);
  }

  /**
   * Removes the specified client session.
   * @param s session to be removed
   */
  public void delete(final ClientListener s) {
    sessions.remove(s);
  }

  /**
   * Checks if the current user has the specified permission.
   * @param p requested permission
   * @param md optional meta data reference
   * @return result of check
   */
  public boolean perm(final Perm p, final MetaData md) {
    final User us = md == null || p == Perm.CREATE || p == Perm.ADMIN ? null :
      md.users.get(user.name);
    return (us == null ? user : us).has(p);
  }
}
