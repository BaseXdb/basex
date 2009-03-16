package org.basex.core.proc;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Process;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.util.TokenBuilder;

/**
 * Evaluates the 'prompt' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Prompt extends Process {
  /**
   * Constructor.
   */
  public Prompt() {
    super(PRINTING);
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    final TokenBuilder curr = new TokenBuilder();
    final Data data = context.data();
    final boolean fs = data != null && data.fs != null;
    if(data != null) {
      final Nodes nodes = context.current();
      final int pre = nodes.nodes[0];
      if(data.kind(pre) == Data.ELEM) {
        curr.add(fs ? data.fs.path(pre) : data.tag(pre));
      }
      if(nodes.size() != 1) curr.add("[...]");
    }
    out.print(BaseX.info(fs ? "%$ " : "%> ", curr));
  }
}
