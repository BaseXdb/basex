package org.basex.index.value;

import static org.basex.util.Token.*;

import org.basex.io.random.*;
import org.basex.util.list.*;

/**
 * A source of value index references with sorted keys: a segment on disk, or the buffer in main
 * memory (see {@link SegmentedValues}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
interface ValueSource {
  /**
   * Returns the number of keys.
   * @return number of keys
   */
  int size();

  /**
   * Returns the key at the specified position.
   * @param i position
   * @return key
   */
  byte[] key(int i);

  /**
   * Returns the position of a key.
   * @param key key
   * @return position, or {@code -(insertion point) - 1} if the key is not found
   */
  default int find(final byte[] key) {
    return find(this, key);
  }

  /**
   * Returns the position of a key by binary search.
   * @param source source
   * @param key key
   * @return position, or {@code -(insertion point) - 1} if the key is not found
   */
  static int find(final ValueSource source, final byte[] key) {
    int l = 0, h = source.size() - 1;
    while(l <= h) {
      final int m = l + h >>> 1, d = compare(source.key(m), key);
      if(d == 0) return m;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return -(l + 1);
  }

  /**
   * Returns the number of references of the key at the specified position, including superseded
   * and deleted ones.
   * @param i position
   * @return number of references
   */
  int count(int i);

  /**
   * Appends the references of the key at the specified position.
   * @param i position
   * @param ids IDs
   * @param poss positions (can be {@code null})
   */
  void refs(int i, IntList ids, IntList poss);

  /**
   * Appends references that are stored as ID distances, optionally followed by positions.
   * @param in input, positioned at the first reference
   * @param count number of references
   * @param token references have positions
   * @param ids IDs
   * @param poss positions (can be {@code null})
   */
  static void refs(final DataAccess in, final int count, final boolean token, final IntList ids,
      final IntList poss) {
    for(int c = 0, id = 0; c < count; c++) {
      id += in.readNum();
      final int pos = token ? in.readNum() : 0;
      ids.add(id);
      if(poss != null) poss.add(pos);
    }
  }
}
