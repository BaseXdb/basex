package org.basex.http.rest;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Lists REST resources.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RESTList extends RESTCmd {
  /**
   * Constructor.
   * @param rs REST session
   */
  RESTList(final RESTSession rs) {
    super(rs);
  }

  @Override
  protected void run0() throws IOException {
    // list all databases
    final Table table = new Table(run(cmds.get(0)));
    final FElem el = new FElem(RESTText.Q_DATABASES).declareNS();
    el.add(RESTText.RESOURCES, token(table.contents.size()));
    list(table, el, RESTText.Q_DATABASE, 1);

    final HTTPContext http = session.http;
    http.initResponse();
    final Serializer ser = Serializer.get(http.res.getOutputStream(), http.serialization);
    ser.serialize(el);
    ser.close();
  }
}
