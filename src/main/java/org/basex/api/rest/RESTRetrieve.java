package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Map;

import org.basex.core.Prop;
import org.basex.core.cmd.List;
import org.basex.core.cmd.ListDB;
import org.basex.core.cmd.Retrieve;
import org.basex.core.cmd.Set;
import org.basex.data.DataText;
import org.basex.io.MimeTypes;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.server.Session;
import org.basex.util.Table;
import org.basex.util.list.TokenList;

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
  void run(final RESTContext ctx) throws RESTException, IOException {
    // open addressed database
    open(ctx);

    final Session session = ctx.session;
    if(ctx.depth() == 0) {
      // list databases
      final Table table = new Table(session.execute(new List()));
      final SerializerProp sprop = new SerializerProp(ctx.serialization);
      final Serializer ser = Serializer.get(ctx.out, sprop);
      initResponse(sprop, ctx);
      ser.openElement(DATABASES, RESOURCES, token(table.contents.size()));
      ser.namespace(REST, RESTURI);
      list(table, ser, DATABASE, 1);
      ser.closeElement();
      ser.close();
    } else if(!exists(ctx)) {
      // list database resources
      final Table table = new Table(session.execute(new ListDB(ctx.all())));
      if(table.contents.size() == 0)
        throw new RESTException(SC_NOT_FOUND, ERR_NORES);

      final String serial = ctx.serialization;
      final SerializerProp sprop = new SerializerProp(serial);
      final Serializer ser = Serializer.get(ctx.out, sprop);
      initResponse(sprop, ctx);

      ser.openElement(DATABASE, DataText.T_NAME, token(ctx.db()),
        RESOURCES, token(table.contents.size()));
      ser.namespace(REST, RESTURI);
      list(table, ser, RESOURCE, 0);
      ser.closeElement();
      ser.close();
    } else if(isRaw(ctx)) {
      final String type = contentType(ctx);
      if(type.equals(MimeTypes.APP_XQUERY)) {
        // execute raw file as query
        final ArrayOutput ao = new ArrayOutput();
        session.setOutputStream(ao);
        session.execute(new Retrieve(ctx.dbpath()));
        query(ao.toString(), ctx);
      } else {
        // retrieve raw file; prefix user parameters with media type
        final String ct = SerializerProp.S_MEDIA_TYPE[0] + "=" + type;
        initResponse(new SerializerProp(ct + ',' + ctx.serialization), ctx);
        session.setOutputStream(ctx.out);
        session.execute(new Retrieve(ctx.dbpath()));
      }
    } else {
      // retrieve xml file
      initResponse(new SerializerProp(ctx.serialization), ctx);
      session.execute(new Set(Prop.SERIALIZER, serial(ctx)));
      session.setOutputStream(ctx.out);
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
