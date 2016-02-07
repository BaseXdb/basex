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
  final QNm var;
  /** Name of parameter. */
  final String name;
  /** Default value. */
  final Value value;

  /**
   * Constructor.
   * @param var variable name
   * @param name name of parameter
   * @param value default value
   */
  RestXqParam(final QNm var, final String name, final Value value) {
    this.var = var;
    this.name = name;
    this.value = value;
  }
}
