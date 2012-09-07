package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Info functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNInfo extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNInfo(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case ERROR: return error(ctx);
      case TRACE: return trace(ctx);
      case AVAILABLE_ENVIRONMENT_VARIABLES: return avlEnvVars();
      default: return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case ENVIRONMENT_VARIABLE: return envVar(ctx);
      default: return super.item(ctx, ii);
    }
  }

  @Override
  public Expr comp(final QueryContext ctx) {
    if(sig == Function.TRACE) type = expr[0].type();
    return this;
  }

  @Override
  public boolean xquery3() {
    return oneOf(sig, ENVIRONMENT_VARIABLE, AVAILABLE_ENVIRONMENT_VARIABLES);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 && xquery3() || u == Use.NDT && oneOf(sig, ERROR, TRACE) ||
        super.uses(u);
  }

  /**
   * Performs the error function.
   * @param ctx query context
   * @return dummy iterator
   * @throws QueryException query exception
   */
  private Iter error(final QueryContext ctx) throws QueryException {
    final int al = expr.length;
    if(al == 0) FUNERR1.thrw(info);

    QNm name = FUNERR1.qname();
    String msg = FUNERR1.desc;

    final Item it = expr[0].item(ctx, info);
    if(it == null) {
      if(al == 1) XPEMPTY.thrw(info, description());
    } else {
      name = (QNm) checkType(it, AtomType.QNM);
    }
    if(al > 1) msg = Token.string(checkEStr(expr[1], ctx));
    final Value val = al > 2 ? ctx.value(expr[2]) : null;
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
      final Iter ir = expr[0].iter(ctx);
      final byte[] s = checkStr(expr[1], ctx);
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it != null) dump(it.serialize().toArray(), s, ctx);
        return it;
      }
    };
  }

  /**
   * Returns all environment variables.
   * @return iterator
   */
  private Iter avlEnvVars() {
    final ValueBuilder vb = new ValueBuilder();
    for(final Object k : System.getenv().keySet().toArray()) vb.add(Str.get(k));
    return vb;
  }

  /**
   * Returns a environment variable.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Item envVar(final QueryContext ctx) throws QueryException {
    final String e = System.getenv(Token.string(checkEStr(expr[0], ctx)));
    return e != null ? Str.get(e) : null;
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param value traced value
   * @param label additional label to display (can be {@code null})
   * @param ctx query context
   */
  static void dump(final byte[] value, final byte[] label, final QueryContext ctx) {
    final TokenBuilder tb = new TokenBuilder();
    if(label != null) tb.add(label);
    tb.add(value);

    // if GUI is used, or if user is no admin, trace info is cached
    if(Prop.gui || !ctx.context.user.has(Perm.ADMIN)) {
      ctx.evalInfo(tb.finish());
    } else {
      Util.errln(tb.toString());
    }
  }

  /**
   * Creates an error function instance.
   * @param ex query exception
   * @param info input info
   * @return function
   */
  public static FNInfo error(final QueryException ex, final InputInfo info) {
    return new FNInfo(info, ERROR, ex.qname(), Str.get(ex.getLocalizedMessage()));
  }
}
