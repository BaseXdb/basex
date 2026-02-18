package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract update expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Update extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expressions
   */
  Update(final InputInfo info, final Expr... expr) {
    super(info, Types.EMPTY_SEQUENCE_Z, expr);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.UPD.oneOf(flags) || Flag.NDT.oneOf(flags) || super.has(flags);
  }

  /**
   * Checks for namespace conflicts in attributes.
   * @param list node list
   * @param target target node
   * @return specified node list
   * @throws QueryException query exception
   */
  final GNodeList checkNS(final GNodeList list, final XNode target) throws QueryException {
    for(final XNode node : list) {
      final QNm name = node.qname();
      final byte[] prefix = name.prefix();
      // attributes without prefix have no namespace
      if(prefix.length == 0) continue;
      // check if attribute and target have the same namespace
      final byte[] uri = target.uri(prefix);
      if(uri != null && !eq(name.uri(), uri)) throw UPNSCONFL_X_X.get(info, name.uri(), uri);
    }
    return list;
  }

  /**
   * Creates a node builder.
   * @param expr node expression
   * @param qc query context
   * @return builder
   * @throws QueryException query exception
   */
  final FBuilder builder(final Expr expr, final QueryContext qc) throws QueryException {
    final FBuilder builder = new FBuilder();
    final Constr constr = new Constr(builder, info, qc).add(expr);
    if(constr.errAtt != null) throw UPNOATTRPER_X.get(info, constr.errAtt);
    if(constr.duplAtt != null) throw UPATTDUPL_X.get(info, constr.duplAtt);
    return builder;
  }
}
