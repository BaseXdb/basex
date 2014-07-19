package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Accessor functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNAcc extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNAcc(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr e = exprs.length == 0 ? checkCtx(qc) : exprs[0];
    switch(func) {
      case POSITION:
        return Int.get(qc.pos);
      case LAST:
        return Int.get(qc.size);
      case STRING:
        return string(e, ii, qc);
      case NUMBER:
        return number(qc.iter(e), qc);
      case STRING_LENGTH:
        return Int.get(len(checkEStr(exprs.length == 0 ? string(e, ii, qc) : e, qc)));
      case NORMALIZE_SPACE:
        return Str.get(norm(checkEStr(e, qc)));
      case NAMESPACE_URI_FROM_QNAME:
        final Item it = e.item(qc, info);
        return it == null ? null : Uri.uri(checkQNm(it, qc, sc).uri());
      default:
        return super.item(qc, ii);
    }
  }

  /**
   * Converts the specified item to a string.
   * @param ex expression
   * @param ii input info
   * @param qc query context
   * @return double iterator
   * @throws QueryException query exception
   */
  private Item string(final Expr ex, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    final Item it = ex.item(qc, info);
    if(it == null) return Str.ZERO;
    if(it instanceof FItem) throw FISTR.get(ii, it.type);
    return it.type == AtomType.STR ? it : Str.get(it.string(ii));
  }

  /**
   * Converts the specified item to a double.
   * @param ir iterator
   * @param qc query context
   * @return double iterator
   * @throws QueryException query exception
   */
  private Item number(final Iter ir, final QueryContext qc) throws QueryException {
    final Item it = ir.next();
    if(it == null || ir.next() != null) return Dbl.NAN;
    if(it instanceof FItem) throw FIATOM.get(info, it.type);
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
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(!oneOf(func, POSITION, LAST) && exprs.length == 0 && !visitor.lock(DBLocking.CTX)) &&
      super.accept(visitor);
  }
}
