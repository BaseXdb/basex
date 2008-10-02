package org.basex.query.xquery.func;

import static org.basex.util.Token.*;

import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQParser;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.iter.Iter;

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
      case EVAL:       return eval(arg);
      case RANDOM:     return random();
      case CONTAINSLC: return contains(arg);
      case FILENAME:   return filename(ctx);
      default: BaseX.notexpected(func); return null;
    }
  }

  /**
   * Performs the eval function.
   * @param arg arguments
   * @return iterator
   * @throws XQException query exception
   */
  private Iter eval(final Iter[] arg) throws XQException {
    final XQContext ct = new XQContext();
    new XQParser(ct).parse(string(checkStr(arg[0])));
    return ct.compile(null).eval(null).item().iter();
  }

  /**
   * Performs the random function.
   * @return iterator
   */
  private Iter random() {
    return Dbl.iter(Math.random());
    /*
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
    */
  }

  /**
   * Performs the contains lower case function.
   * @param arg arguments
   * @return iterator
   * @throws XQException query exception
   */
  private Iter contains(final Iter[] arg) throws XQException {
    if(arg.length == 3) checkColl(arg[2]);
    final Item it = arg[1].atomic(this, true);
    return it == null ? Bln.TRUE.iter() :
      Bln.get(containslc(checkStr(arg[0]), checkStr(it))).iter();
  }

  /**
   * Performs the contains lower case function.
   * @param ctx query context
   * @return iterator
   */
  private Iter filename(final XQContext ctx) {
    return ctx.file == null ? Str.ZERO.iter() :
      Str.iter(token(ctx.file.name()));
  }
}
