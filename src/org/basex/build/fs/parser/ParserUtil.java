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
    if(Token.ws(milliseconds)) return Token.EMPTY;
    int len = milliseconds.length;
    byte[] value;
    if(len < 4) {
      value = new byte[] { 'P', 'T', '0', '.', '0', '0', '0', 'S'};
      int offset = 7 - len;
      for(int i = 0; i < len; i++) {
        if(milliseconds[i] < '0' || milliseconds[i] > '9') {
          BaseX.debug("ParserUtil: Invalid duration: %",
              Token.string(milliseconds));
          return Token.EMPTY;
        }
        value[i + offset] = milliseconds[i];
      }
    } else {
      int newLen = len + 4; // get space for 'P', 'T', '.' and 'S'
      value = new byte[newLen];
      int pos = 0;
      value[pos++] = 'P';
      value[pos++] = 'T';
      int secLen = len - 3; // number of digits for the seconds
      int i = 0;
      for(; i < secLen; i++) {
        if(milliseconds[i] < '0' || milliseconds[i] > '9') {
          BaseX.debug("ParserUtil: Invalid duration: %",
              Token.string(milliseconds));
          return Token.EMPTY;
        }
        value[pos++] = milliseconds[i];
      }
      value[pos++] = '.';
      for(; i < len; i++) {
        if(milliseconds[i] < '0' || milliseconds[i] > '9') {
          BaseX.debug("ParserUtil: Invalid duration: %",
              Token.string(milliseconds));
          return Token.EMPTY;
        }
        value[pos++] = milliseconds[i];
      }
      value[pos] = 'S';
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
        BaseX.debug("Invalid date value: " + Token.string(year));
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
    if(time != 0) return token(time);
    else return Token.EMPTY;
  }
}
