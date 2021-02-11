package org.basex.index;

import org.basex.data.*;

/**
 * Builder for values-based index structures.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ValuesBuilder extends IndexBuilder {
  /** Tokenize flag. */
  protected final boolean tokenize;

  /**
   * Constructor.
   * @param data reference
   * @param type index type
   */
  protected ValuesBuilder(final Data data, final IndexType type) {
    super(data, type);
    tokenize = type == IndexType.TOKEN;
  }
}
