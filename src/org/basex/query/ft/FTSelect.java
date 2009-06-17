package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.FTMatches;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.Tokenizer;

/**
 * Full-text select expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTSelect extends FTExpr {
  /** Filter array. */
  final FTFilter[] filter;
  /** Content flag. If true, the actual text nodes must be parsed. */
  boolean content;

  /**
   * Constructor.
   * @param e expression
   * @param f filters
   */
  public FTSelect(final FTExpr e, final FTFilter... f) {
    super(e);
    filter = f;
    for(final FTFilter fl : f) content |= fl.content();
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    ctx.ftfast &= filter.length == 0;
    final FTExpr e = super.comp(ctx);
    for(final FTFilter f : filter) f.comp(ctx);
    return e;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem it = expr[0].atomic(ctx);
    filter(ctx, it, ctx.fttoken);
    return it;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = expr[0].iter(ctx);

    return new FTIter() {
      @Override
      public FTItem next() throws QueryException {
        FTItem it;
        while((it = ir.next()) != null && !standard()) {
          // [SG] this here won't be executed as, currently, no filters
          //   are allowed for the the index version (see FTSelect)
          //it.convertPos();
          final Tokenizer tok = content ? new Tokenizer(it.str()) : null;
          if(filter(ctx, it, tok)) break;
        }
        return it;
      }
    };
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @param item input node
   * @param ft tokenizer
   * @return result of check
   * @throws QueryException query exception
   */
  boolean filter(final QueryContext ctx, final FTItem item, final Tokenizer ft)
      throws QueryException {

    final FTMatches all = item.all;
    for(final FTFilter f : filter) {
      for(int a = 0; a < all.size; a++) {
        if(!f.filter(ctx, all.match[a], ft)) all.delete(a--);
      }
    }
    // [CG] FT: temporary?
    if(all.size == 0) item.score(0);

    return all.size != 0;
  }

  /**
   * Returns true if no position filters are specified.
   * @return result of check
   */
  boolean standard() {
    return filter.length == 0;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    return expr[0].indexAccessible(ic);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final FTFilter f : filter) f.plan(ser);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr[0]);
    for(final FTFilter f : filter) sb.append(" " + f);
    return sb.toString();
  }
}
