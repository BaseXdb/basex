package org.basex.query.func.util;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class UtilRoot extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value nodes = arg(0).value(qc);
    if(nodes.seqType().type == NodeType.DOCUMENT_NODE) return nodes;

    final Iter iter = nodes.iter();
    final ANodeBuilder list = new ANodeBuilder();
    for(Item item; (item = qc.next(iter)) != null;) {
      final ANode node = item instanceof ANode ? ((ANode) item).root() : null;
      if(node == null || node.type != NodeType.DOCUMENT_NODE) throw NODOC_X.get(info, nodes);
      list.add(node);
    }
    return list.value(this);
  }

  @Override
  public Expr opt(final CompileContext cc) {
    final Expr nodes = arg(0);
    final SeqType st = nodes.seqType();
    if(st.instanceOf(SeqType.DOCUMENT_NODE_ZM)) return nodes;

    exprType.assign(st.zeroOrOne() ? st.occ : Occ.ZERO_OR_MORE).data(nodes);
    return this;
  }

  /**
   * Returns the root of the specified path nodes.
   * @param nodes path nodes
   * @return root, or {@code null} if node cannot be detected
   */
  public static ArrayList<PathNode> nodes(final ArrayList<PathNode> nodes) {
    final ArrayList<PathNode> list = new ArrayList<>();
    for(final PathNode pn : nodes) {
      PathNode node = pn;
      while(node.parent != null) node = node.parent;
      if(!list.contains(node)) list.add(node);
    }
    return list;
  }
}
