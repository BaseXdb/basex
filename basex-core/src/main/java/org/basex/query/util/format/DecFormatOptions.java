package org.basex.query.util.format;

import org.basex.util.options.*;

/**
 * Properties for formatting decimal numbers.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public final class DecFormatOptions extends Options {
  /** Decimal format property: decimal-separator. */
  public static final StringOption DECIMAL_SEPARATOR = new StringOption("decimal-separator");
  /** Decimal format property: grouping-separator. */
  public static final StringOption GROUPING_SEPARATOR = new StringOption("grouping-separator");
  /** Decimal format property: exponent-separator. */
  public static final StringOption EXPONENT_SEPARATOR = new StringOption("exponent-separator");
  /** Decimal format property: infinity. */
  public static final StringOption INFINITY = new StringOption("infinity");
  /** Decimal format property: minus-sign. */
  public static final StringOption MINUS_SIGN = new StringOption("minus-sign");
  /** Decimal format property: NaN. */
  public static final StringOption NAN = new StringOption("NaN");
  /** Decimal format property: percent. */
  public static final StringOption PERCENT = new StringOption("percent");
  /** Decimal format property: per-mille. */
  public static final StringOption PER_MILLE = new StringOption("per-mille");
  /** Decimal format property: zero-digit. */
  public static final StringOption ZERO_DIGIT = new StringOption("zero-digit");
  /** Decimal format property: digit. */
  public static final StringOption DIGIT = new StringOption("digit");
  /** Decimal format property: pattern-separator. */
  public static final StringOption PATTERN_SEPARATOR = new StringOption("pattern-separator");
}
