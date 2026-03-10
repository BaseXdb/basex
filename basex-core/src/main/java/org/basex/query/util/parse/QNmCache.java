package org.basex.query.util.parse;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * QName cache.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QNmCache {
  /** Cached QNames. */
  private final ArrayList<QNmCheck> names = new ArrayList<>();

  /**
   * Adds a element QName to the cache, unless it already has a namespace URI or it can be
   * immediately assigned the fixed default namespace URI. This method is not called for
   * direct element constructors, as their namespace may need to be resolved later.
   * @param name QName
   * @param sc static context
   * @param info input info (can be {@code null})
   */
  public void add(final QNm name, final StaticContext sc, final InputInfo info) {
    if(sc.elemNsFixed && !name.hasPrefix() && !name.hasURI()) name.uri(sc.elemNS);
    else add(name, true, info);
  }

  /**
   * Constructor.
   * @param name qname
   * @param nsElem default check
   * @param info input info (can be {@code null})
   */
  public void add(final QNm name, final boolean nsElem, final InputInfo info) {
    if(!name.hasURI()) names.add(new QNmCheck(name, nsElem, info));
  }

  /**
   * Finalizes the QNames by assigning namespace URIs.
   * @param qp query parser
   * @param npos first entry to be checked
   * @param elemNS default element namespace
   * @throws QueryException query exception
   */
  public void assignURI(final QueryParser qp, final int npos, final byte[] elemNS)
      throws QueryException {
    for(int i = names.size() - 1; i >= npos; --i) {
      if(names.get(i).assign(qp, npos == 0, elemNS)) names.remove(i);
    }
  }

  /**
   * Returns the number of cached QNames.
   * @return number
   */
  public int size() {
    return names.size();
  }
}
