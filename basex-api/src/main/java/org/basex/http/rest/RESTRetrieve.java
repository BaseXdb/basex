package org.basex.http.rest;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Retrieve resources via REST.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RESTRetrieve extends RESTCmd {
  /**
   * Constructor.
   * @param session REST session
   */
  private RESTRetrieve(final RESTSession session) {
    super(session);
  }

  @Override
  protected void run0() throws IOException {
    // open addressed database
    for(final Command cmd : cmds) run(cmd);

    final HTTPContext http = session.http;
    if(run(query(_DB_EXISTS)).equals(Text.TRUE)) {
      // return database resource
      final boolean raw = run(query(_DB_IS_RAW)).equals(Text.TRUE);
      if(raw) {
        final SerializerOptions sopts = http.sopts();
        sopts.set(SerializerOptions.METHOD, SerialMethod.RAW);
        sopts.set(SerializerOptions.MEDIA_TYPE, run(query(_DB_CONTENT_TYPE)));
      }
      http.initResponse();

      context.options.set(MainOptions.SERIALIZER, serial(http));
      run(query(raw ? _DB_RETRIEVE : _DB_OPEN), http.res.getOutputStream());

    } else {
      // list database resources
      final Table table = new Table(run(new List(http.db(), http.dbpath())));
      final FElem el = new FElem(RESTText.Q_DATABASE).declareNS();
      el.add(RESTText.NAME, http.db()).add(RESTText.RESOURCES, token(table.contents.size()));
      list(table, el, RESTText.Q_RESOURCE, 0);

      http.initResponse();
      final Serializer ser = Serializer.get(http.res.getOutputStream(), http.sopts());
      ser.serialize(el);
      ser.close();
    }
  }

  /**
   * Creates a query instance.
   * @param f function
   * @return query
   */
  private AQuery query(final Function f) {
    final HTTPContext http = session.http;
    final String query = "declare variable $d external;" +
        "declare variable $p external;" + f.args("$d", "$p");
    return new XQuery(query).bind("d", http.db()).bind("p", http.dbpath());
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @return command
   */
  static RESTCmd get(final RESTSession session) {
    final HTTPContext http = session.http;
    final String db = http.db();
    if(db.isEmpty()) return new RESTList(session.add(new List()));
    return new RESTRetrieve(session.add(new Open(db)));
  }
}
