package org.basex.index.value;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The base structure of an updatable value index, whose keys are derived from the nodes of their
 * first IDs.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ValueBase extends ValueStore {
  /** Cached positions of keys. */
  private final IndexCache cache = new IndexCache();
  /** Cached keys, derived from the anchors. */
  private final IntObjectMap<byte[]> ctext = new IntObjectMap<>();
  /** Pinned keys of entries whose anchors were deleted or changed. */
  private final IntObjectMap<byte[]> pins = new IntObjectMap<>();
  /** Indicates if pins were added since they were written. */
  private boolean pinned;

  /**
   * Constructor.
   * @param data data reference
   * @param type index type
   * @param prefix file prefix
   * @throws IOException I/O exception
   */
  ValueBase(final Data data, final IndexType type, final String prefix) throws IOException {
    super(data, type, -1, prefix);
    final IOFile file = data.meta.dbFile(prefix + 'p');
    if(file.exists()) {
      try(DataInput in = new DataInput(file)) {
        for(int p = in.readNum(); p > 0; p--) pins.put(in.readNum(), in.readToken());
      }
    }
  }

  @Override
  public byte[] key(final int i) {
    byte[] key = pins.get(i);
    if(key != null) return key;
    key = ctext.get(i);
    if(key != null) return key;

    count(i);
    final int id = idxl.readNum(), pos = type == IndexType.TOKEN ? idxl.readNum() : 0;
    final int pre = data.pre(id);
    if(pre == -1) throw Util.notExpected("%: key % cannot be derived.", type, i);
    final byte[] text = data.text(pre, type == IndexType.TEXT);
    key = type == IndexType.TOKEN ? distinctTokens(text)[pos] : text;
    ctext.put(i, key);
    return key;
  }

  @Override
  public int find(final byte[] key) {
    final IndexEntry entry = cache.get(key);
    if(entry != null) return (int) entry.offset;
    final int i = ValueSource.find(this, key);
    if(i >= 0) cache.add(key, 0, i);
    return i;
  }

  @Override
  public int count(final int i) {
    return idxl.readNum(idxr.read5(i * 5L));
  }

  /**
   * Returns the anchor of an entry.
   * @param i position
   * @return first ID
   */
  int anchor(final int i) {
    count(i);
    return idxl.readNum();
  }

  /**
   * Pins the key of an entry.
   * @param i position
   * @param key key
   */
  void pin(final int i, final byte[] key) {
    if(pins.contains(i)) return;
    pins.put(i, key);
    pinned = true;
  }

  /**
   * Writes the pinned keys if pins were added.
   * @throws IOException I/O exception
   */
  void writePins() throws IOException {
    if(!pinned) return;
    try(DataOutput out = new DataOutput(data.meta.dbFile(prefix + 'p'))) {
      out.writeNum(pins.size());
      for(final int i : pins.keys()) {
        out.writeNum(i);
        out.writeToken(pins.get(i));
      }
    }
    pinned = false;
  }

  /**
   * Returns the number of pinned keys.
   * @return number of keys
   */
  int pins() {
    return pins.size();
  }
}
