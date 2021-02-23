package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class InvDocTest extends Test {
  /** Data reference. */
  private final Data data;
  /** Sorted pre values. */
  private final IntList pres;

  /**
   * Constructor.
   * @param pres sorted pre values
   * @param data data reference
   */
  private InvDocTest(final IntList pres, final Data data) {
    super(NodeType.DOCUMENT_NODE);
    this.pres = pres;
    this.data = data;
  }

  /**
   * Returns a document test. This test will only be called by the {@link AxisPath} expression
   * if the root is no value, or if it only contains database nodes.
   * @param rt compile time root (can be {@code null})
   * @return document test
   * @throws QueryException query exception
   */
  static Test get(final Expr rt) throws QueryException {
    // root unknown: use simple test
    if(!(rt instanceof Value)) return KindTest.DOC;
    final Value root = (Value) rt;

    // use simple test if database contains only one document
    final Data data = root.data();
    if(data == null || data.meta.ndocs == 1) return KindTest.DOC;

    // include pre values of root nodes in document test
    final IntList pres;
    if(root instanceof DBNodeSeq) {
      final DBNodeSeq seq = (DBNodeSeq) root;
      if(seq.all()) return KindTest.DOC;
      pres = new IntList(seq.pres());
    } else {
      // loop through all documents and add pre values of documents
      pres = new IntList(Seq.initialCapacity(root.size()));
      for(final Item item : root) pres.add(((DBNode) item).pre());
    }
    return new InvDocTest(pres.sort(), data);
  }

  @Override
  public Test copy() {
    return new InvDocTest(pres, data);
  }

  @Override
  public boolean matches(final ANode node) {
    // no database node
    if(!(node instanceof DBNode)) return false;
    // ensure that the pre value is contained in the target documents
    final DBNode db = (DBNode) node;
    return data == db.data() && pres.sortedIndexOf(db.pre()) >= 0;
  }

  @Override
  public Test intersect(final Test test) {
    throw Util.notExpected(this);
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof InvDocTest)) return false;
    final InvDocTest test = (InvDocTest) obj;
    return data == test.data && pres.equals(test.pres);
  }

  @Override
  public String toString(final boolean full) {
    return NodeType.DOCUMENT_NODE.toString("(: ids :)");
  }
}
