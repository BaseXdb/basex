package org.basex.index.value;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Index for texts, attribute values and full-texts.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class ValueIndex implements Index {
  /** Value type (texts/attributes). */
  protected final boolean text;
  /** Data instance. */
  protected final Data data;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param text value type (texts/attributes)
   */
  protected ValueIndex(final Data data, final boolean text) {
    this.data = data;
    this.text = text;
  }

  /**
   * Returns the number of index entries.
   * @return number of index entries
   */
  public abstract int size();

  /**
   * Deletes entries from the index.
   * @param map a set of [key, id-list] pairs
   */
  public abstract void delete(final TokenObjMap<IntList> map);

  /**
   * Add entries to the index.
   * @param map a set of [key, id-list] pairs
   */
  public abstract void add(final TokenObjMap<IntList> map);

  /**
   * Flushes the buffered data.
   */
  public abstract void flush();
}
