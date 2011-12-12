package org.basex.index;

import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.data.MetaData;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * This class provides statistics for a tag or attribute name and its contents.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class StatsKey {

  /** Node kind. */
  public Kind kind;
  /** Categories. */
  public TokenList cats;
  /** Counts of nodes. */
  public IntList vasize;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Number of occurrences. */
  public int counter;
  /** Leaf node flag. */
  public boolean leaf;

  /** Reference to meta data. */
  private final MetaData meta;
  /** Maximum text length. */
  private double len;
  /** Advanced index features. */
  public boolean advIndex;
  /** Number of kinds. */
  private int kinds = 5;

  /**
   * Default constructor.
   * @param md meta data
   */
  public StatsKey(final MetaData md) {
    cats = new TokenList();
    vasize = new IntList();
    kind = Kind.INT;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    leaf = true;
    meta = md;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param md meta data
   * @throws IOException I/O exception
   */
  public StatsKey(final DataInput in, final MetaData md) throws IOException {
    int i = in.readNum();
    if(i - kinds >= 0) {
      advIndex = true;
      i = i - kinds;
    }
    kind = Kind.values()[i];
    if(kind == Kind.INT || kind == Kind.DBL) {
      min = in.readDouble();
      max = in.readDouble();
    } else if(kind == Kind.CAT) {
      cats = new TokenList(in.readTokens());
      if(advIndex) vasize = new IntList(in.readNums());
    }
    counter = in.readNum();
    leaf = in.readBool();
    len = in.readDouble();
    meta = md;
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

    out.writeNum(kind.ordinal() + kinds);
    if(kind == Kind.INT || kind == Kind.DBL) {
      out.writeDouble(min);
      out.writeDouble(max);
    } else if(kind == Kind.CAT) {
      out.writeTokens(cats.toArray());
      out.writeNums(vasize.toArray());
    }
    out.writeNum(counter);
    out.writeBool(leaf);
    out.writeDouble(len);
  }

  /**
   * Adds a value. All values are first treated as integer values. If a value
   * can't be converted to an integer, it is treated as double value. If
   * conversion fails again, it is handled as string category. Next, all values
   * are cached. As soon as their number exceeds a maximum, the cached
   * values are skipped, and contents are treated as arbitrary strings.
   * @param val value to be added
   */
  public void add(final byte[] val) {
    final int vl = val.length;
    if(vl > len) len = vl;

    if(vl == 0 || kind == Kind.TEXT || ws(val)) return;

    if(cats != null && cats.size() <= meta.maxcats) {
      if(val.length > meta.maxlen) {
        kind = Kind.TEXT;
        cats = null;
      } else {
        int pos = cats.pos(val);
        if (pos > -1) {
          int t = vasize.get(pos) + 1;
          vasize.set(pos, t);
        } else {
          cats.add(val);
          vasize.add(1);
        }
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
        kind = cats.size() <= meta.maxcats ? Kind.CAT : Kind.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(kind == Kind.CAT) {
      if(cats.size() > meta.maxcats) {
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
