package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract superclass of function types ({@link FuncType}, {@link MapType}, {@link ArrayType}).
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public abstract class FType implements Type {
  /** Wildcard parameter list of {@code function(*)}, {@code map(*)} and {@code array(*)}. */
  static final String[] WILDCARD = { "*" };

  /** Sequence types. */
  private final SeqType[] seqTypes = SeqType.cache(this);

  @Override
  public final SeqType seqType(final Occ occ) {
    return SeqType.get(seqTypes, this, occ);
  }

  @Override
  public final Item cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    throw FUNCCAST_X_X.get(info, this, value);
  }
}
