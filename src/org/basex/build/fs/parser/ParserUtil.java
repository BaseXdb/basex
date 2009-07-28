package org.basex.build.fs.parser;

import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.DateField;
import org.basex.util.Token;

/**
 * Utility methods for file parsers.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class ParserUtil {

  /** Factory to create date and duration values. */
  private static DatatypeFactory factory = null;

  static {
    try {
      factory = DatatypeFactory.newInstance();
    } catch(final DatatypeConfigurationException ex) {
      BaseX.debug(ex.getMessage());
    }
  }

  /** Hidden constructor. */
  private ParserUtil() { /* */}

  /**
   * Converts a duration value in milliseconds to an {@link Duration} instance.
   * @param milliseconds the number of milliseconds to convert.
   * @return the {@link Duration} instance or <code>null</code> if the
   *         conversion fails.
   */
  public static Duration convertMsDuration(final int milliseconds) {
    return factory == null ? null : factory.newDuration(milliseconds);
  }

  /**
   * Checks if the value is of the form <code>mm:ss</code> or if it is a
   * milliseconds value and returns a {@link Duration} instance.
   * @param value the byte array to convert.
   * @return the {@link Duration} instance or <code>null</code> if the
   *         conversion fails.
   */
  public static Duration convertDuration(final byte[] value) {
    return Token.contains(value, ':') ? convertMinSecDuration(value)
        : convertMsDuration(toInt(value));
  }

  /**
   * Converts a duration value of the form <code>mm:ss</code> to an
   * {@link Duration} instance.
   * @param minSec the byte array containing the duration value.
   * @return the {@link Duration} instance or <code>null</code> if the
   *         conversion fails.
   */
  public static Duration convertMinSecDuration(final byte[] minSec) {
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
    if(mins == Integer.MIN_VALUE || NewFSParser.VERBOSE) BaseX.debug(
        "ParserUtil: Invalid min value in minSec duration (%)", string(minSec));
    // read seconds
    final int secs = toInt(minSec, ++i, max);
    if(secs == Integer.MIN_VALUE || NewFSParser.VERBOSE) BaseX.debug(
        "ParserUtil: Invalid sec value in minSec duration (%)", string(minSec));
    final int milliseconds = secs * 1000 + mins * 60000;
    return factory.newDuration(milliseconds);
  }

  /**
   * Converts a time value to an {@link XMLGregorianCalendar} instance. The
   * {@link DatatypeFactory} <code>factory</code> has to be initialized.
   * @param time the time value to convert.
   * @return {@link XMLGregorianCalendar} representing the time value
   */
  private static XMLGregorianCalendar getGCal(final long time) {
    if(time == 0) return null;
    final GregorianCalendar gc = new GregorianCalendar();
    gc.setTimeInMillis(time);
    return factory.newXMLGregorianCalendar(gc);
  }

  /**
   * Converts a date value to an {@link XMLGregorianCalendar} instance.
   * @param date the {@link Date} value to convert.
   * @return the {@link XMLGregorianCalendar} instance or <code>null</code> if
   *         the conversion fails.
   */
  public static XMLGregorianCalendar convertDate(final Date date) {
    final GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    return factory == null ? null : factory.newXMLGregorianCalendar(gc);
  }

  /**
   * Writes the date values of the file to the parser.
   * @param parser the {@link NewFSParser} instance to fire
   *          {@link NewFSParser#metaEvent(Metadata)} events.
   * @param meta the metadata item to use.
   * @param file the file to read time values from.
   * @throws IOException if any error occurs while writing to the parser.
   */
  public static void fireDateEvents(final NewFSParser parser,
      final Metadata meta, final File file) throws IOException {
    if(factory == null) return;
    XMLGregorianCalendar xmlCal;
    final long time = file.lastModified();
    if(time != 0) {
      xmlCal = getGCal(time);
      meta.setDate(DateField.DATE_CONTENT_MODIFIED, xmlCal);
      parser.metaEvent(meta);
    }
  }

  /**
   * Checks if the given byte array only contains valid ascii chars. All invalid
   * chars are removed.
   * @param data the byte array to check.
   * @return a byte array containing only valid ascii chars.
   */
  static byte[] checkAscii(final byte[] data) {
    for(int i = 0, max = data.length; i < max; i++) {
      if(data[i] < 0) data[i] = ' ';
    }
    return data;
  }
}
