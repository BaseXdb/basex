package org.basex.index;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.hash.TokenSet;

/**
 * This class provides statistics for a tag or attribute name and its contents.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class StatsKey {
  /** Kind of Contents. */
  public enum Kind {
    /** Text.     */ TEXT,
    /** Category. */ CAT,
    /** Numeric.  */ INT,
    /** Numeric.  */ DBL,
    /** No texts. */ NONE
  }

  /** Node kind. */
  public Kind kind;
  /** Categories. */
  public TokenSet cats;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Number of occurrences. */
  public int counter;
  /** Leaf node flag. */
  public boolean leaf;

  /** Maximum number of string categories. */
  private final int maxcats;
  /** Maximum text length. */
  private double len;

  /**
   * Default constructor.
   * @param c number of string categories
   */
  public StatsKey(final int c) {
    cats = new TokenSet();
    kind = Kind.INT;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    leaf = true;
    maxcats = c;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param c number of string categories
   * @throws IOException I/O exception
   */
  public StatsKey(final DataInput in, final int c) throws IOException {
    kind = Kind.values()[in.readNum()];

    if(kind == Kind.INT || kind == Kind.DBL) {
      min = in.readDouble();
      max = in.readDouble();
    } else if(kind == Kind.CAT) {
      cats = new TokenSet();
      final int cl = in.readNum();
      for(int i = 0; i < cl; ++i) cats.add(in.readToken());
    }
    counter = in.readNum();
    leaf = in.readBool();
    len = in.readDouble();
    maxcats = c;
  }

  /**
   * Writes the key statistics to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void finish(final DataOutput out) throws IOException {
    if(cats != null && cats.size() == 0) {
      // no value was added
      kind = Kind.NONE;
      leaf = false;
    }

    out.writeNum(kind.ordinal());
    if(kind == Kind.INT || kind == Kind.DBL) {
      out.writeDouble(min);
      out.writeDouble(max);
    } else if(kind == Kind.CAT) {
      final int cl = cats.size();
      out.writeNum(cl);
      for(final byte[] k : cats) out.writeToken(k);
    }
    out.writeNum(counter);
    out.writeBool(leaf);
    out.writeDouble(len);
  }

  /**
   * Adds a value. All values are first treated as integer values. If a value
   * can't be converted to an integer, it is treated as double value. If
   * conversion fails again, it is handled as string category. Next, all values
   * are cached. As soon as their number exceeds {@link #maxcats}, the cached
   * values are skipped, and contents are treated as arbitrary strings.
   * @param val value to be added
   */
  public void add(final byte[] val) {
    final int vl = val.length;
    if(vl > len) len = vl;

    if(vl == 0 || kind == Kind.TEXT || ws(val)) return;

    if(cats != null && cats.size() <= maxcats) {
      if(val.length > MAXLEN) {
        kind = Kind.TEXT;
        cats = null;
      } else {
        cats.add(val);
      }
    }
    if(kind == Kind.INT) {
      final long d = toLong(val);
      if(d == Long.MIN_VALUE) {
        kind = Kind.DBL;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    }
    if(kind == Kind.DBL) {
      final double d = toDouble(val);
      if(Double.isNaN(d)) {
        kind = cats.size() <= maxcats ? Kind.CAT : Kind.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(kind == Kind.CAT) {
      if(cats.size() > maxcats) {
        kind = Kind.TEXT;
        cats = null;
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(counter + "x");
    switch(kind) {
      case CAT:
        sb.append(", " + cats.size() + " values");
        break;
      case DBL:
        sb.append(", numeric(" + min + " - " + max + ")");
        break;
      case INT:
        sb.append(", numeric(" + (int) min + " - " + (int) max + ")");
        break;
      case TEXT:
        sb.append(", strings");
        break;
      default:
        break;
    }
    if(len != 0) sb.append(", " + len + " max. length");
    if(leaf) sb.append(", leaf");
    return sb.toString();
  }
}
