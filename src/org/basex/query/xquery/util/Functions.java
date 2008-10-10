package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Cast;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.FunCall;
import org.basex.query.xquery.expr.Func;
import org.basex.query.xquery.func.FNIndex;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunJava;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.util.Array;
import org.basex.util.Levenshtein;
import org.basex.util.Token;

/**
 * Global expression context.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   * @throws XQException evaluation exception
   */
  public Expr get(final QNm name, final Expr[] args) throws XQException {
    // find function
    final Uri uri = name.uri;
    final byte[] ln = name.ln();

    // parse data type constructors
    if(uri.eq(Uri.XS)) {
      final SeqType seq = new SeqType(name, 1, false);
      if(seq.type == null) typeErr(name);
      if(args.length != 1) Err.or(FUNCTYPE, name.str());
      return new Cast(args[0], seq);
    }

    // check Java functions
    if(Token.startsWith(uri.str(), JAVAPRE)) {
      final String c = Token.string(Token.substring(uri.str(), JAVAPRE.length));
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
      final Uri u = qn.uri;
      final byte[] nm = qn.ln();

      if(Token.eq(ln, nm) && uri.eq(u) && args.length == func[l].args.length) {
        return new FunCall(l, args);
      }
    }

    if(Type.find(name, true) == null) {
      final Func f = new Func(new Var(name), new Var[args.length], false);
      return new FunCall(add(f), args);
    }
    return null;
  }

  /**
   * Throws an error for the specified name.
   * @param type type as string
   * @throws XQException query exception
   */
  public static void typeErr(final QNm type) throws XQException {
    final byte[] ln = type.ln();
    for(final Type t : Type.values()) {
      if(t.par != null &&  Levenshtein.similar(lc(ln), lc(t.name)))
        Err.or(FUNSIMILAR, ln, t.name);
    }
    Err.or(FUNCUNKNOWN, type.str());
  }

  /**
   * Adds a local function.
   * @param fun function instance
   * @return function id
   * @throws XQException evaluation exception
   */
  public int add(final Func fun) throws XQException {
    final QNm name = fun.var.name;

    final Uri uri = name.uri;
    if(uri == Uri.EMPTY) Err.or(FUNNONS, name.str());

    if(NSGlobal.standard(uri)) {
      if(fun.decl) Err.or(NAMERES, name.str());
      else funError(fun.var.name);
    }

    final byte[] ln = name.ln();
    for(int l = 0; l < size; l++) {
      final QNm qn = func[l].var.name;
      final Uri u = qn.uri;
      final byte[] nm = qn.ln();

      if(Token.eq(ln, nm) && uri.eq(u) &&
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
   * @param ctx xquery context
   * @throws XQException xquery exception
   */
  public void comp(final XQContext ctx) throws XQException {
    for(int i = 0; i < size; i++) func[i].comp(ctx);
  }

  /**
   * Checks if all functions have been correctly initialized.
   * @throws XQException xquery exception
   */
  public void check() throws XQException {
    for(int i = 0; i < size; i++) {
      if(!func[i].decl) Err.or(FUNCUNKNOWN, func[i].var.name.str());
    }
  }

  /**
   * Finds similar function names and throws an error message.
   * @param name function name
   * @throws XQException xquery exception
   */
  public void funError(final QNm name) throws XQException {
    // find function
    final byte[] nm = Token.lc(name.ln());
    FNIndex.get().error(nm);

    // find similar local function
    for(int n = 0; n < size; n++) {
      if(Levenshtein.similar(nm, Token.lc(func[n].var.name.ln())))
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
    return Token.string(name());
  }
}
