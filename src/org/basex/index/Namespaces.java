package org.basex.index;

import org.basex.BaseX;
import org.basex.util.Array;
import org.basex.util.Set;

/**
 * This class stores namespaces during the creation of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Namespaces extends Set {
  /** Prefixes. */
  private byte[][] pref = new byte[CAP][];
  /** Values. */
  private int[] vals = new int[CAP];
  /** Pre values. */
  private int[] pre = new int[CAP];
  /** Number of entries. */
  private int sz;

  /**
   * Default Constructor.
   */
  public Namespaces() { }

  /**
   * Constructor, specifying an input file.
   * @param db name of the database
   */
  public Namespaces(final String db) {
    if(db == db) return; // dummy stuff
  /*public Namespaces(final String db) throws IOException {
    final DataInput in = new DataInput(db, DATANS);
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    pref = in.readBytesArray();
    vals = in.readNums();
    pre = in.readNums();
    size = in.readNum();
    sz = in.readNum();
    in.close();
    */
  }

  /**
   * Adds the specified namespace.
   * @param p namespace prefix
   * @param u namespace uri
   * @param s current document size
   */
  public void add(final byte[] p, final byte[] u, final int s) {
    final int i = add(u);
    check();
    pref[sz] = p;
    vals[sz] = i < 0 ? -i : i;
    pre[sz++] = s;
    
    BaseX.debug("Add Namespace % {%}, %", sz, u, p);
  }

  /**
   * Checks the array sizes.
   */
  private void check() {
    if(sz == pref.length) {
      pref = Array.extend(pref);
      vals = Array.extend(vals);
      pre = Array.extend(pre);
    }
  }

  /**
   * Finishes the index structure and optimizes its memory usage.
   * @param db name of the database
   */
  public synchronized void finish(final String db) {
    if(db == db) return; // dummy stuff
    /*
    final DataOutput out = new DataOutput(db, DATANS);
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeBytesArray(pref);
    out.writeNums(vals);
    out.writeNums(pre);
    out.writeNum(size);
    out.writeNum(sz);
    out.close();
    */
  }
}
  
  /**
   * Removes the specified namespace.
   * @param p namespace prefix
  void delete(final byte[] p) {
    BaseX.debug("Delete Namespace " + Token.string(p));

    for(int i = size - 1; i >= 0; i--) {
      if(Token.eq(pref[i], p)) {
        Array.move(pref, i + 1, -1, size - i);
        Array.move(vals, i + 1, -1, size - i);
        Array.move(id, i + 1, -1, size - i);
        size--;
        return;
      }
    }
    BaseX.notexpected();
  }
   */
  