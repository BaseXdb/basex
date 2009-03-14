package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Evaluates the 'optimize' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Export extends Process {
  /** Document Declaration. */
  static final String DOCDECL = "<?xml version=\"1.0\" encoding=\"%\"?>";

  /**
   * Constructor.
   * @param p path
   */
  public Export(final String p) {
    super(DATAREF, p);
  }
  
  @Override
  protected boolean exec() {
    try {
      final PrintOutput out = new PrintOutput(args[0]);
      Nodes current = context.current();
      final boolean root = context.root();
      if(root) {
        out.println(BaseX.info(DOCDECL, Token.UTF8));
      }
      current.serialize(new XMLSerializer(out, !root, current.data.meta.chop));
      out.close();

      return Prop.info ? info(DBEXPORTED, perf.getTimer()) : true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }
}
