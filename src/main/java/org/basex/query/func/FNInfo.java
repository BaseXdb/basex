package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.util.Token;

/**
 * Info functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNInfo extends Fun {
  /**
   * Constructor.
   * @param i query info
   * @param f function definition
   * @param e arguments
   */
  protected FNInfo(final QueryInfo i, final FunDef f, final Expr... e) {
    super(i, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ERROR:
        final int al = expr.length;
        String code = FOER;
        Object num = 0;
        String msg = FUNERR1;

        if(al != 0) {
          final Item it = expr[0].atomic(ctx);
          if(it == null) {
            if(al == 1) emptyError();
          } else {
            code = Token.string(((QNm) checkType(it, Type.QNM)).ln());
            num = null;
          }
          if(al > 1) {
            msg = Token.string(checkStr(expr[1], ctx));
          }
        }
        try {
          error(new Object[] { code, num, msg });
          return null;
        } catch(final QueryException ex) {
          if(al > 2) ex.iter = expr[2].iter(ctx);
          throw ex;
        }
      case TRACE:
        final Iter ir = SeqIter.get(expr[0].iter(ctx));
        msg = Token.string(checkStr(expr[1], ctx)) + " " + ir;
        ctx.evalInfo(msg);
        return ir;
      case ENVS:
        final SeqIter si = new SeqIter();
        for(final Object k : System.getenv().keySet().toArray()) {
          si.add(Str.get(k));
        }
        return si;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ENV:
        final String env = System.getenv(Token.string(checkStr(expr[0], ctx)));
        return env != null ? Str.get(env) : null;
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(func == FunDef.ENV)  return expr[0].item() ? atomic(ctx) : this;
    if(func == FunDef.ENVS) return SeqIter.get(iter(ctx)).finish();
    return this;
  }

  @Override
  public boolean vacuous() {
    return func == FunDef.ERROR;
  }
}
