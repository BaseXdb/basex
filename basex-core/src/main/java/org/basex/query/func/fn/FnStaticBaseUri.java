package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnStaticBaseUri extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    final Uri uri = sc.baseURI();
    return uri == Uri.EMPTY ? Empty.VALUE : uri;
  }
}
