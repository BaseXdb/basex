package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Body of a function item that is implemented in Java.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class FuncItemBody extends Arr {
  /** Function that creates the function item. */
  private final Function origin;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param origin function that creates the function item
   * @param args function arguments
   */
  protected FuncItemBody(final InputInfo info, final SeqType seqType, final Function origin,
      final Expr... args) {
    super(info, seqType, args);
    this.origin = origin;
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.concat("(: ", origin.definition().name.prefixId(), " :)").params(exprs);
  }
}
