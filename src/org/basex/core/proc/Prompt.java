package org.basex.core.proc;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.query.fs.FSUtils;

/**
 * Evaluates the 'prompt' command. 
 * Constructs a prompt (with server-side information) for the client.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Prompt extends Proc {
  @Override
  protected boolean exec() {
    return true;
  }

  @Override
  protected void out(final PrintOutput out) throws IOException {
    final Data data = context.data();
    if(data != null) {
      final Nodes nodes = context.current();
      final int pre = nodes.pre[0];
      if(data.deepfs && pre != 0 && nodes.size == 1) {
        out.print(FSUtils.getName(data, pre));
      } else {
        out.print(data.tag(pre));
      }
      if(nodes.size != 1) out.print("[...]");
    }
  }
}
