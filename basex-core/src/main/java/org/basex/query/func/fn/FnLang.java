package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnLang extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] lang = lc(toEmptyToken(exprs[0], qc));
    final ANode node = toNode(arg(1, qc), qc);
    for(ANode n = node; n != null; n = n.parent()) {
      final AxisIter atts = n.attributes();
      for(ANode at; (at = atts.next()) != null;) {
        if(eq(at.qname().string(), LANG)) {
          final byte[] ln = lc(normalize(at.string()));
          return Bln.get(startsWith(ln, lang) &&
              (lang.length == ln.length || ln[lang.length] == '-'));
        }
      }
    }
    return Bln.FALSE;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    seqType = SeqType.get(exprs[0].seqType().type, Occ.ZERO_ONE);
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 1 || super.has(flag);
  }
}
