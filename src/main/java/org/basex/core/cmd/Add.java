package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOContent;

/**
 * Evaluates the 'add' command and adds a single document to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class Add extends ACreate {
  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Add(final String input) {
    this(input, null);
  }

  /**
   * Constructor, specifying a document name.
   * @param input input XML file or XML string
   * @param name name of document
   */
  public Add(final String input, final String name) {
    this(input, name, null);
  }

  /**
   * Constructor, specifying a document name and a target.
   * @param input input XML file or XML string
   * @param name name of document. If {@code null}, the name of the input
   *   will be used
   * @param target target. If {@code null}, target will be set to root
   */
  public Add(final String input, final String name, final String target) {
    super(DATAREF | User.WRITE, input, name, target == null ? "" : target);
  }

  @Override
  protected boolean run() {
    final String input = args[0];
    final IO io = IO.get(input);
    if(!io.exists()) return error(FILEWHICH, io);

    if(args[1] != null) {
      // set name specified document name
      io.name(args[1]);
    } else if(io instanceof IOContent) {
      // if no name exists, set database name as document name
      io.name(context.data.meta.name + IO.XMLSUFFIX);
    }

    final String name   = io.name();
    final String target = path(args[2]);

    final DirParser p = new DirParser(io, context.prop, target);
    try {
      final Data data = context.data;
      data.insert(data.meta.size, -1, MemBuilder.build(p, context.prop, name));
      data.flush();
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : name);
    }

    context.update();
    return info(PATHADDED, name, perf);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(AS, 1).arg(TO, 2).arg(0);
  }
}
