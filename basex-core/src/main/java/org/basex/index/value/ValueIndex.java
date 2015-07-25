package org.basex.index.value;

import org.basex.index.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides index-supported access to values
 * (attribute values, text contents, full-texts).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface ValueIndex extends Index {
  /**
   * Add entries to the index.
   * @param map a set of [key, id-list] pairs
   */
  void add(final TokenObjMap<IntList> map);

  /**
   * Deletes index entries from the index.
   * @param map a set of [key, id-list] pairs
   */
  void delete(final TokenObjMap<IntList> map);

  /**
   * Replaces an index entry in the index.
   * @param old old record key
   * @param key new record key
   * @param id record id
   */
  void replace(final byte[] old, final byte[] key, final int id);

  /**
   * Flushes the buffered data.
   */
  void flush();
}
