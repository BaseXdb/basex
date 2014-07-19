package org.basex.query.path;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Document test for inverted location paths.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class InvDocTest extends Test {
  /** Database nodes. */
  private final Nodes nodes;

  /**
   * Constructor.
   * @param nodes database document nodes
   */
  private InvDocTest(final Nodes nodes) {
    this.nodes = nodes;
    type = NodeType.DOC;
  }

  /**
   * Returns a document test. This test will be called by {@link AxisPath#index} if the context
   * value only consists of database nodes.
   * @param rt root value
   * @return document test
   */
  static Test get(final Value rt) {
    // use simple test if database contains only one document
    final Data data = rt.data();
    if(data.resources.docs().size() == 1) return Test.DOC;

    // adopt nodes from existing sequence
    if(rt instanceof DBNodeSeq) {
      final DBNodeSeq seq = (DBNodeSeq) rt;
      return seq.complete ? Test.DOC : new InvDocTest(new Nodes(seq.pres, data));
    }

    // loop through all documents and add pre values of documents
    // not more than 2^31 documents supported
    final IntList il = new IntList((int) rt.size());
    final ValueIter ir = rt.iter();
    for(Item it; (it = ir.next()) != null;) il.add(((DBNode) it).pre);
    return new InvDocTest(new Nodes(il.toArray(), data));
  }

  @Override
  public boolean eq(final ANode node) {
    // no database node
    if(!(node instanceof DBNode)) return false;
    // ensure that the pre value is contained in the target documents
    final DBNode db = (DBNode) node;
    return nodes.data == db.data && nodes.contains(db.pre);
  }

  @Override
  public Test copy() {
    return new InvDocTest(new Nodes(nodes));
  }

  @Override
  public Test intersect(final Test other) {
    throw Util.notExpected(this);
  }

  @Override
  public String toString() {
    return new TokenBuilder(NodeType.DOC.string()).add("(...)").toString();
  }
}
