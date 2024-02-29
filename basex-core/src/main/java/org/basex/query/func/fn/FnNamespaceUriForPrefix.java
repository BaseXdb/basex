package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnNamespaceUriForPrefix extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final ANode element = toElem(arg(1), qc);

    if(eq(value, XML)) return Uri.get(XML_URI, false);
    final Atts at = element.nsScope(sc());
    final byte[] uri = at.value(value);
    return uri == null || uri.length == 0 ? Empty.VALUE : Uri.get(uri, false);
  }
}
