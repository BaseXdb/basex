package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract super class for function types.
 * This class is inherited by {@link MapType}, {@link ArrayType}, and {@link FuncType}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public abstract class FType implements Type {
  /** Any function placeholder string. */
  static final String[] WILDCARD = { "*" };

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  @Override
  public final boolean isNumber() {
    return false;
  }

  @Override
  public final boolean isUntyped() {
    return false;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return false;
  }

  @Override
  public final boolean isStringOrUntyped() {
    return false;
  }

  @Override
  public final boolean isSortable() {
    return false;
  }

  @Override
  public final SeqType seqType(final Occ occ) {
    // cannot be instantiated statically due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public final Item cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    throw FUNCCAST_X_X.get(info, this, value);
  }

  @Override
  public final boolean nsSensitive() {
    return false;
  }
}
