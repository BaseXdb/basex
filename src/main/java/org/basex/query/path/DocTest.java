package org.basex.query.path;

import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.DBNodeSeq;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.iter.ValueIter;
import org.basex.util.TokenBuilder;
import org.basex.util.list.IntList;

/**
 * Document test for database nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class DocTest extends Test {
  /** Database nodes. */
  final Nodes nodes;

  /**
   * Constructor.
   * @param n database document nodes
   */
  private DocTest(final Nodes n) {
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
      return seq.complete ? Test.DOC : new DocTest(new Nodes(seq.pres, data));
    }

    // loop through all documents and add pre values of documents
    // not more than 2^31 documents supported
    final IntList il = new IntList((int) ctx.value.size());
    final ValueIter ir = ctx.value.iter();
    for(Item it; (it = ir.next()) != null;) il.add(((DBNode) it).pre);
    return new DocTest(new Nodes(il.toArray(), data));
  }

  @Override
  public boolean eval(final ANode node) {
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
