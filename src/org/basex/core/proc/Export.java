package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
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
  /**
   * Default constructor.
   * @param path export path
   */
  public Export(final String path) {
    this(path, null);
  }

  /**
   * Default constructor, specifying an optional output filename.
   * @param path export path
   * @param name optional name of output file
   */
  public Export(final String path, final String name) {
    super(DATAREF | User.READ | User.ADMIN, path, name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    try {
      final Data data = context.data;
      final int[] docs = data.doc();
      final IO io = IO.get(args[0]);
      if(docs.length != 1) io.md();
      for(final int pre : docs) {
        final IO file = io.merge(IO.get(docs.length == 1 && args[1] != null ?
            args[1] : Token.string(data.text(pre, true))));

        final PrintOutput po = new PrintOutput(file.path());
        final XMLSerializer xml = new XMLSerializer(po, false,
            context.prop.is(Prop.XMLFORMAT));
        xml.encoding(context.prop.get(Prop.XMLENCODING));
        xml.node(data, pre);
        po.close();
      }
      return info(DBEXPORTED, data.meta.name, perf);
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }
}
