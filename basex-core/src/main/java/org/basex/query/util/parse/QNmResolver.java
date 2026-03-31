package org.basex.query.util.parse;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Resolves namespace URIs of QNames whose resolution must be deferred until the surrounding
 * namespace context is known.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QNmResolver {
  /**
   * Entry for a QName whose namespace URI still needs to be resolved.
   * @param name QName to be resolved
   * @param nsElem flag for assigning default element namespace
   * @param info input info (can be {@code null})
   */
  private record Entry(QNm name, boolean nsElem, InputInfo info) { }
  /** QNames to be resolved. */
  private final ArrayList<Entry> entries = new ArrayList<>();

  /**
   * Adds a QName unless it already has a namespace URI or it can be immediately assigned the fixed
   * default namespace URI. This method must not be called for the element names of direct element
   * constructors.
   * @param name QName
   * @param sc static context
   * @param info input info (can be {@code null})
   */
  public void add(final QNm name, final StaticContext sc, final InputInfo info) {
    if(sc.elemNsFixed && !name.hasPrefix() && !name.hasURI()) name.uri(sc.elemNS);
    else add(name, true, info);
  }

  /**
   * Adds a QName unless it already has a namespace URI.
   * @param name qname
   * @param nsElem default check
   * @param info input info (can be {@code null})
   */
  public void add(final QNm name, final boolean nsElem, final InputInfo info) {
    if(!name.hasURI()) entries.add(new Entry(name, nsElem, info));
  }

  /**
   * Finalizes the QNames by assigning namespace URIs.
   * @param qp query parser
   * @param npos first entry to be checked
   * @param elemNS default element namespace
   * @throws QueryException query exception
   */
  public void resolve(final QueryParser qp, final int npos, final byte[] elemNS)
      throws QueryException {
    for(int i = entries.size() - 1; i >= npos; --i) {
      final Entry entry = entries.get(i);
      if(entry.name.hasPrefix()) {
        entry.name.uri(qp.qc.ns.resolve(entry.name.prefix(), qp.sc));
        if(npos == 0 && !entry.name.hasURI())
          throw qp.error(NOURI_X, entry.info, entry.name.prefix());
      } else if(entry.nsElem && !Token.eq(elemNS, QueryText.ANY_URI)) {
        entry.name.uri(elemNS);
      }
      if(entry.name.hasURI()) entries.remove(i);
    }
  }

  /**
   * Returns the number of remaining QNames.
   * @return number
   */
  public int size() {
    return entries.size();
  }
}
