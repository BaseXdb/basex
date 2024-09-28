package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnElementNumber extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(context(qc), qc);
    final ANode root = toNodeOrNull(arg(1), qc);
    final Item predicate = arg(2).item(qc, info);
    if(node == null) return Empty.VALUE;

    final ANode element = toElem(node, qc);
    final ANode rt = root != null ? root : element.root();
    final FItem pred = predicate.isEmpty() ? null : toFunction(predicate, 1, qc);

    int n = 0;
    final HofArgs args = new HofArgs(1);
    for(final ANode nd : rt.descendantOrSelfIter()) {
      if(nd.type != NodeType.ELEMENT || !(pred != null ? test(pred, args.set(0, nd), qc) :
        nd.qname().eq(element.qname()))) continue;
      if(nd.compare(element) > 0) break;
      n++;
    }
    return Int.get(n);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(true, false, cc.qc.focus.value);
  }
}
