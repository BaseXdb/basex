package org.basex.util.hash;

import static org.basex.util.Token.*;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class WeakTokenSet extends ASet {
  /** Hashed keys. */
  protected WeakTokenRef[] keys;
  /** Garbage collected keys. */
  protected ReferenceQueue<byte[]> gcedKeys = new ReferenceQueue<>();
  /** Head of free id list. */
  protected int free;

  /**
   * Default constructor.
   */
  public WeakTokenSet() {
    super(Array.INITIAL_CAPACITY);
    keys = new WeakTokenRef[capacity()];
  }

  /**
   * Stores the specified token, if not yet present in this set, and returns its stored equivalent.
   * @param token token to be stored
   * @return token, or its equivalent that is stored in this set
   */
  public final byte[] put(final byte[] token) {
    final int b = Token.hash(token) & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      final byte[] storedToken = keys[id].get();
      if(eq(token, storedToken)) return storedToken;
    }
    final int s;
    if(free != 0) {
      s = free;
      free = next[free];
    } else if(size < capacity()) {
      s = size++;
    } else {
      cleanUp();
      if(free != 0) {
        s = free;
        free = next[free];
      } else {
        checkSize((id, bucket) -> keys[id].bucket = bucket);
        s = size++;
      }
    }
    next[s] = buckets[b];
    keys[s] = new WeakTokenRef(token, b, gcedKeys);
    buckets[b] = s;
    return token;
  }

  /**
   * Removes garbage collected keys from the set. The deletion of keys will lead to empty entries.
   * If {@link #size} is called after deletions, the original number of entries will be returned.
   */
  public void cleanUp() {
    for(WeakTokenRef key; (key = (WeakTokenRef) gcedKeys.poll()) != null;) {
      final int b = key.bucket;
      for(int p = 0, id = buckets[b];; p = id, id = next[id]) {
        if(id == 0) throw Util.notExpected();
        if(key != keys[id]) continue;
        if(p == 0) {
          buckets[b] = next[id];
        } else {
          next[p] = next[id];
        }
        keys[id] = null;
        next[id] = free;
        free = id;
        break;
      }
    }
  }

  @Override
  protected int hash(final int id) {
    final byte[] token = keys[id].get();
    return token == null ? keys[id].bucket : Token.hash(token);
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Arrays.copyOf(keys, newSize);
  }

  @Override
  public void clear() {
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
    int bucket;

    /**
     * Constructor.
     * @param token the key to be stored
     * @param bucket the initial bucket of this entry
     * @param queue queue for registering this reference to
     */
    WeakTokenRef(final byte[] token, final int bucket, final ReferenceQueue<byte[]> queue) {
      super(token, queue);
      this.bucket = bucket;
    }

    @Override
    public String toString() {
      final byte[] token = get();
      return token == null ? "null" : string(token);
    }
  }
}
