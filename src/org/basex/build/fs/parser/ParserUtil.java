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

  /** Hidden constructor. */
  private ParserUtil() { /* */}

  /**
   * Converts an millisecond value to a valid XML xs:duration value, e.g.
   * PT123.456S (123 seconds and 456 milliseconds). Values < 1 second are
   * skipped.
   * @param milliseconds the milliseconds value to convert.
   * @return the xs:duration value or an empty array if duration < 1 second.
   */
  public static byte[] msToDuration(final byte[] milliseconds) {
    if(milliseconds == null) return null;
    int len = milliseconds.length;
    if(len < 4) {
      // TODO: handle short durations
      return null; // skip values < 1s
    }
    int newLen = len + 4; // get space for 'P', 'T', '.' and 'S'
    byte[] value = new byte[newLen];
    value[0] = 'P';
    value[1] = 'T';
    int secLen = len - 3; // number of digits for the seconds
    System.arraycopy(milliseconds, 0, value, 2, secLen); // copy seconds
    value[2 + secLen] = '.';
    System.arraycopy(milliseconds, secLen, value, 3 + secLen, 3); // copy ms
    value[newLen - 1] = 'S';
    return value;
  }

  /**
   * Checks if the byte array contains a valid year and returns the year in
   * {@link Metadata.DataType#YEAR} format.
   * @param year the year value to convert
   * @return the year.
   */
  public static byte[] convertYear(final byte[] year) {
    // TODO: check encoding
    if(isEmpty(year) || year.length != 4) return null;
    for(int i = 0; i < 4; i++) {
      if(year[i] < '0' || year[i] > '9') { // not a valid number
        BaseX.debug("Invalid date value: " + Token.string(year));
        return null;
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
    if(time != 0) return token(time);
    else return null;
  }

  /**
   * Checks if the array has (non-whitespace) content.
   * @param value the byte array to check.
   * @return true if the array has no content, false otherwise.
   */
  public static boolean isEmpty(final byte[] value) {
    if(value == null || value.length == 0) return true;
    for(byte b : value)
      if(!Token.ws(b)) return false;
    return true;
  }
}
