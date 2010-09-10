package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
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
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNInfo(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case ERROR:
        final int al = expr.length;
        String code = FOER;
        Object num = 0;
        String msg = FUNERR1;

        if(al != 0) {
          final Item it = expr[0].item(ctx, input);
          if(it == null) {
            if(al == 1) Err.or(input, XPEMPTY, desc());
          } else {
            code = Token.string(((QNm) checkType(it, Type.QNM)).ln());
            num = null;
          }
          if(al > 1) {
            msg = Token.string(checkEStr(expr[1], ctx));
          }
        }
        try {
          Err.or(input, new Object[] { code, num, msg });
          return null;
        } catch(final QueryException ex) {
          if(al > 2) ex.iter = expr[2].iter(ctx);
          throw ex;
        }
      case TRACE:
        final Value val = expr[0].value(ctx);
        ctx.evalInfo(checkEStr(expr[1], ctx), val.toString());
        return val.iter();
      case ENVS:
        final ItemIter ir = new ItemIter();
        for(final Object k : System.getenv().keySet().toArray()) {
          ir.add(Str.get(k));
        }
        return ir;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case ENV:
        final String e = System.getenv(Token.string(checkEStr(expr[0], ctx)));
        return e != null ? Str.get(e) : null;
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  public boolean vacuous() {
    return def == FunDef.ERROR;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X11 && (def == FunDef.ENV || def == FunDef.ENVS) ||
      u == Use.CTX && (def == FunDef.ERROR || def == FunDef.TRACE) ||
      super.uses(u);
  }
}
