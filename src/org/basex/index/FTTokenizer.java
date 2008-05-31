package org.basex.index;

import org.basex.util.Token;

/**
 * This class offers an iterator for word-based indexing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTTokenizer {
  /** Temporary token reference. */
  private byte[] tok;
  /** Temporary token start. */
  private int ts;
  /** Temporary token end. */
  private int te;
  /** Temporary token length. */
  private int tl;

  /**
   * Initializes the parser for the specified token.
   * @param t token to be iterated
   */
  public void init(final byte[] t) {
    tok = t;
    te = -1;
    tl = t.length;
  }
  
  /**
   * Checks if more words are found.
   * @return result of check
   */
  public boolean more() {
    ts = -1;
    while(++te <= tl) {
      if(ts == -1) {
        if(te < tl && Token.ftChar(tok[te])) ts = te;
      } else if(te == tl || !Token.ftChar(tok[te])) {
        // ignore words which exceed the maximum length
        if(te - ts <= Token.MAXLEN) return true;
        ts = -1;
      }
    }
    tok = null;
    return false;
  }  
  
  /**
   * Returns the offset of the current word.
   * @return word offset
   */
  int off() {
    return ts;
  }
  
  /**
   * Returns the length of the current word.
   * @return word offset
   */
  int len() {
    return te - ts;
  }
  
  /**
   * Calculates a hash code for the current token.
   * @return hash code
   */
  int hash() {
    int h = 0;
    int s = ts;
    int l = te - s;
    for(int i = 0; i < l; i++) h = (h << 5) - h + Token.ftNorm(tok[s + i]);
    return h;
  }
  
  /**
   * Compares the specified with the current word.
   * @param cont content to be compared
   * @param cs start position in content
   * @param cl content length
   * @return result of comparison
   */
  boolean eq(final byte[] cont, final int cs, final int cl) {
    if(cl != te - ts) return false;
    for(int i = ts, cp = cs; i < te; i++) {
      if(cont[cp++] != Token.ftNorm(tok[i])) return false;
    }
    return true;
  }
  
  /**
   * Copies and returns the current word.
   * @return current word
   */
  byte[] finish() {
    final byte[] tmp = new byte[te - ts];
    for(int t = 0; t < tmp.length; t++) {
      tmp[t] = (byte) Token.ftNorm(tok[ts + t]);
    }
    return tmp;
  }
}
