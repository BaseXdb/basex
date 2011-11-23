package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.index.IndexToken.IndexType;
import org.basex.index.ValuesToken;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.path.AxisStep;
import org.basex.query.util.IndexContext;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CmpG extends Cmp {
  /** Comparators. */
  public enum Op {
    /** General comparison: less or equal. */
    LE("<=", CmpV.Op.LE) {
      @Override
      public Op swap() { return Op.GE; }
      @Override
      public Op invert() { return Op.LT; }
    },

    /** General comparison: less. */
    LT("<", CmpV.Op.LT) {
      @Override
      public Op swap() { return Op.GT; }
      @Override
      public Op invert() { return Op.GE; }
    },

    /** General comparison: greater of equal. */
    GE(">=", CmpV.Op.GE) {
      @Override
      public Op swap() { return Op.LE; }
      @Override
      public Op invert() { return Op.LT; }
    },

    /** General comparison: greater. */
    GT(">", CmpV.Op.GT) {
      @Override
      public Op swap() { return Op.LT; }
      @Override
      public Op invert() { return Op.LE; }
    },

    /** General comparison: equal. */
    EQ("=", CmpV.Op.EQ) {
      @Override
      public Op swap() { return Op.EQ; }
      @Override
      public Op invert() { return Op.NE; }
    },

    /** General comparison: not equal. */
    NE("!=", CmpV.Op.NE) {
      @Override
      public Op swap() { return Op.NE; }
      @Override
      public Op invert() { return Op.EQ; }
    };

    /** String representation. */
    public final String name;
    /** Comparator. */
    final CmpV.Op op;

    /**
     * Constructor.
     * @param n string representation
     * @param c comparator
     */
    private Op(final String n, final CmpV.Op c) {
      name = n;
      op = c;
    }

    /**
     * Swaps the comparator.
     * @return swapped comparator
     */
    public abstract Op swap();

    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract Op invert();

    @Override
    public String toString() { return name; }
  }

  /** Comparator. */
  Op op;
  /** Index expression. */
  private IndexAccess[] iacc = {};
  /** Flag for atomic evaluation. */
  private boolean atomic;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param o operator
   */
  public CmpG(final InputInfo ii, final Expr e1, final Expr e2, final Op o) {
    super(ii, e1, e2);
    op = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

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
      if(e2 instanceof Range && op.op == CmpV.Op.EQ) {
        // position() CMP range
        final long[] rng = ((Range) e2).range(ctx);
        e = rng == null ? this : Pos.get(rng[0], rng[1], input);
      } else {
        // position() CMP number
        e = Pos.get(op.op, e2, e, input);
      }
      if(e != this) ctx.compInfo(OPTWRITE, this);
    } else if(e1.type().eq(SeqType.BLN) && (op == Op.EQ && e2 == Bln.FALSE ||
        op == Op.NE && e2 == Bln.TRUE)) {
      // (A = false()) -> not(A)
      e = Function.NOT.get(input, e1);
    } else {
      // rewrite path CMP number
      e = CmpR.get(this);
      if(e != this) ctx.compInfo(OPTWRITE, this);
    }

    // check if both arguments will always yield one result
    atomic = e1.type().zeroOrOne() && e2.type().zeroOrOne();
    type = SeqType.BLN;
    return e;
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == Op.EQ && expr[1] == Bln.TRUE ||
            op == Op.NE && expr[1] == Bln.FALSE) &&
      expr[0].type().eq(SeqType.BLN) ? expr[0] : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it1 = expr[0].item(ctx, input);
      if(it1 == null) return Bln.FALSE;
      final Item it2 = expr[1].item(ctx, input);
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
      return Bln.get(eval(ir1.next(), expr[1].item(ctx, input)));

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
      final ItemCache ic = new ItemCache();
      if((it1 = ir1.next()) != null) {
        while((it2 = ir2.next()) != null) {
          if(eval(it1, it2)) return Bln.TRUE;
          ic.add(it2);
        }
      }
      ir2 = ic;
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
    final Type ta = a.type;
    final Type tb = b.type;
    if(ta != tb && (!ta.isUntyped() && !tb.isUntyped() && !(ta.isString() &&
        tb.isString()) && !(ta.isNumber() && tb.isNumber()) &&
        !ta.isFunction() && !tb.isFunction() ||
        ta == AtomType.QNM || tb == AtomType.QNM))
      XPTYPECMP.thrw(input, ta, tb);
    return op.op.e(input, a, b);
  }

  @Override
  public CmpG invert() {
    return expr[0].size() != 1 || expr[1].size() != 1 ? this :
      new CmpG(input, expr[0], expr[1], op.invert());
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
    expr[1] = new List(input, expr[1], g.expr[1]).comp(ctx);
    atomic = atomic && expr[1].type().zeroOrOne();
    return true;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // accept only location path, string and equality expressions
    if(op != Op.EQ) return false;
    final AxisStep s = expr[0] instanceof Context ?
        ic.step : indexStep(expr[0]);
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
      /* index access is not possible if returned type is no string or node, if
         expression depends on context, or if it is non-deterministic. examples:
         //*[text() = 1]
         //*[text() = .]
         //*[text() = (if(math:random() < .5) then 'X' else 'Y')]
       */
      if(!t.type.isString() && !t.type.isNode() ||
          arg.uses(Use.CTX) || arg.uses(Use.NDT)) return false;

      ic.addCosts(ic.data.meta.size / 10);
      iacc = Array.add(iacc, new IndexAccess(input, arg, ind, ic));
      return true;
    }

    // loop through all items
    final Iter ir = arg.iter(ic.ctx);
    Item it;
    ic.costs(0);
    while((it = ir.next()) != null) {
      final SeqType t = it.type();
      if(!(t.type.isString() || t.type.isNode())) return false;

      final int is = ic.data.count(new ValuesToken(ind, it.string(input)));
      // add only expressions that yield results
      if(is != 0) {
        iacc = Array.add(iacc, new IndexAccess(input, it, ind, ic));
        ic.addCosts(is);
      }
    }
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) {
    // will only be called for costs != 0
    final boolean text = iacc[0].itype == IndexType.TEXT;
    ic.ctx.compInfo(text ? OPTTXTINDEX : OPTATVINDEX);
    // more than one string - merge index results
    final ParseExpr root = iacc.length == 1 ? iacc[0] : new Union(input, iacc);
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
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, OP, Token.token(op.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return "'" + op + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + op + " ");
  }
}
