package org.basex.query.path;

import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;

/**
 * Doc test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DocTest extends Test {
  
  /** QueryContext. */
  QueryContext ctx;
  
  /**
   * Constructor.
   * @param c query context
   */
  DocTest(final QueryContext c) {
    type = Type.DOC;
    ctx = c;
  }

  @Override
  public boolean eval(final Nod n) {
    boolean check = false;
    for(int p = 0; p < ctx.doc.length; p++) {
      DBNode node = ctx.doc[p];
      if(node != null) {
      if(n.diff(ctx.doc[p]) == 0) {
          check = true;
          break;
        }
      check = false;
      }
    }
    return n.type != type ? false : check;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
