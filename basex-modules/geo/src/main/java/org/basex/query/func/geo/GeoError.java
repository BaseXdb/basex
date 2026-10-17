package org.basex.query.func.geo;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class contains the Geo module's error messages.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public enum GeoError {
  /** Error code. */
  GEO_WHICH(1, "Unrecognized geometry type: %."),
  /** Error code. */
  GEO_READ(2, "Parsing GML: %."),
  /** Error code. */
  GEO_TYPE(3, "Wrong geometry: % expected, % found."),
  /** Error code. */
  GEO_RANGE(4, "Out of range input index: %."),
  /** Error code. */
  GEO_WRITE(5, "%."),
  /** Error code. */
  GEO_ARG(6, "Illegal argument: %.");

  /** Error code. */
  private final String code;
  /** Error message. */
  public final String message;

  /**
   * Constructor.
   * @param number error number
   * @param message message
   */
  GeoError(final int number, final String message) {
    final String n = Integer.toString(number);
    code = "GEO" + "0".repeat(4 - n.length()) + n;
    this.message = message;
  }

  /**
   * Constructs a query exception.
   * @param ii input info (can be {@code null})
   * @param ext extended info
   * @return query exception
   */
  public QueryException get(final InputInfo ii, final Object... ext) {
    return new QueryException(ii, qname(), message, ext);
  }

  /**
   * Returns the namespace URI of this error.
   * @return function
   */
  public final QNm qname() {
    return new QNm(QueryText.EXPERR_PREFIX, code, QueryText.EXPERROR_URI);
  }
}
