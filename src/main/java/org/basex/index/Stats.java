package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Locale;

import org.basex.data.MetaData;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.Serializer;
import org.basex.util.Token;
import org.basex.util.hash.TokenIntMap;

/**
 * This class provides statistical data for an indexed node.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Stats {
  /** Data type. */
  public StatsType type;
  /** Categories. */
  public TokenIntMap cats;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Number of occurrences. */
  public int count;
  /** Leaf node flag. */
  public boolean leaf;

  /**
   * Default constructor.
   */
  public Stats() {
    cats = new TokenIntMap();
    type = StatsType.INTEGER;
    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    leaf = true;
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
    if(cats != null && cats.size() == 0) {
      // no value was added
      type = StatsType.NONE;
      leaf = false;
    }

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

    if(cats != null && cats.size() <= meta.maxcats) {
      if(val.length > meta.maxlen) {
        type = StatsType.TEXT;
        cats = null;
      } else {
        cats.add(val, Math.max(1, cats.value(val) + 1));
      }
    }
    if(type == StatsType.INTEGER) {
      final long d = toLong(val);
      if(d == Long.MIN_VALUE) {
        type = StatsType.DOUBLE;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    }
    if(type == StatsType.DOUBLE) {
      final double d = toDouble(val);
      if(Double.isNaN(d)) {
        type = cats.size() <= meta.maxcats ?
            StatsType.CATEGORY : StatsType.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(type == StatsType.CATEGORY) {
      if(cats.size() > meta.maxcats) {
        type = StatsType.TEXT;
        cats = null;
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(count + "x");
    switch(type) {
      case CATEGORY:
        sb.append(", " + cats.size() + " values");
        break;
      case DOUBLE:
        sb.append(", numeric(" + min + " - " + max + ")");
        break;
      case INTEGER:
        sb.append(", numeric(" + (int) min + " - " + (int) max + ")");
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

  /**
   * Serializes the statistics.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public void plan(final Serializer ser) throws IOException {
    final String t = type.toString().toLowerCase(Locale.ENGLISH);
    ser.attribute(TYPE, Token.token(t));
    ser.attribute(COUNT, Token.token(count));
    switch(type) {
      case CATEGORY:
        for(final byte[] cat : cats) {
          ser.openElement(VALUE, COUNT, Token.token(cats.value(cat)));
          ser.text(cat);
          ser.closeElement();
        }
        break;
      case DOUBLE:
      case INTEGER:
        ser.attribute(MIN, Token.token(min));
        ser.attribute(MAX, Token.token(max));
        break;
      default:
        break;
    }
  }
}
