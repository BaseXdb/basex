package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Copy expression.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
abstract class Copy extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   * @param exprs expressions
   */
  Copy(final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType, exprs);
  }

  @Override
  public final Expr optimize(final CompileContext cc) {
    // do not assign original sequence type (name of node may change):
    // <a/> update { rename node . as 'x' }  ->  <x/>
    final SeqType st = result().seqType();
    exprType.assign(st.type, st.occ);
    return this;
  }

  @Override
  public void checkUp() throws QueryException {
    final Expr modify = modify();
    modify.checkUp();
    if(!modify.vacuous() && !modify.has(Flag.UPD)) throw UPMODIFY.get(info);
  }

  @Override
  public boolean has(final Flag... flags) {
    return flags.length != 0 && super.has(flags);
  }

  /**
   * Returns the updating expression.
   * @return result expression
   */
  final Expr modify() {
    return exprs[0];
  }

  /**
   * Returns the result expression.
   * @return result expression
   */
  final Expr result() {
    return exprs[1];
  }
}
