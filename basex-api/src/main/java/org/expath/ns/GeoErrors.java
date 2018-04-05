package org.expath.ns;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This module contains static error functions for the Geo module.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Masoumeh Seydi
 */
final class GeoErrors {
  /** Private constructor, preventing instantiation. */
  private GeoErrors() { }

  /**
   * GEO0001: Unrecognized geo object.
   * @param name name of element
   * @return query exception
   */
  static QueryException unrecognizedGeo(final byte[] name) {
    return thrw(1, "Unrecognized Geo type: %", name);
  }

  /**
   * GEO0002: GML reader error massage (JTS).
   * @param th throwable
   * @return query exception
   */
  static QueryException gmlReaderErr(final Throwable th) {
    return thrw(2, "Parsing GML 2.0: %", th);
  }

  /**
   * GEO0003: Inappropriate input geometry.
   * @param name name of element
   * @param geo exact Geometry object
   * @return query exception
   */
  static QueryException geoType(final byte[] name, final String geo) {
    return thrw(3, "% is not an appropriate geometry for this function. "
              + "The input geometry should be a %.", name, geo);
  }

  /**
   * GEO0004: Out of range index.
   * @param geoNumber index
   * @return query exception
   */
  static QueryException outOfRangeIdx(final Int geoNumber) {
    return thrw(4, "Out of range input index: %", geoNumber);
  }

  /**
   * GEO0005: GML writer error massage (JTS).
   * @param th throwable
   * @return query exception
   */
  static QueryException gmlWriterErr(final Throwable th) {
    return thrw(5, "%", th);
  }

  /**
   * GEO0006: Illegal argument.
   * @param arg argument
   * @return query exception
   */
  static QueryException illegalArg(final Str arg) {
    return thrw(6, "Illegal argument: %", arg);
  }

  /**
   * Creates an error QName for the specified code.
   * @param code code
   * @return query exception
   */
  static QNm qname(final int code) {
    return new QNm(Util.inf("%s:GEO%04d", QueryText.EXPERR_PREFIX, code),
        QueryText.EXPERROR_URI);
  }

  /**
   * Returns a query exception.
   * @param code code
   * @param msg message
   * @param ext extension
   * @return query exception
   */
  private static QueryException thrw(final int code, final String msg, final Object... ext) {
    return new QueryException(null, qname(code), msg, ext);
  }
}
