package org.basex.core.proc;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdImport;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Evaluates the 'import coll' command and adds a single document
 * to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class ImportColl extends ACreate {
  
  /**
   * Default constructor.
   * @param name of the document
   * @param input input XML string
   */
  public ImportColl(final String name, final String input) {
      super(DATAREF | User.WRITE, input, name);
  }
  
  @Override
  protected boolean run() {
    final String name = args[1];
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    final int pre = findDoc(Token.token(name));
    if(pre != -1) return error(DBDOC, args[0]);

    final DirParser p = new DirParser(io, context.prop);
    MemData d = null;
    try {
      d = new MemBuilder(p).build(name);
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : args[0]);
    }
    final Data data = context.data;
    data.insert(data.meta.size, -1, d);
    data.flush();
    context.update();
    return info(DOCADDED, name, perf);
  }
  
  @Override
  public String toString() {
    return Cmd.IMPORT + " " + CmdImport.COLL + " " + args[1] + " " + args[0];
  }
}
