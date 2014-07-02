package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Info functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNInfo extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNInfo(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ERROR: return error(ctx);
      case TRACE: return trace(ctx);
      case AVAILABLE_ENVIRONMENT_VARIABLES: return avlEnvVars();
      default: return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
      case ENVIRONMENT_VARIABLE: return envVar(ctx);
      default: return super.item(ctx, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext ctx, final VarScope scp) {
    if(func == TRACE) type = exprs[0].type();
    return this;
  }

  /**
   * Performs the error function.
   * @param ctx query context
   * @return dummy iterator
   * @throws QueryException query exception
   */
  private Iter error(final QueryContext ctx) throws QueryException {
    final int al = exprs.length;
    if(al == 0) throw FUNERR1.get(info);

    QNm name = FUNERR1.qname();
    String msg = FUNERR1.desc;

    final Item it = exprs[0].item(ctx, info);
    if(it == null) {
      if(al == 1) throw INVEMPTY.get(info, description());
    } else {
      name = checkQNm(it, ctx, sc);
    }
    if(al > 1) msg = Token.string(checkEStr(exprs[1], ctx));
    final Value val = al > 2 ? ctx.value(exprs[2]) : null;
    throw new QueryException(info, name, msg).value(val);
  }

  /**
   * Performs the trace function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter trace(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final Iter ir = exprs[0].iter(ctx);
      final byte[] label = checkStr(exprs[1], ctx);
      boolean empty = true;
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it != null) {
          dump(it, label, info, ctx);
          empty = false;
        } else if(empty) {
          dump(null, label, info, ctx);
        }
        return it;
      }
    };
  }

  /**
   * Returns all environment variables.
   * @return iterator
   */
  private static ValueIter avlEnvVars() {
    final ValueBuilder vb = new ValueBuilder();
    for(final Object k : System.getenv().keySet()) vb.add(Str.get(k.toString()));
    return vb;
  }

  /**
   * Returns a environment variable.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str envVar(final QueryContext ctx) throws QueryException {
    final String e = System.getenv(Token.string(checkStr(exprs[0], ctx)));
    return e != null ? Str.get(e) : null;
  }

  /**
   * Dumps the specified item.
   * @param it item to be dumped
   * @param label label
   * @param info input info
   * @param ctx query context
   * @throws QueryException query exception
   */
  public static void dump(final Item it, final byte[] label, final InputInfo info,
      final QueryContext ctx) throws QueryException {
    try {
      final byte[] value;
      if(it == null) {
        value = Token.token(SeqType.EMP.toString());
      } else if(it.type == NodeType.ATT || it.type == NodeType.NSP) {
        value = Token.token(it.toString());
      } else {
        value = it.serialize().toArray();
      }
      dump(value, label, ctx);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param value traced value
   * @param label additional label to display (can be {@code null})
   * @param ctx query context
   */
  public static void dump(final byte[] value, final byte[] label, final QueryContext ctx) {
    final TokenBuilder tb = new TokenBuilder();
    if(label != null) tb.add(label);
    tb.add(value);
    final String info = tb.toString();

    // if GUI is used or client is calling, cache trace info
    if(ctx.listen != null || ctx.context.listener != null) {
      ctx.evalInfo(info);
      if(ctx.listen != null) ctx.listen.info(info);
    } else {
      Util.errln(info);
    }
  }

  /**
   * Creates an error function instance.
   * @param ex query exception
   * @param tp type of the expression
   * @return function
   */
  public static FNInfo error(final QueryException ex, final SeqType tp) {
    final FNInfo err = new FNInfo(null, ex.info(), ERROR, ex.qname(),
        Str.get(ex.getLocalizedMessage()));
    err.type = tp;
    return err;
  }
}
