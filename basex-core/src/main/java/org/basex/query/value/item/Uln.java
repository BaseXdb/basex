package org.basex.query.value.item;

import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.fn.FnRound.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Unsigned long ({@code xs:unsignedLong}).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Uln extends ANum {
  /** Maximum unsigned long values. */
  public static final BigDecimal MAXULN = BigDecimal.valueOf(Long.MAX_VALUE).
      multiply(Dec.BD_2).add(BigDecimal.ONE);
  /** Decimal value. */
  private final BigInteger value;

  /**
   * Constructor.
   * @param value decimal value
   */
  public Uln(final BigInteger value) {
    super(AtomType.UNSIGNED_LONG);
    this.value = value;
  }

  @Override
  public byte[] string() {
    return token(value.toString());
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value.signum() != 0;
  }

  @Override
  public long itr() {
    return value.longValue();
  }

  @Override
  public float flt() {
    return value.floatValue();
  }

  @Override
  public double dbl() {
    return value.doubleValue();
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return new BigDecimal(value);
  }

  @Override
  public ANum abs() {
    final long l = itr();
    return l >= 0 ? this : Int.get(-l);
  }

  @Override
  public Uln ceiling() {
    return this;
  }

  @Override
  public Uln floor() {
    return this;
  }

  @Override
  public ANum round(final int prec, final RoundMode mode) {
    if(value.equals(BigInteger.ZERO) || prec >= 0) return this;
    final BigInteger v = Dec.round(new BigDecimal(value), prec, mode).toBigInteger();
    return v.equals(value) ? this : new Uln(v);
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    return item.type == AtomType.UNSIGNED_LONG ? value.equals(((Uln) item).value) :
      item.type == AtomType.DOUBLE || item.type == AtomType.FLOAT ? item.equal(this, coll, ii) :
      value.compareTo(BigInteger.valueOf(item.itr(ii))) == 0;
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item.type == AtomType.UNSIGNED_LONG ? value.compareTo(((Uln) item).value) :
      item.type == AtomType.DOUBLE || item.type == AtomType.FLOAT ?
        -item.compare(this, coll, transitive, ii) :
        value.compareTo(BigInteger.valueOf(item.itr(ii)));
  }

  @Override
  public Object toJava() {
    return new BigInteger(value.toString());
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Uln && value.compareTo(((Uln) obj).value) == 0;
  }
}
