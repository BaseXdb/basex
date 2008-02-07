package org.basex.query.xquery.func;

import static org.basex.util.Token.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQParser;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;

/**
 * Project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNBaseX extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    switch(func) {
      case EVAL:
        new XQParser(ctx).parse(checkStr(arg[0]));
        return ctx.eval(null).item().iter();
      case RANDOM:
        Iter iter = arg[0];
        long s = iter.size();
        if(s == -1) {
          iter = new SeqIter(iter);
          s = iter.size();
        }
        Item i = null;
        long r = (long) (Math.random() * s);
        while(r-- != 0) i = iter.next();
        return i.iter();
      case CONTAINSLC:
        if(arg.length == 3) checkColl(arg[2]);
        Item it = arg[1].atomic(this, true);
        return it == null ? Bln.TRUE.iter() :
          Bln.get(containslc(checkStr(arg[0]), checkStr(it))).iter();
      case FILENAME:
        if(ctx.file == null) return Str.ZERO.iter();
        return Str.iter(token(ctx.file.getName()));
      default:
        throw new RuntimeException("Not defined: " + func);
    }
  }
}
