package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.User;
import org.basex.data.ExprInfo;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.query.expr.Cast;
import org.basex.query.expr.Expr;
import org.basex.query.expr.FuncCall;
import org.basex.query.expr.Func;
import org.basex.query.func.FNIndex;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.func.FunJava;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Levenshtein;
import org.basex.util.Reflect;

/**
 * Container for global function declarations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Functions extends ExprInfo {
  /** Cached function call. */
  private FuncCall[][] calls = { };
  /** Local functions. */
  private Func[] func = { };

  /**
   * Returns the number of functions.
   * @return function
   */
  public int size() {
    return func.length;
  }

  /**
   * Returns the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @param ctx query context
   * @return function instance
   * @param qp query parser
   * @throws QueryException query exception
   */
  public Expr get(final QNm name, final Expr[] args, final QueryContext ctx,
      final QueryParser qp) throws QueryException {

    // find function
    final byte[] uri = name.uri().atom();
    final byte[] ln = name.ln();

    // parse data type constructors
    if(eq(uri, XSURI)) {
      final Type type = Type.find(name, true);
      if(type == null || type == Type.NOT || type == Type.AAT) {
        final Levenshtein ls = new Levenshtein();
        for(final Type t : Type.values()) {
          if(t.par != null && ls.similar(lc(ln), lc(t.nam), 0))
            qp.error(FUNSIMILAR, ln, t.nam);
        }
        qp.error(FUNCUNKNOWN, name.atom());
      }
      if(args.length != 1) qp.error(FUNCTYPE, name.atom());
      return new Cast(qp.input(), args[0], SeqType.get(type, SeqType.Occ.ZO));
    }

    // check Java functions - only allowed with administrator permissions
    if(startsWith(uri, JAVAPRE) && ctx.context.user.perm(User.ADMIN)) {
      final String c = string(substring(uri, JAVAPRE.length));
      // convert dashes to upper-case initials
      final StringBuilder sb = new StringBuilder(c);
      sb.append(".");
      boolean dash = false;
      for(final char b : string(ln).toCharArray()) {
        if(dash) {
          sb.append(Character.toUpperCase(b));
          dash = false;
        } else {
          dash = b == '-';
          if(!dash) sb.append(b);
        }
      }

      final String java = sb.toString();
      final int i = java.lastIndexOf(".");
      final Class<?> cls = Reflect.find(java.substring(0, i));
      if(cls == null) qp.error(FUNCJAVA, java);
      final String mth = java.substring(i + 1);
      return new FunJava(qp.input(), cls, mth, args);
    }

    // check predefined functions
    final Fun fun = FNIndex.get().get(ln, uri, args, qp);
    if(fun != null) {
      ctx.updating |= fun.def == FunDef.PUT;
      return fun;
    }

    // find local function
    for(int l = 0; l < func.length; ++l) {
      final QNm qn = func[l].var.name;
      if(eq(ln, qn.ln()) && eq(uri, qn.uri().atom()) && args.length ==
        func[l].args.length) return add(qp.input(), qn, l, args);
    }

    // add function call for function that has not been defined yet
    if(Type.find(name, false) == null) {
      return add(qp.input(), name, add(new Func(qp.input(),
          new Var(name), new Var[args.length], false), qp), args);
    }
    return null;
  }

  /**
   * Registers and returns a new function call.
   * @param ii input info
   * @param nm function name
   * @param id function id
   * @param arg arguments
   * @return new function call
   */
  private FuncCall add(final InputInfo ii, final QNm nm, final int id,
      final Expr[] arg) {
    final FuncCall call = new FuncCall(ii, nm, arg);
    calls[id] = Array.add(calls[id], call);
    return call;
  }

  /**
   * Adds a local function.
   * @param fun function instance
   * @param qp query parser
   * @return function id
   * @throws QueryException query exception
   */
  public int add(final Func fun, final QueryParser qp) throws QueryException {
    final QNm name = fun.var.name;

    final byte[] uri = name.uri().atom();
    if(uri.length == 0) qp.error(FUNNONS, name.atom());

    if(NSGlobal.standard(uri)) {
      if(fun.declared) qp.error(NAMERES, name.atom());
      else funError(fun.var.name, qp);
    }

    final byte[] ln = name.ln();
    for(int l = 0; l < func.length; ++l) {
      final QNm qn = func[l].var.name;
      final byte[] u = qn.uri().atom();
      final byte[] nm = qn.ln();

      if(eq(ln, nm) && eq(uri, u) && fun.args.length == func[l].args.length) {
        // declare function that has been called before
        if(!func[l].declared) {
          func[l] = fun;
          return l;
        }
        // duplicate declaration
        qp.error(FUNCDEFINED, fun);
      }
    }
    // add function skeleton
    func = Array.add(func, fun);
    calls = Array.add(calls, new FuncCall[0]);
    return func.length - 1;
  }

  /**
   * Checks if all functions have been correctly declared, and initializes
   * all function calls.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    // initialize function calls
    for(int i = 0; i < func.length; ++i) {
      for(final FuncCall c : calls[i]) c.init(func[i]);
    }
    for(final Func f : func) f.check();
  }

  /**
   * Compiles the functions.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void comp(final QueryContext ctx) throws QueryException {
    // only compile those functions that are used
    for(int i = 0; i < func.length; ++i) {
      if(calls[i].length != 0) func[i].comp(ctx);
    }
  }

  /**
   * Finds similar function names and throws an error message.
   * @param name function name
   * @param qp query parser
   * @throws QueryException query exception
   */
  public void funError(final QNm name, final QueryParser qp)
      throws QueryException {
    // find function
    FNIndex.get().error(name.ln(), qp);

    // find similar local function
    final Levenshtein ls = new Levenshtein();
    final byte[] nm = lc(name.ln());
    for(int n = 0; n < func.length; ++n) {
      if(ls.similar(nm, lc(func[n].var.name.ln()), 0))
        qp.error(FUNSIMILAR, name.atom(), func[n].var.name.atom());
    }
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(func.length == 0) return;
    ser.openElement(this);
    for(int i = 0; i < func.length; ++i) func[i].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < func.length; ++i) sb.append(func[i].toString());
    return sb.toString();
  }
}
