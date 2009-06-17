package org.basex.query.util;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Cast;
import org.basex.query.expr.Expr;
import org.basex.query.expr.FunCall;
import org.basex.query.expr.Func;
import org.basex.query.func.FNIndex;
import org.basex.query.func.Fun;
import org.basex.query.func.FunJava;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.util.Array;
import org.basex.util.Levenshtein;
import org.basex.util.Token;

/**
 * Global expression context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Functions extends ExprInfo {
  /** Local functions. */
  private Func[] func = new Func[1];
  /** Number of local functions. */
  private int size;

  /**
   * Returns the function with the specified id.
   * @param id function id
   * @return function
   */
  public Func get(final int id) {
    return func[id];
  }

  /**
   * Returns the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @return function instance
   * @throws QueryException evaluation exception
   */
  public Expr get(final QNm name, final Expr[] args) throws QueryException {
    // find function
    final byte[] uri = name.uri.str();
    final byte[] ln = name.ln();

    // parse data type constructors
    if(eq(uri, XSURI)) {
      final SeqType seq = new SeqType(name, 1, false);
      if(seq.type == null) typeErr(name);
      if(args.length != 1) Err.or(FUNCTYPE, name.str());
      return new Cast(args[0], seq);
    }

    // check Java functions - not supported in server mode
    if(!Prop.server && Token.startsWith(uri, JAVAPRE)) {
      final String c = Token.string(Token.substring(uri, JAVAPRE.length));
      // convert dashes to upper-case initials
      final StringBuilder sb = new StringBuilder(c);
      sb.append(".");
      boolean dash = false;
      for(final char b : Token.string(ln).toCharArray()) {
        if(dash) {
          sb.append(Character.toUpperCase(b));
          dash = false;
        } else {
          dash = b == '-';
          if(!dash) sb.append(b);
        }
      }

      final String java = sb.toString();
      try {
        final int i = java.lastIndexOf(".");
        final Class<?> cls = Class.forName(java.substring(0, i));
        final String mth = java.substring(i + 1);
        return new FunJava(cls, mth, args);
      } catch(final ClassNotFoundException ex) {
        Err.or(FUNCJAVA, java);
      }
    }

    // check predefined functions
    final Fun fun = FNIndex.get().get(ln, uri, args);
    if(fun != null) return fun;

    // find local function
    for(int l = 0; l < size; l++) {
      final QNm qn = func[l].var.name;
      if(Token.eq(ln, qn.ln()) && eq(uri, qn.uri.str()) &&
          args.length == func[l].args.length) return new FunCall(l, args);
    }

    if(Type.find(name, true) == null) {
      return new FunCall(add(new Func(new Var(name),
          new Var[args.length], false)), args);
    }
    return null;
  }

  /**
   * Throws an error for the specified name.
   * @param type type as string
   * @throws QueryException query exception
   */
  public static void typeErr(final QNm type) throws QueryException {
    final byte[] ln = type.ln();
    final Levenshtein ls = new Levenshtein();
    for(final Type t : Type.values()) {
      if(t.par != null &&  ls.similar(lc(ln), lc(token(t.name))))
        Err.or(FUNSIMILAR, ln, t.name);
    }
    Err.or(FUNCUNKNOWN, type.str());
  }

  /**
   * Adds a local function.
   * @param fun function instance
   * @return function id
   * @throws QueryException evaluation exception
   */
  public int add(final Func fun) throws QueryException {
    final QNm name = fun.var.name;

    final byte[] uri = name.uri.str();
    if(uri.length == 0) Err.or(FUNNONS, name.str());

    if(NSGlobal.standard(uri)) {
      if(fun.decl) Err.or(NAMERES, name.str());
      else funError(fun.var.name);
    }

    final byte[] ln = name.ln();
    for(int l = 0; l < size; l++) {
      final QNm qn = func[l].var.name;
      final byte[] u = qn.uri.str();
      final byte[] nm = qn.ln();

      if(Token.eq(ln, nm) && eq(uri, u) &&
          fun.args.length == func[l].args.length) {
        if(!func[l].decl) {
          func[l] = fun;
          return l;
        }
        Err.or(FUNCDEFINED, fun);
      }
    }

    func = Array.check(func, size);
    func[size] = fun;
    return size++;
  }

  /**
   * Compiles the functions.
   * @param ctx query context
   * @throws QueryException xquery exception
   */
  public void comp(final QueryContext ctx) throws QueryException {
    for(int i = 0; i < size; i++) func[i].comp(ctx);
  }

  /**
   * Checks if all functions have been correctly initialized.
   * @throws QueryException xquery exception
   */
  public void check() throws QueryException {
    for(int i = 0; i < size; i++) {
      if(!func[i].decl) Err.or(FUNCUNKNOWN, func[i].var.name.str());
    }
  }

  /**
   * Finds similar function names and throws an error message.
   * @param name function name
   * @throws QueryException xquery exception
   */
  public void funError(final QNm name) throws QueryException {
    // find function
    final byte[] nm = Token.lc(name.ln());
    FNIndex.get().error(nm);

    // find similar local function
    final Levenshtein ls = new Levenshtein();
    for(int n = 0; n < size; n++) {
      if(ls.similar(nm, Token.lc(func[n].var.name.ln())))
        Err.or(FUNSIMILAR, name.str(), func[n].var.name.str());
    }
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(size == 0) return;
    ser.openElement(this);
    for(int i = 0; i < size; i++) func[i].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return name();
  }
}
