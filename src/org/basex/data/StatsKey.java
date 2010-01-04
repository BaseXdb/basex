package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.TokenSet;

/**
 * This class provides statistics for a tag or attribute name
 * and its contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class StatsKey {
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
  public TokenSet cats;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Average text-length. */
  public double len;
  /** Number of occurrences. */
  public int counter;
  /** Leaf node flag. */
  public boolean leaf;

  /**
   * Default constructor.
   */
  public StatsKey() {
    cats = new TokenSet();
    kind = Kind.INT;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    leaf = true;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public StatsKey(final DataInput in) throws IOException {
    kind = Kind.values()[in.readNum()];

    if(kind == Kind.INT || kind == Kind.DBL) {
      min = in.readDouble();
      max = in.readDouble();
    } else if(kind == Kind.CAT) {
      cats = new TokenSet();
      final int cl = in.readNum();
      for(int i = 0; i < cl; i++) cats.add(in.readBytes());
    }
    counter = in.readNum();
    leaf = in.readBool();
    len = in.readDouble();
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
      out.writeDouble(min);
      out.writeDouble(max);
    } else if(kind == Kind.CAT) {
      final int cl = cats.size();
      out.writeNum(cl);
      for(int i = 1; i <= cl; i++) out.writeBytes(cats.key(i));
    }
    out.writeNum(counter);
    out.writeBool(leaf);
    out.writeDouble(len);
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
    final int vl = val.length;
    len = counter == 0 ? vl : (len * (counter - 1) + vl) / counter;

    if(vl == 0 || kind == Kind.TEXT || ws(val)) return;

    if(cats != null && cats.size() < MAXCATS) {
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
    final double avg = (int) (len * 100) / 100d;
    if(len != 0) sb.append(", " + avg + " avg. chars");
    if(leaf) sb.append(", leaf");
    return sb.toString();
  }
}
