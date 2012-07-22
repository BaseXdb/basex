package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.item.ANum;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-12, BSD License
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
      public OpG swap() { return OpG.LE; }
      @Override
      public OpG invert() { return OpG.LT; }
    },

    /** General comparison: greater. */
    GT(">", OpV.GT) {
      @Override
      public OpG swap() { return OpG.LT; }
      @Override
      public OpG invert() { return OpG.LE; }
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
      public OpG invert() { return OpG.EQ; }
    };

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
  OpG op;
  /** Index expression. */
  private ValueAccess[] va = {};
  /** Flag for atomic evaluation. */
  private boolean atomic;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param o operator
   * @param ii input info
   */
  public CmpG(final Expr e1, final Expr e2, final OpG o, final InputInfo ii) {
    super(ii, e1, e2);
    op = o;
    type = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);

    // swap expressions; add text() to location paths to simplify optimizations
    if(swap()) {
      op = op.swap();
      ctx.compInfo(OPTSWAP, this);
    }
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].addText(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    Expr e = this;
    if(oneIsEmpty()) {
      e = optPre(Bln.FALSE, ctx);
    } else if(allAreValues()) {
      e = preEval(ctx);
    } else if(e1.isFunction(Function.COUNT)) {
      e = compCount(op.op);
      if(e != this) ctx.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
    } else if(e1.isFunction(Function.POSITION)) {
      if(e2 instanceof RangeSeq && op.op == OpV.EQ) {
        // position() CMP range
        final long p1 = ((RangeSeq) e2).itemAt(0).itr(info);
        final long p2 = p1 + e2.size() - 1;
        e = Pos.get(p1, p2, info);
      } else {
        // position() CMP number
        e = Pos.get(op.op, e2, e, info);
      }
      if(e != this) ctx.compInfo(OPTWRITE, this);
    } else if(e1.type().eq(SeqType.BLN) && (op == OpG.EQ && e2 == Bln.FALSE ||
        op == OpG.NE && e2 == Bln.TRUE)) {
      // (A = false()) -> not(A)
      e = Function.NOT.get(info, e1);
      ctx.compInfo(OPTWRITE, this);
    } else {
      // rewrite path CMP number
      e = CmpR.get(this);
      if(e == this) e = CmpSR.get(this);
      if(e != this) ctx.compInfo(OPTWRITE, this);
    }
    if(e != this) return e;

    // check if both arguments will always yield one result
    atomic = e1.type().zeroOrOne() && e2.type().zeroOrOne();
    if(atomic) ctx.compInfo(OPTATOMIC, this);
    return this;
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpG.EQ && expr[1] == Bln.TRUE ||
            op == OpG.NE && expr[1] == Bln.FALSE) &&
      expr[0].type().eq(SeqType.BLN) ? expr[0] : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it1 = expr[0].item(ctx, info);
      if(it1 == null) return Bln.FALSE;
      final Item it2 = expr[1].item(ctx, info);
      if(it2 == null) return Bln.FALSE;
      return Bln.get(eval(it1, it2));
    }

    final Iter ir1 = ctx.iter(expr[0]);
    final long is1 = ir1.size();

    // skip empty result
    if(is1 == 0) return Bln.FALSE;
    final boolean s1 = is1 == 1;

    // evaluate single items
    if(s1 && expr[1].size() == 1)
      return Bln.get(eval(ir1.next(), expr[1].item(ctx, info)));

    Iter ir2 = ctx.iter(expr[1]);
    final long is2 = ir2.size();

    // skip empty result
    if(is2 == 0) return Bln.FALSE;
    final boolean s2 = is2 == 1;

    // evaluate single items
    if(s1 && s2) return Bln.get(eval(ir1.next(), ir2.next()));

    // evaluate iterator and single item
    Item it1, it2;
    if(s2) {
      it2 = ir2.next();
      while((it1 = ir1.next()) != null) if(eval(it1, it2)) return Bln.TRUE;
      return Bln.FALSE;
    }

    // evaluate two iterators
    if(!ir2.reset()) {
      // cache items for next comparisons
      final ValueBuilder vb = new ValueBuilder();
      if((it1 = ir1.next()) != null) {
        while((it2 = ir2.next()) != null) {
          if(eval(it1, it2)) return Bln.TRUE;
          vb.add(it2);
        }
      }
      ir2 = vb;
    }

    while((it1 = ir1.next()) != null) {
      ir2.reset();
      while((it2 = ir2.next()) != null) if(eval(it1, it2)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Compares a single item.
   * @param a first item to be compared
   * @param b second item to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean eval(final Item a, final Item b) throws QueryException {
    final Type ta = a.type, tb = b.type;
    if(!(a instanceof FItem || b instanceof FItem) &&
        (ta == tb || ta.isUntyped() || tb.isUntyped() ||
        a instanceof ANum && b instanceof ANum ||
        a instanceof AStr && b instanceof AStr)) return op.op.eval(info, a, b);
    throw XPTYPECMP.thrw(info, ta, tb);
  }

  @Override
  public CmpG invert() {
    return expr[0].size() != 1 || expr[1].size() != 1 ? this :
      new CmpG(expr[0], expr[1], op.invert(), info);
  }

  /**
   * Creates a union of the existing and the specified expressions.
   * @param g general comparison
   * @param ctx query context
   * @return true if union was successful
   * @throws QueryException query exception
   */
  boolean union(final CmpG g, final QueryContext ctx) throws QueryException {
    if(op != g.op || !expr[0].sameAs(g.expr[0])) return false;
    expr[1] = new List(info, expr[1], g.expr[1]).compile(ctx);
    atomic = atomic && expr[1].type().zeroOrOne();
    return true;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // accept only location path, string and equality expressions
    if(op != OpG.EQ) return false;
    final AxisStep s = expr[0] instanceof Context ? ic.step : indexStep(expr[0]);
    if(s == null) return false;

    // check which index applies
    final boolean text = s.test.type == NodeType.TXT && ic.data.meta.textindex;
    final boolean attr = s.test.type == NodeType.ATT && ic.data.meta.attrindex;
    if(!text && !attr) return false;

    // support expressions
    final IndexType ind = text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    final Expr arg = expr[1];
    if(!arg.isValue()) {
      final SeqType t = arg.type();
      /* index access is not possible if returned type is no string or not untyped, if
         expression depends on context, or if it is non-deterministic. examples:
         //*[text() = 1]
         //*[text() = .]
         //*[text() = (if(random:double() < .5) then 'X' else 'Y')]
       */
      if(!t.type.isStringOrUntyped() || arg.uses(Use.CTX) || arg.uses(Use.NDT))
        return false;

      ic.addCosts(ic.data.meta.size / 10);
      va = Array.add(va, new ValueAccess(info, arg, ind, ic));
      return true;
    }

    // loop through all items
    final Iter ir = arg.iter(ic.ctx);
    Item it;
    ic.costs(0);
    while((it = ir.next()) != null) {
      if(!it.type.isStringOrUntyped()) return false;

      final int is = ic.data.count(new StringToken(ind, it.string(info)));
      // add only expressions that yield results
      if(is != 0) {
        va = Array.add(va, new ValueAccess(info, it, ind, ic));
        ic.addCosts(is);
      }
    }
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) {
    // will only be called for costs != 0
    final boolean text = va[0].itype == IndexType.TEXT;
    ic.ctx.compInfo(text ? OPTTXTINDEX : OPTATVINDEX);
    // more than one string - merge index results
    final ParseExpr root = va.length == 1 ? va[0] : new Union(info, va);
    return ic.invert(expr[0], root, text);
  }

  /**
   * If possible, returns the last location step of the specified expression.
   * @param expr expression
   * @return location step
   */
  public static AxisStep indexStep(final Expr expr) {
    // check if index can be applied
    if(!(expr instanceof AxisPath)) return null;
    // accept only single axis steps as first expression
    final AxisPath path = (AxisPath) expr;
    // path must contain no root node
    return path.root != null ? null : path.step(path.steps.length - 1);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), expr);
  }

  @Override
  public String description() {
    return "'" + op + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + op + ' ');
  }
}
