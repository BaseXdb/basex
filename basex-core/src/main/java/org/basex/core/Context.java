package org.basex.core;

import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.random.*;
import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.seq.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serves as a central database context.
 * It references the currently opened database, options, client sessions, users and other meta data.
 * Next, the instance of this class will be passed on to all operations, as it organizes concurrent
 * data access, ensuring that no job will concurrently write to the same data instances.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Context {
  /** Client listener. Set to {@code null} in standalone/server mode. */
  public final ClientListener listener;
  /** Blocked clients. */
  public final ClientBlocker blocker;
  /** Job pool. */
  public final JobPool jobs;
  /** Asynchronous queries. */
  public final QueryPool queries;
  /** Options. */
  public final MainOptions options;
  /** Static options. */
  public final StaticOptions soptions;
  /** Client sessions. */
  public final Sessions sessions;
  /** Opened databases. */
  public final Datas datas;
  /** Users. */
  public final Users users;
  /** EXPath package repository. */
  public final EXPathRepo repo;
  /** Databases list. */
  public final Databases databases;
  /** Log. */
  public final Log log;
  /** Locked jobs. */
  public final Locking locks;

  /** Current node context. Set if it does not contain all documents of the current database. */
  private DBNodes current;
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
   * Constructor, called by clients, and adopting the variables of the specified context.
   * The {@link #user} reference must be set after calling this method.
   * @param ctx main context
   */
  public Context(final Context ctx) {
    this(ctx, null);
  }

  /**
   * Constructor, called by clients, and adopting the variables of the main context.
   * The {@link #user} reference must be set after calling this method.
   * @param ctx main context
   * @param listener client listener
   */
  public Context(final Context ctx, final ClientListener listener) {
    this.listener = listener;
    soptions = ctx.soptions;
    options = new MainOptions(ctx.options);
    datas = ctx.datas;
    sessions = ctx.sessions;
    databases = ctx.databases;
    blocker = ctx.blocker;
    locks = ctx.locks;
    users = ctx.users;
    repo = ctx.repo;
    log = ctx.log;
    jobs = ctx.jobs;
    queries = ctx.queries;
  }

  /**
   * Private constructor.
   * @param soptions static options
   */
  private Context(final StaticOptions soptions) {
    this.soptions = soptions;
    options = new MainOptions();
    datas = new Datas();
    sessions = new Sessions();
    blocker = new ClientBlocker();
    databases = new Databases(soptions);
    locks = soptions.get(StaticOptions.GLOBALLOCK) ? new JobLocking(this) : new DBLocking(this);
    users = new Users(soptions);
    repo = new EXPathRepo(soptions);
    log = new Log(soptions);
    user = users.get(UserText.ADMIN);
    listener = null;
    jobs = new JobPool();
    queries = new QueryPool();
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
    jobs.close();
    // stop sessions, close data references
    while(!sessions.isEmpty()) sessions.get(0).quit();
    datas.close();
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
    return new DBNodes(data, true, data.resources.docs().toArray());
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
   * Invalidates the current node set. Will be recreated once it is requested.
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
    return datas.pinned(db) || TableDiskAccess.locked(db, this);
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
   * Filters databases to the ones that have the specified permission.
   * @param perm requested permission
   * @param dbs list of databases
   * @return resulting list
   */
  public StringList filter(final Perm perm, final StringList dbs) {
    final StringList sl = new StringList(dbs.size());
    for(final String db : dbs) if(perm(perm, db)) sl.add(db);
    return sl;
  }
}
