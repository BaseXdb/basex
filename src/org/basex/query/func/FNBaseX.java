package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.ft.Tokenizer;
import org.basex.index.IndexToken;
import org.basex.index.ValuesToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.IndexAccess;
import org.basex.query.ft.FTIndex;
import org.basex.query.ft.FTIndexAccess;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class FNBaseX extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter[] arg = new Iter[expr.length];
    for(int a = 0; a < expr.length; a++) arg[a] = ctx.iter(expr[a]);

    switch(func) {
      case EVAL:  return eval(ctx);
      case INDEX: return index(ctx);
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
      final byte[] i = expr[1].i() ? checkStr((Item) expr[1]) : null;
      // query string is empty; return true
      if(expr[1].e() || i != null && i.length == 0) return Bln.TRUE;
      // input string is empty; return false
      if(expr[0].e() && i != null && i.length != 0) return Bln.FALSE;
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
    qt.parse(string(checkStr(expr[0], ctx)));
    qt.compile();
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
  private Iter index(final QueryContext ctx) throws QueryException {
    final Data data = ctx.data();
    if(data == null) Err.or(XPNOCTX, this);

    final IndexContext ic = new IndexContext(ctx, data, null, true);
    final String type = string(checkStr(expr[1], ctx)).toLowerCase();
    final byte[] word = checkStr(expr[0], ctx);

    if(type.equals(FULLTEXT)) {
      if(!data.meta.ftxindex) Err.or(NOIDX, FULLTEXT);
      return new FTIndexAccess(new FTIndex(data, word),
          new Tokenizer(word), ic).iter(ctx);
    }
    
    IndexToken it = null;
    if(type.equals(TEXT)) {
      it = new ValuesToken(true, word);
      if(!data.meta.txtindex) Err.or(NOIDX, TEXT);
    } else if(type.equals(ATTRIBUTE)) {
      it = new ValuesToken(false, word);
      if(!data.meta.atvindex) Err.or(NOIDX, ATTRIBUTE);
    } else {
      Err.or(WHICHIDX, type);
    }
    return new IndexAccess(it, ic).iter(ctx);
  }

  /**
   * Performs the contains lower case function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item contains(final QueryContext ctx) throws QueryException {
    final byte[] qu = checkStr(expr[1], ctx);
    final Iter iter = ctx.iter(expr[0]);
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
