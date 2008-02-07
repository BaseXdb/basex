package org.basex.build.fs;

import org.basex.build.fs.metadata.AbstractExtractor;
import org.basex.util.Array;
import org.basex.util.Set;

/**
 * This hash structure indexes the available filesystem extractors.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ExtractorIndex extends Set {
  /** Hash values. */
  private AbstractExtractor[] values = new AbstractExtractor[CAP];

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param val value
   */
  public void put(final byte[] key, final AbstractExtractor val) {
    final int i = add(key);
    if(i > 0) values[i] = val;
  }

  /**
   * Returns the class for the specified key.
   * @param tok key to be found
   * @return value or null if nothing was found
   */
  public AbstractExtractor get(final byte[] tok) {
    return values[id(tok)];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Array.extend(values);
  }
}
