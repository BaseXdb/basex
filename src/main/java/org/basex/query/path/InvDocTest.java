package org.basex.query.path;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Document test for inverted location paths.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class InvDocTest extends Test {
  /** Database nodes. */
  private final Nodes nodes;

  /**
   * Constructor.
   * @param n database document nodes
   */
  private InvDocTest(final Nodes n) {
    type = NodeType.DOC;
    nodes = n;
  }

  /**
   * Returns a document test. This test will only be utilized by
   * {@link AxisPath#index} if all context values are database nodes.
   * @param ctx query context
   * @param data data reference
   * @return document test
   */
  static Test get(final QueryContext ctx, final Data data) {
    // use simple test if database contains only one node
    if(data.single()) return Test.DOC;

    // adopt nodes from existing sequence
    if(ctx.value instanceof DBNodeSeq) {
      final DBNodeSeq seq = (DBNodeSeq) ctx.value;
      return seq.complete ? Test.DOC : new InvDocTest(new Nodes(seq.pres, data));
    }

    // loop through all documents and add pre values of documents
    // not more than 2^31 documents supported
    final IntList il = new IntList((int) ctx.value.size());
    final ValueIter ir = ctx.value.iter();
    for(Item it; (it = ir.next()) != null;) il.add(((DBNode) it).pre);
    return new InvDocTest(new Nodes(il.toArray(), data));
  }

  @Override
  public boolean eq(final ANode node) {
    // no document node, or no database instance
    if(node.type != type || !(node instanceof DBNode)) return false;
    // ensure that the pre value is contained in the target documents
    final DBNode db = (DBNode) node;
    return nodes.data == db.data && nodes.contains(db.pre);
  }

  @Override
  public String toString() {
    return new TokenBuilder(NodeType.DOC.string()).add("(...)").toString();
  }
}
