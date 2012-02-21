package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.io.MimeTypes.*;

import java.io.*;

import org.basex.api.*;
import org.basex.core.cmd.*;
import org.basex.io.in.*;
import org.basex.server.*;

/**
 * REST-based evaluation of PUT operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTPut extends RESTCode {
  @Override
  void run(final HTTPContext ctx) throws HTTPException, IOException {
    // parse database options
    parseOptions(ctx);

    // create new database or update resource
    final Session session = ctx.session;
    if(ctx.depth() == 0) throw new HTTPException(SC_NOT_FOUND, ERR_NOPATH);

    boolean xml = true;
    final InputStream in = ctx.in;
    final String ct = ctx.req.getContentType();
    // choose correct importer
    if(APP_JSON.equals(ct)) {
      session.execute("set parser json");
    } else if(APP_JSONML.equals(ct)) {
      session.execute("set parser json");
      session.execute("set parseropt jsonml=true");
    } else if(TEXT_PLAIN.equals(ct)) {
      session.execute("set parser text");
    } else if(TEXT_CSV.equals(ct)) {
      session.execute("set parser csv");
    } else if(TEXT_HTML.equals(ct)) {
      session.execute("set parser html");
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
    throw new HTTPException(SC_CREATED, session.info());
  }
}
