package org.basex.core;

import static org.basex.core.Text.*;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.server.ServerProcess;
import org.basex.server.Sessions;

/**
 * This class serves as a central database context.
 * It references the currently opened database. Moreover, it provides
 * references to the currently used, marked and copied node sets.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Context {
  /** Client connections. */
  public final Sessions sessions;
  /** Database pool. */
  public final DataPool pool;
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
    prop = new Prop();
    pool = new DataPool();
    sessions = new Sessions();
    users = new Users(true);
    user = users.get(ADMIN);
  }

  /**
   * Constructor. {@link #user} reference must be set after calling this.
   * @param ctx parent context
   */
  public Context(final Context ctx) {
    prop = new Prop();
    pool = ctx.pool;
    sessions = ctx.sessions;
    users = ctx.users;
  }

  /**
   * Closes the database context.
   */
  public void close() {
    while(sessions.size() > 0) sessions.get(0).exit();
    pool.close();
  }

  /**
   * Returns true if all current nodes refer to document nodes.
   * @return result of check
   */
  public boolean root() {
    if(current == null) return true;
    for(final int n : current.nodes) if(data.kind(n) != Data.DOC) return false;
    return true;
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
    current = new Nodes(data, true);
  }

  /**
   * Adds the specified data reference to the pool.
   * @param d data reference
   */
  public void pin(final Data d) {
    pool.add(d);
  }

  /**
   * Pins the specified database.
   * @param name name of database
   * @return data reference
   */
  public Data pin(final String name) {
    return pool.pin(name);
  }

  /**
   * Unpins a data reference.
   * @param d data reference
   * @return true if reference was removed from the pool
   */
  public boolean unpin(final Data d) {
    return pool.unpin(d);
  }

  /**
   * Checks if the specified database is pinned.
   * @param db name of database
   * @return int use-status
   */
  public boolean pinned(final String db) {
    return pool.pinned(db);
  }

  /**
   * Returns the number of references of the specified database in the pool.
   * @param db name of the database
   * @return number of references
   */
  public int size(final String db) {
    return pool.size(db);
  }

  /**
   * Adds the specified session.
   * @param s session to be added
   */
  public void add(final ServerProcess s) {
    sessions.add(s);
  }

  /**
   * Removes the specified session.
   * @param s session to be removed
   */
  public void delete(final ServerProcess s) {
    sessions.delete(s);
  }

  /**
   * Checks if the current user has the specified permissions.
   * @param p permissions
   * @param d optional data reference
   * @return result of check (-1: ok, other: failure)
   */
  public int perm(final int p, final Data d) {
    final User u = user;
    int up = u.perm;
    if(d != null) {
      final User us = d.meta.users.get(u.name);
      if(us != null) up = up & ~(User.READ | User.WRITE) | us.perm;
    }
    int i = 4;
    while(--i >= 0 && (1 << i & p) == 0 || (1 << i & up) != 0);
    return i;
  }
}
