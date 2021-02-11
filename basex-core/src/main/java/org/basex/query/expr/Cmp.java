package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract comparison.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /** Collation (can be {@code null}). */
  final Collation coll;
  /** Static context. */
  final StaticContext sc;

  /** Check: true. */
  private static final long[] COUNT_TRUE = { };
  /** Check: false. */
  private static final long[] COUNT_FALSE = { };
  /** Check: empty. */
  private static final long[] COUNT_EMPTY = { };
  /** Check: exists. */
  private static final long[] COUNT_EXISTS = { };

  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   * @param coll collation (can be {@code null})
   * @param seqType sequence type
   * @param sc static context
   */
  Cmp(final InputInfo info, final Expr expr1, final Expr expr2, final Collation coll,
      final SeqType seqType, final StaticContext sc) {
    super(info, seqType, expr1, expr2);
    this.coll = coll;
    this.sc = sc;
  }

  /**
   * Swaps the operands of the expression if this might improve performance.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  final boolean swap() {
    // move value, or path without root, to second position
    final Expr expr1 = exprs[0], expr2 = exprs[1];

    final boolean swap = POSITION.is(expr2) || !(expr2 instanceof Value) && (
      // move static value to the right: $words = 'words'
      expr1 instanceof Value ||
      // hashed comparisons: move larger sequences to the right: $small = $large
      expr1.size() > 1 && expr1.size() > expr2.size() &&
      expr1.seqType().type.instanceOf(AtomType.ANY_ATOMIC_TYPE) ||
      // hashed comparisons: . = $words
      expr1 instanceof VarRef && expr1.seqType().occ.max > 1 &&
        !(expr2 instanceof VarRef && expr2.seqType().occ.max > 1) ||
      // index rewritings: move path to the left: word/text() = $word
      !(expr1 instanceof Path && ((Path) expr1).root == null) &&
        expr2 instanceof Path && ((Path) expr2).root == null
    );

    if(swap) {
      exprs[0] = expr2;
      exprs[1] = expr1;
    }
    return swap;
  }

  /**
   * If possible, returns an optimized expression with inverted operands.
   * @param cc compilation context
   * @return original or modified expression
   * @throws QueryException query exception
   */
  public final Expr invert(final CompileContext cc) throws QueryException {
    final Expr expr = invert();
    return expr != null ? expr.optimize(cc) : this;
  }

  /**
   * If possible, returns an expression with inverted operands.
   * @return original or {@code null}
   */
  public abstract Expr invert();

  /**
   * Returns the value operator of the expression.
   * @return operator, or {@code null} for node comparisons
   */
  public abstract OpV opV();

  /**
   * Performs various optimizations.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr opt(final CompileContext cc) throws QueryException {
    final OpV op = opV();
    Expr expr = optPos(op, cc);
    if(expr == this) expr = optEqual(op, cc);
    if(expr == this) expr = optCount(op, cc);
    if(expr == this) expr = optBoolean(op, cc);
    if(expr == this) expr = optEmptyString(op, cc);
    if(expr == this) expr = optStringLength(op, cc);
    return expr;
  }

  /**
   * Optimizes this expression as predicate.
   * @param root root expression
   * @param cc compilation context
   * @return resulting expression
   * @throws QueryException query exception
   */
  public final Expr optPred(final Expr root, final CompileContext cc) throws QueryException {
    final Type type = root.seqType().type;
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final OpV opV = opV();
    if(positional()) {
      if(Preds.numeric(expr2) && opV == OpV.EQ) {
        // position() = NUMBER  ->  NUMBER
        return expr2;
      } else if(LAST.is(expr2)) {
        switch(opV) {
          // position() =/>= last()  ->  last()
          case EQ: case GE: return expr2;
          // position() <= last()  ->  true()
          case LE: return Bln.TRUE;
          // position() > last()  ->  false()
          case GT: return Bln.FALSE;
          // position() </!= last()  ->  handled in {@link Filter} expression
          default:
        }
      }
    } else if(type instanceof NodeType && type != NodeType.NODE && expr1 instanceof ContextFn &&
        (this instanceof CmpG ? expr2 instanceof Value : expr2 instanceof Item) && opV == OpV.EQ) {

      // skip functions that do not refer to the current context item
      final ContextFn func = (ContextFn) expr1;
      final Value value = (Value) expr2;
      if(func.exprs.length > 0 && !(func.exprs[0] instanceof ContextValue)) return this;

      final ArrayList<QNm> qnames = new ArrayList<>();
      NamePart part = null;
      if(expr2.seqType().type.isStringOrUntyped()) {
        // local-name() eq 'a'  ->  self::*:a
        if(LOCAL_NAME.is(func)) {
          part = NamePart.LOCAL;
          for(final Item item : value) {
            final byte[] name = item.string(info);
            if(XMLToken.isNCName(name)) qnames.add(new QNm(name));
          }
        } else if(NAMESPACE_URI.is(func)) {
          // namespace-uri() = ('URI1', 'URI2')  ->  self::Q{URI1}* | self::Q{URI2}*
          for(final Item item : value) {
            final byte[] uri = item.string(info);
            if(Token.eq(Token.normalize(uri), uri)) qnames.add(new QNm(Token.COLON, uri));
          }
          if(qnames.size() == value.size()) part = NamePart.URI;
        } else if(NAME.is(func)) {
          // (db-without-ns)[name() = 'city']  ->  (db-without-ns)[self::city]
          final Data data = cc.qc.focus.value.data();
          final byte[] dataNs = data != null ? data.defaultNs() : null;
          if(dataNs != null && dataNs.length == 0) {
            part = NamePart.LOCAL;
            for(final Item item : value) {
              final byte[] name = item.string(info);
              if(XMLToken.isNCName(name)) qnames.add(new QNm(name));
            }
          }
        }
      } else if(NODE_NAME.is(func) && expr2.seqType().type == AtomType.QNAME) {
        // node-name() = xs:QName('pref:local')  ->  self::pref:local
        part = NamePart.FULL;
        for(final Item item : value) {
          qnames.add((QNm) item);
        }
      }

      if(part != null) {
        final ExprList paths = new ExprList(2);
        for(final QNm qname : qnames) {
          final Test test = new NameTest(qname, part, (NodeType) type, cc.sc().elemNS);
          final Expr step = Step.get(cc, null, info, test);
          if(step != Empty.VALUE) paths.add(Path.get(cc, info, null, step));
        }
        return paths.isEmpty() ? Bln.FALSE : paths.size() == 1 ? paths.get(0) :
          new Union(info, paths.finish()).optimize(cc);
      }
    }
    return this;
  }

  /**
   * Tries to simplify an expression with equal operands.
   * @param op operator
   * @param cc compilation context
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr optEqual(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    Expr expr = optEqual(expr1, expr2, op, cc);
    if(expr != this) return expr;

    // (if(A) then 'B' else 'C') = C  ->  boolean(A)
    if(expr1 instanceof If && !expr1.has(Flag.NDT)) {
      final If iff = (If) expr1;
      boolean invert = false;
      for(final Expr ex : iff.exprs) {
        expr = optEqual(ex, expr2, op, cc);
        if(expr != this) {
          invert ^= expr == Bln.FALSE;
          return cc.function(invert ? NOT : BOOLEAN, info, iff.cond);
        }
        invert = true;
      }
    }
    return this;
  }

  /**
   * Tries to simplify an expression with equal operands.
   * @param expr1 first operand
   * @param expr2 second operand
   * @param op operator
   * @param cc compilation context
   * @return resulting expression
   */
  private Expr optEqual(final Expr expr1, final Expr expr2, final OpV op, final CompileContext cc) {
    final SeqType st1 = expr1.seqType();
    final Type type1 = st1.type;
    if(expr1.equals(expr2) &&
      // keep: () = (), (1,2) != (1,2), (1,2) eq (1,2)
      (op != OpV.EQ || this instanceof CmpV ? st1.one() : st1.oneOrMore()) &&
      // keep: xs:double('NaN') = xs:double('NaN')
      (type1.isStringOrUntyped() || type1.instanceOf(AtomType.DECIMAL) ||
          type1 == AtomType.BOOLEAN) &&
      // keep: random:integer() = random:integer()
      // keep if no context is available: last() = last()
      !expr1.has(Flag.NDT) && (!expr1.has(Flag.CTX) || cc.qc.focus.value != null)
    ) {
      // 1 = 1  ->  true()
      // <x/> ne <x/>  ->  false()
      // (1, 2) >= (1, 2)  ->  true()
      // (1, 2, 3)[last() = last()]  ->  (1, 2, 3)
      return Bln.get(op == OpV.EQ || op == OpV.GE || op == OpV.LE);
    }
    return this;
  }

  /**
   * Tries to rewrite boolean comparisons.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optBoolean(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1.seqType().eq(SeqType.BOOLEAN_O)) {
      // boolean(A) = true()   ->  boolean(A)
      if(op == OpV.EQ && expr2 == Bln.TRUE || op == OpV.NE && expr2 == Bln.FALSE) return expr1;
      // boolean(A) = false()  ->  not(boolean(A))
      if(op == OpV.EQ && expr2 == Bln.FALSE || op == OpV.NE && expr2 == Bln.TRUE)
        return cc.function(NOT, info, expr1);
    }
    return this;
  }

  /**
   * Tries to rewrite {@code fn:count}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optCount(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(!(COUNT.is(expr1))) return this;

    // distinct values checks
    final Expr arg = expr1.arg(0), count = exprs[1];
    if(COUNT.is(count)) {
      final Expr carg = count.arg(0);
      // count(E) = count(distinct-values(E))
      if(DISTINCT_VALUES.is(carg) && arg.equals(carg.arg(0)))
        return ((FnDistinctValues) carg).duplicates(op, cc);
      // count(distinct-values(E)) = count(E)
      if(DISTINCT_VALUES.is(arg) && arg.arg(0).equals(carg))
        return ((FnDistinctValues) arg).duplicates(op.swap(), cc);
    }
    // count(distinct-values(E)) = int
    if(DISTINCT_VALUES.is(arg) && count instanceof Int) {
      final long size1 = arg.arg(0).size(), size2 = ((Int) count).itr();
      if(size1 != -1 && size1 == size2) return ((FnDistinctValues) arg).duplicates(op.swap(), cc);
    }

    final ExprList args = new ExprList(3);
    if(count instanceof ANum) {
      final double cnt = ((ANum) count).dbl();
      if(arg.seqType().zeroOrOne()) {
        // count(ZeroOrOne)
        if(cnt > 1) {
          return Bln.get(op == OpV.LT || op == OpV.LE || op == OpV.NE);
        }
        if(cnt == 1) {
          return op == OpV.NE || op == OpV.LT ? cc.function(EMPTY, info, arg) :
                 op == OpV.EQ || op == OpV.GE ? cc.function(EXISTS, info, arg) :
                 Bln.get(op == OpV.LE);
        }
      }
      final long[] counts = countRange(op, cnt);
      // count(A) >= 0  ->  true()
      if(counts == COUNT_TRUE || counts == COUNT_FALSE) {
        return Bln.get(counts == COUNT_TRUE);
      }
      // count(A) > 0  ->  exists(A)
      if(counts == COUNT_EMPTY || counts == COUNT_EXISTS) {
        return cc.function(counts == COUNT_EMPTY ? EMPTY : EXISTS, info, arg);
      }
      // count(A) > 1  ->  util:within(A, 2)
      if(counts != null) {
        for(final long c : counts) args.add(Int.get(c));
      }
    } else if(op == OpV.EQ || op == OpV.GE || op == OpV.LE) {
      final SeqType st2 = count.seqType();
      if(st2.type.instanceOf(AtomType.INTEGER)) {
        if(count instanceof RangeSeq) {
          final long[] range = ((RangeSeq) count).range(false);
          args.add(Int.get(range[0])).add(Int.get(range[1]));
        } else if(st2.one() && (count instanceof VarRef || count instanceof ContextValue)) {
          args.add(count).add(count);
        }
        if(!args.isEmpty()) {
          if(op == OpV.GE) args.remove(args.size() - 1);
          else if(op == OpV.LE) args.set(0, Int.ONE);
        }
      }
    }
    if(args.isEmpty()) return this;

    // count(A) = 1  ->  util:within(A, 1, 1)
    args.insert(0, arg);
    return cc.function(_UTIL_WITHIN, info, args.finish());
  }

  /**
   * Tries to rewrite {@code fn:string-length}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optStringLength(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(!(STRING_LENGTH.is(expr1) && expr2 instanceof ANum)) return this;

    final Expr[] args = expr1.args();
    final long[] counts = countRange(op, ((ANum) expr2).dbl());
    if(counts == COUNT_TRUE || counts == COUNT_FALSE) {
      // string-length(A) >= 0  ->  true()
      final Expr arg1 = args.length > 0 ? args[0] : cc.qc.focus.value;
      if(arg1 != null) {
        final SeqType st1 = arg1.seqType();
        if(st1.zero() || st1.one() && st1.type.isStringOrUntyped())
          return Bln.get(counts == COUNT_TRUE);
      }
    }
    if(counts == COUNT_EMPTY || counts == COUNT_EXISTS) {
      // string-length(A) > 0  ->  boolean(string(A))
      final Function func = counts == COUNT_EMPTY ? NOT : BOOLEAN;
      return cc.function(func, info, cc.function(STRING, info, args));
    }
    return this;
  }

  /**
   * Tries to rewrite comparisons with empty strings.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optEmptyString(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType();
    if(st1.one() && st1.type.isStringOrUntyped() && expr2 == Str.EMPTY) {
      if(op == OpV.LT) return Bln.FALSE;
      if(op == OpV.GE) return Bln.TRUE;
      // do not rewrite GT, as it may be rewritten to a range expression later on
      if(op != OpV.GT) {
        // EQ and LE can be treated identically
        final Function func = op == OpV.NE ? BOOLEAN : NOT;
        return cc.function(func, info, cc.function(DATA, info, exprs[0]));
      }
    }
    return this;
  }

  /**
   * Positional optimizations.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optPos(final OpV op, final CompileContext cc) throws QueryException {
    if(!positional()) return this;

    Expr expr = ItrPos.get(exprs[1], op, info);
    if(expr == null) expr = Pos.get(exprs[1], op, info, cc);
    return expr != null ? expr : this;
  }

  /**
   * Indicates if this is a positional comparison.
   * @return result of check
   */
  boolean positional() {
    return POSITION.is(exprs[0]);
  }

  /**
   * Analyzes the comparison and returns its optimization type.
   * @param op operator
   * @param count count to compare against
   * @return comparison type, min/max range or {@code null}
   */
  private static long[] countRange(final OpV op, final double count) {
    // skip special cases
    if(!Double.isFinite(count)) return null;

    // > (v<0), != (v<0), >= (v<=0), != integer(v)
    final long cnt = (long) count;
    if((op == OpV.GT || op == OpV.NE) && count < 0 ||
        op == OpV.GE && count <= 0 ||
        op == OpV.NE && count != cnt) return COUNT_TRUE;
    // < (v<=0), <= (v<0), = (v<0), != integer(v)
    if(op == OpV.LT && count <= 0 ||
      (op == OpV.LE || op == OpV.EQ) && count < 0 ||
       op == OpV.EQ && count != cnt) return COUNT_FALSE;
    // < (v<=1), <= (v<1), = (v=0)
    if(op == OpV.LT && count <= 1 ||
       op == OpV.LE && count < 1 ||
       op == OpV.EQ && count == 0) return COUNT_EMPTY;
    // > (v<1), >= (v<=1), != (v=0)
    if(op == OpV.GT && count < 1 ||
       op == OpV.GE && count <= 1 ||
       op == OpV.NE && count == 0) return COUNT_EXISTS;
    // range queries
    if(op == OpV.GT) return new long[] { (long) Math.floor(count) + 1 };
    if(op == OpV.GE) return new long[] { (long) Math.ceil(count) };
    if(op == OpV.LT) return new long[] { 0, (long) Math.ceil(count) - 1 };
    if(op == OpV.LE) return new long[] { 0, (long) Math.floor(count) };
    if(op == OpV.EQ) return new long[] { cnt, cnt };
    return null;
  }
}
