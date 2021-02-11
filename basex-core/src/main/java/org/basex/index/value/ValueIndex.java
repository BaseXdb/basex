package org.basex.index.value;

import org.basex.data.*;
import org.basex.index.*;

/**
 * Index for texts, attribute values and full-texts.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ValueIndex implements Index {
  /** Index type. */
  protected final IndexType type;
  /** Data instance. */
  protected final Data data;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   */
  protected ValueIndex(final Data data, final IndexType type) {
    this.data = data;
    this.type = type;
  }

  /**
   * Returns the number of index entries.
   * @return number of index entries
   */
  public abstract int size();

  /**
   * Deletes entries from the index.
   * @param values value cache with [key, id-list] pairs
   */
  public abstract void delete(ValueCache values);

  /**
   * Add entries to the index.
   * @param values value cache with [key, id-list] pairs
   */
  public abstract void add(ValueCache values);

  /**
   * Flushes the buffered data.
   */
  public abstract void flush();
}
