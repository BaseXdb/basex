package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.json.*;
import org.basex.query.func.java.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * External variables and context value to be bound at compile time.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class QueryBindings {
  /** Query context. */
  private final QueryContext qc;
  /** Bound values, indexed by variable names ({@link QNm#EMPTY} for the context value). */
  private final QNmMap<Value> values = new QNmMap<>();

  /**
   * Constructor.
   * @param qc query context
   */
  QueryBindings(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Binds a value to a global variable or the context value, unless a value has already been bound.
   * @param name name of variable; context value if empty string or {@code null}
   * @param value value to be bound (object or XQuery {@link Value})
   * @param type type (can be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  void bind(final String name, final Object value, final String type, final StaticContext sc)
      throws QueryException {
    final QNm qnm = name == null || name.isEmpty() ? QNm.EMPTY : qname(name, sc);
    if(!values.contains(qnm)) values.put(qnm, cast(value, type));
  }

  /**
   * Returns the bound context value.
   * @return context value (can be {@code null})
   */
  Value context() {
    return values.get(QNm.EMPTY);
  }

  /**
   * Assigns the bound values and the global bindings to the external variables.
   * @throws QueryException query exception
   */
  void assign() throws QueryException {
    if(qc.globalBindings) {
      final Map<String, String> map = qc.options().toMap(MainOptions.BINDINGS);
      for(final Entry<String, String> entry : map.entrySet()) {
        bind(entry.getKey(), Atm.get(entry.getValue()), null, qc.main.sc);
      }
    }
    qc.vars.bindExternal(qc, values, true);
  }

  /**
   * Casts a value to the specified type.
   * @param value value to be cast
   * @param type type (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Object value, final String type) throws QueryException {
    Object object = value;
    if(object instanceof final String string) {
      final StringList strings = new StringList(1);
      // strings containing multiple items (value \1 ...)
      if(string.indexOf('\1') == -1) {
        strings.add(string);
      } else {
        object = strings.add(string.split("\1")).toArray();
      }

      // subtypes overriding the global value (value \2 type)
      if(string.indexOf('\2') != -1) {
        final ValueBuilder vb = new ValueBuilder(qc, strings.size());
        for(final String str : strings) {
          final int i = str.indexOf('\2');
          final String val = i == -1 ? str : str.substring(0, i);
          final String tp = i == -1 ? type : str.substring(i + 1);
          vb.add(cast(val, tp));
        }
        return vb.value();
      }
    }

    // no type specified: return original value or convert as Java object
    if(type == null || type.isEmpty()) {
      return object instanceof final Value val ? val : JavaCall.toValue(object, qc, null);
    }

    // convert JSON input
    if(type.equalsIgnoreCase(MainParser.JSON.name())) {
      try {
        final TextInput ti = new TextInput(new IOContent(object.toString()));
        return JsonConverter.get(W3JsonOptions.w3()).convert(ti, "", null, qc);
      } catch(final IOException ex) {
        throw BASEX_ERROR_X.get(null, ex);
      }
    }

    // parse target type
    final StaticContext sc = qc.main != null ? qc.main.sc : new StaticContext(qc);
    final SeqType st = AQueryParser.parseSeqType(type, qc, sc);
    if(st.eq(Types.EMPTY_SEQUENCE_Z)) return Empty.VALUE;
    final Type tp = st.type;

    // cast XQuery values
    if(object instanceof final Value val) {
      // cast single item
      if(val.size() == 1) return tp.cast((Item) val, qc, null);
      // cast sequence
      final ValueBuilder vb = new ValueBuilder(qc, val.size());
      for(final Item item : val) vb.add(tp.cast(item, qc, null));
      return vb.value(tp.toAttachable());
    }

    // cast sequences
    if(object instanceof final Object[] array) {
      final ValueBuilder vb = new ValueBuilder(qc, array.length);
      for(final Object val : array) vb.add(tp.cast(val, qc, null));
      return vb.value(tp.toAttachable());
    }

    // cast any other object to XDM
    return tp.cast(object, qc, null);
  }

  /**
   * Converts the specified variable name to a QName.
   * @param name name of variable
   * @param sc static context
   * @return QName
   * @throws QueryException query exception
   */
  private static QNm qname(final String name, final StaticContext sc) throws QueryException {
    return QNm.parse(token(Strings.startsWith(name, '$') ? name.substring(1) : name), null, sc);
  }
}
