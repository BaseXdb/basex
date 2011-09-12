package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Map;

import org.basex.server.Session;

/**
 * REST-based evaluation of PUT operations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class RESTPut extends RESTCode {
  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    final Map<?, ?> map = ctx.req.getParameterMap();
    if(map.size() != 0) throw new RESTException(SC_BAD_REQUEST, ERR_NOPARAM);

    // create new database or update resource
    final Session session = ctx.session;
    if(ctx.depth() == 0) {
      throw new RESTException(SC_NOT_FOUND, ERR_NOPATH);
    } else if(ctx.depth() == 1) {
      session.create(ctx.db(), ctx.req.getInputStream());
    } else {
      open(ctx);
      session.replace(ctx.dbpath(), ctx.req.getInputStream());
    }

    // return correct status and command info
    ctx.res.setStatus(SC_CREATED);
    ctx.out.write(token(session.info()));
  }
}
