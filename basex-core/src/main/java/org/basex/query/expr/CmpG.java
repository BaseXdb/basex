package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CmpG extends Cmp {
  /** Comparators. */
  public enum OpG {
    /** General comparison: less or equal. */
    LE("<=", OpV.LE) {
      @Override
      public OpG swap() { return OpG.GE; }
      @Override
      public OpG invert() { return OpG.LT; }
    },

    /** General comparison: less. */
    LT("<", OpV.LT) {
      @Override
      public OpG swap() { return OpG.GT; }
      @Override
      public OpG invert() { return OpG.GE; }
    },

    /** General comparison: greater of equal. */
    GE(">=", OpV.GE) {
      @Override
      public OpG swap() { return LE; }
      @Override
      public OpG invert() { return LT; }
    },

    /** General comparison: greater. */
    GT(">", OpV.GT) {
      @Override
      public OpG swap() { return LT; }
      @Override
      public OpG invert() { return LE; }
    },

    /** General comparison: equal. */
    EQ("=", OpV.EQ) {
      @Override
      public OpG swap() { return OpG.EQ; }
      @Override
      public OpG invert() { return OpG.NE; }
    },

    /** General comparison: not equal. */
    NE("!=", OpV.NE) {
      @Override
      public OpG swap() { return OpG.NE; }
      @Override
      public OpG invert() { return EQ; }
    };

    /** Cached enums (faster). */
    public static final OpG[] VALUES = values();
    /** String representation. */
    public final String name;
    /** Comparator. */
    final OpV op;

    /**
     * Constructor.
     * @param n string representation
     * @param c comparator
     */
    OpG(final String n, final OpV c) {
      name = n;
      op = c;
    }

    /**
     * Swaps the comparator.
     * @return swapped comparator
     */
    public abstract OpG swap();

    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract OpG invert();

    @Override
    public String toString() { return name; }
  }

  /** Comparator. */
  public OpG op;
  /** Flag for atomic evaluation. */
  private boolean atomic;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation
   * @param info input info
   */
  public CmpG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final InputInfo info) {
    super(info, expr1, expr2, coll);
    this.op = op;
    type = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // avoid reoptimization
    if(atomic) return this;

    // swap expressions; add text() to location paths to simplify optimizations
    if(swap()) {
      op = op.swap();
      qc.compInfo(OPTSWAP, this);
    }

    final Expr e1 = exprs[0];
    final Expr e2 = exprs[1];
    Expr e = this;
    if(oneIsEmpty()) {
      e = optPre(Bln.FALSE, qc);
    } else if(allAreValues()) {
      e = preEval(qc);
    } else if(e1.isFunction(Function.COUNT)) {
      e = compCount(op.op);
      if(e != this) qc.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
    } else if(e1.isFunction(Function.POSITION)) {
      if(e2 instanceof RangeSeq && op.op == OpV.EQ) {
        // position() CMP range
        final long p1 = ((Value) e2).itemAt(0).itr(info);
        final long p2 = p1 + e2.size() - 1;
        e = Pos.get(p1, p2, info);
      } else {
        // position() CMP number
        e = Pos.get(op.op, e2, e, info);
      }
      if(e != this) qc.compInfo(OPTWRITE, this);
    } else if(e1.type().eq(SeqType.BLN) && (op == OpG.EQ && e2 == Bln.FALSE ||
        op == OpG.NE && e2 == Bln.TRUE)) {
      // (A = false()) -> not(A)
      e = Function.NOT.get(null, info, e1);
      qc.compInfo(OPTWRITE, this);
    } else {
      // rewrite path CMP number
      e = CmpR.get(this);
      if(e == this) e = CmpSR.get(this);
      if(e != this) qc.compInfo(OPTWRITE, this);
    }
    if(e != this) return e;

    // check if both arguments will always yield one result
    atomic = e1.type().zeroOrOne() && e2.type().zeroOrOne();
    if(atomic) qc.compInfo(OPTATOMIC, this);
    return this;
  }

  @Override
  public Expr compEbv(final QueryContext qc) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpG.EQ && exprs[1] == Bln.TRUE ||
            op == OpG.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].type().eq(SeqType.BLN) ? exprs[0] : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it1 = exprs[0].item(qc, info);
      if(it1 == null) return Bln.FALSE;
      final Item it2 = exprs[1].item(qc, info);
      if(it2 == null) return Bln.FALSE;
      return Bln.get(eval(it1, it2, coll));
    }

    final Iter ir1 = qc.iter(exprs[0]);
    final long is1 = ir1.size();

    // skip empty result
    if(is1 == 0) return Bln.FALSE;
    final boolean s1 = is1 == 1;

    // evaluate single items
    if(s1 && exprs[1].size() == 1)
      return Bln.get(eval(ir1.next(), exprs[1].item(qc, info), coll));

    Iter ir2 = qc.iter(exprs[1]);
    final long is2 = ir2.size();

    // skip empty result
    if(is2 == 0) return Bln.FALSE;
    final boolean s2 = is2 == 1;

    // evaluate single items
    if(s1 && s2) return Bln.get(eval(ir1.next(), ir2.next(), coll));

    // evaluate iterator and single item
    Item it1, it2;
    if(s2) {
      it2 = ir2.next();
      while((it1 = ir1.next()) != null) {
        if(eval(it1, it2, coll)) return Bln.TRUE;
      }
      return Bln.FALSE;
    }

    // evaluate two iterators
    if(!ir2.reset()) {
      // cache items for next comparisons
      final ValueBuilder vb = new ValueBuilder();
      it1 = ir1.next();
      if(it1 == null) return Bln.FALSE;
      while((it2 = ir2.next()) != null) {
        if(eval(it1, it2, coll)) return Bln.TRUE;
        vb.add(it2);
      }
      ir2 = vb;
    }

    while((it1 = ir1.next()) != null) {
      ir2.reset();
      while((it2 = ir2.next()) != null) {
        if(eval(it1, it2, coll)) return Bln.TRUE;
      }
    }
    return Bln.FALSE;
  }

  /**
   * Compares a single item.
   * @param a first item to be compared
   * @param b second item to be compared
   * @param cl collation
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean eval(final Item a, final Item b, final Collation cl) throws QueryException {
    final Type ta = a.type, tb = b.type;
    if(!(a instanceof FItem || b instanceof FItem) &&
        (ta == tb || ta.isUntyped() || tb.isUntyped() ||
        a instanceof ANum && b instanceof ANum ||
        a instanceof AStr && b instanceof AStr)) return op.op.eval(a, b, cl, info);
    throw Err.INVTYPECMP.get(info, ta, tb);
  }

  @Override
  public CmpG invert() {
    return exprs[0].size() != 1 || exprs[1].size() != 1 ? this :
      new CmpG(exprs[0], exprs[1], op.invert(), coll, info);
  }

  /**
   * Creates a union of the existing and the specified expressions.
   * @param g general comparison
   * @param qc query context
   * @param scp variable scope
   * @return true if union was successful
   * @throws QueryException query exception
   */
  boolean union(final CmpG g, final QueryContext qc, final VarScope scp) throws QueryException {
    if(op != g.op || !exprs[0].sameAs(g.exprs[0])) return false;
    exprs[1] = new List(info, exprs[1], g.exprs[1]).compile(qc, scp);
    atomic = atomic && exprs[1].type().zeroOrOne();
    return true;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // only equality expressions on default collation can be rewritten
    if(op != OpG.EQ || coll != null) return false;

    // location path, string
    if(!ii.check(exprs[0], false)) return false;

    // support expressions
    final Data data = ii.ic.data;
    final Expr arg = exprs[1];
    final ParseExpr root;
    if(!arg.isValue()) {
      /* index access is not possible if returned type is not a string or untyped; if
         expression depends on context; or if it is non-deterministic. examples:
         for $x in ('a', 1) return //*[text() = $x]
         //*[text() = .]
         //*[text() = (if(random:double() < .5) then 'X' else 'Y')]
       */
      if(!arg.type().type.isStringOrUntyped() || arg.has(Flag.CTX) || arg.has(Flag.NDT) ||
          arg.has(Flag.UPD)) return false;

      // estimate costs (tend to worst case)
      ii.costs = Math.max(1, data.meta.size / 10);
      root = new ValueAccess(info, arg, ii.text, ii.name, ii.ic);

    } else {
      // loop through all items
      ii.costs = 0;
      final Iter ir = arg.iter(ii.qc);
      final ArrayList<ValueAccess> va = new ArrayList<>();
      final TokenSet strings = new TokenSet();
      for(Item it; (it = ir.next()) != null;) {
        // only strings and untyped items are supported
        if(!it.type.isStringOrUntyped()) return false;
        // do no use index if string is empty
        final byte[] string = it.string(info);
        if(string.length == 0) return false;

        // add only expressions that yield results and have not been requested before
        if(!strings.contains(string)) {
          strings.put(string);
          final int costs = data.costs(new StringToken(ii.text, string));
          if(costs != 0) {
            va.add(new ValueAccess(info, it, ii.text, ii.name, ii.ic));
            ii.costs += costs;
          }
        }
      }
      // more than one string - merge index results
      final int vs = va.size();
      root = vs == 1 ? va.get(0) : new Union(info, va.toArray(new ValueAccess[vs]));
    }

    ii.create(root, info, Util.info(ii.text ? OPTTXTINDEX : OPTATVINDEX, arg), false);
    return true;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr a = exprs[0].copy(qc, scp, vs), b = exprs[1].copy(qc, scp, vs);
    return new CmpG(a, b, op, coll, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), exprs);
  }

  @Override
  public String description() {
    return "'" + op + "' operator";
  }

  @Override
  public String toString() {
    return toString(" " + op + ' ');
  }
}
