package org.basex.query.pf;

import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.util.TokenBuilder;

/**
 * This is a container for XQuery node sequences.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class PFR implements Result {
  /** Values container. */
  private V[] val;
  /** Number of stored values. */
  private int size;

  /**
   * Value constructor.
   */
  PFR() {
    val = new V[8];
    size = 0;
  }

  /**
   * Value constructor.
   * @param v initial values
   */
  PFR(final V... v) {
    val = v;
    size = v.length;
  }

  /**
   * Adds a value to the sequence.
   * @param v value to be added
   */
  void add(final V v) {
    if(size == val.length) {
      final V[] tmp = new V[size << 1];
      System.arraycopy(val, 0, tmp, 0, size);
      val = tmp;
    }
    val[size++] = v;
  }

  /** {@inheritDoc} */
  public int size() {
    return size;
  }

  /** {@inheritDoc} */
  public boolean same(final Result v) {
    if(!(v instanceof PFR) || size != v.size()) return false;
    final PFR seq = (PFR) v;
    for(int s = 0; s < size; s++) if(!val[s].eq(seq.val[s])) return false;
    return true;
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser) throws Exception {
    for(int i = 0; i < size; i++) ser.item(val[i].s());
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add("XSeq[");
    for(int i = 0; i < size; i++) {
      if(i > 0) tb.add(',');
      tb.add(val[i].toString());
    }
    tb.add(']');
    return tb.toString();
  }
}
