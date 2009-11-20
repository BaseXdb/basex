package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.User;
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
public final class Export extends Proc {
  /** Document Declaration. */
  private static final String DOCDECL =
    "<?xml version=\"1.0\" encoding=\"%\"?>";

  /**
   * Default constructor.
   * @param path export path
   */
  public Export(final String path) {
    super(DATAREF | User.READ | User.ADMIN, path);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    try {
      final Data data = context.data;
      final int[] docs = data.doc();
      final IO io = IO.get(args[0]);
      if(docs.length != 1) io.md();
      for(final int pre : docs) {
        final IO file = docs.length == 1 ? io :
          io.merge(IO.get(Token.string(data.text(pre))));

        final PrintOutput po = new PrintOutput(file.path());
        po.println(Main.info(DOCDECL, Token.UTF8));
        new XMLSerializer(po).node(data, pre);
        po.close();
      }
      return info(DBEXPORTED, data.meta.name, perf);
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }
}
