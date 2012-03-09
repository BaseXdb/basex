package org.basex.http.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.rest.RESTText.*;
import static org.basex.io.MimeTypes.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;
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
  void run(final HTTPContext http) throws HTTPException, IOException {
    // parse database options
    parseOptions(http);

    // create new database or update resource
    final Session session = http.session();
    if(http.depth() == 0) throw new HTTPException(SC_NOT_FOUND, ERR_NOPATH);

    boolean xml = true;
    final InputStream in = http.in;
    final String ct = http.req.getContentType();
    // choose correct importer
    if(APP_JSON.equals(ct)) {
      session.execute("set parser json");
    } else if(APP_JSONML.equals(ct)) {
      session.execute("set parser json");
      session.execute("set parseropt jsonml=true");
    } else if(TEXT_CSV.equals(ct)) {
      session.execute("set parser csv");
    } else if(TEXT_HTML.equals(ct)) {
      session.execute("set parser html");
    } else if(TEXT_PLAIN.equals(ct)) {
      session.execute("set parser text");
    } else if(ct != null && !APP_XML.equals(ct)) {
      xml = false;
    }

    if(http.depth() == 1) {
      // store data as XML or raw file, depending on content type
      if(xml) {
        session.create(http.db(), in);
      } else {
        session.create(http.db(), new ArrayInput(""));
        session.store(http.db(), in);
      }
    } else {
      open(http);
      // store data as XML or raw file, depending on content type
      if(xml) {
        session.replace(http.dbpath(), in);
      } else {
        session.execute(new Delete(http.dbpath()));
        session.store(http.dbpath(), in);
      }
    }

    // return correct status and command info
    throw new HTTPException(SC_CREATED, session.info());
  }
}
