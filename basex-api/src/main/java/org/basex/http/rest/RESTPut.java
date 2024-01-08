package org.basex.http.rest;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.core.MainOptions.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.util.http.*;

/**
 * REST-based evaluation of PUT operations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class RESTPut {
  /** Private constructor. */
  private RESTPut() { }

  /**
   * Creates and returns a REST command.
   * @param session REST session
   * @return code
   * @throws IOException I/O exception
   */
  public static RESTExec get(final RESTSession session) throws IOException {
    // create new database or update resource
    final HTTPConnection conn = session.conn;
    final String db = conn.db();
    if(db.isEmpty()) throw HTTPStatus.NO_DATABASE_SPECIFIED.get();

    RESTCmd.assignOptions(session);

    final MainOptions options = conn.context.options;
    final InputStream is = conn.request.getInputStream();
    final MediaType mt = conn.mediaType();

    // choose correct importer
    boolean xml = true;
    if(mt.isJSON()) {
      final JsonParserOptions opts = new JsonParserOptions();
      opts.assign(mt);
      options.set(MainOptions.JSONPARSER, opts);
      options.set(MainOptions.PARSER, MainParser.JSON);
    } else if(mt.isCSV()) {
      final CsvParserOptions opts = new CsvParserOptions();
      opts.assign(mt);
      options.set(MainOptions.CSVPARSER, opts);
      options.set(MainOptions.PARSER, MainParser.CSV);
    } else if(mt.is(MediaType.TEXT_HTML)) {
      final HtmlOptions opts = new HtmlOptions();
      opts.assign(mt);
      options.set(MainOptions.HTMLPARSER, opts);
      options.set(MainOptions.PARSER, MainParser.HTML);
    } else if(!mt.is(MediaType.ALL_ALL) && !mt.isXml()) {
      xml = false;
    }

    // store data as XML or binary resource, depending on content type
    final String path = conn.dbpath();
    if(path.isEmpty()) {
      // do not OPEN database
      session.clear();
      if(xml) {
        session.add(new CreateDB(db), is);
      } else {
        session.add(new CreateDB(db)).add(new BinaryPut(db), is);
      }
    } else if(xml) {
      session.add(new Put(path), is);
    } else {
      session.add(new Delete(path)).add(new BinaryPut(path), is);
    }
    return new RESTExec(session, true);
  }
}
