package org.basex.core.proc;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;

/**
 * Evaluates the 'delete' command. Deletes a node from the table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Updates extends Proc {
  /**
   * Performs a query for update operations.
   * @param query query to be performed
   * @param err if this string is specified, it is thrown if the results
   * don't yield element nodes
   * @return resulting node set
   */
  protected final Nodes query(final String query, final String err) {
    try {
      final XPathProcessor qu = new XPathProcessor(query);
      progress(qu);
      final Nodes nodes = qu.queryNodes(context.current());
      // check if all result nodes are tags
      if(err != null) {
        final Data data = context.data();
        for(int i = nodes.size - 1; i >= 0; i--) {
          if(data.kind(nodes.pre[i]) != Data.ELEM) {
            error(err);
            return null;
          }
        }
      }
      return nodes;
    } catch(final QueryException ex) {
      BaseX.debug(ex);
      error(ex.getMessage());
      return null;
    }
  }
}
