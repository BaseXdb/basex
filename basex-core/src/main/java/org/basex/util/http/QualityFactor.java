package org.basex.util.http;

import org.basex.util.*;

/**
 * Quality factors of weighted HTTP header values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QualityFactor {
  /** Private constructor. */
  private QualityFactor() { }

  /**
   * Returns the quality factor of a token in a weighted header list.
   * @param header header value (can be {@code null})
   * @param token token, compared case-insensitively
   * @return quality factor, or {@code 0} if the token is not listed or its factor is invalid
   */
  public static double of(final String header, final String token) {
    if(header == null) return 0;
    for(final String element : Strings.split(header, ',')) {
      final int p = element.indexOf(';');
      if((p == -1 ? element : element.substring(0, p)).trim().equalsIgnoreCase(token)) {
        return get(p == -1 ? null : MediaType.parameters(element.substring(p + 1)).get("q"));
      }
    }
    return 0;
  }

  /**
   * Returns the quality factor of a parameter value.
   * @param value value (can be {@code null})
   * @return quality factor, {@code 1} if the value is {@code null}, {@code 0} if it is invalid
   */
  public static double get(final String value) {
    final double qf = parse(value);
    return Double.isNaN(qf) ? 0 : qf;
  }

  /**
   * Checks if a parameter value is a valid quality factor.
   * @param value value
   * @return result of check
   */
  public static boolean valid(final String value) {
    return !Double.isNaN(parse(value));
  }

  /**
   * Parses a parameter value.
   * @param value value (can be {@code null})
   * @return quality factor, {@code 1} if the value is {@code null}, {@code NaN} if it is invalid
   */
  private static double parse(final String value) {
    if(value == null) return 1;
    final double qf = Token.toDouble(Token.token(value));
    return qf >= 0 && qf <= 1 ? qf : Double.NaN;
  }
}
