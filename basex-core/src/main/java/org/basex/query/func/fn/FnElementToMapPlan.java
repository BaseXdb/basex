package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnElementToMapPlan extends PlanFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = arg(0).unwrappedIter(qc);

    // collect element and attribute nodes
    final QNmMap<GNodeList> elemNames = new QNmMap<>();
    final QNmMap<GNodeList> attrNames = new QNmMap<>();
    for(Item item; (item = iter.next()) != null;) {
      final XNode node = (XNode) (item.type.instanceOf(NodeType.DOCUMENT) ? item :
        toElem(item, qc));
      for(final GNode desc : node.descendantIter(true)) {
        qc.checkStop();
        if(desc.kind() == Kind.ELEMENT) {
          elemNames.computeIfAbsent(desc.qname(), GNodeList::new).add(desc);
          for(final GNode attr : children(Kind.ATTRIBUTE, desc)) {
            attrNames.computeIfAbsent(attr.qname(), GNodeList::new).add(attr);
          }
        }
      }
    }

    // check structure and types (the result is a map keyed by element/attribute names)
    final MapBuilder ecm = new MapBuilder();
    for(final QNm name : elemNames) {
      final PlanEntry pe = entry(elemNames.get(name).finish());
      final XQMap plan = new XQRecordMap(Records.ELEMENT_CONVERSION_PLAN.get(),
        Str.get(pe.layout.toString()),
        pe.child != null ? Str.get(pe.child.uri().length != 0 ? pe.child.eqName() :
          pe.child.local()) : Empty.VALUE,
        pe.type != null && pe.type != PlanType.STRING ? Str.get(pe.type.toString()) :
          Empty.VALUE);
      ecm.put(name.unique(), plan);
    }
    for(final QNm attr : attrNames) {
      final PlanType pt = PlanType.get(attrNames.get(attr).finish());
      if(pt != null && pt != PlanType.STRING) {
        final XQMap acm = new XQRecordMap(Records.ATTRIBUTE_CONVERSION_PLAN.get(),
            Str.get(pt.toString()));
        ecm.put(Strings.concat('@', attr.unique()), acm);
      }
    }
    return ecm.map();
  }
}
