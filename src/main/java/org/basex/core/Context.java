package org.basex.core;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.resource.*;
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
  /** Client-related properties. */
  public final Prop prop = new Prop();
  /** Main properties. */
  public final MainProp mprop;
  /** Client connections. */
  public final Sessions sessions;
  /** Event pool. */
  public final Events events;
  /** Database pool. */
  public final Datas datas;
  /** Users. */
  public final Users users;
  /** Package repository. */
  public final Repo repo;

  /** User reference. */
  public User user;
  /** Log. */
  public Log log;

  // GUI references
  /** Marked nodes. */
  public Nodes marked;
  /** Copied nodes. */
  public Nodes copied;
  /** Focused node. */
  public int focused = -1;

  /** Path to the documents in the database. */
  private String docpath;
  /** Node context. */
  private Nodes current;
  /** Process locking. */
  private final ILocking locks;
  /** Data reference. */
  private Data data;
  /** Databases list. */
  private Databases databases;

  /**
   * Default constructor, which is only called once in a project.
   */
  public Context() {
    this(new MainProp());
  }

  /**
   * Default constructor, which is only called once in a project.
   * @param props initial properties
   */
  public Context(final HashMap<String, String> props) {
    this(new MainProp(props));
  }

  /**
   * Constructor, called by clients, and adopting the variables of the main process.
   * The {@link #user} reference must be set after calling this method.
   * @param ctx context of the main process
   * @param cl client listener
   */
  public Context(final Context ctx, final ClientListener cl) {
    mprop = ctx.mprop;
    datas = ctx.datas;
    events = ctx.events;
    sessions = ctx.sessions;
    locks = ctx.locks;
    users = ctx.users;
    repo = ctx.repo;
    databases = ctx.databases;
    listener = cl;
  }

  /**
   * Private constructor.
   * @param mp main properties
   */
  private Context(final MainProp mp) {
    mprop = mp;
    datas = new Datas();
    events = new Events();
    sessions = new Sessions();
    locks = mp.is(MainProp.DBLOCKING) ? new DBLocking(mp) : new ProcessLocking(this);
    users = new Users(true);
    repo = new Repo(this);
    user = users.get(ADMIN);
    databases = databases();
    listener = null;
  }

  /**
   * Closes the database context. Should only be called from the main context instance.
   */
  public synchronized void close() {
    while(!sessions.isEmpty()) sessions.get(0).quit();
    datas.close();
  }

  /**
   * Returns {@code true} if the current context belongs to a client user.
   * @return result of check
   */
  public boolean client() {
    return listener != null;
  }

  /**
   * Returns {@code true} if the current node set contains all documents.
   * @return result of check
   */
  public boolean root() {
    return current != null && current.root;
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
    if(current == null && data != null) {
      final Resources res = data.resources;
      current = new Nodes((docpath == null ? res.docs() :
        res.docs(docpath)).toArray(), data);
      current.root = docpath == null;
    }
    return current;
  }

  /**
   * Sets the current node context.
   * @param curr node set
   */
  public void current(final Nodes curr) {
    current = curr;
  }

  /**
   * Sets the specified data instance as current database.
   * @param d data reference
   */
  public void openDB(final Data d) {
    openDB(d, null);
  }

  /**
   * Sets the specified data instance as current database and restricts
   * the context nodes to the given path.
   * @param d data reference
   * @param p database path
   */
  public void openDB(final Data d, final String p) {
    data = d;
    docpath = p;
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
   * Adds the specified data reference to the pool.
   * @param d data reference
   */
  public void pin(final Data d) {
    datas.add(d);
  }

  /**
   * Pins and returns an existing data reference for the specified database, or
   * returns {@code null}.
   * @param name name of database
   * @return data reference
   */
  public Data pin(final String name) {
    return datas.pin(name);
  }

  /**
   * Unpins a data reference.
   * @param d data reference
   * @return {@code true} if reference was removed from the pool
   */
  public boolean unpin(final Data d) {
    return datas.unpin(d);
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of database
   * @return result of check
   */
  public boolean pinned(final String db) {
    return datas.pinned(db) || TableDiskAccess.locked(db, this);
  }

  /**
   * Locks the specified process and starts a timeout thread.
   * @param pr process
   */
  public void register(final Progress pr) {
    // administrators will not be affected by the timeout
    if(!user.has(Perm.ADMIN)) pr.startTimeout(mprop.num(MainProp.TIMEOUT));

    // get touched databases
    StringList sl = new StringList();
    if(!pr.databases(sl)) {
      // databases cannot be determined... pass on all existing databases
      sl = databases.listDBs();
    } else {
      // replace empty string with currently opened database and return array
      for(int d = 0; d < sl.size(); d++) {
        if(data != null && sl.get(d).isEmpty()) sl.set(d, data.meta.name);
      }
    }
    locks.acquire(pr, sl);
  }

  /**
   * Unlocks the process and stops the timeout.
   * @param pr process
   */
  public void unregister(final Progress pr) {
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
    final User us = md == null || p == Perm.CREATE || p == Perm.ADMIN ?
        null : md.users.get(user.name);
    return (us == null ? user : us).has(p);
  }

  /**
   * Returns a reference to currently available databases.
   * @return available databases
   */
  public Databases databases() {
    if(databases == null || !databases.dbpath.eq(mprop.dbpath()))
      databases = new Databases(this);
    return databases;
  }
}
