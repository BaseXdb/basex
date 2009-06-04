package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.Tokenizer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * Index-based full-text select expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FTSelectIndex extends FTExpr {
  /** Position filter. */
  final FTSelect sel;
  /** Content flag. If true, the actual text nodes must be parsed. */
  boolean content;

  /**
   * Constructor.
   * @param s full-text selections
   */
  FTSelectIndex(final FTSelect s) {
    sel = s;
    for(final FTFilter f : sel.filter) content |= f.content();
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = sel.expr[0].iter(ctx);

    return new FTIter() {
      @Override
      public FTItem next() throws QueryException {
        FTItem it;
        while(!(it = ir.next()).empty() && !sel.standard()) {
          // [SG] this here won't be executed as, currently, no filters
          //   are allowed for the the index version (see FTSelect)
          it.convertPos();
          final Tokenizer tok = content ? new Tokenizer(it.str()) : null;
          if(sel.filter(ctx, it, tok)) break;
        }
        return it;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    sel.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return sel.toString();
  }
}
