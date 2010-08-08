package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Data.IndexType;
import org.basex.io.IO;
import org.basex.io.TextInput;
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
    switch(func) {
      case INDEX: return index(ctx);
      default:    return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(func) {
      case EVAL:   return eval(ctx);
      case READ:   return read(ctx);
      case RANDOM: return random();
      case RUN:    return run(ctx);
      case DB:     return db(ctx);
      case NODEID: return nodeId(ctx);
      case FSPATH: return fspath(ctx);
      default:     return super.atomic(ctx, ii);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Performs the query function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item run(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    try {
      return eval(ctx, io.content());
    } catch(final IOException ex) {
      Err.or(input, NODOC, ex.getMessage());
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
  private Item eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {
    final QueryContext qt = new QueryContext(ctx.context);
    qt.parse(string(qu));
    qt.compile();
    return qt.iter().finish();
  }

  /**
   * Performs the read function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item read(final QueryContext ctx) throws QueryException {
    try {
      return Str.get(TextInput.content(checkIO(expr[0], ctx)).finish());
    } catch(final IOException ex) {
      Err.or(input, NODOC, ex.getMessage());
      return null;
    }
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
      tb.add(data.fs.path(((DBNode) it.atomic(ctx, input)).pre, false));
    }
    return tb.size() == 0 ? Str.ZERO : Str.get(tb.finish());
  }

  /**
   * Performs the db function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private DBNode db(final QueryContext ctx) throws QueryException {
    DBNode node = ctx.doc(checkStr(expr[0], ctx), false, true, input);

    if(expr.length == 2) {
      final int pre = (int) checkItr(expr[1], ctx);
      if(pre < 0 || pre >= node.data.meta.size) Err.or(input, NOPRE, pre);
      node = new DBNode(node.data, pre);
    }
    return node;
  }

  /**
   * Performs the node-id function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Itr nodeId(final QueryContext ctx) throws QueryException {
    final Nod node = checkNode(expr[0].atomic(ctx, input));
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
    if(data == null) Err.or(input, XPNOCTX, this);

    final IndexContext ic = new IndexContext(ctx, data, null, true);
    final String type = string(checkEStr(expr[1], ctx)).toLowerCase();

    if(type.equals(FULLTEXT)) {
      if(!data.meta.ftxindex) Err.or(input, NOIDX, FULLTEXT);
      return new FTIndexAccess(input, new FTWords(input, data,
          checkEStr(expr[0], ctx), ctx.ftpos == null), ic).iter(ctx);
    }
    if(type.equals(TEXT)) {
      if(!data.meta.txtindex) Err.or(input, NOIDX, TEXT);
      return new IndexAccess(input, expr[0], IndexType.TXT, ic).iter(ctx);
    }
    if(type.equals(ATTRIBUTE)) {
      if(!data.meta.atvindex) Err.or(input, NOIDX, ATTRIBUTE);
      return new IndexAccess(input, expr[0], IndexType.ATV, ic).iter(ctx);
    }

    Err.or(input, WHICHIDX, type);
    return null;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.CTX && func == FunDef.RANDOM || super.uses(u, ctx);
  }
}
