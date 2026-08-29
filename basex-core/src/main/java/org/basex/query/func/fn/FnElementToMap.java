package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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

    // a document node is represented by its single element child (may be preceded by comments, PIs)
    XNode elem = (XNode) node;
    if(elem.type.instanceOf(NodeType.DOCUMENT)) {
      for(final GNode child : elem.childIter()) {
        if(child.kind() == Kind.ELEMENT) {
          elem = (XNode) child;
          break;
        }
      }
    }

    final Plan plan = buildPlan(options, qc);

    // create result
    final Item value = entry(elem, plan).apply(elem, null, plan, qc);
    return value.isEmpty() ? value : XQMap.get(Str.get(nodeName(elem, null, plan, qc)), value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, ElementsOptions::new, cc);
    return this;
  }
}
