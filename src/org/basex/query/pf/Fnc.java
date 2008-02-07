package org.basex.query.pf;

import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.query.pf.PFT.*;

/**
 * Function implementations.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
enum Fnc {
  /** Addition. */
  PLUS(FPLUS) {
    @Override
    V e(final V v1, final V v2) {
      return v1 instanceof I ? new I(v1.i() + v2.i()) : new D(v1.d() + v2.d());
    }
  },

  /** Subtraction. */
  MINUS(FMINUS) {
    @Override
    V e(final V v1, final V v2) {
      return v1 instanceof I ? new I(v1.i() - v2.i()) : new D(v1.d() - v2.d());
    }
  },

  /** Multiplication. */
  MULT(FMULT) {
    @Override
    V e(final V v1, final V v2) {
      return v1 instanceof I ? new I(v1.i() * v2.i()) : new D(v1.d() * v2.d());
    }
  },

  /** Multiplication. */
  DIV(FDIV) {
    @Override
    V e(final V v1, final V v2) {
      return v1 instanceof I ? new I(v1.i() / v2.i()) : new D(v1.d() / v2.d());
    }
  },

  /** Modulo. */
  MOD(FMOD) {
    @Override
    V e(final V v1, final V v2) {
      return v1 instanceof I ? new I(v1.i() % v2.i()) : new D(v1.d() % v2.d());
    }
  },

  /** Absolute. */
  ABS(FABS) {
    @Override
    V e(final V v1, final V v2) {
      return v1 instanceof I ? new I(Math.abs(v1.i())) :
        new D(Math.abs(v1.d()));
    }
  },

  /** Concat. */
  CONC(FCONC) {
    @Override
    V e(final V v1, final V v2) {
      return new S(Token.concat(v1.s(), v2.s()));
    }
  },

  /** Contains. */
  CONT(FCONT) {
    @Override
    V e(final V v1, final V v2) {
      return B.v(Token.contains(v1.s(), v2.s()));
    }
  },

  /** Ceiling. */
  CEIL(FCEIL) {
    @Override
    V e(final V v1, final V v2) {
      return new D(Math.ceil(v1.d()));
    }
  },

  /** Floor. */
  FLOOR(FFLOOR) {
    @Override
    V e(final V v1, final V v2) {
      return new D(Math.floor(v1.d()));
    }
  },

  /** Round. */
  ROUND(FROUND) {
    @Override
    V e(final V v1, final V v2) {
      // only double values?
      return new D(Math.round(v1.d()));
    }
  },

  /** Number. */
  NUMB(FNUMB) {
    @Override
    V e(final V v1, final V v2) throws QueryException {
      return new D(v1.s());
    }
  };

  /** Function name. */
  private byte[] name;

  /**
   * Executes the function.
   * @param v1 first value
   * @param v2 second value
   * @return result
   * @throws QueryException query exception
   */
  abstract V e(V v1, V v2) throws QueryException;
  
  /**
   * Constructor.
   * @param nam function name
   */
  Fnc(final String nam) { name = Token.token(nam); }

  /**
   * Returns the specified function.
   * @param fun function to be found
   * @return function reference
   * @throws QueryException query exception
   */
  static final Fnc f(final byte[] fun) throws QueryException {
    for(final Fnc c : values()) if(Token.eq(fun, c.name)) return c;
    throw new QueryException(PFFUN, fun);
  }
}
