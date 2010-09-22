package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.IO;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.IndexAccess;
import org.basex.query.ft.FTIndexAccess;
import org.basex.query.ft.FTWords;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Nod;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.NodIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNBaseX extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNBaseX(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case INDEX: return index(ctx);
      case EVAL:  return eval(ctx);
      case RUN:   return run(ctx);
      case DB:    return db(ctx);
      default:    return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case RANDOM: return random();      
      case NODEID: return nodeId(ctx);
      case FSPATH: return fspath(ctx);
      default:     return super.item(ctx, ii);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Performs the query function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter run(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    try {
      return eval(ctx, io.content());
    } catch(final IOException ex) {
      NODOC.thrw(input, ex.getMessage());
      return null;
    }
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {
    final QueryContext qt = new QueryContext(ctx.context);
    qt.parse(string(qu));
    qt.compile();
    return ItemIter.get(qt.iter());
  }

  /**
   * Returns the filesystem path for this node.
   * @param ctx query context
   * @return filesystem path
   * @throws QueryException query exception
   */
  private Item fspath(final QueryContext ctx) throws QueryException {
    final Data data = ctx.data();
    if(data == null || data.fs == null) return Str.ZERO;

    final Iter iter = ctx.iter(expr[0]);
    Item it;
    final TokenBuilder tb = new TokenBuilder();
    boolean first = true;
    while((it = iter.next()) != null) {
      if(first) first = false;
      else tb.add('\n');
      tb.add(data.fs.path(((DBNode) it.item(ctx, input)).pre, false));
    }
    return tb.size() == 0 ? Str.ZERO : Str.get(tb.finish());
  }

  /**
   * Performs the db function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter db(final QueryContext ctx) throws QueryException {
    NodIter iter = new NodIter();
    DBNode node = ctx.doc(checkStr(expr[0], ctx), true, true, input);

    if(expr.length == 2) {
      final int pre = (int) checkItr(expr[1], ctx);
      if(pre < 0 || pre >= node.data.meta.size) NOPRE.thrw(input, pre);
      node = new DBNode(node.data, pre);
      iter.add(node);
    } else {
      for(int p = 0; p < node.data.meta.size;
      p += node.data.size(p, node.data.kind(p))) {
        iter.add(new DBNode(node.data, p));
      }
    }
    return iter;
  }

  /**
   * Performs the node-id function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Itr nodeId(final QueryContext ctx) throws QueryException {
    final Nod node = checkNode(expr[0].item(ctx, input));
    if(!(node instanceof DBNode)) Err.type(this, Type.NOD, node);
    final DBNode dbnode = (DBNode) node;
    return Itr.get(dbnode.data.id(dbnode.pre));
  }

  /**
   * Performs the random function.
   * @return iterator
   */
  private Dbl random() {
    return Dbl.get(Math.random());
  }

  /**
   * Performs the index function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter index(final QueryContext ctx) throws QueryException {
    final Data data = ctx.data();
    if(data == null) XPNOCTX.thrw(input, this);

    final IndexContext ic = new IndexContext(ctx, data, null, true);
    final String tp = string(checkEStr(expr[1], ctx)).toLowerCase();

    if(tp.equals(FULLTEXT)) {
      if(!data.meta.ftxindex) NOIDX.thrw(input, FULLTEXT);
      return new FTIndexAccess(input, new FTWords(input, data,
          checkEStr(expr[0], ctx), ctx.ftpos == null), ic).iter(ctx);
    }
    if(tp.equals(TEXT)) {
      if(!data.meta.txtindex) NOIDX.thrw(input, TEXT);
      return new IndexAccess(input, expr[0], IndexType.TEXT, ic).iter(ctx);
    }
    if(tp.equals(ATTRIBUTE)) {
      if(!data.meta.atvindex) NOIDX.thrw(input, ATTRIBUTE);
      return new IndexAccess(input, expr[0], IndexType.ATTV, ic).iter(ctx);
    }

    WHICHIDX.thrw(input, tp);
    return null;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && def == FunDef.RANDOM || super.uses(u);
  }
}
