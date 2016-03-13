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
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class RESTPut {
  /** Private constructor. */
  private RESTPut() { }

  /**
   * Creates REST code.
   * @param session REST session
   * @return code
   * @throws IOException I/O exception
   */
  public static RESTExec get(final RESTSession session) throws IOException {
    // create new database or update resource
    final HTTPContext http = session.http;
    final String db = http.db();
    if(db.isEmpty()) throw HTTPCode.NO_PATH.get();

    RESTCmd.parseOptions(session);

    final MainOptions options = session.context.options;
    final InputStream is = http.req.getInputStream();
    final MediaType mt = http.contentType();

    // choose correct importer
    boolean xml = true;
    final String ct = mt.type();
    if(mt.is(MediaType.APPLICATION_JSON)) {
      final JsonParserOptions opts = new JsonParserOptions();
      opts.assign(mt);
      options.set(MainOptions.JSONPARSER, opts);
      options.set(MainOptions.PARSER, MainParser.JSON);
    } else if(mt.is(MediaType.TEXT_CSV)) {
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
    } else if(!ct.isEmpty() && !mt.isXML()) {
      xml = false;
    }

    // store data as XML or raw file, depending on content type
    final String path = http.dbpath();
    if(path.isEmpty()) {
      if(xml) {
        session.add(new CreateDB(db), is);
      } else {
        session.add(new CreateDB(db));
        session.add(new Store(db), is);
      }
    } else {
      session.add(new Open(db));
      if(xml) {
        session.add(new Replace(path), is);
      } else {
        session.add(new Delete(path));
        session.add(new Store(path), is);
      }
    }
    final RESTExec cmd = new RESTExec(session);
    cmd.code = HTTPCode.CREATED_X;
    return cmd;
  }
}
