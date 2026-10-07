package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnNamespaceUriForPrefix extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value prefix = definition.types[0].coerce(arg(0).value(qc), qc, info);
    final byte[] value = prefix.isEmpty() ? EMPTY : ((Item) prefix).string(info);
    final XNode element = toElem(arg(1), qc);

    if(eq(value, XML)) return Uri.get(XML_URI, false);
    final Atts at = element.nsScope(qc);
    final byte[] uri = at.value(value);
    return uri == null || uri.length == 0 ? Empty.VALUE : Uri.get(uri, false);
  }
}
