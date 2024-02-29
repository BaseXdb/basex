package org.basex.query.util.parse;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Container for parsed function data.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FuncBuilder {
  /** Input Info. */
  public final InputInfo info;

  /** Variable scope (literals). */
  public VarScope vs;
  /** Parameters (literals). */
  public Var[] params;
  /** Annotations (literals). */
  public AnnList anns = AnnList.EMPTY;
  /** Runtime flag (literals). */
  public boolean runtime;
  /** Fixed argument list (literals). */
  private Expr[] fixedArgs;
  /** Argument counter (literals). */
  private int c;

  /** Keyword-based arguments (initialized if required). */
  public QNmMap<Expr> keywords;
  /** Arguments. */
  private ExprList args;
  /** Placeholders (initialized if required). */
  private IntList holes;

  /**
   * Constructor.
   * @param info input info
   */
  public FuncBuilder(final InputInfo info) {
    this.info = info;
  }

  /**
   * Initializes the builder.
   * @param arguments initial arguments
   * @param kw initial keywords (can be {@code null})
   * @return self reference
   */
  public FuncBuilder init(final Expr[] arguments, final QNmMap<Expr> kw) {
    args = arguments != null ? new ExprList(arguments) : new ExprList(4);
    keywords = kw;
    return this;
  }

  /**
   * Adds an argument.
   * @param arg argument ({@code null} for placeholder)
   */
  public void add(final Expr arg) {
    if(arg != null) {
      args.add(arg);
    } else {
      if(holes == null) holes = new IntList(4);
      holes.add(holes.size() + args.size());
    }
  }

  /**
   * Adds a keyword expression.
   * @param qnm name of keyword
   * @param arg argument ({@code null} for placeholder)
   * @return {@code true} if the entry already exists
   */
  public boolean add(final QNm qnm, final Expr arg) {
    if(keywords == null) keywords = new QNmMap<>();
    final boolean contains = keywords.contains(qnm);
    keywords.put(qnm, arg);
    return contains;
  }

  /**
   * Returns function arguments.
   * @return arguments
   */
  public Expr[] args() {
    return fixedArgs != null ? fixedArgs : args.toArray();
  }

  /**
   * Returns placeholder positions.
   * @return placeholders (can be {@code null})
   */
  public int[] holes() {
    return holes.finish();
  }

  /**
   * Indicates if this is a partial function application.
   * @return placeholders (can be {@code null})
   */
  public boolean partial() {
    return holes != null;
  }

  /**
   * Returns the function arity.
   * @return arity
   */
  public int arity() {
    return fixedArgs != null ? fixedArgs.length : args.size() + holes.size();
  }

  // LITERALS =====================================================================================

  /**
   * Initializes the builder for function literals.
   * @param a arity
   * @param rt {@code true} if the function is created at runtime
   * @return self reference
   */
  public FuncBuilder initLiteral(final int a, final boolean rt) {
    fixedArgs = new Expr[a];
    params = new Var[a];
    vs = new VarScope();
    runtime = rt;
    return this;
  }

  /**
   * Adds a parameter and argument.
   * @param name parameter name
   * @param st parameter type
   * @param qc query context
   */
  public void add(final QNm name, final SeqType st, final QueryContext qc) {
    final Var var = vs.addNew(name, st, true, qc, info);
    params[c] = var;
    fixedArgs[c++] = new VarRef(info, var);
  }
}
