package org.basex.util.hash;

import java.lang.ref.*;
import java.util.*;

import org.basex.util.*;

/**
 * A set for tokens that allows its elements to be garbage collected. This is achieved by holding
 * WeakReferences to the tokens, making them eligible for garbage collection when there are no
 * strong references left from outside of this set. The set entry is then maintained from the gc'ed
 * reference showing up in a reference queue. The first entry of the token set (offset 0) is always
 * empty.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class WeakTokenSet extends ASet {
  /** Hashed keys. */
  private WeakTokenRef[] keys;
  /** Garbage collected keys. */
  private ReferenceQueue<byte[]> gcedKeys = new ReferenceQueue<>();
  /** Head of free ID list. */
  private int free;

  /**
   * Default constructor.
   */
  public WeakTokenSet() {
    super(INITIAL_CAPACITY);
    keys = new WeakTokenRef[capacity()];
  }

  /**
   * Stores the specified key, if not yet present in this set, and returns its stored equivalent.
   * @param key key to be stored
   * @return key, or its equivalent that is stored in this set
   */
  public byte[] put(final byte[] key) {
    final int h = Token.hashCode(key), c = capacity();
    int b = h & c - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      final byte[] stored = keys[i].get();
      if(Token.eq(key, stored)) return stored;
    }

    if(size == c && free == 0) cleanUp();
    final int s;
    if(free != 0) {
      s = free;
      free = next[s];
    } else {
      if(checkCapacity((i, bucket) -> keys[i].bucket = bucket)) b = h & capacity() - 1;
      s = size++;
    }
    next[s] = buckets[b];
    keys[s] = new WeakTokenRef(key, b, gcedKeys);
    buckets[b] = s;
    return key;
  }

  /**
   * Removes garbage collected keys from the set. The deletion of keys will lead to empty entries.
   * If {@link #size} is called after deletions, the original number of entries will be returned.
   */
  private void cleanUp() {
    for(WeakTokenRef key; (key = (WeakTokenRef) gcedKeys.poll()) != null;) {
      final int b = key.bucket;
      for(int p = 0, i = buckets[b];; p = i, i = next[i]) {
        if(i == 0) throw Util.notExpected();
        if(key != keys[i]) continue;
        if(p == 0) {
          buckets[b] = next[i];
        } else {
          next[p] = next[i];
        }
        keys[i] = null;
        next[i] = free;
        free = i;
        break;
      }
    }
  }

  @Override
  protected int hashCode(final int index) {
    final byte[] token = keys[index].get();
    return token == null ? keys[index].bucket : Token.hashCode(token);
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Arrays.copyOf(keys, newSize);
  }

  @Override
  protected void clear() {
    gcedKeys = new ReferenceQueue<>();
    free = 0;
    Arrays.fill(keys, null);
    super.clear();
  }

  @Override
  public String toString() {
    return toString(keys);
  }

  /**
   * A weak reference to a token. It is aware of the bucket where it is currently held in the set.
   * This allows removal of the key even after the referred token has been garbage collected (and
   * thus its hash code cannot be calculated any longer).
   */
  private static final class WeakTokenRef extends WeakReference<byte[]> {
    /** The current bucket where this reference is in. */
    private int bucket;

    /**
     * Constructor.
     * @param key the key to be stored
     * @param bucket the initial bucket of this entry
     * @param queue queue for registering this reference to
     */
    private WeakTokenRef(final byte[] key, final int bucket, final ReferenceQueue<byte[]> queue) {
      super(key, queue);
      this.bucket = bucket;
    }

    @Override
    public String toString() {
      final byte[] token = get();
      return token == null ? "null" : Token.string(token);
    }
  }
}
