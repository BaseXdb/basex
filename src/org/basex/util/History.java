package org.basex.util;

/**
 * This class stores strings in a history.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class History {
  /** Maximum number of strings to be stored. */
  private static final int MAX = 50;
  /** String history. */
  private String[] hist;
  /** History position. */
  private int pos;
  
  /**
   * Constructor.
   * @param h string array
   */
  public History(final String[] h) {
    hist = h != null ? h : new String[0];
    pos = hist.length;
    if(pos != 0) pos--;
  }
  
  /**
   * Returns the previous string.
   * @return previous string
   */
  public String prev() {
    return hist.length == 0 ? null : hist[pos == 0 ? pos : --pos];
  }
  
  /**
   * Returns the next string.
   * @return previous string
   */
  public String next() {
    return hist.length == 0 ? null : hist[pos + 1 == hist.length ? pos : ++pos];
  }
  
  /**
   * Stores a string in the history. 
   * @param str string to be stored
   */
  public void store(final String str) {
    if(str.length() == 0) return;
    
    pos = hist.length;
    // deletes a former entry of the current string
    for(int i = 0; i < pos; i++) {
      if(str.equals(hist[i])) {
        if(i + 1 < pos--) Array.move(hist, i + 1, -1, pos - i);
        break;
      }
    }
    if(pos == MAX) {
      Array.move(hist, 1, -1, MAX - 1);
    } else {
      hist = Array.resize(hist, pos, ++pos);
    }
    hist[--pos] = str;
  }
  
  /**
   * Returns the string array.
   * @return string array
   */
  public String[] strings() {
    return hist;
  }
}
