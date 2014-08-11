package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.DeepCompare.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Simple functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNSimple extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case ONE_OR_MORE:
        final Iter ir = exprs[0].iter(qc);
        final long len = ir.size();
        if(len == 0) throw ONEORMORE.get(info);
        if(len > 0) return ir;
        return new Iter() {
          private boolean first = true;
          @Override
          public Item next() throws QueryException {
            final Item it = ir.next();
            if(first) {
              if(it == null) throw ONEORMORE.get(info);
              first = false;
            }
            return it;
          }
        };
      case UNORDERED:
        return qc.iter(exprs[0]);
      default:
        return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case ONE_OR_MORE:
        final Value val = qc.value(exprs[0]);
        if(val.isEmpty()) throw ONEORMORE.get(info);
        return val;
      case UNORDERED:
        return qc.value(exprs[0]);
      default:
        return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr e = exprs.length == 1 ? exprs[0] : null;
    switch(func) {
      case FALSE:
        return Bln.FALSE;
      case TRUE:
        return Bln.TRUE;
      case EMPTY:
        return Bln.get(e.iter(qc).next() == null);
      case EXISTS:
        return Bln.get(e.iter(qc).next() != null);
      case BOOLEAN:
        return Bln.get(e.ebv(qc, info).bool(info));
      case NOT:
        return Bln.get(!e.ebv(qc, info).bool(info));
      case DEEP_EQUAL:
        return Bln.get(deep(qc));
      case DEEP_EQUAL_OPT:
        return Bln.get(deepOpt(qc));
      case ZERO_OR_ONE:
        Iter ir = e.iter(qc);
        Item it = ir.next();
        if(it != null && ir.next() != null) throw ZEROORONE.get(info);
        return it;
      case EXACTLY_ONE:
        ir = e.iter(qc);
        it = ir.next();
        if(it == null || ir.next() != null) throw EXACTLYONE.get(info);
        return it;
      default:
        return super.item(qc, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    if(exprs.length == 0) return this;
    final Expr e = exprs[0];

    switch(func) {
      case EMPTY:
      case EXISTS:
        // ignore non-deterministic expressions (e.g.: error())
        return e.size() == -1 || e.has(Flag.NDT) || e.has(Flag.CNS) || e.has(Flag.UPD) ? this :
          Bln.get(func == Function.EMPTY ^ e.size() != 0);
      case BOOLEAN:
        // simplify, e.g.: if(boolean(A)) -> if(A)
        return e.seqType().eq(SeqType.BLN) ? e : this;
      case NOT:
        if(e.isFunction(Function.EMPTY)) {
          // simplify: not(empty(A)) -> exists(A)
          qc.compInfo(QueryText.OPTWRITE, this);
          exprs = ((Arr) e).exprs;
          func = Function.EXISTS;
        } else if(e.isFunction(Function.EXISTS)) {
          // simplify: not(exists(A)) -> empty(A)
          qc.compInfo(QueryText.OPTWRITE, this);
          exprs = ((Arr) e).exprs;
          func = Function.EMPTY;
        } else if(e instanceof CmpV || e instanceof CmpG) {
          // simplify: not('a' = 'b') -> 'a' != 'b'
          final Cmp c = ((Cmp) e).invert();
          return c == e ? this : c;
        } else if(e.isFunction(Function.NOT)) {
          // simplify: not(not(A)) -> boolean(A)
          return compBln(((Arr) e).exprs[0], info);
        } else {
          // simplify, e.g.: not(boolean(A)) -> not(A)
          exprs[0] = e.compEbv(qc);
        }
        return this;
      case ZERO_OR_ONE:
        seqType = SeqType.get(e.seqType().type, Occ.ZERO_ONE);
        return e.seqType().zeroOrOne() ? e : this;
      case EXACTLY_ONE:
        seqType = SeqType.get(e.seqType().type, Occ.ONE);
        return e.seqType().one() ? e : this;
      case ONE_OR_MORE:
        seqType = SeqType.get(e.seqType().type, Occ.ONE_MORE);
        return e.seqType().mayBeZero() ? this : e;
      case UNORDERED:
        return e;
      default:
        return this;
    }
  }

  @Override
  public Expr compEbv(final QueryContext qc) {
    if(exprs.length == 0) return this;
    final Expr e = exprs[0];

    Expr ex = this;
    if(func == Function.BOOLEAN) {
      // (test)[boolean(A)] -> (test)[A]
      if(!e.seqType().mayBeNumber()) ex = e;
    } else if(func == Function.EXISTS) {
      // if(exists(node*)) -> if(node*)
      if(e.seqType().type instanceof NodeType) ex = e;
    }
    if(ex != this) qc.compInfo(QueryText.OPTWRITE, this);
    return ex;
  }

  /**
   * Checks items for deep equality.
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deep(final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(2, qc);
    return new DeepCompare(info).collation(coll).equal(qc.iter(exprs[0]), qc.iter(exprs[1]));
  }

  /**
   * Checks items for deep equality.
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deepOpt(final QueryContext qc) throws QueryException {
    final DeepCompare cmp = new DeepCompare(info);
    final Mode[] modes = Mode.values();
    if(exprs.length == 3) {
      for(final Item it : exprs[2].atomValue(qc, info)) {
        final byte[] key = uc(toToken(it));
        boolean found = false;
        for(final Mode m : modes) {
          found = eq(key, token(m.name()));
          if(found) {
            cmp.flag(m);
            break;
          }
        }
        if(!found) throw INVALIDOPTION_X.get(info, key);
      }
    }
    return cmp.equal(qc.iter(exprs[0]), qc.iter(exprs[1]));
  }
}
