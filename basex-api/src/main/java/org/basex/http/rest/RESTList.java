package org.basex.http.rest;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Lists REST resources.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RESTList extends RESTCmd {
  /**
   * Constructor.
   * @param session REST session
   */
  RESTList(final RESTSession session) {
    super(session);
  }

  @Override
  protected void run0() throws IOException {
    String result = "";
    for(final Command cmd : session) result = run(cmd);

    final Table table = new Table(result);
    final FElem elem = new FElem(RESTText.Q_DATABASES).declareNS();
    elem.add(RESTText.RESOURCES, token(table.contents.size()));
    list(table, elem, RESTText.Q_DATABASE, 1);

    final HTTPConnection conn = session.conn;
    conn.initResponse();
    try(Serializer ser = Serializer.get(conn.response.getOutputStream(), conn.sopts())) {
      ser.serialize(elem);
    }
  }
}
