package org.basex.query.func.rest;

import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class RestInit extends RestFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    WebModules.get(qc.context).init();
    return Empty.VALUE;
  }
}
