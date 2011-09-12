package org.basex.api.rest;

import static org.basex.api.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.cmd.List;
import org.basex.core.cmd.ListDB;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.server.Session;
import org.basex.util.Table;
import org.basex.util.list.TokenList;

/**
 * This class retrieves resources.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class RESTList extends RESTCode {
  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    // try to open addressed database
    open(ctx);

    final SerializerProp sprop = new SerializerProp(ctx.serialization);
    final Serializer ser = Serializer.get(ctx.out, sprop);
    initOutput(sprop, ctx);

    final Session session = ctx.session;
    if(ctx.depth() == 0) {
      // retrieve databases
      final Table table = new Table(session.execute(new List()));
      ser.openElement(DATABASES, RESOURCES, token(table.contents.size()));
      ser.namespace(REST, RESTURI);
      list(table, ser, DATABASE, 1);
      ser.closeElement();
    } else {
      // retrieve database resources
      final Table table = new Table(session.execute(new ListDB(ctx.all())));
      ser.openElement(DATABASE, NAME, token(ctx.db()),
        RESOURCES, token(table.contents.size()));
      ser.namespace(REST, RESTURI);
      list(table, ser, RESOURCE, 0);
      ser.closeElement();
    }
    ser.close();
  }

  /**
   * Lists the table contents.
   * @param table table reference
   * @param xml serializer
   * @param header table header
   * @param skip number of columns to skip
   * @throws IOException I/O exceptions
   */
  private void list(final Table table, final Serializer xml,
      final byte[] header, final int skip) throws IOException {

    for(final TokenList l : table.contents) {
      xml.openElement(header);
      // don't show last attribute (input path)
      for(int i = 1; i < l.size() - skip; i++) {
        xml.attribute(lc(table.header.get(i)), l.get(i));
      }
      xml.text(l.get(0));
      xml.closeElement();
    }
  }
}
