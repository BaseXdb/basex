package org.basex.util;

/**
 * This is a simple container for native byte array values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz
 */
public class ByteArrayList {
  
  /** Value array. */
  public byte[][] list = new byte[8][];
  /** Current array size. */
  public byte size;
  
  /**
   * Default constructor.
   */
  public ByteArrayList() {
  }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
   */
  public ByteArrayList(final byte is) {
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
   * Removes the element at the specified position in this list. 
   * Shifts any subsequent elements to the left (subtracts one 
   * from their indices). 
   * @param index - the index of the element to removed.
   * @return the element that was removed from the list.
   */
  public byte[] remove(final int index) {
    if(size == 0 || size < index) throw new IndexOutOfBoundsException();
    byte[] elem = list[index];
    System.arraycopy(list, index + 1, list, index, size - index);
    --size;
    return elem;
  }
  
  /**
   * Finishes the byte array.
   * @return byte array
   */
  public byte[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
  
  /**
   * Sorts the list in ASCII order.
   *
   * @param num numeric sort
   * @param asc ascending
   */
  public void sort(final boolean num, final boolean asc) {
    if(size > 1) sort(0, size, num, asc);    
  }
  
  /**
   * Sorts the array.
   * @param s offset
   * @param e length
   * @param g numeric sort
   * @param f ascending/descending sort
   */
  private void sort(final int s, final int e, final boolean g,
      final boolean f) {
    
    if(e < 7) {
      for(int i = s; i < e + s; i++) {
        for(int j = i; j > s; j--) {
          final int h = g ? s(list[j - 1], list[j]) : 
            d(list[j - 1], list[j]);          
          if(f ? h < 0 : h > 0) break;
          s(j, j - 1);
        }
      }
      return;
    }

    int m = s + (e >> 1);
    if(e > 7) {
      int l = s;
      int n = s + e - 1;
      if(e > 40) {
        final int k = e >>> 3;
        l = m(l, l + k, l + (k << 1));
        m = m(m - k, m, m + k);
        n = m(n - (k << 1), n - k, n);
      }
      m = m(l, m, n);
    }
    final byte[] v = list[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = g ? s(list[b], v) : d(list[b], v);
        if(f ? h > 0 : h < 0) break;
        if(h == 0) s(a++, b);
        b++;
      }
      while(c >= b) {
        final int h = g ? s(list[c], v) : d(list[c], v);
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--);
        c--;
      }
      if(b > c) break;
      s(b++, c--);
    }

    int k;
    final int n = s + e;
    k = Math.min(a - s, b - a);
    s(s, b - k, k);
    k = Math.min(d - c, n - d - 1);
    s(b, n - k, k);

    if((k = b - a) > 1) sort(s, k, g, f);
    if((k = d - c) > 1) sort(n - k, k, g, f);
  }

  /**
   * Compares two numeric tokens and returns an integer.
   * @param a first token
   * @param b second token
   * @return result
   */
  private int s(final byte[] a, final byte[] b) {
    final long n = Token.toLong(a) - Token.toLong(b);
    return n > 0 ? 1 : n < 0 ? -1 : 0;
  }

  /**
   * Compares two tokens and returns an integer.
   * @param a first token
   * @param b second token
   * @return result
   */
  private int d(final byte[] a, final byte[] b) {   
    return a == null ? b == null ? 0 : -1 : b == null ? 1 : cmp(a, b);
  }
    
  /**
   * Compares two character arrays for equality.
   * @param tok token to be compared
   * @param tok2 second token to be compared
   * @return 0 if tok equals tok2, -1 tok > tok0, 1 tok < tok2
   */
  public static int cmp(final byte[] tok, final byte[] tok2) {       
    int len = tok.length;    
    if (tok.length > tok2.length) len = tok2.length;            
    for(int t = 0; t != len; t++) {
      if(tok2[t] > tok[t]) return 1;
      else if(tok2[t] < tok[t]) return -1;
    }
    return 0;
  }
  
  /**
   * Swaps two array values.
   * @param a first offset
   * @param b second offset
   */
  private void s(final int a, final int b) {
    final byte[] l = list[a];
    list[a] = list[b];
    list[b] = l;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of values
   */
  private void s(final int a, final int b, final int n) {
    for(int i = 0; i < n; i++) s(a + i, b + i);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param a first offset
   * @param b second offset
   * @param c thirst offset
   * @return median
   */
  private int m(final int a, final int b, final int c) {
    return cmp(list[a], list[b]) < 1 ?
        (cmp(list[b], list[c]) < 1 ? b : cmp(list[a], list[c]) < 1 ? c : a) :
        (cmp(list[b], list[c]) < 1 ? b : cmp(list[a], list[c]) < 1 ? c : a);
  }
}

