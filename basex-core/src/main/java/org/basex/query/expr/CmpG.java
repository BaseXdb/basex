package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-15, BSD License
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
    public final OpV op;

    /**
     * Constructor.
     * @param name string representation
     * @param op comparator
     */
    OpG(final String name, final OpV op) {
      this.name = name;
      this.op = op;
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

  /** Static context. */
  final StaticContext sc;
  /** Comparator. */
  OpG op;
  /** Flag for atomic evaluation. */
  private boolean atomic;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation
   * @param sc static context
   * @param info input info
   */
  public CmpG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(info, expr1, expr2, coll);
    this.op = op;
    this.sc = sc;
    seqType = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // swap expressions; add text() to location paths to simplify optimizations
    if(swap()) {
      op = op.swap();
      qc.compInfo(OPTSWAP, this);
    }

    // check if both arguments will always yield one result
    final Expr e1 = exprs[0], e2 = exprs[1];
    final SeqType st1 = e1.seqType();

    // one value is empty (e.g.: () = local:expensive() )
    if(oneIsEmpty()) return optPre(Bln.FALSE, qc);
    // rewrite count() function
    if(e1.isFunction(Function.COUNT)) {
      final Expr e = compCount(op.op);
      if(e != this) {
        qc.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
        return e;
      }
    }
    // position() CMP expr
    if(e1.isFunction(Function.POSITION)) {
      final Expr e = Pos.get(op.op, e2, this, info);
      if(e != this) {
        qc.compInfo(OPTWRITE, this);
        return e;
      }
    }
    // (A = false()) -> not(A)
    if(st1.eq(SeqType.BLN) && (op == OpG.EQ && e2 == Bln.FALSE || op == OpG.NE && e2 == Bln.TRUE)) {
      qc.compInfo(OPTWRITE, this);
      return Function.NOT.get(null, info, e1);
    }

    // rewrite expr CMP (range expression or number)
    ParseExpr e = CmpR.get(this);
    // rewrite expr CMP string)
    if(e == this) e = CmpSR.get(this);
    if(e != this) {
      // pre-evaluate optimized expression
      qc.compInfo(OPTWRITE, this);
      return allAreValues() ? e.preEval(qc) : e;
    }

    final SeqType st2 = e2.seqType();
    if(st1.zeroOrOne() && !st1.mayBeArray() && st2.zeroOrOne() && !st2.mayBeArray()) {
      atomic = true;
      qc.compInfo(OPTATOMIC, this);
    }
    return allAreValues() ? preEval(qc) : this;
  }

  @Override
  public Expr optimizeEbv(final QueryContext qc, final VarScope scp) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpG.EQ && exprs[1] == Bln.TRUE || op == OpG.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN) ? exprs[0] : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it1 = exprs[0].item(qc, info);
      if(it1 == null) return Bln.FALSE;
      final Item it2 = exprs[1].item(qc, info);
      if(it2 == null) return Bln.FALSE;
      return Bln.get(eval(it1, it2));
    }

    // retrieve iterators
    Iter ir1 = exprs[0].atomIter(qc, info);
    final long is1 = ir1.size();
    if(is1 == 0) return Bln.FALSE;
    final boolean s1 = is1 == 1;

    Iter ir2 = exprs[1].atomIter(qc, info);
    final long is2 = ir2.size();
    if(is2 == 0) return Bln.FALSE;

    // evaluate single items
    final boolean s2 = is2 == 1;
    if(s1 && s2) return Bln.get(eval(ir1.next(), ir2.next()));

    if(s1) {
      // first iterator yields single result
      final Item it1 = ir1.next();
      for(Item it2; (it2 = ir2.next()) != null;) if(eval(it1, it2)) return Bln.TRUE;
      return Bln.FALSE;
    }

    if(s2) {
      // second iterator yields single result
      final Item it2 = ir2.next();
      for(Item it1; (it1 = ir1.next()) != null;) if(eval(it1, it2)) return Bln.TRUE;
      return Bln.FALSE;
    }

    // swap iterators if first iterator returns more results than second
    final boolean swap = is1 > is2;
    if(swap) {
      final Iter ir = ir1;
      ir1 = ir2;
      ir2 = ir;
    }

    // loop through all items of first and second iterator
    for(Item it1; (it1 = ir1.next()) != null;) {
      if(ir2 == null) ir2 = exprs[swap ? 0 : 1].atomIter(qc, info);
      for(Item it2; (it2 = ir2.next()) != null;) {
        if(swap ? eval(it2, it1) : eval(it1, it2)) return Bln.TRUE;
      }
      ir2 = null;
    }

    // give up
    return Bln.FALSE;
  }

  /**
   * Compares a single item.
   * @param it1 first item to be compared
   * @param it2 second item to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean eval(final Item it1, final Item it2) throws QueryException {
    final Type t1 = it1.type, t2 = it2.type;
    if(!(it1 instanceof FItem || it2 instanceof FItem) &&
        (t1 == t2 || t1.isUntyped() || t2.isUntyped() ||
        it1 instanceof ANum && it2 instanceof ANum ||
        it1 instanceof AStr && it2 instanceof AStr)) return op.op.eval(it1, it2, coll, sc, info);
    throw diffError(info, it1, it2);
  }

  @Override
  public CmpG invert() {
    final Expr e1 = exprs[0], e2 = exprs[1];
    return e1.size() != 1 || e1.seqType().mayBeArray() || e2.size() != 1 ||
        e2.seqType().mayBeArray() ? this : new CmpG(e1, e2, op.invert(), coll, sc, info);
  }

  /**
   * Creates a union of the existing and the specified expressions.
   * @param g general comparison
   * @param qc query context
   * @param scp variable scope
   * @return resulting expression or {@code null}
   * @throws QueryException query exception
   */
  CmpG union(final CmpG g, final QueryContext qc, final VarScope scp) throws QueryException {
    if(op != g.op || coll != g.coll || !exprs[0].sameAs(g.exprs[0])) return null;

    final Expr list = new List(info, exprs[1], g.exprs[1]).optimize(qc, scp);
    final CmpG cmp = new CmpG(exprs[0], list, op, coll, sc, info);
    final SeqType st = list.seqType();
    cmp.atomic = atomic && st.zeroOrOne() && !st.mayBeArray();
    return cmp;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // only equality expressions on default collation can be rewritten
    if(op != OpG.EQ || coll != null) return false;

    // check if index rewriting is possible
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
      if(!arg.seqType().type.isStringOrUntyped() || arg.has(Flag.CTX) || arg.has(Flag.NDT) ||
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
        // do not use index if string is empty
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
  public CmpG copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpG(exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs), op, coll, sc, info);
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
