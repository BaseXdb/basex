package org.basex.core;

import org.basex.data.Data;
import org.basex.data.Nodes;
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
  /** Database pool. */
  private final DataPool pool;
  /** Users. */
  public final Users users;
  /** Database properties. */
  public Prop prop;
  /** Server flag. */
  public boolean server;
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
    this(null);
  }

  /**
   * Constructor.
   * @param ctx parent context
   */
  public Context(final Context ctx) {
    this(new Prop(), ctx);
  }

  /**
   * Constructor, defining an initial property file and an
   * optional parent context.
   * @param pr property file
   * @param ctx parent context
   */
  private Context(final Prop pr, final Context ctx) {
    prop = pr;
    pool = ctx == null ? new DataPool() : ctx.pool;
    sessions = ctx == null ? new Sessions() : ctx.sessions;
    users = new Users(this);
  }

  /**
   * Closes the database instance.
   */
  public void close() {
    for(int i = 0; i < sessions.size();) sessions.get(i).exit();
    pool.close();
    users.writeList();
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
    current = new Nodes(data.doc(), data, true);
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
   * Checks if the specified database is pinned.
   * @param db name of database
   * @return int use-status
   */
  public boolean pinned(final String db) {
    return pool.pinned(db);
  }

  /**
   * Returns information on the opened database instances.
   * @return data reference
   */
  public String info() {
    return pool.info();
  }
  
  /**
   * Returns number of references of the specified database in the pool.
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
}
