package org.basex.query.util.parse;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * QName cache.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QNmCache {
  /** Cached QNames. */
  private final ArrayList<QNmCheck> names = new ArrayList<>();

  /**
   * Adds a QName to the cache.
   * @param name QName
   * @param ii input info
   */
  public void add(final QNm name, final InputInfo ii) {
    add(name, true, ii);
  }

  /**
   * Constructor.
   * @param name qname
   * @param nsElem default check
   * @param ii input info
   */
  public void add(final QNm name, final boolean nsElem, final InputInfo ii) {
    names.add(new QNmCheck(name, nsElem, ii));
  }

  /**
   * Finalizes the QNames by assigning namespace URIs.
   * @param qp query parser
   * @param npos first entry to be checked
   * @throws QueryException query exception
   */
  public void assignURI(final QueryParser qp, final int npos) throws QueryException {
    for(int i = npos; i < names.size(); i++) {
      if(names.get(i).assign(qp, npos == 0)) names.remove(i--);
    }
  }

  /**
   * Returns the number of caches QNames.
   * @return number
   */
  public int size() {
    return names.size();
  }
}
