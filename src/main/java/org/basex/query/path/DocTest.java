package org.basex.query.path;

import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.ValueIter;

/**
 * Document test for database nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DocTest extends Test {
  /** Database nodes. */
  final Nodes nodes;

  /**
   * Constructor.
   * @param v values
   * @param d data reference
   */
  private DocTest(final Value v, final Data d) {
    type = Type.DOC;

    // not more than 2^31 documents supported
    final int[] n = new int[(int) v.size()];
    final ValueIter ir = v.iter();
    Item it;
    int c = 0;
    // loop through all documents and add pre values of documents
    while((it = ir.next()) != null) n[c++] = ((DBNode) it).pre;
    nodes = new Nodes(n, d);
  }

  /**
   * Returns a document test. This test will only be called by
   * {@link AxisPath#index} if all context values are database document nodes.
   * @param ctx query context
   * @param data data reference
   * @return document test
   */
  static Test get(final QueryContext ctx, final Data data) {
    // use simple test if database contains only one document
    // (i.e., if size of the first document equals the database size)
    return data.size(0, Data.DOC) == data.meta.size ? Test.DOC :
      new DocTest(ctx.value, data);
  }

  @Override
  public boolean eval(final Nod nod) {
    // no document node, or no database instance
    if(nod.type != type || !(nod instanceof DBNode)) return false;
    // ensure that the pre value is contained in the target documents
    final DBNode db = (DBNode) nod;
    return nodes.data == db.data && nodes.contains(((DBNode) nod).pre);
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
