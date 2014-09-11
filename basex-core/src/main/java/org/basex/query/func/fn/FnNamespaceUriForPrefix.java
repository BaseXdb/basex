package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnNamespaceUriForPrefix extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] pref = toEmptyToken(exprs[0], qc);
    final ANode an = toElem(exprs[1], qc);
    if(eq(pref, XML)) return Uri.uri(XML_URI, false);
    final Atts at = an.nsScope();
    final byte[] s = at.value(pref);
    return s == null || s.length == 0 ? null : Uri.uri(s, false);
  }
}
