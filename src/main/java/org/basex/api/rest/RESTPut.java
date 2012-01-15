package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;

import java.io.IOException;
import java.util.Map;

import org.basex.io.MimeTypes;
import org.basex.io.in.ArrayInput;
import org.basex.server.Session;

/**
 * REST-based evaluation of PUT operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTPut extends RESTCode {
  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    final Map<?, ?> map = ctx.req.getParameterMap();
    if(map.size() != 0) throw new RESTException(SC_BAD_REQUEST, ERR_NOPARAM);

    // create new database or update resource
    final Session session = ctx.session;
    if(ctx.depth() == 0) throw new RESTException(SC_NOT_FOUND, ERR_NOPATH);

    final String ct = ctx.req.getContentType();
    final boolean xml = ct == null || MimeTypes.APP_XML.equals(ct);
    if(ctx.depth() == 1) {
      // store data as XML or raw file, depending on content type
      if(xml) {
        session.create(ctx.db(), ctx.in);
      } else {
        session.create(ctx.db(), new ArrayInput(""));
        session.store(ctx.db(), ctx.in);
      }
    } else {
      open(ctx);
      // store data as XML or raw file, depending on content type
      if(xml) {
        session.replace(ctx.dbpath(), ctx.in);
      } else {
        session.store(ctx.dbpath(), ctx.in);
      }
    }

    // return correct status and command info
    throw new RESTException(SC_CREATED, session.info());
  }
}
