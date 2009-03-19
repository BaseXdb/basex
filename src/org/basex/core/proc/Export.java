package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
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
      final Data data = context.data();
      final int[] docs = data.doc();
      for(final int pre : docs) {
        IO file = IO.get(args[0]);
        
        // more documents - use original name and use argument as path
        if(docs.length != 1) {
          file = file.merge(IO.get(Token.string(data.text(pre))));
        }
        final PrintOutput out = new PrintOutput(file.path());
        out.println(BaseX.info(DOCDECL, Token.UTF8));
        new XMLSerializer(out, false, data.meta.chop).node(data, pre);
        out.close();

      }
      return Prop.info ? info(DBEXPORTED, perf.getTimer()) : true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }
}
