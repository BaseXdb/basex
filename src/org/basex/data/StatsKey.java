package org.basex.data;

import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This class defines a key (tag/attribute name) for the {@link Stats}
 * panel.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StatsKey {
  /** Maximum number of categories. */
  private static final int MAXCATS = 100;
  /** Maximum length of category entries. */
  private static final int MAXCATLEN = 100;
  /** Tag/Attribute flag. */
  public enum Kind {
    /** Text.     */ TEXT,
    /** Category. */ CAT,
    /** Numeric.  */ INT,
    /** Numeric.  */ DBL,
    /** No Texts. */ NONE
  };
  /** Node kind. */
  public Kind kind = Kind.INT;
  /** Categories. */
  public Set cats = new Set();
  /** Minimum value. */
  public double min = Double.MAX_VALUE;
  /** Maximum value. */
  public double max = Double.MIN_VALUE;
  
  /**
   * Adds a value.
   * @param val value to be added
   */
  void add(final byte[] val) {
    if(val.length == 0 || kind == Kind.TEXT) return;
    
    if(cats != null && cats.size < MAXCATS) {
      if(val.length > MAXCATLEN) {
        kind = Kind.TEXT;
        cats = null;
      } else {
        cats.add(val);
      }
    }
    if(kind == Kind.INT) {
      final long d = Token.toLong(val);
      if(d == Long.MIN_VALUE) {
        kind = Kind.DBL;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    }
    if(kind == Kind.DBL) {
      final double d = Token.toDouble(val);
      if(d != d) {
        kind = cats.size < 10 ? Kind.CAT : Kind.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(kind == Kind.CAT) {
      if(cats.size == MAXCATS) {
        kind = Kind.TEXT;
        cats = null;
      }
    }
  }

  /** Finishes the key. */
  public void finish() {
    if(cats != null && cats.size() == 0) kind = Kind.NONE;
  }
}
