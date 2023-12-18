package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Properties for formatting decimal numbers.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public final class DecFormatOptions extends Options {
  /** Decimal format property: decimal-separator. */
  public static final StringOption DECIMAL_SEPARATOR = new StringOption(DF_DEC);
  /** Decimal format property: grouping-separator. */
  public static final StringOption GROUPING_SEPARATOR = new StringOption(DF_GRP);
  /** Decimal format property: exponent-separator. */
  public static final StringOption EXPONENT_SEPARATOR = new StringOption(DF_EXP);
  /** Decimal format property: infinity. */
  public static final StringOption INFINITY = new StringOption(DF_INF);
  /** Decimal format property: minus-sign. */
  public static final StringOption MINUS_SIGN = new StringOption(DF_MIN);
  /** Decimal format property: NaN. */
  public static final StringOption NAN = new StringOption(DF_NAN);
  /** Decimal format property: percent. */
  public static final StringOption PERCENT = new StringOption(DF_PC);
  /** Decimal format property: per-mille. */
  public static final StringOption PER_MILLE = new StringOption(DF_PM);
  /** Decimal format property: zero-digit. */
  public static final StringOption ZERO_DIGIT = new StringOption(DF_ZD);
  /** Decimal format property: digit. */
  public static final StringOption DIGIT = new StringOption(DF_DIG);
  /** Decimal format property: pattern-separator. */
  public static final StringOption PATTERN_SEPARATOR = new StringOption(DF_PAT);

  /**
   * Convert these properties to a map suitable for instantiating a DecFormatter.
   * @return the map
   */
  public TokenMap toTokenMap() {
    final TokenMap map = new TokenMap();
    if(contains(DECIMAL_SEPARATOR)) map.put(token(DF_DEC), token(get(DECIMAL_SEPARATOR)));
    if(contains(GROUPING_SEPARATOR)) map.put(token(DF_GRP), token(get(GROUPING_SEPARATOR)));
    if(contains(EXPONENT_SEPARATOR)) map.put(token(DF_EXP), token(get(EXPONENT_SEPARATOR)));
    if(contains(INFINITY)) map.put(token(DF_INF), token(get(INFINITY)));
    if(contains(MINUS_SIGN)) map.put(token(DF_MIN), token(get(MINUS_SIGN)));
    if(contains(NAN)) map.put(token(DF_NAN), token(get(NAN)));
    if(contains(PERCENT)) map.put(token(DF_PC), token(get(PERCENT)));
    if(contains(PER_MILLE)) map.put(token(DF_PM), token(get(PER_MILLE)));
    if(contains(ZERO_DIGIT)) map.put(token(DF_ZD), token(get(ZERO_DIGIT)));
    if(contains(DIGIT)) map.put(token(DF_DIG), token(get(DIGIT)));
    if(contains(PATTERN_SEPARATOR)) map.put(token(DF_PAT), token(get(PATTERN_SEPARATOR)));
    return map;
  }
}
