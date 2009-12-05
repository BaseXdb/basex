package org.deepfs.fsml.util;

import static org.basex.util.Token.*;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.basex.core.Main;
import org.basex.util.Token;

/**
 * Utility methods for file parsers.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class ParserUtil {

  /** 1 Kibibyte. */
  private static final int KIB = 1024;
  /** 1 Mebibyte. */
  private static final int MIB = 1048576;
  /** 1 Gibibyte. */
  private static final int GIB = 1073741824;
  /** 1 Tibibyte. */
  private static final long TIB = 1099511627776L;
  /** 1 Pebibyte. */
  private static final long PIB = 1125899906842624L;
  /** Byte abbreviation. */
  private static final byte[] B_STR = token(" B");
  /** Kibibyte abbreviation. */
  private static final byte[] KIB_STR = token(" KiB");
  /** Mebibyte abbreviation. */
  private static final byte[] MIB_STR = token(" MiB");
  /** Gibibyte abbreviation. */
  private static final byte[] GIB_STR = token(" GiB");
  /** Tibibyte abbreviation. */
  private static final byte[] TIB_STR = token(" TiB");
  /** Pebibyte abbreviation. */
  private static final byte[] PIB_STR = token(" PiB");

  /** Factory to create date and duration values. */
  private static DatatypeFactory factory;

  static {
    try {
      factory = DatatypeFactory.newInstance();
    } catch(final DatatypeConfigurationException ex) {
      Main.debug(ex.getMessage());
    }
  }

  /** Hidden constructor. */
  private ParserUtil() { /* */}

  /**
   * Converts a duration value in milliseconds to an {@link Duration} instance.
   * @param milliseconds the number of milliseconds to convert
   * @return the {@link Duration} instance or <code>null</code> if the
   *         conversion fails
   */
  public static Duration convertMsDuration(final int milliseconds) {
    return factory == null ? null : factory.newDuration(milliseconds);
  }

  /**
   * Checks if the value is of the form <code>mm:ss</code> or if it is a
   * milliseconds value and returns a {@link Duration} instance.
   * @param value the byte array to convert
   * @return the {@link Duration} instance or <code>null</code> if the
   *         conversion fails
   */
  public static Duration convertDuration(final byte[] value) {
    return Token.contains(value, ':') ? convertMinSecDuration(value)
        : convertMsDuration(toInt(value));
  }

  /**
   * Converts a duration value of the form <code>mm:ss</code> to an
   * {@link Duration} instance.
   * @param minSec the byte array containing the duration value
   * @return the {@link Duration} instance or <code>null</code> if the
   *         conversion fails
   */
  private static Duration convertMinSecDuration(final byte[] minSec) {
    if(factory == null) return null;
    byte b;
    int i = 0;
    final int max = minSec.length;
    if(max == 0) return null;
    b = minSec[0];
    // skip whitespaces
    while(i < max) {
      if(b >= '0' && b <= ':') break;
      b = minSec[++i];
    }
    final int startPos = i;
    // read minutes
    int mins = 0;
    while(i < max) {
      if(b == ':') {
        if(i != startPos) mins = toInt(minSec, startPos, i);
        break;
      }
      b = minSec[++i];
    }
    if(mins == Integer.MIN_VALUE) Main.debug(
        "ParserUtil: Invalid min value in minSec duration (%)", string(minSec));
    // read seconds
    final int secs = toInt(minSec, ++i, max);
    if(secs == Integer.MIN_VALUE) Main.debug(
        "ParserUtil: Invalid sec value in minSec duration (%)", string(minSec));
    final int milliseconds = secs * 1000 + mins * 60000;
    return factory.newDuration(milliseconds);
  }

  /**
   * Converts a date value to an xml date value.
   * @param gc the calendar value to convert
   * @return the calendar
   */
  public static XMLGregorianCalendar convertDateTime(
      final GregorianCalendar gc) {
    final XMLGregorianCalendar xgc = factory == null ? null
        : factory.newXMLGregorianCalendar(gc);
    return xgc;
  }

  /**
   * Converts a date value to an xml date value.
   * @param date the {@link Date} value to convert
   * @return the calendar
   */
  public static XMLGregorianCalendar convertDateTime(final Date date) {
    final GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    return convertDateTime(gc);
  }

  /**
   * Converts the given values into an xml date value.
   * @param year the year
   * @param month the month
   * @param day the number of days
   * @return the calendar
   */
  public static XMLGregorianCalendar convertDate(final int year,
      final int month, final int day) {
    final XMLGregorianCalendar xgc = factory == null ? null
        : factory.newXMLGregorianCalendar();
    xgc.setYear(year);
    xgc.setMonth(month);
    xgc.setDay(day);
    return xgc;
  }

  /**
   * Converts the given values into an xml date value.
   * @param year the year
   * @param month the month
   * @return the calendar
   */
  public static XMLGregorianCalendar convertYearMonth(final int year,
      final int month) {
    final XMLGregorianCalendar xgc = factory == null ? null
        : factory.newXMLGregorianCalendar();
    xgc.setYear(year);
    xgc.setMonth(month);
    return xgc;
  }

  /**
   * Converts a year value to an xml date value.
   * @param year the year value to convert
   * @return the calendar
   */
  public static XMLGregorianCalendar convertYear(final int year) {
    final XMLGregorianCalendar xgc = factory == null ? null
        : factory.newXMLGregorianCalendar();
    xgc.setYear(year);
    return xgc;
  }

  /**
   * Checks if the given byte array only contains valid UTF-8 characters. All
   * invalid chars are replaced by spaces.
   * @param data the byte array to check
   * @return a byte array containing only valid UTF-8 chars
   */
  public static byte[] checkUTF(final byte[] data) {
    final int size = data.length;
    int b;
    for(int i = 0; i < size; i++) { // loop all chars
      b = data[i] & 0xFF;
      if(b >= 0 && b < ' ' && !ws(b)) data[i] = ' '; // control character
      else if(b > 0x7F) { // not an ascii char ... perhaps UTF-8?
        final int numBytes;
        if(b >= 0xC2 && b <= 0xDF) { // two byte UTF-8 char
          numBytes = 2;
        } else if(b >= 0xE0 && b <= 0xEF) { // three byte UTF-8 char
          numBytes = 3;
        } else if(b >= 0xF0 && b <= 0xF4) { // four byte UTF-8 char
          numBytes = 4;
        } else {
          data[i] = ' '; // not an UTF-8 character
          continue;
        }
        if(size - i < numBytes) { // UTF-8 sequence is not complete
          while(i < size)
            data[i++] = ' ';
        } else {
          boolean valid = true;
          for(int j = 1; j < numBytes; j++) {
            final int b2 = data[i + j] & 0xFF;
            if(b2 < 0x80 || b2 > 0xBF) {
              valid = false;
              break;
            }
          }
          // replace complete sequence if any of the chars is invalid
          if(!valid) {
            data[i] = ' ';
            for(int j = 1; j < numBytes; j++) {
              data[i + j] = ' ';
            }
          } else i += numBytes - 1;
        }
      } // else valid ASCII char,
    }
    return data;
  }

  /**
   * Converts a size value (number of bytes) into a human readable string (e.g.
   * 100 MiB).
   * @param size the number of bytes to convert to a human readable string
   * @return the size as byte array
   */
  public static byte[] humanReadableSize(final long size) {
    final float d = 100.0f;
    assert size >= 0;
    if(size < KIB) return concat(token(size), B_STR);
    else if(size < MIB) {
      return concat(token(Math.round(size * d / KIB) / d), KIB_STR);
    } else if(size < GIB) {
      return concat(token(Math.round(size * d / MIB) / d), MIB_STR);
    } else if(size < TIB) {
      return concat(token(Math.round(size * d / GIB) / d), GIB_STR);
    } else if(size < PIB) {
      return concat(token(Math.round(size * d / TIB) / d), TIB_STR);
    } else {
      return concat(token(Math.round(size * d / PIB) / d), PIB_STR);
    }
  }
}
