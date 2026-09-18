package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnElementToMap extends PlanFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item node = (Item) Types.DOCUMENT_OR_ELEMENT_ZO.coerce(arg(0).value(qc), qc, info);
    final ElementsOptions options = options(1, ElementsOptions::new, qc);
    if(node.isEmpty()) return Empty.VALUE;

    final ElementToMap mapping = new ElementToMap(options, qc.shared, qc, info);
    return mapping.convert((XNode) node, null);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, ElementsOptions::new, cc);
    return this;
  }
}
