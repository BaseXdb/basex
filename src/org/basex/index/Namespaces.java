package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This class stores namespaces during the creation of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Namespaces extends Set {
  /** XML Token. */
  private static final byte[] XML = Token.token("xml");
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
   * @throws IOException I/O exception
   */
  public Namespaces(final String db) throws IOException {
    // ignore missing namespace input
    if(!IO.dbfile(db, DATANS).exists()) return;
    
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
   * Returns the namespace for the specified qname and pre value.
   * @param n tag/attribute name
   * @param p pre value
   * @return namespace
   */
  public int get(final byte[] n, final int p) {
    final int s = indexOf(n, ':');
    if(s == -1) return 0;
    int i = sz - 1;
    while(i >= 0 && p > pre[i]) i--;
    return ns(n, s, i + 1);
  }

  /**
   * Returns the namespace for the specified qname.
   * @param n tag/attribute name
   * @return namespace
   */
  public int get(final byte[] n) {
    return ns(n, indexOf(n, ':'), sz - 1);
  }

  /**
   * Returns the namespace for the specified qname.
   * @param n prefix
   * @param s prefix index
   * @param p index offset to start with
   * @return namespace
   */
  private int ns(final byte[] n, final int s, final int p) {
    if(s == -1) return 0;
    final byte[] pr = substring(n, 0, s);
    if(eq(XML, pr)) return 0;
    for(int i = p; i >= 0; i--) if(eq(pref[i], pr)) return vals[i];
    BaseX.notexpected();
    return 0;
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
   * @throws IOException I/O exception
   */
  public synchronized void finish(final String db) throws IOException {
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
  }
}
  