package org.basex.build.file;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.csv.*;

/**
 * This class parses files in the CSV format
 * and sends events to the specified database builder.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link Prop#CSVPARSER} option.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CSVParser extends XMLParser {
  /**
   * Constructor.
   * @param source document source
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public CSVParser(final IO source, final Prop pr) throws IOException {
    super(toXML(source, pr.get(Prop.CSVPARSER)), pr);
  }

  /**
   * Converts a JSON document to XML.
   * @param io io reference
   * @param options parsing options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXML(final IO io, final String options) throws IOException {
    // get parser properties
    final CsvProp cprop = new CsvProp(options);

    // parse input and convert to XML node
    try {
      final CsvParser conv = new CsvParser(cprop.separator(), cprop.is(CsvProp.HEADER));
      final NewlineInput nli = new NewlineInput(io).encoding(cprop.get(CsvProp.ENCODING));
      // cache XML representation
      final IOContent xml = new IOContent(conv.convert(nli).serialize().toArray());
      xml.name(io.name());
      return xml;
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }
}
