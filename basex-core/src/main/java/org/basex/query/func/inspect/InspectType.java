package org.basex.query.func.inspect;

import java.util.*;

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
 * @author BaseX Team 2005-23, BSD License
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
      return name().toLowerCase(Locale.ENGLISH);
    }
  }

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value value = arg(0).value(qc);
    final InspectOptions options = toOptions(arg(1), new InspectOptions(), true, qc);
    final Mode mode = options.get(InspectOptions.MODE);
    final boolean item = options.get(InspectOptions.ITEM);

    SeqType st = null;
    switch(mode) {
      case EXPRESSION:
        st = arg(0).seqType();
        break;
      case VALUE:
        st = value.seqType();
        break;
      default:
        // combine types of all items to get more specific type
        for(final Item it : value) {
          final SeqType st2 = it.seqType();
          st = st == null ? st2 : st.union(st2);
        }
        if(st == null) st = SeqType.EMPTY_SEQUENCE_Z;
        st = st.with(value.seqType().occ);

        // compare with original type, which may be more specific (in particular for node types)
        final SeqType et = arg(0).seqType();
        if(et.instanceOf(st)) st = et;
    }
    return Str.get((item ? st.type : st).toString());
  }
}
