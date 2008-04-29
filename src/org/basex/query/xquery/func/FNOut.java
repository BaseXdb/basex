package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * Output functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNOut extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    switch(func) {
      case ERROR:
        final int al = arg.length;
        String code = FOER;
        Object num = 0;
        String msg = FUNERR1;
        
        if(al != 0) {
          final Item it = arg[0].atomic(this, true);
          if(it == null) {
            if(al == 1) Err.empty(this);
          } else {
            code = Token.string(((QNm) check(it, Type.QNM)).ln());
            num = null;
          }
          if(al > 1) {
            msg = Token.string(checkStr(arg[1]));
            if(al > 2) msg += " (" + Token.string(checkStr(arg[2])) + ")";
          }
        }
        Err.or(new Object[] { code, num, msg});
        return null;
      case TRACE:
        msg = Token.string(checkStr(arg[1])) + ": " + arg[0];
        ctx.evalInfo(msg);
        //BaseX.outln(msg);
        return arg[0];
      default:
        throw new RuntimeException("Not defined: " + func);
    }
  }
}
