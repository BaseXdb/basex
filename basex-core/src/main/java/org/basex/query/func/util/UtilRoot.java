package org.basex.query.func.util;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UtilRoot extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value nodes = arg(0).value(qc);
    if(nodes.seqType().type.kind() == Kind.DOCUMENT) return nodes;

    final GNodeBuilder list = new GNodeBuilder();
    for(final Item item : nodes) {
      final GNode root = root(item);
      if(root == null) throw NODOC_X.get(info, nodes);
      list.add(root);
    }
    return list.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr nodes = arg(0);
    final SeqType st = nodes.seqType();
    final Type type = st.type;
    if(type.instanceOf(NodeType.DOCUMENT)) return nodes;

    Type tp = null;
    if(type.instanceOf(NodeType.NODE)) {
      tp = NodeType.DOCUMENT;
    } else if(type.instanceOf(NodeType.JNODE)) {
      tp = Types.JNODE_ROOT;
    }
    if(tp != null) {
      exprType.assign(tp, st.zeroOrOne() ? st.occ : Occ.ZERO_OR_MORE).data(nodes);
    }
    return this;
  }

  /**
   * Returns the root of the specified node.
   * @param item node item
   * @return root
   */
  private GNode root(final Item item) {
    if(item instanceof final GNode gnode) {
      final GNode root = gnode.root();
      if(root.kind().oneOf(Kind.DOCUMENT, Kind.JNODE)) return root;
    }
    return null;
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
