package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnLang extends ContextFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final byte[] lang = lc(toZeroToken(arg(0), qc));
    ANode node = toNodeOrNull(arg(1), qc);
    if(node == null) node = toNode(context(qc), qc);

    for(ANode nd = node; nd != null; nd = nd.parent()) {
      final BasicNodeIter atts = nd.attributeIter();
      for(ANode at; (at = atts.next()) != null;) {
        if(eq(at.qname().string(), LANG)) {
          final byte[] ln = lc(normalize(at.string()));
          return startsWith(ln, lang) && (lang.length == ln.length || ln[lang.length] == '-');
        }
      }
    }
    return false;
  }

  @Override
  public int contextIndex() {
    return 1;
  }
}
