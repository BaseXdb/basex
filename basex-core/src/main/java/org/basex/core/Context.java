package org.basex.core;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.random.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Context {
  /** Blocked clients. */
  public final ClientBlocker blocker;
  /** Job pool. */
  public final JobPool jobs;
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
  /** Locking. */
  public final Locking locking;

  /** External objects (HTTP context, HTTP requests). */
  private final HashSet<Object> external;
  /** Client info. Set to {@code null} in standalone/server mode. */
  private final ClientInfo client;
  /** Current node context. {@code null} if all documents of the current database are referenced. */
  private DBNodes current;
  /** User reference. */
  private User user;
  /** Currently opened database. */
  private Data data;
  /** Indicates if the class has been closed/finalized. */
  private boolean closed;

  // GUI references

  /** Marked nodes. {@code null} if database is closed. */
  public DBNodes marked;
  /** Copied nodes {@code null} if database is closed.. */
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
   * The {@link #user} must be set after calling this method.
   * @param ctx main context
   */
  public Context(final Context ctx) {
    this(ctx, null);
  }

  /**
   * Constructor, called by clients, and adopting the variables of the main context.
   * The {@link #user} must be set after calling this method.
   * @param ctx main context
   * @param client client info (can be {@code null})
   */
  public Context(final Context ctx, final ClientInfo client) {
    this.client = client;
    soptions = ctx.soptions;
    options = new MainOptions(ctx.options);
    datas = ctx.datas;
    sessions = ctx.sessions;
    databases = ctx.databases;
    blocker = ctx.blocker;
    locking = ctx.locking;
    users = ctx.users;
    repo = ctx.repo;
    log = ctx.log;
    jobs = ctx.jobs;
    external = new HashSet<>(ctx.external);
  }

  /**
   * Private constructor.
   * @param soptions static options
   */
  public Context(final StaticOptions soptions) {
    this.soptions = soptions;
    options = new MainOptions();
    datas = new Datas();
    sessions = new Sessions();
    blocker = new ClientBlocker();
    databases = new Databases(soptions);
    locking = new Locking(soptions);
    users = new Users(soptions);
    repo = new EXPathRepo(soptions);
    log = new Log(soptions);
    user = users.get(UserText.ADMIN);
    jobs = new JobPool(soptions);
    external = new HashSet<>();
    client = null;
  }

  /**
   * Returns the user of this context.
   * @return user (can be {@code null} if it has not yet been assigned
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
    if(closed) return;
    closed = true;
    jobs.close();
    sessions.close();
    datas.close();
    log.close();
    closeDB();
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
   * @return node set, or {@code null} if no database is opened
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
   * @param db database pattern (can be {@code null})
   * @return result of check
   */
  public boolean perm(final Perm perm, final String db) {
    return user.has(perm, db);
  }

  /**
   * Returns the host and port of a client.
   * @return address (or {@code null})
   */
  public String clientAddress() {
    return client != null ? client.clientAddress() : null;
  }

  /**
   * Returns the name of the current client or user.
   * @return user name (or {@code null})
   */
  public String clientName() {
    return client != null ? client.clientName() : user != null ? user.name() : null;
  }

  /**
   * Returns all databases for which the current user has read access.
   * @return resulting list
   */
  public StringList listDBs() {
    return listDBs(null);
  }

  /**
   * Returns all databases for which the current user has read access.
   * @param pattern database pattern (can be {@code null})
   * @return resulting list
   */
  public StringList listDBs(final String pattern) {
    final StringList dbs = databases.listDBs(pattern), sl = new StringList(dbs.size());
    for(final String db : dbs) {
      if(perm(Perm.READ, db)) sl.add(db);
    }
    return sl;
  }

  /**
   * Assigns an external object.
   * @param object external object
   */
  public void setExternal(final Object object) {
    external.add(object);
  }

  /**
   * Returns an external object.
   * @param clz class of external object
   * @return object or {@code null}
   */
  public Object getExternal(final Class<?> clz) {
    for(final Object object : external) {
      if(object.getClass() == clz) return object;
    }
    return null;
  }
}
