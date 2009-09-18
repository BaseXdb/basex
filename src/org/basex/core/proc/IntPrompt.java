package org.basex.core.proc;

import java.io.IOException;

import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.util.TokenBuilder;

/**
 * Internal command, returning a console prompt.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntPrompt extends Process {
  /**
   * Default constructor.
   */
  public IntPrompt() {
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
        curr.add(fs ? data.fs.path(pre, false) : data.tag(pre));
      }
      if(nodes.size() != 1) curr.add("[...]");
    }
    out.print(Main.info(fs ? "%[basex] $ " : "%> ", curr));
  }
}
