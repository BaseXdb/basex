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
import org.basex.query.util.Err;

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
    public Item ev(final Item a, final Item b) throws QueryException {
      final boolean t1 = a.u() || a.n();
      final boolean t2 = b.u() || b.n();
      if(t1 ^ t2) errNum(!t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          final long l1 = a.itr();
          final long l2 = b.itr();
          checkRange(l1 + (double) l2);
          return Itr.get(l1 + l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() + b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() + b.dbl());
        return Dec.get(a.dec().add(b.dec()));
      }

      if(a.type == b.type) {
        if(!a.d()) errNum(!t1 ? a : b);
        if(a.type == Type.YMD) return new YMd((YMd) a, (YMd) b, true);
        if(a.type == Type.DTD) return new DTd((DTd) a, (DTd) b, true);
      }
      if(a.type == Type.DTM) return new Dtm((Date) a, checkDur(b), true);
      if(b.type == Type.DTM) return new Dtm((Date) b, checkDur(a), true);
      if(a.type == Type.DAT) return new Dat((Date) a, checkDur(b), true);
      if(b.type == Type.DAT) return new Dat((Date) b, checkDur(a), true);
      if(a.type == Type.TIM) {
        if(b.type != Type.DTD) errType(Type.DTD, b);
        return new Tim((Tim) a, (DTd) b, true);
      }
      if(b.type == Type.TIM) {
        if(a.type != Type.DTD) errType(Type.DTD, b);
        return new Tim((Tim) b, (DTd) a, true);
      }
      errType(a.type, b);
      return null;
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item ev(final Item a, final Item b) throws QueryException {
      final boolean t1 = a.u() || a.n();
      final boolean t2 = b.u() || b.n();
      if(t1 ^ t2) errNum(!t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          final long l1 = a.itr();
          final long l2 = b.itr();
          checkRange(l1 - (double) l2);
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
        errNum(!t1 ? a : b);
      }
      if(a.type == Type.DTM) return new Dtm((Date) a, checkDur(b), false);
      if(a.type == Type.DAT) return new Dat((Date) a, checkDur(b), false);
      if(a.type == Type.TIM) {
        if(b.type != Type.DTD) errType(Type.DTD, b);
        return new Tim((Tim) a, (DTd) b, false);
      }
      errType(a.type, b);
      return null;
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item ev(final Item a, final Item b) throws QueryException {
      if(a.type == Type.YMD) {
        if(!b.n()) errNum(b);
        return new YMd((Dur) a, b.dbl(), true);
      }
      if(b.type == Type.YMD) {
        if(!a.n()) errNum(a);
        return new YMd((Dur) b, a.dbl(), true);
      }
      if(a.type == Type.DTD) {
        if(!b.n()) errNum(b);
        return new DTd((Dur) a, b.dbl(), true);
      }
      if(b.type == Type.DTD) {
        if(!a.n()) errNum(a);
        return new DTd((Dur) b, a.dbl(), true);
      }

      final boolean t1 = a.u() || a.n();
      final boolean t2 = b.u() || b.n();
      if(t1 ^ t2) errType(a.type, b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          final long l1 = a.itr();
          final long l2 = b.itr();
          checkRange(l1 * (double) l2);
          return Itr.get(l1 * l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() * b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() * b.dbl());
        return Dec.get(a.dec().multiply(b.dec()));
      }
      errNum(!t1 ? a : b);
      return null;
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item ev(final Item a, final Item b) throws QueryException {
      if(a.type == b.type) {
        if(a.type == Type.YMD) {
          final BigDecimal bd = BigDecimal.valueOf(((YMd) b).ymd());
          if(bd.equals(BigDecimal.ZERO)) Err.or(DATEZERO, info());
          return Dec.get(BigDecimal.valueOf(((YMd) a).ymd()).divide(
              bd, 20, BigDecimal.ROUND_HALF_EVEN));
        }
        if(a.type == Type.DTD) {
          final BigDecimal bd = ((DTd) b).dtd();
          if(bd.equals(BigDecimal.ZERO)) Err.or(DATEZERO, info());
          return Dec.get(((DTd) a).dtd().divide(bd, 20,
              BigDecimal.ROUND_HALF_EVEN));
        }
      }
      if(a.type == Type.YMD) {
        if(!b.n()) errNum(b);
        return new YMd((Dur) a, b.dbl(), false);
      }
      if(a.type == Type.DTD) {
        if(!b.n()) errNum(b);
        return new DTd((Dur) a, b.dbl(), false);
      }

      checkNum(a, b);
      final Type t = type(a, b);
      if(t == Type.DBL) return Dbl.get(a.dbl() / b.dbl());
      if(t == Type.FLT) return Flt.get(a.flt() / b.flt());

      final BigDecimal b1 = a.dec();
      final BigDecimal b2 = b.dec();
      if(b2.signum() == 0) Err.or(DIVZERO, a);
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, BigDecimal.ROUND_HALF_EVEN));
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Item ev(final Item a, final Item b) throws QueryException {
      checkNum(a, b);
      final double d1 = a.dbl();
      final double d2 = b.dbl();
      if(d2 == 0) Err.or(DIVZERO, a);
      final double d = d1 / d2;
      if(Double.isNaN(d) || Double.isInfinite(d)) Err.or(DIVFLOW, d1, d2);

      final Type t = type(a, b);
      return Itr.get(t == Type.ITR ? a.itr() / b.itr() : (long) d);
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item ev(final Item a, final Item b) throws QueryException {
      checkNum(a, b);
      final Type t = type(a, b);
      if(t == Type.DBL) return Dbl.get(a.dbl() % b.dbl());
      if(t == Type.FLT) return Flt.get(a.flt() % b.flt());

      if(t == Type.ITR) {
        final long b1 = a.itr();
        final long b2 = b.itr();
        if(b2 == 0) Err.or(DIVZERO, a);
        return Itr.get(b1 % b2);
      }

      final BigDecimal b1 = a.dec();
      final BigDecimal b2 = b.dec();
      if(b2.signum() == 0) Err.or(DIVZERO, a);
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
   * @param a first item
   * @param b second item
   * @return result type
   * @throws QueryException query exception
   */
  public abstract Item ev(final Item a, final Item b) throws QueryException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param a first item
   * @param b second item
   * @return type
   */
  static final Type type(final Item a, final Item b) {
    if(a.type == DBL || b.type == DBL || a.u() || b.u()) return DBL;
    if(a.type == FLT || b.type == FLT) return FLT;
    if(a.type == DEC || b.type == DEC) return DEC;
    return ITR;
  }

  /**
   * Returns a type error.
   * @param t expected type
   * @param it item
   * @throws QueryException query exception
   */
  void errType(final Type t, final Item it)
      throws QueryException {
    Err.type(info(), t, it);
  }

  /**
   * Returns a numeric type error.
   * @param it item
   * @throws QueryException query exception
   */
  void errNum(final Item it) throws QueryException {
    Err.num(info(), it);
  }

  /**
   * Returns a duration type error.
   * @param it item
   * @return duration
   * @throws QueryException query exception
   */
  Dur checkDur(final Item it) throws QueryException {
    if(!it.d()) Err.or(XPDUR, info(), it.type);
    return (Dur) it;
  }

  /**
   * Checks if the specified items are numeric or untyped.
   * @param a first item
   * @param b second item
   * @throws QueryException query exception
   */
  final void checkNum(final Item a, final Item b)
      throws QueryException {
    if(!a.u() && !a.n()) errNum(a);
    if(!b.u() && !b.n()) errNum(b);
  }

  /**
   * Checks if the specified value is outside the integer range.
   * @param d value to be checked
   * @throws QueryException query exception
   */
  void checkRange(final double d) throws QueryException {
    if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) Err.or(RANGE, d);
  }

  /**
   * Returns a string representation of the operator.
   * @return string
   */
  String info() {
    return "'" + name + "' operator";
  }

  @Override
  public String toString() {
    return name;
  }
}
