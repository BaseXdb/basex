package org.basex.query.expr.path;

import org.basex.data.*;
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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class InvDocTest extends Test {
  /** Data reference. */
  private final Data data;
  /** Pre values. */
  private final IntList pres;

  /**
   * Constructor.
   * @param pres pre values
   * @param data data reference
   */
  private InvDocTest(final IntList pres, final Data data) {
    this.pres = pres;
    this.data = data;
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
    if(data.meta.ndocs == 1) return Test.DOC;

    // adopt nodes from existing sequence
    if(rt instanceof DBNodeSeq) {
      final DBNodeSeq seq = (DBNodeSeq) rt;
      return seq.all ? Test.DOC : new InvDocTest(new IntList(seq.pres), data);
    }

    // loop through all documents and add pre values of documents
    // not more than 2^31 documents supported
    final IntList il = new IntList((int) rt.size());
    for(final Item it : rt) il.add(((DBNode) it).pre);
    return new InvDocTest(il, data);
  }

  @Override
  public boolean eq(final ANode node) {
    // no database node
    if(!(node instanceof DBNode)) return false;
    // ensure that the pre value is contained in the target documents
    final DBNode db = (DBNode) node;
    return data == db.data && pres.contains(db.pre);
  }

  @Override
  public Test copy() {
    return new InvDocTest(pres, data);
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
