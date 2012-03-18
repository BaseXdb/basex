package org.basex.http.restxq;

import org.basex.query.item.*;

/**
 * This class contains a single RESTXQ parameter.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqParam {
  /** Variable name. */
  final QNm name;
  /** Name of parameter. */
  final String key;
  /** Default value. */
  final Value value;

  /**
   * Constructor.
   * @param n variable name
   * @param k name of parameter
   * @param v default value
   */
  RestXqParam(final QNm n, final String k, final Value v) {
    name = n;
    key = k;
    value = v;
  }
}
