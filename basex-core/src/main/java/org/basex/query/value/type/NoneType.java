package org.basex.query.value.type;

import java.io.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery none type (result type of fn:error).
 *
 * @author BaseX Team 2005-23, BSD License
 */
public enum NoneType implements Type {
  /** None type. */
  NONE;

  @Override
  public Value cast(final Item item, final QueryContext qc, final StaticContext sc,
      InputInfo ii) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Value cast(final Object value, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Item read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    throw Util.notExpected();
  }

  @Override
  public SeqType seqType(Occ occ) {
    return new SeqType(this, Occ.ZERO_OR_MORE);
  }

  @Override
  public boolean eq(Type type) {
    return type == this;
  }

  @Override
  public boolean instanceOf(Type type) {
    return eq(type);
  }

  @Override
  public Type union(Type type) {
    return type;
  }

  @Override
  public Type intersect(Type type) {
    return type;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isUntyped() {
    return false;
  }

  @Override
  public boolean isNumberOrUntyped() {
    return false;
  }

  @Override
  public boolean isStringOrUntyped() {
    return false;
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  @Override
  public AtomType atomic() {
    return null;
  }

  @Override
  public ID id() {
    return Type.ID.NON;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public String toString() {
    return "none";
  }
}
