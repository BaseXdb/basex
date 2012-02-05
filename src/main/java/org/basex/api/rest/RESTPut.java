package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.io.MimeTypes.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.basex.core.cmd.Delete;
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
    if(!map.isEmpty()) throw new RESTException(SC_BAD_REQUEST, ERR_NOPARAM);

    // create new database or update resource
    final Session session = ctx.session;
    if(ctx.depth() == 0) throw new RESTException(SC_NOT_FOUND, ERR_NOPATH);

    boolean xml = true;
    InputStream in = ctx.in;
    final String ct = ctx.req.getContentType();
    if(APP_JSON.equals(ct)) {
      // convert json to xml
      in = new ArrayInput("<JSON/>");
      xml = true;
    } else if(TEXT_PLAIN.equals(ct)) {
      // convert text to xml
      in = new ArrayInput("<TEXT/>");
    } else if(TEXT_CSV.equals(ct)) {
      // convert CSV to xml
      in = new ArrayInput("<CSV/>");
    } else if(TEXT_HTML.equals(ct)) {
      // convert HTML to xml
      in = new ArrayInput("<HTML/>");
    } else if(ct != null && !APP_XML.equals(ct)) {
      xml = false;
    }

    if(ctx.depth() == 1) {
      // store data as XML or raw file, depending on content type
      if(xml) {
        session.create(ctx.db(), in);
      } else {
        session.create(ctx.db(), new ArrayInput(""));
        session.store(ctx.db(), in);
      }
    } else {
      open(ctx);
      // store data as XML or raw file, depending on content type
      if(xml) {
        session.replace(ctx.dbpath(), in);
      } else {
        session.execute(new Delete(ctx.dbpath()));
        session.store(ctx.dbpath(), in);
      }
    }

    // return correct status and command info
    throw new RESTException(SC_CREATED, session.info());
  }
}
