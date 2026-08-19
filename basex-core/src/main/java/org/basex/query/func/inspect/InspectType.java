package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectType extends StandardFunc {
  /** Inspection options. */
  public static class InspectOptions extends Options {
    /** Mode. */
    public static final EnumOption<Mode> MODE = new EnumOption<>("mode", Mode.COMPUTED);
    /** Item. */
    public static final BooleanOption ITEM = new BooleanOption("item", false);
  }

  /** Inspection mode. */
  public enum Mode {
    /** Combined.   */ COMPUTED,
    /** Value.      */ VALUE,
    /** Expression. */ EXPRESSION;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final InspectOptions options = options(1, InspectOptions::new, qc);
    final Mode mode = options.get(InspectOptions.MODE);
    final boolean item = options.get(InspectOptions.ITEM);

    final SeqType et = arg(0).seqType();
    SeqType st = input.seqType();
    switch(mode) {
      case EXPRESSION -> st = et;
      case VALUE -> { }
      default -> {
        // compare refined with original type, which may be more specific (e.g. for node types)
        input.refineType();
        if(et.instanceOf(st)) st = et;
      }
    }
    return Str.get((item ? st.with(Occ.EXACTLY_ONE) : st).toString());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, InspectOptions::new, cc);
    return this;
  }
}
