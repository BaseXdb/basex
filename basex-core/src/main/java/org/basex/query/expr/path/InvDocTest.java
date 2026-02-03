package org.basex.query.expr.path;

import org.basex.data.*;
import org.basex.query.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class InvDocTest extends Test {
  /** Data reference. */
  private final Data data;
  /** Sorted PRE values. */
  private final IntList pres;

  /**
   * Constructor.
   * @param pres sorted PRE values
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
   * @param root compile time root value
   * @return document test
   * @throws QueryException query exception
   */
  static Test get(final Value root) throws QueryException {
    // use simple test if database contains only one document
    final Data data = root.data();
    if(data == null || data.meta.ndocs == 1) return NodeTest.DOCUMENT_NODE;

    // include PRE values of root nodes in document test
    final IntList pres;
    if(root instanceof final DBNodeSeq seq) {
      if(seq.all()) return NodeTest.DOCUMENT_NODE;
      pres = new IntList(seq.pres());
    } else {
      // loop through all documents and add PRE values of documents
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
  public boolean matches(final XNode node) {
    // database node, ensure that the PRE value is contained in the target documents
    return node instanceof final DBNode db && data == db.data() &&
        pres.sortedIndexOf(db.pre()) >= 0;
  }

  @Override
  public Test intersect(final Test test) {
    throw Util.notExpected(this);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final InvDocTest idt && data == idt.data &&
        pres.equals(idt.pres);
  }

  @Override
  public String toString(final boolean full) {
    return NodeType.DOCUMENT_NODE.toString("(: ids :)");
  }
}
