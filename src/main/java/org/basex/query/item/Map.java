package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.util.map.HashTrie;
import org.basex.util.InputInfo;

/**
 * The map item.
 *
 * @author Leo Woerteler
 */
public class Map extends FunItem {

  public Map(final HashTrie m, final QueryContext ctx, final InputInfo ii) {
    super((QNm) null, null, null, null, false);
  }

}
