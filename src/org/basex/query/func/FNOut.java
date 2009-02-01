package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.BaseX;
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
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    switch(func) {
      case ERROR:
        final int al = arg.length;
        String code = FOER;
        Object num = 0;
        String msg = FUNERR1;

        if(al != 0) {
          final Item it = arg[0].atomic();
          if(it == null) {
            if(al == 1) Err.empty(this);
          } else {
            code = Token.string(((QNm) check(it, Type.QNM)).ln());
            num = null;
          }
          if(al > 1) {
            msg = Token.string(checkStr(arg[1]));
          }
        }
        try {
          Err.or(new Object[] { code, num, msg });
        } catch(final QueryException ex) {
          if(al > 2) ex.iter = SeqIter.get(arg[2]);
          throw ex;
        }
        BaseX.notexpected(); return null;
      case TRACE:
        final Iter si = SeqIter.get(arg[0]);
        msg = Token.string(checkStr(arg[1])) + " " + si;
        ctx.evalInfo(msg);
        //BaseX.outln(msg);
        return si;
      default:
        BaseX.notexpected(func); return null;
    }
  }
}
