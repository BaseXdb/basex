package org.basex.core;

import static org.basex.core.Text.*;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Nodes;
import org.basex.index.Resources;
import org.basex.query.util.pkg.Repo;
import org.basex.server.ClientListener;
import org.basex.server.Sessions;

/**
 * This class serves as a central database context.
 * It references the currently opened database. Moreover, it provides
 * references to the currently used, marked and copied node sets.
 *
 * This class should only be instantiated once in a project; otherwise,
 * database updates may lead to conflicts.
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

  // GUI references
  /** Marked nodes. */
  public Nodes marked;
  /** Copied nodes. */
  public Nodes copied;
  /** Focused node. */
  public int focused = -1;

  /** Database path. */
  private String path;
  /** Node context. */
  private Nodes current;
  /** Process locking. */
  private final Lock lock;
  /** Data reference. */
  private Data data;

  /**
   * Default constructor, which should only be called once in a project.
   */
  public Context() {
    listener = null;
    mprop = new MainProp();
    datas = new Datas();
    events = new Events();
    sessions = new Sessions();
    lock = new Lock(this);
    users = new Users(true);
    repo = new Repo(this);
    user = users.get(ADMIN);
  }

  /**
   * Constructor, passing on the main context.
   * The {@link #user} reference must be set after calling this method.
   * @param ctx parent database context
   * @param cl client listener
   */
  public Context(final Context ctx, final ClientListener cl) {
    listener = cl;
    mprop = ctx.mprop;
    datas = ctx.datas;
    events = ctx.events;
    sessions = ctx.sessions;
    lock = ctx.lock;
    users = ctx.users;
    repo = ctx.repo;
  }

  /**
   * Closes the database context.
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
      current = new Nodes(
          (path == null ? res.docs() : res.docs(path)).toArray(), data);
      current.root = path == null;
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
    path = p;
    copied = null;
    set(null, new Nodes(d));
  }

  /**
   * Resets the current database context.
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
   * @return true if reference was removed from the pool
   */
  public boolean unpin(final Data d) {
    return datas.unpin(d);
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of database
   * @return int use-status
   */
  public boolean pinned(final String db) {
    return datas.pinned(db);
  }

  /**
   * Registers a process.
   * @param w writing flag
   */
  public void register(final boolean w) {
    lock.lock(w);
  }

  /**
   * Unregisters a process.
   * @param w writing flag
   */
  public void unregister(final boolean w) {
    lock.unlock(w);
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
  public boolean perm(final int p, final MetaData md) {
    final User us = md == null || p == User.CREATE || p == User.ADMIN ? null :
        md.users.get(user.name);
    return (us == null ? user : us).perm(p);
  }
}
