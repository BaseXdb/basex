package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Accessor functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNAcc extends StandardFunc {

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case DATA: return data(qc).iter();
      default:   return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case DATA: return data(qc);
      default:   return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case POSITION:
        ctxValue(qc);
        return Int.get(qc.pos);
      case LAST:
        ctxValue(qc);
        return Int.get(qc.size);
      case STRING:
        return string(qc, ii);
      case NUMBER:
        return number(qc);
      case STRING_LENGTH:
        return Int.get(length(exprs.length == 0 ? string(qc, ii).string(ii) :
          toToken(arg(0, qc), qc, true)));
      case NORMALIZE_SPACE:
        return Str.get(normalize(toToken(arg(0, qc), qc, true)));
      case NAMESPACE_URI_FROM_QNAME:
        final QNm qnm = toQNm(arg(0, qc), qc, sc, true);
        return qnm == null ? null : Uri.uri(qnm.uri());
      default:
        return super.item(qc, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    if(func == DATA && exprs.length == 1) {
      final SeqType t = exprs[0].seqType();
      seqType = t.type instanceof NodeType ? SeqType.get(AtomType.ATM, t.occ) : t;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param qc query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Value data(final QueryContext qc) throws QueryException {
    return (exprs.length == 0 ? ctxValue(qc) : exprs[0]).atomValue(qc, info);
  }

  /**
   * Converts the evaluated expression to a string.
   * @param qc query context
   * @param ii input info
   * @return string
   * @throws QueryException query exception
   */
  private Item string(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = arg(0, qc).item(qc, info);
    if(it instanceof FItem) throw FISTRING_X.get(ii, it.type);
    return it == null ? Str.ZERO : it.type == AtomType.STR ? it : Str.get(it.string(ii));
  }

  /**
   * Converts the evaluated expression to a double.
   * @param qc query context
   * @return double
   * @throws QueryException query exception
   */
  private Item number(final QueryContext qc) throws QueryException {
    final Item it = arg(0, qc).atomItem(qc, info);
    if(it == null) return Dbl.NAN;
    if(it.type == AtomType.DBL) return it;
    try {
      if(info != null) info.check(true);
      return AtomType.DBL.cast(it, qc, sc, info);
    } catch(final QueryException ex) {
      return Dbl.NAN;
    } finally {
      if(info != null) info.check(false);
    }
  }

  @Override
  public boolean has(final Flag flag) {
    final boolean ctx = exprs.length == 0;
    return flag == Flag.X30 && func == DATA && ctx || flag == Flag.CTX && ctx || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(!oneOf(func, POSITION, LAST) && exprs.length == 0 && !visitor.lock(DBLocking.CTX)) &&
      super.accept(visitor);
  }
}
