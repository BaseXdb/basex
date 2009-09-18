package org.basex.core.proc;

import static org.basex.core.Text.*;

import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Evaluates the 'export' command and saves the currently opened database
 * to disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Export extends Process {
  /** Document Declaration. */
  private static final String DOCDECL =
    "<?xml version=\"1.0\" encoding=\"%\"?>";

  /**
   * Default constructor.
   * @param path export path
   */
  public Export(final String path) {
    super(DATAREF, path);
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
        out.println(Main.info(DOCDECL, Token.UTF8));
        new XMLSerializer(out, false, data.meta.chop).node(data, pre);
        out.close();

      }
      return info(DBEXPORTED, data.meta.name, perf.getTimer());
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }

  @Override
  public String toString() {
    return Cmd.EXPORT + args();
  }
}
