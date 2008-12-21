package org.basex.query.xpath;

import static org.basex.Text.*;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;

/**
 * XPath Processor, containing the XPath parser.
 * The {@link #create()} method evaluates the query and returns
 * a query context.
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
    super(q.trim().length() == 0 ? "." : q);
  }

  @Override
  public XPContext create() throws QueryException {
    return new XPParser(query).parse();
  }

  /**
   * Parses the specified query and returns the result nodes.
   * @param nodes node context
   * @return result of query
   * @throws QueryException query exception
   */
  public Nodes queryNodes(final Nodes nodes) throws QueryException {
    final Result res = query(nodes);
    if(!(res instanceof Nodes)) throw new QueryException(QUERYNODESERR);
    return (Nodes) res;
  }
}
