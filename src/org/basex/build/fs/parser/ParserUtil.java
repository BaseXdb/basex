package org.basex.build.fs.parser;

import static org.basex.util.Token.*;

import java.io.File;

import org.basex.util.Token;

/**
 * Utility methods for file parsers.
 * @author Bastian Lemke
 */
public final class ParserUtil {

  /** Hidden constructor. */
  private ParserUtil() { /* */}

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
        if(b != ':') return Token.EMPTY;
        if(secPos != -1) return Token.EMPTY; // only one colon allowed
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
    int count = 0;
    byte[] value = { 0, 0, 0, 0, '-', '-'};
    for(int i = 0, max = year.length; i < max && count < 4; i++) {
      if(Token.digit(year[i])) value[count++] = year[i];
      else if(year[i] != 0) { // not a valid number
        return Token.EMPTY;
      }
    }
    if(count < 4) {
      return Token.EMPTY;
    }
    return value;
  }

  /**
   * Converts the byte array content (that must be of the form
   * "YYYY:MM:DD hh:mm:ss") to a valid xs:dateTime value.
   * @param dateTime the dateTime value to convert.
   * @return the converted byte array.
   */
  public static byte[] convertDateTime(final byte[] dateTime) {
    if(dateTime.length != 20) return Token.EMPTY;
    int i = 0;
    for(; i < 4; i++) {
      if(!Token.digit(dateTime[i])) return Token.EMPTY;
    }
    if(dateTime[i] != ':') return Token.EMPTY;
    dateTime[i++] = '-';
    for(; i < 7; i++) {
      if(!Token.digit(dateTime[i])) return Token.EMPTY;
    }
    if(dateTime[i] != ':') return Token.EMPTY;
    dateTime[i++] = '-';
    for(; i < 10; i++) {
      if(!Token.digit(dateTime[i])) return Token.EMPTY;
    }
    if(dateTime[i] != ' ') return Token.EMPTY;
    dateTime[i++] = 'T';
    for(; i < 13; i++) {
      if(!Token.digit(dateTime[i])) return Token.EMPTY;
    }
    if(dateTime[i++] != ':') return Token.EMPTY;
    for(; i < 16; i++) {
      if(!Token.digit(dateTime[i])) return Token.EMPTY;
    }
    if(dateTime[i++] != ':') return Token.EMPTY;
    for(; i < 19; i++) {
      if(!Token.digit(dateTime[i])) return Token.EMPTY;
    }
    return dateTime;
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

  /**
   * Checks if the given byte array only contains valid ascii chars. All invalid
   * chars are removed.
   * @param data the byte array to check.
   * @return a byte array containing only valid ascii chars.
   */
  public static byte[] checkAscii(final byte[] data) {
    for(int i = 0, max = data.length; i < max; i++) {
      if(data[i] < 0) data[i] = ' ';
    }
    return data;
  }
}
