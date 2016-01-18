package org.basex.http.restxq;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This class contains a single RESTXQ parameter.
 *
 * @author BaseX Team 2005-16, BSD License
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
   * @param name variable name
   * @param key name of parameter
   * @param value default value
   */
  RestXqParam(final QNm name, final String key, final Value value) {
    this.name = name;
    this.key = key;
    this.value = value;
  }
}
