package org.basex.util.hash;

import static org.basex.util.Token.*;

import java.lang.ref.*;
import java.util.*;

import org.basex.util.*;

/**
 * A set for tokens that allows its elements to be garbage collected. This is achieved by holding
 * WeakRefefences to the byte arrays, making them eligible for garbage collection when there are
 * no strong references left from outside of this set. The set entry is then maintained from the
 * gc'ed reference showing up in a reference queue.
 * The first entry of the token set (offset 0) is always empty.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class WeakTokenSet extends ASet implements Iterable<byte[]> {
  /** Hashed keys. */
  protected ElementRef[] keys;
  /** Garbage collected keys. */
  protected ReferenceQueue<byte[]> gcedKeys = new ReferenceQueue<>();
  /** Head of free id list. */
  protected int free;

  /**
   * Default constructor.
   */
  public WeakTokenSet() {
    super(Array.INITIAL_CAPACITY);
    keys = new ElementRef[capacity()];
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @return unique id of stored key (larger than zero)
   */
  public final byte[] put(final byte[] key) {
    for(ElementRef gcedKey; (gcedKey = (ElementRef) gcedKeys.poll()) != null;) {
      remove(gcedKey);
    }
    final int b = Token.hash(key) & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      byte[] cachedKey = keys[id].get();
      if(eq(key, cachedKey)) return cachedKey;
    }
    int s;
    if(free != 0) {
      s = free;
      free = next[free];
    }
    else {
      checkSize((id, bucket) -> keys[id].bucket = bucket);
      s = size++;
    }
    next[s] = buckets[b];
    keys[s] = new ElementRef(key, b, gcedKeys);
    buckets[b] = s;
    return key;
  }

  /**
   * Removes the entry with the specified element reference.
   * The deletion of keys will lead to empty entries. If {@link #size} is called after
   * deletions, the original number of entries will be returned.
   * @param ref the element reference to be removed.
   */
  protected void remove(final ElementRef ref) {
    for (int i = 0; i < 2; ++i) {
      final int b = ref.bucket;
      for(int p = 0, id = buckets[b]; id != 0; p = id, id = next[id]) {
        if(ref != keys[id]) continue;
        if(p == 0) {
          buckets[b] = next[id];
        }
        else {
          next[p] = next[next[p]];
        }
        keys[id] = null;
        next[id] = free;
        free = id;
        return;
      }
    }
    throw Util.notExpected();
  }

  @Override
  protected int hash(final int id) {
    byte[] key = keys[id].get();
    return key == null ? keys[id].bucket : Token.hash(key);
    }

  @Override
  protected void rehash(final int newSize) {
    keys = Arrays.copyOf(keys, newSize);
  }

  @Override
  public void clear() {
    throw Util.notExpected();
  }

  @Override
  public final Iterator<byte[]> iterator() {
    throw Util.notExpected();
//    return new ArrayIterator<>(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(keys);
  }

  /**
   *  A reference to a token set element. It is represented as a WeakReference, but it also is
   *  aware of the bucket where it is currently held in the set. This allows removal of the element
   *  even after the referred byte array has been garbage collected (and thus its hash code cannot
   *  be calculated any longer).
   */
  protected static class ElementRef extends WeakReference<byte[]> {
    /** The current bucket where this reference. */
    int bucket;

    /**
     * Constructor.
     * @param key the key to be cached.
     * @param bucket the initial bucket of this entry.
     * @param queue queue for registering this reference to.  */
    ElementRef(final byte[] key, final int bucket, final ReferenceQueue<? super byte[]> queue) {
      super(key, queue);
      this.bucket = bucket;
    }
  }
}