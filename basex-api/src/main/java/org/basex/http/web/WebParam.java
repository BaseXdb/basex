package org.basex.http.web;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This class contains a single Web parameter.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebParam {
  /** Variable name. */
  public final QNm var;
  /** Name of parameter. */
  public final String name;
  /** Default value. */
  public final Value value;

  /**
   * Constructor.
   * @param var variable name
   * @param name name of parameter
   * @param value default value
   */
  public WebParam(final QNm var, final String name, final Value value) {
    this.var = var;
    this.name = name;
    this.value = value;
  }
}
