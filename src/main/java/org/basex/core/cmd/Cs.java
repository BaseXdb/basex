package org.basex.core.cmd;

import org.basex.core.User;
import org.basex.data.Nodes;

/**
 * Evaluates the 'cs' command and sets a new initial context set.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Cs extends AQuery {
  /**
   * Default constructor.
   * @param query query
   */
  public Cs(final String query) {
    super(DATAREF | User.READ, query);
  }

  @Override
  protected boolean run() {
    queryNodes();
    if(result == null) return false;

    if(result.size() != 0) {
      final Nodes nodes = (Nodes) result;
      context.current = nodes;
      // determine if new context set refers to root documents
      final int[] docs = context.data.doc();
      if(nodes.nodes.length != docs.length) return true;
      for(int i = 0; i < docs.length; ++i) {
        if(nodes.nodes[i] != docs[i]) return true;
      }
      nodes.doc = true;
    }
    return true;
  }
}
