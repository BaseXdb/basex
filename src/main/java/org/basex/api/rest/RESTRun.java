package org.basex.api.rest;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.basex.api.HTTPSession;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.basex.io.IOFile;
import org.basex.util.Util;

/**
 * REST-based evaluation of XQuery files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class RESTRun extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTRun(final String in, final Map<String, String[]> vars, final byte[] it) {
    super(in, vars, it);
  }

  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    // get root directory for files
    final Context context = HTTPSession.context();
    final String path = context.mprop.get(MainProp.HTTPPATH);

    // check if file is not found, is a folder or points to parent folder...
    final IOFile root = new IOFile(path);
    final IOFile io = new IOFile(path, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
        throw new RESTException(HttpServletResponse.SC_NOT_FOUND,
            Util.info(FILEWHICH, input));

    // set query path
    ctx.session.execute(new Set(Prop.QUERYPATH, io.path()));

    // perform query
    query(string(io.read()), ctx);
  }
}
