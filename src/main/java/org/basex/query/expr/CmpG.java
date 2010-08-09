package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.data.Data.IndexType;
import org.basex.index.ValuesToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.Step;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * General comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CmpG extends Arr {
  /** Comparators. */
  public enum Op {
    /** General comparison: less or equal. */
    LE("<=", CmpV.Op.LE) {
      @Override
      public Op invert() { return Op.GE; }
    },

    /** General comparison: less. */
    LT("<", CmpV.Op.LT) {
      @Override
      public Op invert() { return Op.GT; }
    },

    /** General comparison: greater of equal. */
    GE(">=", CmpV.Op.GE) {
      @Override
      public Op invert() { return Op.LE; }
    },

    /** General comparison: greater. */
    GT(">", CmpV.Op.GT) {
      @Override
      public Op invert() { return Op.LT; }
    },

    /** General comparison: equal. */
    EQ("=", CmpV.Op.EQ) {
      @Override
      public Op invert() { return Op.EQ; }
    },

    /** General comparison: not equal. */
    NE("!=", CmpV.Op.NE) {
      @Override
      public Op invert() { return Op.NE; }
    };

    /** String representation. */
    public final String name;
    /** Comparator. */
    final CmpV.Op cmp;

    /**
     * Constructor.
     * @param n string representation
     * @param c comparator
     */
    private Op(final String n, final CmpV.Op c) {
      name = n;
      cmp = c;
    }

    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract Op invert();

    @Override
    public String toString() { return name; }
  };

  /** Comparator. */
  Op cmp;
  /** Index expression. */
  private IndexAccess[] iacc = {};
  /** Flag for atomic evaluation. */
  private boolean atom;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpG(final InputInfo ii, final Expr e1, final Expr e2, final Op c) {
    super(ii, e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].addText(ctx);

    if(expr[0].value() && !expr[1].value() || expr[0] instanceof AxisPath &&
        ((AxisPath) expr[0]).root != null && expr[1] instanceof AxisPath) {
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
      cmp = cmp.invert();
    }

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    // check if both arguments will always yield one result
    atom = e1.returned(ctx).one() && e2.returned(ctx).one();

    Expr e = this;
    if(e1.value() && e2.value()) {
      e = preEval(ctx);
    } else if(e1 instanceof Fun) {
      final Fun fun = (Fun) e1;
      if(fun.func == FunDef.COUNT) {
        e = CmpV.count(this, cmp.cmp);
        if(e != this) ctx.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
      } else if(fun.func == FunDef.POS) {
        if(e2 instanceof Range) {
          // position() CMP range
          final long[] rng = ((Range) e2).range(ctx);
          e = rng == null ? this : Pos.get(rng[0], rng[1], input);
        } else {
          // position() CMP number
          e = Pos.get(cmp.cmp, e2, this, input);
        }
        if(e != this) ctx.compInfo(OPTWRITE, this);
      }
    } else {
      // rewrite path CMP number
      e = CmpR.get(this);
      if(e != this) ctx.compInfo(OPTWRITE, this);
    }
    return e;
  }
  
  @Override
  public Bln atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atom) return Bln.get(eval(expr[0].atomic(ctx, input),
        expr[1].atomic(ctx, input)));

    final Iter ir1 = ctx.iter(expr[0]);
    final long is1 = ir1.size();

    // skip empty result
    if(is1 == 0) return Bln.FALSE;
    final boolean s1 = is1 == 1;

    // evaluate single items
    if(s1 && expr[1].value() && expr[1].size(ctx) == 1)
      return Bln.get(eval(ir1.next(), expr[1].atomic(ctx, input)));

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
      final ItemIter ir = new ItemIter();
      if((it1 = ir1.next()) != null) {
        while((it2 = ir2.next()) != null) {
          if(eval(it1, it2)) return Bln.TRUE;
          ir.add(it2);
        }
      }
      ir2 = ir;
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
    if(a.type != b.type && !a.unt() && !b.unt() && !(a.str() && b.str()) &&
        !(a.num() && b.num())) Err.or(input, XPTYPECMP, a.type, b.type);
    return cmp.cmp.e(input, a, b);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // accept only location path, string and equality expressions
    final Step s = indexStep(expr[0]);
    if(s == null || cmp != Op.EQ) return false;

    final boolean text = ic.data.meta.txtindex && s.test.type == Type.TXT;
    final boolean attr = !text && ic.data.meta.atvindex &&
      s.simple(Axis.ATTR, true);

    // no text or attribute index applicable
    if(!text && !attr) return false;
    final IndexType type = text ? IndexType.TXT : IndexType.ATV;

    // support expressions
    final Expr arg = expr[1];
    if(!arg.value()) {
      final SeqType ret = arg.returned(ic.ctx);
      // index access not possible if returned type is no string or node,
      // and if expression depends on context
      if(!ret.type.str && !ret.type.node() || arg.uses(Use.CTX, ic.ctx))
        return false;

      ic.is += Math.max(1, ic.data.meta.size / 10);
      iacc = Array.add(iacc, new IndexAccess(input, arg, type, ic));
      return true;
    }

    // loop through all items
    final Iter ir = arg.iter(ic.ctx);
    Item it;
    ic.is = 0;
    while((it = ir.next()) != null) {
      final SeqType ret = it.returned(ic.ctx);
      if(!ret.type.str && !ret.type.node()) return false;

      ic.is += ic.data.nrIDs(new ValuesToken(type, it.atom()));
      iacc = Array.add(iacc, new IndexAccess(input, it, type, ic));
    }
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) {
    // more than one string - merge index results
    final Expr root = iacc.length == 1 ? iacc[0] : new Union(input, iacc);

    final AxisPath orig = (AxisPath) expr[0];
    final AxisPath path = orig.invertPath(root, ic.step);

    if(iacc[0].type == IndexType.TXT) {
      ic.ctx.compInfo(OPTTXTINDEX);
    } else {
      ic.ctx.compInfo(OPTATVINDEX);
      // add attribute step
      final Step step = orig.step[orig.step.length - 1];
      Step[] steps = { Step.get(input, Axis.SELF, step.test) };
      for(final Step s : path.step) steps = Array.add(steps, s);
      path.step = steps;
    }
    return path;
  }

  /**
   * Returns the indexable index step or null.
   * @param expr expression arguments
   * @return result of check
   */
  public static Step indexStep(final Expr expr) {
    // check if index can be applied
    if(!(expr instanceof AxisPath)) return null;

    // accept only single axis steps as first expression
    final AxisPath path = (AxisPath) expr;
    if(path.root != null) return null;

    // step must not contain predicates
    return path.step[path.step.length - 1];
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
  }

  @Override
  public String desc() {
    return "'" + cmp + "' expression";
  }

  @Override
  public String color() {
    return "FF9966";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(cmp.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
