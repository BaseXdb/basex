package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Untyped atomic item ({@code xs:untypedAtomic}).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Atm extends Item {
  /** Zero-length atomic item. */
  public static final Atm EMPTY = new Atm(Token.EMPTY);

  /** String data. */
  private final byte[] value;

  /**
   * Constructor.
   * @param value value
   */
  private Atm(final byte[] value) {
    super(AtomType.UNTYPED_ATOMIC);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Atm get(final String value) {
    return get(Token.token(value));
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Atm get(final byte[] value) {
    return value.length == 0 ? EMPTY : new Atm(value);
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return value;
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value.length != 0;
  }

  @Override
  public boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return comparable(item) ? Token.eq(value, item.string(ii), coll) :
      item.equal(this, coll, sc, ii);
  }

  @Override
  public boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    return comparable(item) && Token.eq(string(deep.info), item.string(deep.info), deep);
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    return item.type.isUntyped() ? Token.diff(value, item.string(ii), coll) :
      -item.diff(this, coll, ii);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // E[xs:untypedAtomic('x')]  ->  E[true()]
    return cc.simplify(this, mode.oneOf(Simplify.EBV, Simplify.PREDICATE) ?
      Bln.get(this != EMPTY) : this, mode);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Atm && Token.eq(value, ((Atm) obj).value);
  }

  @Override
  public String toJava() {
    return Token.string(value);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.quoted(value);
  }
}
