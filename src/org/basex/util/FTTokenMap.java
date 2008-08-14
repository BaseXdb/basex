package org.basex.util;


/**
 * This is a simple hash map, extending the even simpler
 * {@link Set hash set}.
 * 
 * It is used to store fulltext data for a token.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTTokenMap extends Set {
  /** Hash values. */
  private byte[] values = new byte[CAP];
  /** Number of position values. **/
  private int[] sizes = new int[CAP];
  /** Pointer on token data. **/
  private long[] pointers = new long[CAP];


  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param s size
   * @param p pointer
   */
  public void add(final byte[] key, final int s, final long p) {
    final int i = add(key);
    if(i > 0) {
      sizes[i] = s;
      pointers[i] = p;
    }
  }

  /**
   * Returns the value for the specified key.
   * @param id key to be found
   * @return size or -1 if nothing was found
   */
  public int getSize(final int id) {
    return id != 0 ? sizes[id] : -1;
  }

  /**
   * Returns the value for the specified key.
   * @param id key to be found
   * @return pointer or -1 if nothing was found
   */
  public long getPointer(final int id) {
    return id != 0 ? pointers[id] : -1;
  }

   @Override
  protected void rehash() {
    super.rehash();
    values = Array.extend(values);
    pointers = Array.extend(pointers);
    sizes = Array.extend(sizes);
  }
}
