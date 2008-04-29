package org.basex.build;

import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class stores namespaces during the creation of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class NSBuilder {
  /** Maximum level depth. */
  private static final int CAP = 2;
  /** Prefixes. */
  private byte[][] pref = new byte[CAP][];
  /** Values. */
  private byte[][] vals = new byte[CAP][];
  /** Namespace id. */
  private int[] id = new int[CAP];
  /** Namespace id counter. */
  private int counter;
  /** Number of entries. */
  private int size;

  /**
   * Adds the specified namespace.
   * @param p namespace prefix
   * @param v namespace value
   */
  void add(final byte[] p, final byte[] v) {
    check();
    pref[size] = p;
    vals[size] = v;
    id[size++] = counter++;
  }

  /**
   * Removes the specified namespace.
   * @param p namespace prefix
   */
  void delete(final byte[] p) {
    check();
    for(int i = size - 1; i >= 0; i--) {
      if(Token.eq(pref[i], p)) {
        Array.move(pref, i + 1, -1, size - i);
        Array.move(vals, i + 1, -1, size - i);
        Array.move(id, i + 1, -1, size - i);
        size--;
        break;
      }
    }
  }
  
  /**
   * Checks the array sizes.
   */
  private void check() {
    if(size == pref.length) {
      pref = Array.extend(pref);
      vals = Array.extend(vals);
      id = Array.extend(id);
    }
  }
}
