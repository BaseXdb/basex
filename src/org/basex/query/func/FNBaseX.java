package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.io.IO;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.IndexAccess;
import org.basex.query.ft.FTIndexAccess;
import org.basex.query.ft.FTWords;
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
      default:    return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FILENAME:   return filename(ctx);
      case READ:       return text(ctx);
      case RANDOM:     return random();
      default:         return super.atomic(ctx);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    final QueryContext qt = new QueryContext(ctx.context);
    qt.parse(string(checkStr(expr[0], ctx)));
    qt.compile();
    return qt.iter();
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item text(final QueryContext ctx) throws QueryException {
    final byte[] name = checkStr(expr[0], ctx);
    final IO io = IO.get(string(name));
    if(!Prop.web && io.exists()) {
      try {
        return Str.get(io.content());
      } catch(final IOException ex) {
        BaseX.debug(ex);
      }
    }
    Err.or(NOFILE, name);
    return null;
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
      return new FTIndexAccess(new FTWords(
          data, word, ctx.ftpos == null), ic).iter(ctx);
    }
    if(type.equals(TEXT)) {
      if(!data.meta.txtindex) Err.or(NOIDX, TEXT);
      return new IndexAccess(expr[0], Type.TXT, ic).iter(ctx);
    }
    if(type.equals(ATTRIBUTE)) {
      if(!data.meta.atvindex) Err.or(NOIDX, ATTRIBUTE);
      return new IndexAccess(expr[0], Type.ATV, ic).iter(ctx);
    }

    Err.or(WHICHIDX, type);
    return null;
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
