package org.basex.index.value;

import java.util.*;

import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * A snapshot of the keys of a value index buffer, with the live references of the buffer.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class BufferSource implements ValueSource {
  /** Buffer. */
  private final IndexBuffer buffer;
  /** Sorted keys. */
  private final byte[][] keys;

  /**
   * Constructor.
   * @param buffer buffer
   */
  BufferSource(final IndexBuffer buffer) {
    this.buffer = buffer;
    keys = buffer.keys().finish();
    Arrays.sort(keys, Token::compare);
  }

  @Override
  public int size() {
    return keys.length;
  }

  @Override
  public byte[] key(final int i) {
    return keys[i];
  }

  @Override
  public int count(final int i) {
    return buffer.count(keys[i]);
  }

  @Override
  public void refs(final int i, final IntList ids, final IntList poss) {
    buffer.live(keys[i], ids, poss, false);
  }
}
