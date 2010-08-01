package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.item.Type.*;
import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Dec;
import org.basex.query.item.Dtm;
import org.basex.query.item.Dur;
import org.basex.query.item.Flt;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Tim;
import org.basex.query.item.Type;
import org.basex.query.item.YMd;

/**
 * Calculation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item ev(final ParseExpr e, final Item a, final Item b)
        throws QueryException {

      final boolean t1 = a.unt() || a.num();
      final boolean t2 = b.unt() || b.num();
      if(t1 ^ t2) errNum(e, !t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          final long l1 = a.itr();
          final long l2 = b.itr();
          checkRange(e, l1 + (double) l2);
          return Itr.get(l1 + l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() + b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() + b.dbl());
        return Dec.get(a.dec().add(b.dec()));
      }

      if(a.type == b.type) {
        if(!a.dur()) errNum(e, !t1 ? a : b);
        if(a.type == Type.YMD) return new YMd((YMd) a, (YMd) b, true);
        if(a.type == Type.DTD) return new DTd((DTd) a, (DTd) b, true);
      }
      if(a.type == Type.DTM) return new Dtm((Date) a, checkDur(e, b), true);
      if(b.type == Type.DTM) return new Dtm((Date) b, checkDur(e, a), true);
      if(a.type == Type.DAT) return new Dat((Date) a, checkDur(e, b), true);
      if(b.type == Type.DAT) return new Dat((Date) b, checkDur(e, a), true);
      if(a.type == Type.TIM) {
        if(b.type != Type.DTD) errType(e, Type.DTD, b);
        return new Tim((Tim) a, (DTd) b, true);
      }
      if(b.type == Type.TIM) {
        if(a.type != Type.DTD) errType(e, Type.DTD, b);
        return new Tim((Tim) b, (DTd) a, true);
      }
      errType(e, a.type, b);
      return null;
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item ev(final ParseExpr e, final Item a, final Item b)
        throws QueryException {

      final boolean t1 = a.unt() || a.num();
      final boolean t2 = b.unt() || b.num();
      if(t1 ^ t2) errNum(e, !t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          final long l1 = a.itr();
          final long l2 = b.itr();
          checkRange(e, l1 - (double) l2);
          return Itr.get(l1 - l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() - b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() - b.dbl());
        return Dec.get(a.dec().subtract(b.dec()));
      }

      if(a.type == b.type) {
        if(a.type == Type.DTM || a.type == Type.DAT || a.type == Type.TIM)
          return new DTd((Date) a, (Date) b);
        if(a.type == Type.YMD) return new YMd((YMd) a, (YMd) b, false);
        if(a.type == Type.DTD) return new DTd((DTd) a, (DTd) b, false);
        errNum(e, !t1 ? a : b);
      }
      if(a.type == Type.DTM) return new Dtm((Date) a, checkDur(e, b), false);
      if(a.type == Type.DAT) return new Dat((Date) a, checkDur(e, b), false);
      if(a.type == Type.TIM) {
        if(b.type != Type.DTD) errType(e, Type.DTD, b);
        return new Tim((Tim) a, (DTd) b, false);
      }
      errType(e, a.type, b);
      return null;
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item ev(final ParseExpr e, final Item a, final Item b)
        throws QueryException {

      if(a.type == Type.YMD) {
        if(!b.num()) errNum(e, b);
        return new YMd((Dur) a, b.dbl(), true);
      }
      if(b.type == Type.YMD) {
        if(!a.num()) errNum(e, a);
        return new YMd((Dur) b, a.dbl(), true);
      }
      if(a.type == Type.DTD) {
        if(!b.num()) errNum(e, b);
        return new DTd((Dur) a, b.dbl(), true);
      }
      if(b.type == Type.DTD) {
        if(!a.num()) errNum(e, a);
        return new DTd((Dur) b, a.dbl(), true);
      }

      final boolean t1 = a.unt() || a.num();
      final boolean t2 = b.unt() || b.num();
      if(t1 ^ t2) errType(e, a.type, b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          final long l1 = a.itr();
          final long l2 = b.itr();
          checkRange(e, l1 * (double) l2);
          return Itr.get(l1 * l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() * b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() * b.dbl());
        return Dec.get(a.dec().multiply(b.dec()));
      }
      errNum(e, !t1 ? a : b);
      return null;
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item ev(final ParseExpr e, final Item a, final Item b)
        throws QueryException {

      if(a.type == b.type) {
        if(a.type == Type.YMD) {
          final BigDecimal bd = BigDecimal.valueOf(((YMd) b).ymd());
          if(bd.equals(BigDecimal.ZERO)) e.error(DATEZERO, info());
          return Dec.get(BigDecimal.valueOf(((YMd) a).ymd()).divide(
              bd, 20, BigDecimal.ROUND_HALF_EVEN));
        }
        if(a.type == Type.DTD) {
          final BigDecimal bd = ((DTd) b).dtd();
          if(bd.equals(BigDecimal.ZERO)) e.error(DATEZERO, info());
          return Dec.get(((DTd) a).dtd().divide(bd, 20,
              BigDecimal.ROUND_HALF_EVEN));
        }
      }
      if(a.type == Type.YMD) {
        if(!b.num()) errNum(e, b);
        return new YMd((Dur) a, b.dbl(), false);
      }
      if(a.type == Type.DTD) {
        if(!b.num()) errNum(e, b);
        return new DTd((Dur) a, b.dbl(), false);
      }

      checkNum(e, a, b);
      final Type t = type(a, b);
      if(t == Type.DBL) return Dbl.get(a.dbl() / b.dbl());
      if(t == Type.FLT) return Flt.get(a.flt() / b.flt());

      final BigDecimal b1 = a.dec();
      final BigDecimal b2 = b.dec();
      if(b2.signum() == 0) e.error(DIVZERO, a);
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, BigDecimal.ROUND_HALF_EVEN));
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Item ev(final ParseExpr e, final Item a, final Item b)
        throws QueryException {

      checkNum(e, a, b);
      final double d1 = a.dbl();
      final double d2 = b.dbl();
      if(d2 == 0) e.error(DIVZERO, a);
      final double d = d1 / d2;
      if(Double.isNaN(d) || Double.isInfinite(d)) e.error(DIVFLOW, d1, d2);
      return Itr.get(type(a, b) == Type.ITR ? a.itr() / b.itr() : (long) d);
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item ev(final ParseExpr e, final Item a, final Item b)
        throws QueryException {

      checkNum(e, a, b);
      final Type t = type(a, b);
      if(t == Type.DBL) return Dbl.get(a.dbl() % b.dbl());
      if(t == Type.FLT) return Flt.get(a.flt() % b.flt());

      if(t == Type.ITR) {
        final long b1 = a.itr();
        final long b2 = b.itr();
        if(b2 == 0) e.error(DIVZERO, a);
        return Itr.get(b1 % b2);
      }

      final BigDecimal b1 = a.dec();
      final BigDecimal b2 = b.dec();
      if(b2.signum() == 0) e.error(DIVZERO, a);
      final BigDecimal q = b1.divide(b2, 0, BigDecimal.ROUND_DOWN);
      return Dec.get(b1.subtract(q.multiply(b2)));
    }
  };

  /** Name of operation. */
  final String name;

  /**
   * Constructor.
   * @param n name
   */
  private Calc(final String n) {
    name = n;
  }

  /**
   * Performs the calculation.
   * @param e calling expression
   * @param a first item
   * @param b second item
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item ev(final ParseExpr e, final Item a, final Item b)
    throws QueryException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param a first item
   * @param b second item
   * @return type
   */
  static final Type type(final Item a, final Item b) {
    if(a.type == DBL || b.type == DBL || a.unt() || b.unt()) return DBL;
    if(a.type == FLT || b.type == FLT) return FLT;
    if(a.type == DEC || b.type == DEC) return DEC;
    return ITR;
  }

  /**
   * Returns a type error.
   * @param e calling expression
   * @param t expected type
   * @param it item
   * @throws QueryException query exception
   */
  final void errType(final ParseExpr e, final Type t, final Item it)
      throws QueryException {
    e.typeError(info(), t, it);
  }

  /**
   * Returns a numeric type error.
   * @param e calling expression
   * @param it item
   * @throws QueryException query exception
   */
  final void errNum(final ParseExpr e, final Item it) throws QueryException {
    e.error(XPTYPENUM, info(), it.type);
  }

  /**
   * Returns a duration type error.
   * @param e calling expression
   * @param it item
   * @return duration
   * @throws QueryException query exception
   */
  final Dur checkDur(final ParseExpr e, final Item it) throws QueryException {
    if(!it.dur()) e.error(XPDUR, info(), it.type);
    return (Dur) it;
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param e calling expression
   * @param a first item
   * @param b second item
   * @throws QueryException query exception
   */
  final void checkNum(final ParseExpr e, final Item a,
      final Item b) throws QueryException {
    if(!a.unt() && !a.num()) errNum(e, a);
    if(!b.unt() && !b.num()) errNum(e, b);
  }

  /**
   * Checks if the specified value is outside the integer range.
   * @param e calling expression
   * @param d value to be checked
   * @throws QueryException query exception
   */
  final void checkRange(final ParseExpr e, final double d)
      throws QueryException {
    if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) e.error(RANGE, d);
  }

  /**
   * Returns a string representation of the operator.
   * @return string
   */
  final String info() {
    return "'" + name + "' operator";
  }

  @Override
  public String toString() {
    return name;
  }
}
