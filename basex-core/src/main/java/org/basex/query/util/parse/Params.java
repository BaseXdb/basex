package org.basex.query.util.parse;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Parameter list.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Params {
  /** Parameters. */
  private ArrayList<Param> params;
  /** Return type. */
  private SeqType type;

  /**
   * Returns the number of parameters.
   * @return count
   */
  public int size() {
    return params != null ? params.size() : 0;
  }

  /**
   * Adds a parameter.
   * @param name name
   * @param st sequence type
   * @param expr default expression (can be {@code null})
   * @param info input info (can be {@code null})
   * @return self reference
   */
  public Params add(final QNm name, final SeqType st, final Expr expr, final InputInfo info) {
    final Param param = new Param();
    param.name = name;
    param.type = st;
    param.expr = expr;
    param.info = info;
    if(params == null) params = new ArrayList<>(4);
    params.add(param);
    return this;
  }

  /**
   * Assigns the sequence type.
   * @param st sequence type
   * @return self reference
   */
  public Params seqType(final SeqType st) {
    type = st;
    return this;
  }

  /**
   * Finalizes the parameters.
   * @param qc query context
   * @param vars local variables
   * @return self reference
   * @throws QueryException query exception
   */
  public Params finish(final QueryContext qc, final LocalVars vars) throws QueryException {
    final int size = size();
    if(size > 0) {
      // check if the parameter names contain duplicates
      if(size > 1) {
        final QNmSet names = new QNmSet();
        for(final Param param : params) {
          if(!names.add(param.name)) throw FUNCDUPL_X.get(param.info, param.name);
        }
      }
      // create variables
      for(final Param param : params) {
        param.var = new Var(param.name, param.type, qc, param.info, true);
        vars.add(param.var);
      }
    }
    return this;
  }

  /**
   * Returns the variables values of the specified parameter list.
   * @return default values
   */
  public Var[] vars() {
    final int ps = size();
    final Var[] vars = new Var[ps];
    for(int p = 0; p < ps; p++) vars[p] = params.get(p).var;
    return vars;
  }

  /**
   * Returns the default values of the specified parameter list.
   * @return default values
   */
  public Expr[] defaults() {
    final int ps = size();
    final Expr[] defaults = new Expr[ps];
    for(int p = 0; p < ps; p++) defaults[p] = params.get(p).expr;
    return defaults;
  }

  /**
   * Returns the sequence type.
   * @return sequence type
   */
  public SeqType seqType() {
    return type;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append('(');
    if(size() != 0) {
      for(final Param param : params) {
        sb.append(sb.length() != 1 ? ", " : "").append(param);
      }
    }
    return sb.append(')').toString();
  }

  /**
   * Parameter.
   */
  private static final class Param {
    /** Input info (can be {@code null}). */
    private InputInfo info;
    /** Name. */
    private QNm name;
    /** Type. */
    private SeqType type;
    /** Default expression. */
    private Expr expr;

    /** Variable. */
    private Var var;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder().append('$').append(name);
      if(type != null) sb.append(" as ").append(type);
      if(expr != null) sb.append(" := ").append(expr);
      return sb.toString();
    }
  }
}
