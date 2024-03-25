package org.basex.query.util.parse;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Container for parsed function data.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FuncBuilder {
  /** Input Info. */
  public final InputInfo info;
  /** Keyword-based arguments (initialized if required). */
  public QNmMap<Expr> keywords;
  /** Number of placeholders. */
  public int placeholders;
  /** Arity. */
  public int arity;

  /** Annotations (literals). */
  public AnnList anns = AnnList.EMPTY;
  /** Parameters (literals). */
  public Var[] params;
  /** Variable scope (literals). */
  public VarScope vs;
  /** Runtime flag (literals). */
  public boolean runtime;

  /** Arguments. */
  private final ExprList args;

  /**
   * Constructor.
   * @param info input info
   */
  public FuncBuilder(final InputInfo info) {
    this(info, new ExprList(4));
  }

  /**
   * Constructor.
   * @param info input info
   * @param args initial arguments
   */
  public FuncBuilder(final InputInfo info, final ExprList args) {
    this.info = info;
    this.args = args;
  }

  /**
   * Constructor.
   * @param info input info
   * @param args initial arguments
   * @param kw initial keywords (can be {@code null})
   */
  public FuncBuilder(final InputInfo info, final Expr[] args, final QNmMap<Expr> kw) {
    this(info, new ExprList(args));
    keywords = kw;
  }

  /**
   * Constructor for function literals.
   * @param info input info
   * @param a arity
   * @param rt {@code true} if the function is created at runtime
   */
  public FuncBuilder(final InputInfo info, final int a, final boolean rt) {
    this(info, new ExprList(a));
    params = new Var[a];
    vs = new VarScope();
    runtime = rt;
  }

  /**
   * Adds an argument.
   * @param arg argument ({@link Empty#UNDEFINED} for placeholder)
   * @param name name of parameter (can be {@code null})
   * @return {@code true} if a key has been supplied more than once
   */
  public boolean add(final Expr arg, final QNm name) {
    boolean duplicate = false;
    if(name != null) {
      if(keywords == null) keywords = new QNmMap<>();
      else duplicate = keywords.contains(name);
      keywords.put(name, arg);
    } else {
      args.add(arg);
    }
    if(arg == Empty.UNDEFINED) placeholders++;
    arity++;
    return duplicate;
  }

  /**
   * Adds a parameter and argument for a function literal.
   * @param name parameter name
   * @param st parameter type
   * @param qc query context
   */
  public void add(final QNm name, final SeqType st, final QueryContext qc) {
    final Var var = vs.addNew(name, st, true, qc, info);
    params[arity] = var;
    add(new VarRef(info, var), null);
  }

  /**
   * Returns function arguments.
   * @return arguments
   */
  public Expr[] args() {
    // function literals: return array with full capacity
    return params != null ? args.list : args.toArray();
  }
}
