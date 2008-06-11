package org.basex.query.xpath;

import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;

/**
 * XPath Processor, containing the XPath parser. The {@link #parse()} method
 * evaluates the query and returns the parsed expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public final class XPathProcessor extends QueryProcessor {
  /**
   * XPath Query Constructor.
   * @param q query
   */
  public XPathProcessor(final String q) {
    super(q.length() == 0 ? "." : q);
  }

  @Override
  public XPContext create() throws QueryException {
    return new XPParser(query).parse();
  }
}
