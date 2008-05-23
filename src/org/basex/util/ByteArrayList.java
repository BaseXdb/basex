package org.basex.util;

/**
 * This is a simple container for native int array values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ByteArrayList {
  /** Value array. */
  public byte[][] list = new byte[8][];
  /** Current array size. */
  public int size;
  /** Flag for found values in list. **/
  public boolean found = false;
  
  /**
   * Default constructor.
   */
  public ByteArrayList() { }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
   */
  public ByteArrayList(final int is) {
    list = new byte[is][];
  }
  
  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final byte[] v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   * @param keylength length of the key
   * @return index of added value
   */
  public int addSorted(final byte[] v, final int keylength) {
    found = false;
    
    // find inserting position
    int l = 0, r = size, m = size / 2;
    int res = -1;
    if (r > 1) {
      while (r > l) {
        m = (r + l) / 2;
        res = cmp(v, list[m], keylength);
        if (res == -1) r = m - 1;
        else if (res == 1) l = m + 1;
        else {
          found = true;
          return m;
        }
      } 
    }
    
    if (l < size) { 
      res = cmp(v, list[l], keylength);
    
      if (res == 0) {
        found = true;
        return l;
      } else {
        if (res == 1) {
          // v > list[m]
          l++;
        } else {
          // v < list[m]
          //m--;  
        }
      }
    }
      if(size == list.length) {
        final byte[][] tmp = new byte[size << 1][];
        System.arraycopy(list, 0, tmp, 0, l);
        tmp[l] = v;
        if (size - l > 0) 
          System.arraycopy(list, l, tmp, l + 1, size - l);
        list = tmp;
        size++;  
      } else {
        System.arraycopy(list, l, list, l + 1, size - l);
        list[l] = v;
        size++;
      }
      return l;
    
  }
  
  /**
   * Compares two int[] at spezified length.
   * returns -1 if v1 > v2
   *         1  if v1 < v2
   *         0  if v1 = v2
   * @param v1 value1
   * @param v2 value2 
   * @param kl number of ints to compare
   * @return int result
   */
  public int cmp(final byte[] v1, final byte[] v2, final int kl) {
    if (kl > v1.length || kl > v2.length) return -2;
    
    for (int i = 0; i < kl; i++) {
      if (v1[i] > v2[i]) return 1;
      if (v1[i] < v2[i]) return -1;
    }
    
    return 0;
  }
  

  /**
   * Finishes the int array.
   * @return int array
   */
  public byte[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }
}
