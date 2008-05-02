package org.basex.data;

import java.io.IOException;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This class contains statistics for the tag or attribute name of a document.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StatsKey {
  /** Maximum number of categories. */
  private static final int MAXCATS = 50;
  /** Kind of Contents. */
  public enum Kind {
    /** Text.     */ TEXT,
    /** Category. */ CAT,
    /** Numeric.  */ INT,
    /** Numeric.  */ DBL,
    /** No Texts. */ NONE
  };
  /** Node kind. */
  public Kind kind;
  /** Categories. */
  public Set cats;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  
  /**
   * Default Constructor.
   */
  public StatsKey() {
    cats = new Set();
    kind = Kind.INT;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
  }
  
  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   */
  public StatsKey(final DataInput in) {
    kind = Kind.values()[in.readNum()];
    if(kind == Kind.INT || kind == Kind.DBL) {
      min = Token.toDouble(in.readBytes());
      max = Token.toDouble(in.readBytes());
    } else if(kind == Kind.CAT) {
      cats = new Set();
      final int cl = in.readNum();
      for(int i = 0; i < cl; i++) cats.add(in.readBytes());
    }
  }

  /**
   * Writes the key statistics to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void finish(final DataOutput out) throws IOException {
    if(cats != null && cats.size() == 0) kind = Kind.NONE;
    
    out.writeNum(kind.ordinal());
    if(kind == Kind.INT || kind == Kind.DBL) {
      out.writeBytes(Token.token(min));
      out.writeBytes(Token.token(max));
    } else if(kind == Kind.CAT) {
      final int cl = cats.size();
      out.writeNum(cl);
      for(int i = 1; i <= cl; i++) out.writeBytes(cats.key(i));
    }
  }
  
  /**
   * Adds a value. All values are first treated as integer values. If a value
   * can't be converted to an integer, it is treated as double value. If
   * conversion fails again, it is handled as string category. Next, all values
   * are cached. As soon as their number exceeds {@link #MAXCATS}, the cached
   * values are skipped, and contents are treated as arbitrary strings.
   * @param val value to be added
   */
  public void add(final byte[] val) {
    if(val == null || val.length == 0 || kind == Kind.TEXT ||
        Token.ws(val)) return;
    
    if(cats != null && cats.size() < MAXCATS) {
      if(val.length > Token.MAXLEN) {
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
        kind = cats.size() < MAXCATS ? Kind.CAT : Kind.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(kind == Kind.CAT) {
      if(cats.size() == MAXCATS) {
        kind = Kind.TEXT;
        cats = null;
      }
    }
  }

  @Override
  public String toString() {
    switch(kind) {
      case CAT:  return ", " + cats.size() + " values";
      case DBL:  return ", numeric(" + min + " - " + max + ")";
      case INT:  return ", numeric(" + (int) min + " - " + (int) max + ")";
      case TEXT: return ", strings";
      case NONE: return "";
    }
    return null;
  }
}
