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
  /** Default item. */
  final Item item;

  /**
   * Constructor.
   * @param v variable name
   * @param k name of parameter
   * @param i default item
   */
  RestXqParam(final QNm v, final String k, final Item i) {
    name = v;
    key = k;
    item = i;
  }
}
