package org.basex.core;

import static org.basex.core.Text.*;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.server.ServerSession;
import org.basex.server.Sessions;

/**
 * This class offers as central database context.
 * It references the currently opened database. Moreover, it provides
 * references to the currently used, marked and copied node sets.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Context {
  /** Current client connections. */
  public final Sessions sessions;
  /** Users. */
  public final Users users;
  /** Database properties. */
  public final Prop prop;
  /** Database pool. */
  public final DataPool pool;
  /** Current user. */
  public User user;
  /** Current query file. */
  public IO query;

  /** Central data reference. */
  private Data data;
  /** Current context. */
  private Nodes current;
  /** Currently marked nodes. */
  private Nodes marked;
  /** Currently copied nodes. */
  private Nodes copied;

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
   * Returns data reference.
   * @return data reference
   */
  public Data data() {
    return data;
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
  }

  /**
   * Updates references to the document nodes.
   */
  public void update() {
    // [CG] necessary check?
    if(data != null) current = new Nodes(data.doc(), data, true);
  }

  /**
   * Returns the current context set.
   * @return current context set
   */
  public Nodes current() {
    return current;
  }

  /**
   * Sets the current context set.
   * @param curr current context set
   */
  public void current(final Nodes curr) {
    current = curr;
  }

  /**
   * Sets the data reference.
   * @param d data reference
   */
  public void data(final Data d) {
    data = d;
  }

  /**
   * Returns the copied context set.
   * @return copied context set
   */
  public Nodes copied() {
    return copied;
  }

  /**
   * Sets the current node set as copy.
   * @param copy current node set as copy.
   */
  public void copy(final Nodes copy) {
    copied = copy;
  }

  /**
   * Returns the marked context set.
   * @return marked context set
   */
  public Nodes marked() {
    return marked;
  }

  /**
   * Sets the marked context set.
   * @param mark marked context set
   */
  public void marked(final Nodes mark) {
    marked = mark;
  }

  /**
   * Pins the pool.
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
   * Adds the specified data reference to the pool.
   * @param d data reference
   */
  public void addToPool(final Data d) {
    pool.add(d);
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
  public void add(final ServerSession s) {
    sessions.add(s);
  }

  /**
   * Removes the specified session.
   * @param s session to be removed
   */
  public void delete(final ServerSession s) {
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
