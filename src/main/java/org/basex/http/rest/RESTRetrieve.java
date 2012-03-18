package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.data.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class retrieves resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RESTRetrieve extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTRetrieve(final String in, final Map<String, String[]> vars,
      final byte[] it) {
    super(in, vars, it);
  }

  @Override
  void run(final HTTPContext http) throws HTTPException, IOException {
    // open addressed database
    open(http);

    final Session session = http.session();
    if(http.depth() == 0) {
      // list databases
      final Table table = new Table(session.execute(new List()));
      final SerializerProp sprop = new SerializerProp(http.serialization);
      final Serializer ser = Serializer.get(http.out, sprop);
      http.initResponse(sprop);
      ser.openElement(DATABASES, RESOURCES, token(table.contents.size()));
      ser.namespace(REST, RESTURI);
      list(table, ser, DATABASE, 1);
      ser.closeElement();
      ser.close();
    } else if(!exists(http)) {
      // list database resources
      final Table table = new Table(session.execute(new ListDB(http.path())));
      if(table.contents.isEmpty()) HTTPErr.UNKNOWN_PATH.thrw();

      final String serial = http.serialization;
      final SerializerProp sprop = new SerializerProp(serial);
      final Serializer ser = Serializer.get(http.out, sprop);
      http.initResponse(sprop);

      ser.openElement(DATABASE, DataText.T_NAME, token(http.db()),
        RESOURCES, token(table.contents.size()));
      ser.namespace(REST, RESTURI);
      list(table, ser, RESOURCE, 0);
      ser.closeElement();
      ser.close();
    } else if(isRaw(http)) {
      final String type = contentType(http);
      if(type.equals(MimeTypes.APP_XQUERY)) {
        // execute raw file as query
        final ArrayOutput ao = new ArrayOutput();
        session.setOutputStream(ao);
        session.execute(new Retrieve(http.dbpath()));
        query(ao.toString(), http, true);
      } else {
        // retrieve raw file; prefix user parameters with media type
        final String ct = SerializerProp.S_MEDIA_TYPE[0] + "=" + type;
        http.initResponse(new SerializerProp(ct + ',' + http.serialization));
        session.setOutputStream(http.out);
        session.execute(new Retrieve(http.dbpath()));
      }
    } else {
      // retrieve xml file
      http.initResponse(new SerializerProp(http.serialization));
      session.execute(new Set(Prop.SERIALIZER, serial(http)));
      session.setOutputStream(http.out);
      session.query(".").execute();
    }
  }

  /**
   * Lists the table contents.
   * @param table table reference
   * @param ser serializer
   * @param header table header
   * @param skip number of columns to skip
   * @throws IOException I/O exceptions
   */
  private static void list(final Table table, final Serializer ser,
      final byte[] header, final int skip) throws IOException {

    for(final TokenList l : table.contents) {
      ser.openElement(header);
      // don't show last attribute (input path)
      for(int i = 1; i < l.size() - skip; i++) {
        ser.attribute(lc(table.header.get(i)), l.get(i));
      }
      ser.text(l.get(0));
      ser.closeElement();
    }
  }
}
