package org.basex.core;

import static org.basex.core.Text.*;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.server.ServerProcess;
import org.basex.server.Sessions;

/**
 * This class serves as a central database context.
 * It references the currently opened database. Moreover, it provides
 * references to the currently used, marked and copied node sets.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Context {
  /** Client connections. */
  public final Sessions sessions;
  /** Process locking. */
  public final Lock lock;
  /** Database pool. */
  public final DataPool datas;
  /** Trigger pool. */
  public final TriggerPool triggers;
  /** Users. */
  public final Users users;
  /** Database properties. */
  public final Prop prop;
  /** User reference. */
  public User user;
  /** Current query file. */
  public IO query;

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

  /**
   * Constructor.
   */
  public Context() {
    prop = new Prop(true);
    datas = new DataPool();
    triggers = new TriggerPool();
    sessions = new Sessions();
    lock = new Lock();
    users = new Users(true);
    user = users.get(ADMIN);
  }

  /**
   * Constructor. {@link #user} reference must be set after calling this.
   * @param ctx parent context
   */
  public Context(final Context ctx) {
    prop = new Prop(true);
    datas = ctx.datas;
    triggers = ctx.triggers;
    sessions = ctx.sessions;
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
    data = d;
    copied = null;
    marked = new Nodes(d);
    update();
  }

  /**
   * Removes the current database context.
   */
  public void closeDB() {
    data = null;
    current = null;
    marked = null;
    copied = null;
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
  public boolean pinned(final String db) {
    return datas.pinned(db);
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
    sessions.delete(s);
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
