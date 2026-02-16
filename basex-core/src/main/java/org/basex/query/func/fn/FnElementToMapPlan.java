package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
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
    final Iter iter = arg(0).iter(qc);

    // collect element and attribute nodes
    final QNmMap<ANodeList> elemNames = new QNmMap<>();
    final QNmMap<ANodeList> attrNames = new QNmMap<>();
    for(Item item; (item = iter.next()) != null;) {
      for(final XNode desc : toNode(item).descendantIter(true)) {
        if(desc.kind() == Kind.ELEMENT) {
          elemNames.computeIfAbsent(desc.qname(), ANodeList::new).add(desc);
          for(final XNode attr : children(Kind.ATTRIBUTE, desc)) {
            attrNames.computeIfAbsent(attr.qname(), ANodeList::new).add(attr);
          }
        }
      }
    }

    // check structure and types
    final MapBuilder map = new MapBuilder();
    for(final QNm name : elemNames) {
      final PlanEntry pe = entry(elemNames.get(name).finish());
      final MapBuilder mb = new MapBuilder();
      mb.put(LAYOUT, Str.get(pe.layout.toString()));
      if(pe.type != null && pe.type != PlanType.STRING) {
        mb.put(TYPE, Str.get(pe.type.toString()));
      }
      if(pe.child != null) {
        mb.put(CHILD, pe.child.uri().length != 0 ? pe.child.eqName() : pe.child.local());
      }
      map.put(name.unique(), mb.map());
    }
    for(final QNm attr : attrNames) {
      final PlanType pt = PlanType.get(attrNames.get(attr).finish());
      if(pt != null && pt != PlanType.STRING) {
        map.put(Strings.concat('@', attr.unique()), XQMap.get(TYPE, Str.get(pt.toString())));
      }
    }
    return map.map();
  }
}
