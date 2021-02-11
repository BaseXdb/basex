package org.basex.index.stats;

import static org.basex.index.stats.StatsType.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.hash.*;

/**
 * This class provides statistical data for an indexed node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Stats {
  /** Distinct values (value, number of occurrence). */
  public TokenIntMap values;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Number of occurrences. */
  public int count;
  /** Data type. */
  public byte type;

  /** Leaf node flag. Indicates if all nodes only have a text node as child. */
  private boolean leaf;

  /**
   * Default constructor.
   */
  public Stats() {
    values = new TokenIntMap();
    type = NONE;
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    leaf = true;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Stats(final DataInput in) throws IOException {
    // ignore higher bits of older databases (skipped since version 9.0)
    final int t = in.readNum() & 0xF;
    type = (byte) t;

    if(isInteger(t) || isDouble(t)) {
      min = in.readDouble();
      max = in.readDouble();
    }
    if(isCategory(t)) {
      values = new TokenIntMap(in);
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
    // finalize statistics: switch to category type if map with distinct values exists
    if(values != null) {
      if(values.isEmpty()) {
        values = null;
      } else if(!isCategory(type)) {
        type = type == INTEGER ? INTEGER_CATEGORY :
               type == DOUBLE ? DOUBLE_CATEGORY : STRING_CATEGORY;
      }
    }

    out.writeNum(type);
    if(isNumeric(type)) {
      out.writeDouble(min);
      out.writeDouble(max);
    }
    if(isCategory(type)) {
      values.write(out);
    }

    out.writeNum(count);
    out.writeBool(leaf);
    // legacy (required before version 7.1)
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
    byte t = type;
    final int vl = value.length;
    // only analyze non-empty values
    if(vl > 0) {
      // start with integer type
      if(t == NONE) {
        t = INTEGER;
      }
      // try to save new value as integer
      if(t == INTEGER) {
        final long d = toLong(value);
        if(d == Long.MIN_VALUE) {
          t = DOUBLE;
        } else {
          if(min > d) min = d;
          if(max < d) max = d;
        }
      }
      // try to save new value as double
      if(t == DOUBLE) {
        final double d = toDouble(value);
        if(Double.isNaN(d)) {
          t = STRING;
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
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(count + "x");
    if(!isNone(type)) {
      sb.append(", ");
      final int size = values != null ? values.size() : 0;
      if(size > 1) sb.append(size).append(" distinct ");
      sb.append(StatsType.toString(type));
      if(size != 1) sb.append('s');
      if(isNumeric(type)) {
        sb.append(" [");
        final int mn = (int) min, mx = (int) max;
        if(mn == min) sb.append(mn); else sb.append(min);
        sb.append(", ");
        if(mx == max) sb.append(mx); else sb.append(max);
        sb.append(']');
      }
    }
    if(leaf) sb.append(", leaf");
    return sb.toString();
  }
}
