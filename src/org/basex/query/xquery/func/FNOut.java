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
        if(arg.length == 0) Err.or(FUNERR1);
        final Item it = arg[0].atomic(this, true);
        if(it == null && arg.length == 1) Err.empty(this);
        final byte[] n = it == null && arg.length != 1 ?
            Token.token(FUNERR) : ((QNm) check(it, Type.QNM)).ln();
        if(arg.length == 1) Err.or(FUNERR2, n);
        final byte[] dsc = checkStr(arg[1]);
        if(arg.length == 2) Err.or(FUNERR3, n, dsc);
        Err.or(FUNERR4, n, dsc, checkStr(arg[2]));
        return null;
      case TRACE:
        final String msg = Token.string(checkStr(arg[1])) + ": " + arg[0];
        ctx.evalInfo(msg);
        System.out.println(msg);
        return arg[0];
      default:
        throw new RuntimeException("Not defined: " + func);
    }
  }
}
