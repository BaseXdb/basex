package org.basex.core;

import java.io.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.random.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.seq.*;
import org.basex.server.*;
import org.basex.util.log.*;
import org.basex.util.options.*;

/**
 * This class serves as a central database context.
 * It references the currently opened database, options, client sessions, users and other metadata.
 * Next, the instance of this class will be passed on to all operations, as it organizes concurrent
 * data access, ensuring that no job will concurrently write to the same data instances.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Context {
  /** Blocked clients. */
  public final ClientBlocker blocker;
  /** Job pool. */
  public final JobPool jobs;
  /** Main options. */
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
  /** Key/value store. */
  public final Stores stores;
  /** Cache. */
  public final Caches caches;

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
  /** Copied nodes {@code null} if database is closed. */
  public DBNodes copied;
  /** Focused node. */
  public int focused = -1;

  /**
   * Default constructor, to be called once in the lifetime of a project.
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
    user = ctx.user;
    repo = ctx.repo;
    log = ctx.log;
    jobs = ctx.jobs;
    stores = ctx.stores;
    caches = ctx.caches;
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
    stores = new Stores(this);
    caches = new Caches(this);
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
   * Sets the user of this context. This method should only be called once.
   * @param us user
   */
  public void user(final User us) {
    user = us;
  }

  /**
   * Closes the database context. Must only be called on the global database context,
   * and not on client instances.
   */
  public synchronized void close() {
    if(closed) return;
    closed = true;
    stores.close();
    jobs.close();
    sessions.close();
    datas.close();
    log.close();
    closeDB();
  }

  /**
   * Initializes a server instance.
   * @throws IOException I/O exception
   */
  public void initServer() throws IOException {
    new Jobs(this).init();
    users.init(this);
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
   * Returns the host and port of a client.
   * @return address (or {@code null})
   */
  public String clientAddress() {
    return client != null ? client.clientAddress() : null;
  }

  /**
   * Returns the name of the current client or user.
   * @return username (or {@code null})
   */
  public String clientName() {
    return client != null ? client.clientName() : user != null ? user.name() : null;
  }

  /**
   * Assigns an external object.
   * @param object external object
   */
  public void setExternal(final Object object) {
    external.add(object);
  }

  /**
   * Returns an external object of the specified class or interface.
   * @param clz class of external object
   * @return object or {@code null}
   */
  public Object getExternal(final Class<?> clz) {
    for(final Object object : external) {
      final Class<?> c = object.getClass();
      if(c == clz) return object;
      for(final Class<?> inter : c.getInterfaces()) {
        if(inter == clz) return object;
      }
    }
    return null;
  }

  /**
   * Returns all options visible to the current user.
   * @return options (with names in lower-case)
   */
  public HashMap<String, Object> options() {
    final HashMap<String, Object> map = new HashMap<>();
    if(user().has(Perm.ADMIN)) {
      for(final Option<?> o : soptions) {
        map.put(o.name().toLowerCase(Locale.ENGLISH), soptions.get(o));
      }
    }
    for(final Option<?> o : options) {
      map.put(o.name().toLowerCase(Locale.ENGLISH), options.get(o));
    }
    return map;
  }

  /**
   * Returns the value of an option visible to the current user.
   * @param name name of option (case-insensitive)
   * @return value or {@code null}
   */
  public Object option(final String name) {
    final String uc = name.toUpperCase(Locale.ENGLISH);
    Options opts = options;
    Option<?> opt = opts.option(uc);
    if(opt == null && user().has(Perm.ADMIN)) {
      opts = soptions;
      opt = opts.option(uc);
    }
    return opt != null ? opts.get(opt) : null;
  }
}
