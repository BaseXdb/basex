package org.basex.query.func.index;

import static org.basex.index.stats.StatsType.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class IndexFacets extends IndexFn {
  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final boolean flat = defined(1) && eq(toToken(arg(1), qc), FLAT);
    return FDoc.build().add(flat ? flat(data) : tree(data, data.paths.root().get(0))).finish();
  }

  /**
   * Returns a flat facet representation.
   * @param data data reference
   * @return element
   */
  private static FBuilder flat(final Data data) {
    final FBuilder elem = FElem.build(NodeType.DOCUMENT_NODE.qname());
    index(data.elemNames, Q_ELEMENT, elem);
    index(data.attrNames, Q_ATTRIBUTE, elem);
    return elem;
  }

  /**
   * Returns a tree facet representation.
   * @param data data reference
   * @param root root node
   * @return element
   */
  private static FBuilder tree(final Data data, final PathNode root) {
    final FBuilder elem = FElem.build(ANode.type(root.kind).qname());
    final boolean elm = root.kind == Data.ELEM;
    final Names names = elm ? data.elemNames : data.attrNames;
    if(root.kind == Data.ATTR || elm) elem.add(Q_NAME, names.key(root.name));
    stats(root.stats, elem);
    for(final PathNode pn : root.children) elem.add(tree(data, pn));
    return elem;
  }

  /**
   * Evaluates name index information.
   * @param names name index
   * @param name element name
   * @param root root node
   */
  private static void index(final Names names, final QNm name, final FBuilder root) {
    final int ns = names.size();
    for(int n = 1; n <= ns; n++) {
      final FBuilder sub = FElem.build(name).add(Q_NAME, names.key(n));
      stats(names.stats(n), sub);
      root.add(sub);
    }
  }

  /**
   * Attaches statistical information to the specified element.
   * @param stats statistics
   * @param elem element
   */
  private static void stats(final Stats stats, final FBuilder elem) {
    final int type = stats.type;
    if(!isNone(type)) elem.add(Q_TYPE, StatsType.toString(type));
    elem.add(Q_COUNT, stats.count);
    if(isInteger(type) || isDouble(type)) {
      final int mn = (int) stats.min, mx = (int) stats.max;
      elem.add(Q_MIN, mn == stats.min ? mn : stats.min);
      elem.add(Q_MAX, mx == stats.max ? mx : stats.max);
    }
    if(isCategory(type)) {
      final TokenIntMap map = stats.values;
      final IntList list = new IntList(map.size());
      final TokenList values = new TokenList(map.size());
      for(final byte[] value : map) {
        list.add(map.get(value));
        values.add(value);
      }
      for(final int o : list.createOrder(false)) {
        final byte[] value = values.get(o);
        elem.add(FElem.build(Q_ENTRY).add(Q_COUNT, map.get(value)).add(value));
      }
    }
  }
}
