package org.basex.query.xpath.func;

import org.basex.util.Set;

/**
 * Index for XPath functions and variables.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @param <V> Entry type
 */
public final class XPathIndex<V> extends Set {
  /** Hash values. */
  private EE<?>[] values = new EE<?>[CAP];

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param val value
   */
  void index(final byte[] key, final V val) {
    final int i = add(key);
    if(i > 0) values[i] = new EE<V>(val);
  }

  /**
   * Returns the class for the specified key.
   * @param tok key to be found
   * @return value or null if nothing was found
   */
  @SuppressWarnings("unchecked")
  V get(final byte[] tok) {
    final int id = id(tok);
    return id != 0 ? (V) values[id].value : null;
  }

  @Override
  protected void rehash() {
    super.rehash();
    final EE<?>[] v = new EE<?>[size << 1];
    System.arraycopy(values, 0, v, 0, size);
    values = v;
  }

  /**
   * Hash entry.
   * @param <V> entry type
   */
  private static class EE<V> {
    /** Stored entry. */
    final V value;

    /**
     * Constructor.
     * @param v value
     */
    EE(final V v) {
      value = v;
    }
  }
}
