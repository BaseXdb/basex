package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Evaluates the 'export' command. Exports the current database as XML
 * document.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Export extends Proc {
  /** Document Declaration. */
  static final String DOCDECL = "<?xml version=\"1.0\" encoding=\"%\"?>";

  @Override
  protected boolean exec() {
    try {
      final String name = cmd.arg(0);
      final PrintOutput out = new PrintOutput(name);
      final Data data = context.data();
      final Nodes current = context.current();
      
      if(current.size == 1 && current.pre[0] == 0) {
        out.println(BaseX.info(DOCDECL, Token.UTF8));
        new Nodes(0, data).serialize(
            new XMLSerializer(out, false, data.meta.chop));
      } else {
        current.serialize(new XMLSerializer(out, true, false));
      }
      out.close();

      return Prop.info ? info(DBEXPORTED, perf.getTimer()) : true;
    } catch(final Exception ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }
}
