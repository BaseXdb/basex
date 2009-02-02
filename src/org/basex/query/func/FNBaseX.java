package org.basex.query.func;

import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;

/**
 * Project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNBaseX extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter[] arg = new Iter[args.length];
    for(int a = 0; a < args.length; a++) arg[a] = ctx.iter(args[a]);

    switch(func) {
      case EVAL: return eval(ctx);
      default:   return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case CONTAINSLC: return contains(ctx);
      case FILENAME:   return filename(ctx);
      case RANDOM:     return random();
      default: return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(func == FunDef.CONTAINSLC) {
      final byte[] i = args[1].i() ? checkStr((Item) args[1]) : null;
      // query string is empty; return true
      if(args[1].e() || i != null && i.length == 0) return Bln.TRUE;
      // input string is empty; return false
      if(args[0].e() && i != null && i.length != 0) return Bln.FALSE;
    }
    return this;
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    final QueryContext qt = new QueryContext();
    qt.parse(string(checkStr(args[0], ctx)));
    qt.compile(null);
    return qt.iter();
  }

  /**
   * Performs the random function.
   * @return iterator
   */
  private Item random() {
    return Dbl.get(Math.random());
  }

  /**
   * Performs the contains lower case function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item contains(final QueryContext ctx) throws QueryException {
    final byte[] qu = checkStr(args[1], ctx);
    final Iter iter = ctx.iter(args[0]);
    Item it;
    while((it = iter.next()) != null) {
      if(containslc(checkStr(it), qu)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Performs the contains lower case function.
   * @param ctx query context
   * @return iterator
   */
  private Item filename(final QueryContext ctx) {
    return ctx.file == null ? Str.ZERO : Str.get(token(ctx.file.name()));
  }
}
