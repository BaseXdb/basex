package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
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
      return EnumOption.string(name());
    }
  }

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value value = arg(0).value(qc);
    final InspectOptions options = toOptions(arg(1), new InspectOptions(), qc);
    final Mode mode = options.get(InspectOptions.MODE);
    final boolean item = options.get(InspectOptions.ITEM);

    SeqType et = arg(0).seqType(), st = value.seqType();
    switch(mode) {
      case EXPRESSION:
        st = et;
        break;
      case VALUE:
        break;
      default:
        // compare refined with original type, which may be more specific (e.g. for node types)
        value.refineType();
        if(et.instanceOf(st)) st = et;
    }
    return Str.get((item ? st.type : st).toString());
  }
}
