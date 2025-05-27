package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnElementToMap extends PlanFn {
  /** Options. */
  public static class ElementsOptions extends Options {
    /** Option. */
    public static final StringOption ATTRIBUTE_MARKER =
        new StringOption("attribute-marker", "@");
    /** Option. */
    public static final EnumOption<NameFormat> NAME_FORMAT =
        new EnumOption<>("name-format", NameFormat.DEFAULT);
    /** Option. */
    public static final ValueOption PLAN =
        new ValueOption("plan", SeqType.MAP_ZO);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item element = arg(0).item(qc, info);
    if(element.isEmpty()) return Empty.VALUE;

    final ANode elem = toElem(element, qc);
    final ElementsOptions options = toOptions(arg(1), new ElementsOptions(), qc);

    final Plan plan = new Plan();
    plan.name = options.get(ElementsOptions.NAME_FORMAT);
    plan.marker = options.get(ElementsOptions.ATTRIBUTE_MARKER);

    final Value pln = options.get(ElementsOptions.PLAN);
    if(!pln.isEmpty()) {
      toMap(pln, qc).forEach((key, value) -> {
        final byte[] token = key.string(info);
        final boolean attr = Token.startsWith(token, '@');
        final QNm name;
        if(Token.eq(token, Token.cpToken('*'))) {
          name = QNm.EMPTY;
        } else {
          name = qc.shared.parseQName(attr ? Token.substring(token, 1) : token, true, sc());
        }
        if(name != null) {
          final PlanEntry pe = new PlanEntry();
          pe.attribute = attr;
          final XQMap map = toMap(value, qc);
          final Value layout = map.get(LAYOUT);
          if(!layout.isEmpty()) pe.layout = Enums.get(PlanLayout.class, toString(layout, qc));
          final Value type = map.get(TYPE);
          if(!type.isEmpty()) pe.type = Enums.get(PlanType.class, toString(type, qc));
          final Value child = map.get(CHILD);
          if(!child.isEmpty()) {
            final QNm qnm = qc.shared.parseQName(toToken(child, qc), true, sc());
            if(qnm != null) pe.child = qnm;
          }
          plan.entries.put(name, pe);

          // error handling
          final Function<String, QueryException> where = s ->
            INVALIDOPTION_X.get(info, Util.info("Missing key '%' (node: %).", s, name));
          final BiFunction<String, Object, QueryException> why = (s, t) ->
            INVALIDOPTION_X.get(info, Util.info("Unexpected key '%':'%' (node: %).", s, t, name));
          if(pe.layout == null) {
            if(!pe.attribute) throw where.apply("layout");
          } else {
            if(pe.attribute) throw why.apply("layout", pe.layout);
          }
          if(pe.layout == PlanLayout.LIST || pe.layout == PlanLayout.LIST_PLUS) {
            if(pe.child == null) throw where.apply("child");
          } else {
            if(pe.child != null) throw why.apply("child", pe.child);
          }
          if(pe.layout == PlanLayout.SIMPLE || pe.layout == PlanLayout.SIMPLE_PLUS ||
              pe.attribute) {
            if(!pe.attribute && pe.type == PlanType.SKIP) throw why.apply("type", pe.type);
          } else {
            if(pe.type != null) throw why.apply("type", pe.type);
          }
        }
      });
    }

    // create result
    final Item value = entry(elem, plan).apply(elem, null, plan, qc);
    return value.isEmpty() ? value : XQMap.get(Str.get(nodeName(elem, null, plan, qc)), value);
  }
}
