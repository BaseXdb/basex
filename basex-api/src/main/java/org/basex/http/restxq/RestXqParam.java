package org.basex.http.restxq;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This class contains a single RESTXQ parameter.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * @param nm variable name
   * @param param name of parameter
   * @param def default value
   */
  RestXqParam(final QNm nm, final String param, final Value def) {
    name = nm;
    key = param;
    value = def;
  }
}
