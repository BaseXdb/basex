package org.basex.build.file;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.csv.*;
import org.basex.query.value.node.*;

/**
 * This class parses files in the CSV format
 * and sends events to the specified database builder.
 *
 * <p>The parser provides some options, which can be specified via
 * <code>SET PARSEROPT ...</code>:</p>
 *
 * <ul>
 *   <li><code>separator</code> defines the column separator, which can be
 *   <code>comma</code>, <code>semicolon</code>, or <code>tab</code>
 *   (default: <code>comma</code>).</li>
 *   <li><code>header</code> specifies if the input file contains a header.
 *   Can be set to <code>yes</code> or <code>no</code>
 *   (default: <code>yes</code>)</li>
 *   <li><code>format</code> specifies the XML format, which can be
 *   <code>simple</code> or <code>verbose</code>
 *   (default: <code>verbose</code>).</li>
 * </ul>
 *
 * <p>All options are separated by commas, and the keys and values are
 * separated by equality sign (=).</p>
 *
 * <p><b>Example</b>:
 * <code>SET PARSEROPT separator=tab,format=simple,header=no; CREATE DB ...
 * </code><br/>
 * <b>Description</b>: Use tabs as separator, choose simple XML format,
 * and indicate that the file contains no header.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CSVParser extends XMLParser {
  /** Separators. */
  public static final String[] SEPARATORS = { "comma", "semicolon", "tab", "space" };

  /**
   * Constructor.
   * @param source document source
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public CSVParser(final IO source, final Prop pr) throws IOException {
    super(toXML(source, pr.get(Prop.PARSEROPT)), pr);
  }

  /**
   * Converts a JSON document to XML.
   * @param io io reference
   * @param options parsing options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXML(final IO io, final String options) throws IOException {
    // set parser properties
    final ParserProp props = new ParserProp(options);
    final boolean header = props.is(ParserProp.HEADER);

    // set separator
    final String val = props.get(ParserProp.SEPARATOR).toLowerCase(Locale.ENGLISH);
    int s = -1;
    for(int i = 0; i < SEPARATORS.length && s == -1; i++) {
      if(val.equals(SEPARATORS[i])) s = CsvParser.SEPMAPPINGS[i];
    }
    if(s == -1) {
      final int i = toInt(token(val));
      if(i > 0) s = i;
      else throw new BaseXException(INVALID_VALUE_X_X, ParserProp.SEPARATOR[0], val);
    }
    int separator = s;

    // retrieve content in correct encoding
    String encoding = props.get(ParserProp.ENCODING);
    final byte[] content = new NewlineInput(io).encoding(encoding).content();

    // parse input and convert to XML node
    try {
      final CsvParser conv = new CsvParser(separator, header);
      final ANode node = conv.convert(content);
      // cache XML representation
      final IOContent xml = new IOContent(node.serialize().toArray());
      xml.name(io.name());
      return xml;
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }
}
