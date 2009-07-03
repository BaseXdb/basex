/**
 *
 */
package org.basex.build.fs.parser;

import static org.basex.util.Token.*;

import java.io.File;

import org.basex.BaseX;
import org.basex.util.Token;

/**
 * Utility methods for file parsers.
 * @author Bastian Lemke
 */
public final class ParserUtil {

  /** If true, verbose debug messages are created. */
  private static final boolean VERBOSE = true;

  /** Hidden constructor. */
  private ParserUtil() { /* */}

  /**
   * Returns an empty array and creates a debug message if <code>VERBOSE</code>
   * is true.
   * @param ms the original byte array that should be converted.
   * @return an empty byte array.
   */
  private static byte[] error(final byte[] ms) {
    if(VERBOSE) {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      boolean first = true;
      for(byte b : ms) {
        if(first) {
          first = false;
          sb.append(b);
        } else {
          sb.append("," + b);
        }
      }
      sb.append("}");
      BaseX.debug("Invalid duration (%, %)", ms, sb);
    }
    return Token.EMPTY;
  }

  /**
   * Tries to convert the given byte array to a valid xs:duration
   * representation.
   * @param value the byte array to convert.
   * @param milliseconds true, if <code>value</code> represents a milliseconds
   *          value, false if it represents a seconds value. The value of this
   *          parameter is ignored, if <code>value</code> contains minutes and
   *          seconds.
   * @return the xs:duration representation or an empty array if
   *         <code>value</code> could not be converted.
   */
  public static byte[] toDuration(final byte[] value, //
      final boolean milliseconds) {
    int len = value.length;
    byte[] a = new byte[len];
    int secPos = -1;
    int pos = 0;
    for(int i = 0; i < len; i++) {
      byte b = value[i];
      if(b == 0) continue;
      if(b < '0' || b > '9') {
        if(b != ':') return error(value);
        if(secPos != -1) return error(value); // only one colon allowed
        secPos = i;
      } else {
        a[pos++] = b;
      }
    }
    if(pos == 0) return Token.EMPTY;
    if(secPos == -1) return milliseconds ? msToDuration(a, pos)
        : secToDuration(a, pos);
    return minSecToDuration(a, secPos, pos);
  }

  /**
   * Converts a byte array containing minutes and seconds to a valid xs:duration
   * value (e.g. PT12M34S -> 12 minutes, 34 seconds).
   * @param minSec the array containing the minutes and seconds as ASCII chars.
   * @param secPos the position in the array that marks to start of the second
   *          value.
   * @param length the number of ASCII chars to read.
   * @return the xs:duration value as byte array.
   */
  public static byte[] minSecToDuration(final byte[] minSec, final int secPos,
      final int length) {
    byte[] value = new byte[length + 4];
    int p = 0;
    value[p++] = 'P';
    value[p++] = 'T';
    if(secPos > 0) {
      System.arraycopy(minSec, 0, value, 2, secPos); // copy minutes
      p += secPos;
      value[p++] = 'M';
    }
    // copy seconds
    System.arraycopy(minSec, secPos, value, p, length - secPos);
    value[length + 3] = 'S';
    return value;
  }

  /**
   * Converts a seconds value to a valid xs:duration value (e.g. PT123S -> 123
   * seconds).
   * @param seconds byte array with the seconds as ASCII chars.
   * @param length the number of ASCII chars to read.
   * @return the xs:duration value as byte array.
   */
  public static byte[] secToDuration(final byte[] seconds, final int length) {
    byte[] value = new byte[length + 3];
    value[0] = 'P';
    value[1] = 'T';
    System.arraycopy(seconds, 0, value, 2, length);
    value[length + 2] = 'S';
    return value;
  }

  /**
   * Converts an millisecond value to a valid XML xs:duration value, e.g.
   * PT123.456S (123 seconds and 456 milliseconds).
   * @param ms the milliseconds value to convert.
   * @param length the number of ASCII chars to read.
   * @return the xs:duration value or an empty array if duration < 1 second.
   */
  public static byte[] msToDuration(final byte[] ms, final int length) {
    byte[] value;
    if(length < 4) {
      value = new byte[] { 'P', 'T', '0', '.', '0', '0', '0', 'S'};
      int offset = 7 - length;
      System.arraycopy(ms, 0, value, offset, length);
    } else {
      value = new byte[length + 4]; // get space for 'P', 'T', '.' and 'S'
      value[0] = 'P';
      value[1] = 'T';
      int secLen = length - 3;
      System.arraycopy(ms, 0, value, 2, secLen);
      value[2 + secLen] = '.';
      System.arraycopy(ms, secLen, value, 3 + secLen, 3);
      value[length + 3] = 'S';
    }
    return value;
  }

  /**
   * Checks if the byte array contains a valid year and returns the year in
   * {@link Metadata.DataType#YEAR} format.
   * @param year the year value to convert
   * @return the year.
   */
  public static byte[] convertYear(final byte[] year) {
    // [BL] check encoding
    if(year.length != 4) return Token.EMPTY;
    for(int i = 0; i < 4; i++) {
      if(year[i] < '0' || year[i] > '9') { // not a valid number
        BaseX.debug("Invalid date value: %", year);
        return Token.EMPTY;
      }
    }
    byte[] y = new byte[6];
    System.arraycopy(year, 0, y, 0, 4);
    y[4] = '-';
    y[5] = '-';
    return y;
  }

  /**
   * Calculates the mtime value for the file.
   * @param file the file to calculate the mtime for.
   * @return the mtime value.
   */
  public static byte[] getMTime(final File file) {
    // current time storage: minutes from 1.1.1970
    final long time = file.lastModified() / 60000;
    return time != 0 ? token(time) : Token.EMPTY;
  }
}
