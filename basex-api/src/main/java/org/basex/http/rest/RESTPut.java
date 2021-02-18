package org.basex.http.rest;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.text.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.util.http.*;

/**
 * REST-based evaluation of PUT operations.
 *
 * @author BaseX Team 2005-21, BSD License
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
    if(db.isEmpty()) throw HTTPCode.NO_DATABASE_SPECIFIED.get();

    RESTCmd.parseOptions(session);

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
    } else if(mt.isText()) {
      final TextOptions opts = new TextOptions();
      opts.assign(mt);
      options.set(MainOptions.TEXTPARSER, opts);
      options.set(MainOptions.PARSER, MainParser.TEXT);
    } else if(!mt.is(MediaType.ALL_ALL) && !mt.isXML()) {
      xml = false;
    }

    // store data as XML or raw file, depending on content type
    final String path = conn.dbpath();
    if(path.isEmpty()) {
      // do not OPEN database
      session.clear();
      if(xml) {
        session.add(new CreateDB(db), is);
      } else {
        session.add(new CreateDB(db)).add(new Store(db), is);
      }
    } else {
      if(xml) {
        session.add(new Replace(path), is);
      } else {
        session.add(new Delete(path)).add(new Store(path), is);
      }
    }
    return new RESTExec(session, true);
  }
}
