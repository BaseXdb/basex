package org.basex.query.func.xslt;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XsltInit extends XsltFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    MAP.clear();
    return Empty.VALUE;
  }
}
