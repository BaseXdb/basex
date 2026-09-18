package org.basex.index.value;

import org.basex.index.*;
import org.basex.util.list.*;

/**
 * Sequential reader of a value index structure.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ValueReader implements SegmentReader {
  /** Source. */
  private final ValueSource source;
  /** IDs of the current key. */
  private final IntList ids = new IntList();
  /** Positions of the current key. */
  private final IntList poss = new IntList();
  /** Current key ({@code null} if it has not been read yet). */
  private byte[] key;
  /** Current position. */
  private int i;
  /** Indicates if the references of the current key have been read. */
  private boolean read;

  /**
   * Constructor.
   * @param source source
   */
  ValueReader(final ValueSource source) {
    this.source = source;
  }

  @Override
  public byte[] key() {
    if(key == null && i < source.size()) key = source.key(i);
    return key;
  }

  @Override
  public int[] ids() {
    refs();
    return ids.toArray();
  }

  @Override
  public int[] poss() {
    refs();
    return poss.toArray();
  }

  /**
   * Reads the references of the current key.
   */
  private void refs() {
    if(read) return;
    ids.reset();
    poss.reset();
    source.refs(i, ids, poss);
    read = true;
  }

  @Override
  public void next() {
    i++;
    key = null;
    read = false;
  }

  @Override
  public void close() { }
}
