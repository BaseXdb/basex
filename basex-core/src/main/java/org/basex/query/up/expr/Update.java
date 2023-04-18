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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
abstract class Update extends Arr {
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr expressions
   */
  Update(final StaticContext sc, final InputInfo info, final Expr... expr) {
    super(info, SeqType.EMPTY_SEQUENCE_Z, expr);
    this.sc = sc;
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.UPD.in(flags) || Flag.NDT.in(flags) || super.has(flags);
  }

  /**
   * Checks if the new namespaces have conflicting namespaces.
   * @param list node list
   * @param target target node
   * @return specified node list
   * @throws QueryException query exception
   */
  final ANodeList checkNS(final ANodeList list, final ANode target) throws QueryException {
    for(final ANode node : list) {
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
   * Returns a node list.
   * @param constr node constructor
   * @param attr attributes or elements
   * @return list
   */
  final ANodeList toList(final Constr constr, final boolean attr) {
    final ANodeList list = attr ? constr.builder.attributes : constr.builder.children;
    return list != null ? list : new ANodeList();
  }
}
