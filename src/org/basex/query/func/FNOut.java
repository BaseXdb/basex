package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Output functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNOut extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ERROR:
        final int al = args.length;
        String code = FOER;
        Object num = 0;
        String msg = FUNERR1;

        if(al != 0) {
          final Item it = args[0].atomic(ctx);
          if(it == null) {
            if(al == 1) Err.empty(this);
          } else {
            code = Token.string(((QNm) check(it, Type.QNM)).ln());
            num = null;
          }
          if(al > 1) {
            msg = Token.string(checkStr(args[1], ctx));
          }
        }
        try {
          Err.or(new Object[] { code, num, msg });
          return null;
        } catch(final QueryException ex) {
          if(al > 2) ex.iter = args[2].iter(ctx);
          throw ex;
        }
      case TRACE:
        final Iter si = SeqIter.get(args[0].iter(ctx));
        msg = Token.string(checkStr(args[1], ctx)) + " " + si;
        ctx.evalInfo(msg);
        //BaseX.outln(msg);
        return si;
      default:
        return super.iter(ctx);
    }
  }
}
