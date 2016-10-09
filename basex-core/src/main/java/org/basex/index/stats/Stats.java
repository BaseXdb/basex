package org.basex.index.stats;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.hash.*;

/**
 * This class provides statistical data for an indexed node.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class Stats {
  /** Distinct values (value, number of occurrence). */
  public TokenIntMap values;
  /** Data type. */
  public StatsType type;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Number of occurrences. */
  public int count;

  /** Leaf node flag. Indicates if all nodes only have a text node as child. */
  private boolean leaf;

  /**
   * Default constructor.
   */
  public Stats() {
    values = new TokenIntMap();
    type = StatsType.NONE;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    leaf = true;
  }

  /**
   * Getter for leaf flag.
   * @return leaf flag
   */
  public boolean isLeaf() {
    return leaf;
  }

  /**
   * Setter for leaf flag.
   * @param l leaf or not
   */
  public void setLeaf(final boolean l) {
    leaf = l;
    if(!l) type = StatsType.STRING;
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
        values = new TokenIntMap(in);
      } else {
        values = new TokenIntMap();
        final int cl = in.readNum();
        for(int i = 0; i < cl; ++i) values.add(in.readToken());
      }
    }
    count = in.readNum();
    leaf = in.readBool();
    // legacy since version 7.1
    in.readDouble();
  }

  /**
   * Writes the key statistics to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    // finalize statistics: switch to category type if map with distinct values exists
    StatsType t = type;
    if((t == StatsType.NONE || t == StatsType.STRING) && values != null && !values.isEmpty())
      t = StatsType.CATEGORY;

    // 0x10 indicates format introduced with Version 7.1
    out.writeNum(t.ordinal() | 0x10);
    if(t == StatsType.INTEGER || t == StatsType.DOUBLE) {
      out.writeDouble(min);
      out.writeDouble(max);
    } else if(t == StatsType.CATEGORY) {
      values.write(out);
    }
    out.writeNum(count);
    out.writeBool(leaf);
    // legacy since version 7.1
    out.writeDouble(0);
  }

  /**
   * Adds a value. All values are first treated as integer values. If a value cannot be converted
   * to an integer, it is treated as double value. If conversion fails again, it is handled as
   * string category. Next, all values are cached. As soon as their number exceeds a maximum,
   * the cached values are skipped, and contents are treated as arbitrary strings.
   * @param value value to be added
   * @param meta meta data
   */
  public void add(final byte[] value, final MetaData meta) {
    StatsType t = type;
    final int vl = value.length;
    // only analyze non-empty values
    if(vl > 0) {
      // start with integer type
      if(t == StatsType.NONE) {
        t = StatsType.INTEGER;
      }
      // try to save new value as integer
      if(t == StatsType.INTEGER) {
        final long d = toLong(value);
        if(d == Long.MIN_VALUE) {
          t = StatsType.DOUBLE;
        } else {
          if(min > d) min = d;
          if(max < d) max = d;
        }
      }
      // try to save new value as double
      if(t == StatsType.DOUBLE) {
        final double d = toDouble(value);
        if(Double.isNaN(d)) {
          t = StatsType.STRING;
        } else {
          if(min > d) min = d;
          if(max < d) max = d;
        }
      }
    }
    type = t;

    // save distinct values
    if(values != null) {
      if(vl > meta.maxlen || vl > 0 && ws(value)) {
        // give up categories if string is too long or only consists of whitespaces
        values = null;
      } else {
        values.put(value, Math.max(1, values.get(value) + 1));
        // give up categories if number of entries exceeds limit
        if(values.size() > meta.maxcats) values = null;
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(count + "x");
    String typ = "", ext = "";
    switch(type) {
      case CATEGORY:
        typ = "distinct values";
        ext = "(" + values.size() + ")";
        break;
      case DOUBLE:
        typ = "doubles";
        ext = "[" + min + ", " + max + "]";
        break;
      case INTEGER:
        typ = "integers";
        ext = "[" + (int) min + ", " + (int) max + "]";
        break;
      case STRING:
        typ = "strings";
        break;
      default:
        break;
    }
    if(!typ.isEmpty()) sb.append(", ").append(typ);
    if(!ext.isEmpty()) sb.append(' ').append(ext);
    if(leaf) sb.append(", leaf");
    return sb.toString();
  }
}
