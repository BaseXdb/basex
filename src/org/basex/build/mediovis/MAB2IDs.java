package org.basex.build.mediovis;

import org.basex.util.Array;
import org.basex.util.Set;

/**
 * This is a hash map for MAB2 IDs, extending the {@link Set hash set}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MAB2IDs extends Set {
  /** Hash values. */
  private MAB2Entry[] values = new MAB2Entry[CAP];

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @return mab2 entry
   */
  public MAB2Entry index(final byte[] key) {
    final int i = add(key);
    if(i < 0) return values[-i];
    values[i] = new MAB2Entry(key);
    return values[i];
  }

  /**
   * Returns the specified entry.
   * @param p entry position
   * @return mab2 entry
   */
  public MAB2Entry get(final int p) {
    return values[p];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Array.extend(values);
  }
}
