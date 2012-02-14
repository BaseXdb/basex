package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.server.Session;

/**
 * REST-based evaluation of DELETE operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTDelete extends RESTCode {
  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    // parse database options
    parseOptions(ctx);
    // open addressed database
    open(ctx);

    final Session session = ctx.session;
    if(ctx.depth() == 0) {
      throw new RESTException(SC_NOT_FOUND, ERR_NOPATH);
    } else if(ctx.depth() == 1) {
      session.execute(new DropDB(ctx.db()));
    } else {
      session.execute(new Delete(ctx.dbpath()));
    }
    // return command info
    ctx.out.write(token(session.info()));
  }
}
