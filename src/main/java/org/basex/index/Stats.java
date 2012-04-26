package org.basex.index;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.hash.*;

/**
 * This class provides statistical data for an indexed node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Stats {
  /** Categories. */
  public TokenIntMap cats;
  /** Data type. */
  public StatsType type;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Number of occurrences. */
  public int count;
  /** Leaf node flag. This flag indicates if a node has children other than
   * texts and attributes. */
  private boolean leaf;

  /**
   * Default constructor.
   */
  public Stats() {
    cats = new TokenIntMap();
    type = StatsType.NONE;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    leaf = true;
  }

  /**
   * Getter for leaf.
   * @return leaf
   */
  public boolean isLeaf() {
    return leaf;
  }

  /**
   * Setter for leaf.
   * @param l leaf or not
   */
  public void setLeaf(final boolean l) {
    leaf = l;
    if(!l) type = StatsType.TEXT;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Stats(final DataInput in) throws IOException {
    // 0x10 indicates format introduced with Version 7.1
    final int k = in.readNum();
    type = StatsType.values()[k & 0xF];

    if(type == StatsType.INTEGER || type == StatsType.DOUBLE) {
      min = in.readDouble();
      max = in.readDouble();
    } else if(type == StatsType.CATEGORY) {
      if(k > 0xF) {
        cats = new TokenIntMap(in);
      } else {
        cats = new TokenIntMap();
        final int cl = in.readNum();
        for(int i = 0; i < cl; ++i) cats.add(in.readToken());
      }
    }
    count = in.readNum();
    leaf = in.readBool();
    in.readDouble();
  }

  /**
   * Writes the key statistics to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    // 0x10 indicates format introduced with Version 7.1
    out.writeNum(type.ordinal() | 0x10);
    if(type == StatsType.INTEGER || type == StatsType.DOUBLE) {
      out.writeDouble(min);
      out.writeDouble(max);
    } else if(type == StatsType.CATEGORY) {
      cats.write(out);
    }
    out.writeNum(count);
    out.writeBool(leaf);
    // legacy since version 7.1
    out.writeDouble(0);
  }

  /**
   * Adds a value. All values are first treated as integer values. If a value
   * can't be converted to an integer, it is treated as double value. If
   * conversion fails again, it is handled as string category. Next, all values
   * are cached. As soon as their number exceeds a maximum, the cached
   * values are skipped, and contents are treated as arbitrary strings.
   * @param val value to be added
   * @param meta meta data
   */
  public void add(final byte[] val, final MetaData meta) {
    final int vl = val.length;
    if(vl == 0 || type == StatsType.TEXT || ws(val)) return;

    StatsType t = type;
    if(t == StatsType.NONE) t = StatsType.INTEGER;

    if(cats != null && cats.size() <= meta.maxcats) {
      if(val.length > meta.maxlen) {
        t = StatsType.TEXT;
        cats = null;
      } else {
        cats.add(val, Math.max(1, cats.value(val) + 1));
      }
    }
    if(t == StatsType.INTEGER) {
      final long d = toLong(val);
      if(d == Long.MIN_VALUE) {
        t = StatsType.DOUBLE;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    }
    if(t == StatsType.DOUBLE) {
      final double d = toDouble(val);
      if(Double.isNaN(d)) {
        t = cats.size() <= meta.maxcats ? StatsType.CATEGORY : StatsType.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(t == StatsType.CATEGORY) {
      if(cats.size() > meta.maxcats) {
        t = StatsType.TEXT;
        cats = null;
      }
    }
    type = t;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(count + "x");
    switch(type) {
      case CATEGORY:
        sb.append(", " + cats.size() + " values");
        break;
      case DOUBLE:
        sb.append(", numeric(" + min + " - " + max + ')');
        break;
      case INTEGER:
        sb.append(", numeric(" + (int) min + " - " + (int) max + ')');
        break;
      case TEXT:
        sb.append(", strings");
        break;
      default:
        break;
    }
    if(leaf) sb.append(", leaf");
    return sb.toString();
  }
}
