package org.basex.core;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.util.Performance;

/**
 * This class stores the reference to the currently opened database.
 * Moreover, it provides references to the currently used, marked and
 * copied node sets.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Context {
  /** Database pool. */
  private final DataPool pool = new DataPool();
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
    // read database properties
    Prop.read();
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
   * Sets a new data instance.
   * @param d data reference
   */
  public void data(final Data d) {
    if(data != null) {
      BaseX.errln("Warning: database still open.");
      close();
    }
    data = d;
    copied = null;
    marked = new Nodes(d);
    update();
  }

  /**
   * Updates references to the document nodes.
   */
  public void update() {
    current = new Nodes(data.doc(), data);
  }

  /**
   * Closes the database instance.
   * @return true if operation was successful
   */
  public synchronized boolean close() {
    try {
      if(data != null) {
        final Data d = data;
        data = null;
        current = null;
        marked = null;
        copied = null;
        if(pool.unpin(d)) d.close();
        if(Prop.mainmem) Performance.gc(1);
      }
      return true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return false;
    }
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
}
