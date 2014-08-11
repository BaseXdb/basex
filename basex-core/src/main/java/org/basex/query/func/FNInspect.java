package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.inspect.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Inspect functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNInspect extends BuiltinFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNInspect(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _INSPECT_FUNCTIONS: return functions(qc);
      default:                 return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _INSPECT_FUNCTION: return function(qc);
      case _INSPECT_MODULE:   return module(qc);
      case _INSPECT_CONTEXT:  return context(qc);
      case _INSPECT_XQDOC:    return xqdoc(qc);
      default:                return super.item(qc, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    if(func == Function._INSPECT_FUNCTIONS && exprs.length == 0) {
      for(final StaticFunc sf : qc.funcs.funcs()) sf.compile(qc);
      return functions(qc).value();
    }
    return this;
  }

  /**
   * Performs the function function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item function(final QueryContext qc) throws QueryException {
    final FItem fn = toFunc(exprs[0], qc);
    final QNm name = fn.funcName();
    final StaticFunc sf = name == null ? null : qc.funcs.get(name, fn.arity(), null, false);
    return new PlainDoc(qc, info).function(name, sf, fn.funcType(), fn.annotations(), null);
  }

  /**
   * Performs the context function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item context(final QueryContext qc) throws QueryException {
    return new PlainDoc(qc, info).context();
  }

  /**
   * Performs the module function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item module(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return new PlainDoc(qc, info).parse(checkPath(exprs[0], qc));
  }

  /**
   * Performs the xqdoc function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item xqdoc(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    return new XQDoc(qc, info).parse(checkPath(exprs[0], qc));
  }

  /**
   * Performs the functions function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder functions(final QueryContext qc) throws QueryException {
    // about to be updated in a future version
    final ArrayList<StaticFunc> old = new ArrayList<>();
    if(exprs.length > 0) {
      // cache existing functions
      for(final StaticFunc sf : qc.funcs.funcs()) old.add(sf);
      try {
        final IO io = checkPath(exprs[0], qc);
        qc.parse(Token.string(io.read()), io.path(), sc);
        qc.compile();
      } catch(final IOException ex) {
        throw IOERR_X.get(info, ex);
      } finally {
        qc.close();
      }
    }

    final ValueBuilder vb = new ValueBuilder();
    for(final StaticFunc sf : qc.funcs.funcs()) {
      if(old.contains(sf)) continue;
      final FuncItem fi = Functions.getUser(sf, qc, sf.sc, info);
      if(sc.mixUpdates || !fi.annotations().contains(Ann.Q_UPDATING)) vb.add(fi);
    }
    return vb;
  }

  @Override
  public boolean has(final Flag flag) {
    // do not relocate function, as it introduces new code
    return flag == Flag.NDT && func == Function._INSPECT_FUNCTIONS && exprs.length == 1 ||
        super.has(flag);
  }
}
