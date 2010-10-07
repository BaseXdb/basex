package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;

/**
 * Doc test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DocTest extends Test {
  /** Query context. */
  final QueryContext ctx;

  /**
   * Constructor.
   * @param c query context
   */
  DocTest(final QueryContext c) {
    type = Type.DOC;
    // [AW] better: cache collection path instead of query context,
    //   or only pass on correct documents nodes
    ctx = c;
  }

  @Override
  public boolean eval(final Nod n) {
    if(n.type != type) return false;
    // [AW] here: compare path instead of document array
    for(int p = 0; p < ctx.docs; p++) {
      if(n.is(ctx.doc[p])) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
