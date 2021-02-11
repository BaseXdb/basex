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
 * @author BaseX Team 2005-21, BSD License
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
    for(final Command cmd : session) run(cmd);

    final HTTPConnection conn = session.conn;
    final SerializerOptions sopts = conn.sopts();
    if(run(query(_DB_EXISTS)).equals(Text.TRUE)) {
      // return database resource
      final boolean raw = run(query(_DB_IS_RAW)).equals(Text.TRUE);
      if(raw) sopts.set(SerializerOptions.MEDIA_TYPE, run(query(_DB_CONTENT_TYPE)));
      conn.initResponse();

      context.options.set(MainOptions.SERIALIZER, sopts);
      run(query(raw ? _DB_RETRIEVE : _DB_OPEN), conn.response.getOutputStream());

    } else {
      // list database resources
      final Table table = new Table(run(new List(conn.db(), conn.dbpath())));
      final FElem elem = new FElem(RESTText.Q_DATABASE).declareNS();
      elem.add(RESTText.NAME, conn.db()).add(RESTText.RESOURCES, token(table.contents.size()));
      list(table, elem, RESTText.Q_RESOURCE, 0);

      conn.initResponse();
      try(Serializer ser = Serializer.get(conn.response.getOutputStream(), sopts)) {
        ser.serialize(elem);
      }
    }
  }

  /**
   * Creates a query instance.
   * @param f function
   * @return query
   */
  private XQuery query(final Function f) {
    final HTTPConnection conn = session.conn;
    final String query = "declare variable $d external;" +
        "declare variable $p external;" + f.args(" $d", " $p");
    return new XQuery(query).bind("d", conn.db()).bind("p", conn.dbpath());
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @return command
   */
  static RESTCmd get(final RESTSession session) {
    final String db = session.conn.db();
    return db.isEmpty() ? new RESTList(session.add(new List())) : new RESTRetrieve(session);
  }
}
