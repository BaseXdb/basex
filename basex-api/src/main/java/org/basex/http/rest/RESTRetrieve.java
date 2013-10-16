package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class retrieves resources.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class RESTRetrieve extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTRetrieve(final String in, final Map<String, String[]> vars, final byte[] it) {
    super(in, vars, it);
  }

  @Override
  void run(final HTTPContext http) throws IOException {
    // open addressed database
    open(http);

    final LocalSession session = http.session();
    SerializerOptions sopts = http.serialization;
    if(http.depth() == 0) {
      // list databases
      final Table table = new Table(session.execute(new List()));
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sopts);
      http.initResponse(sopts);

      final FElem el = new FElem(Q_DATABASES).declareNS();
      el.add(RESOURCES, token(table.contents.size()));
      list(table, el, Q_DATABASE, 1);
      ser.serialize(el);
      ser.close();
    } else if(!exists(http)) {
      // list database resources
      final Table table = new Table(session.execute(new List(http.db(), http.dbpath())));
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sopts);
      http.initResponse(sopts);

      final FElem el = new FElem(Q_DATABASE).declareNS();
      el.add(NAME, http.db());
      el.add(RESOURCES, token(table.contents.size()));
      list(table, el, Q_RESOURCE, 0);
      ser.serialize(el);
      ser.close();
    } else if(isRaw(http)) {
      // retrieve raw file; prefix user parameters with media type
      if(sopts == null) sopts = new SerializerOptions();
      sopts.set(SerializerOptions.MEDIA_TYPE, contentType(http));
      http.initResponse(sopts);
      session.setOutputStream(http.res.getOutputStream());
      session.execute(new Retrieve(http.dbpath()));
    } else {
      // retrieve xml file
      http.initResponse(sopts);
      session.execute(new Set(MainOptions.SERIALIZER, serial(http)));
      session.setOutputStream(http.res.getOutputStream());
      session.query(".").execute();
    }
  }

  /**
   * Lists the table contents.
   * @param table table reference
   * @param root root node
   * @param header table header
   * @param skip number of columns to skip
   */
  private static void list(final Table table, final FElem root, final QNm header,
      final int skip) {

    for(final TokenList l : table.contents) {
      final FElem el = new FElem(header);
      // don't show last attribute (input path)
      for(int i = 1; i < l.size() - skip; i++) {
        el.add(new QNm(lc(table.header.get(i))), l.get(i));
      }
      el.add(l.get(0));
      root.add(el);
    }
  }
}
