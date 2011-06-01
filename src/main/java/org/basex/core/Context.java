package org.basex.core;

import static org.basex.core.Text.*;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.util.Repo;
import org.basex.server.ServerProcess;
import org.basex.server.Sessions;

/**
 * This class serves as a central database context.
 * It references the currently opened database. Moreover, it provides
 * references to the currently used, marked and copied node sets.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Context {
  /** Client connections. */
  public final Sessions sessions;
  /** Event pool. */
  public final Events events;
  /** Database pool. */
  public final Datas datas;
  /** Users. */
  public final Users users;
  /** Database properties. */
  public final Prop prop;

  /** Session reference. */
  public ServerProcess session;
  /** User reference. */
  public User user;
  /** Current query file. */
  public IO query;
  /** Package repository. */
  public Repo repo;

  /** Data reference. */
  public Data data;
  /** Node context. */
  public Nodes current;

  // GUI references
  /** Marked nodes. */
  public Nodes marked;
  /** Copied nodes. */
  public Nodes copied;
  /** Focused node. */
  public int focused = -1;

  /** Process locking. */
  private final Lock lock;

  /**
   * Constructor.
   */
  public Context() {
    prop = new Prop(true);
    datas = new Datas();
    events = new Events();
    sessions = new Sessions();
    lock = new Lock(this);
    users = new Users(true);
    user = users.get(ADMIN);
    repo = new Repo(this);
  }

  /**
   * Constructor. {@link #user} reference must be set after calling this.
   * @param ctx parent context
   * @param s server process
   */
  public Context(final Context ctx, final ServerProcess s) {
    prop = new Prop(true);
    datas = ctx.datas;
    events = ctx.events;
    sessions = ctx.sessions;
    session = s;
    lock = ctx.lock;
    users = ctx.users;
  }

  /**
   * Closes the database context.
   */
  public synchronized void close() {
    while(sessions.size() > 0) sessions.get(0).exit();
    datas.close();
  }

  /**
   * Returns true if the current node set contains all documents.
   * @return result of check
   */
  public boolean root() {
    return current != null && current.root;
  }

  /**
   * Returns all document nodes.
   * @return result of check
   */
  public int[] doc() {
    return current.root ? current.list : data.doc();
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
   * @param path database path
   */
  public void openDB(final Data d, final String path) {
    data = d;
    copied = null;
    set(new Nodes(path == null ? d.doc() : d.doc(path), d), new Nodes(d));
    current.root = path == null;
  }

  /**
   * Removes the current database context.
   */
  public void closeDB() {
    data = null;
    set(null, null);
    copied = null;
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
   * Updates references to the document nodes.
   */
  public void update() {
    current = new Nodes(data.doc(), data);
    current.root = true;
  }

  /**
   * Adds the specified data reference to the pool.
   * @param d data reference
   */
  public synchronized void pin(final Data d) {
    datas.add(d);
  }

  /**
   * Pins the specified database.
   * @param name name of database
   * @return data reference
   */
  public synchronized Data pin(final String name) {
    return datas.pin(name);
  }

  /**
   * Unpins a data reference.
   * @param d data reference
   * @return true if reference was removed from the pool
   */
  public synchronized boolean unpin(final Data d) {
    return datas.unpin(d);
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of database
   * @return int use-status
   */
  public synchronized boolean pinned(final String db) {
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
   * Adds the specified session.
   * @param s session to be added
   */
  public synchronized void add(final ServerProcess s) {
    sessions.add(s);
  }

  /**
   * Removes the specified session.
   * @param s session to be removed
   */
  public synchronized void delete(final ServerProcess s) {
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
