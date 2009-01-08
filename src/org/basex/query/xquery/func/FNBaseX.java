package org.basex.query.xquery.func;

import static org.basex.util.Token.*;
import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
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
  
  @Override
  public Expr c(final XQContext ctx) throws XQException {
    if(func == FunDef.CONTAINSLC) {
      final byte[] i = args[1].i() ? checkStr((Item) args[1]) : null;
      // query string is empty; return true
      if(args[1].e() || i != null && i.length == 0) return Bln.TRUE;
      // input string is empty; return false
      if(args[0].e() && i != null && i.length != 0) return Bln.FALSE;
    }
    return this;
  }

  /**
   * Performs the eval function.
   * @param arg arguments
   * @return iterator
   * @throws XQException query exception
   */
  private Iter eval(final Iter[] arg) throws XQException {
    final XQContext ct = new XQContext();
    ct.parse(string(checkStr(arg[0])));
    ct.compile(null);
    return ct.iter();
  }

  /**
   * Performs the random function.
   * @return iterator
   */
  private Iter random() {
    return Dbl.get(Math.random()).iter();
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
    final byte[] qu = checkStr(arg[1]);
    Item it;
    while((it = arg[0].next()) != null) {
      if(containslc(checkStr(it), qu)) return Bln.TRUE.iter();
    }
    return Bln.FALSE.iter();
  }

  /**
   * Performs the contains lower case function.
   * @param ctx query context
   * @return iterator
   */
  private Iter filename(final XQContext ctx) {
    return ctx.file == null ? Str.ZERO.iter() :
      Str.get(token(ctx.file.name())).iter();
  }
}
