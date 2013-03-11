package org.basex.query.util.format;

import org.basex.util.*;

/**
 * Format parser for integers and dates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FormatParser extends FormatUtil {
  /** Input information. */
  protected final InputInfo info;

  /** Case. */
  Case cs;
  /** Primary format token. */
  byte[] primary;
  /** Primary format or mandatory digit. */
  int digit = -1;
  /** Ordinal suffix; {@code null} if not specified. */
  byte[] ordinal;
  /** Minimum width. */
  int min;
  /** Maximum width. */
  int max = Integer.MAX_VALUE;

  /**
   * Constructor for formatting integers.
   * @param ii input info
   */
  protected FormatParser(final InputInfo ii) {
    info = ii;
  }
}
