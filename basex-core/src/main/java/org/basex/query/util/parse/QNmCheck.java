package org.basex.query.util.parse;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Cache for checking QNames after their construction.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class QNmCheck {
  /** QName to be checked. */
  private final QNm name;
  /** Flag for assigning default element namespace. */
  private final boolean nsElem;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param name qname
   * @param nsElem default check
   * @param info input info
   */
  QNmCheck(final QNm name, final boolean nsElem, final InputInfo info) {
    this.name = name;
    this.nsElem = nsElem;
    this.info = info;
  }

  /**
   * Assigns the namespace URI that is currently in scope.
   * @param parser query parser
   * @param check check if prefix URI was assigned
   * @return true if URI has a URI
   * @throws QueryException query exception
   */
  boolean assign(final QueryParser parser, final boolean check) throws QueryException {
    if(name.hasURI()) return true;

    if(name.hasPrefix()) {
      name.uri(parser.sc.ns.uri(name.prefix()));
      if(check && !name.hasURI()) throw parser.error(NOURI_X, info, name.string());
    } else if(nsElem) {
      name.uri(parser.sc.elemNS);
    }
    return name.hasURI();
  }
}
