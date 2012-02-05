package org.basex.build.file;

import java.io.IOException;
import org.basex.build.xml.XMLParser;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.in.NewlineInput;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.util.*;
import org.basex.query.util.json.JSONConverter;
import org.basex.query.util.json.JsonMLConverter;

/**
 * This class parses files in the JSON format
 * and sends events to the specified database builder.
 *
 * <p>The parser provides some options, which can be specified via
 * <code>SET PARSEROPT ...</code>:</p>
 *
 * <ul>
 *   <li><code>jsonml</code> specifies if the input is parsed in the JsonML
 *   format (default: <code>no</code>).</li>
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
public final class JSONParser extends XMLParser {
  /**
   * Constructor.
   * @param source document source
   * @param target target path
   * @param prop database properties
   * @throws IOException I/O exception
   */
  public JSONParser(final IO source, final String target, final Prop prop)
      throws IOException {
    super(toXML(source, prop.get(Prop.PARSEROPT)), target, prop);
  }

  /**
   * Converts a JSON document to XML.
   * @param io io reference
   * @param options parsing options
   * @return parser
   * @throws IOException I/O exception
   */
  public static IO toXML(final IO io, final String options)
    throws IOException {

    // set parser properties
    final ParserProp props = new ParserProp(options);
    final boolean jsonml = props.is(ParserProp.JSONML);
    final String encoding = props.get(ParserProp.ENCODING);

    // parse input, using specified encoding
    final byte[] content = new NewlineInput(io, encoding).content();

    // parse input and convert to XML node
    final ANode node;
    try {
      final XMLConverter conv = jsonml ?
          new JsonMLConverter(null) : new JSONConverter(null);
      node = conv.parse(content);
    } catch(final QueryException ex) {
      throw new BaseXException(ex.getLocalizedMessage());
    }

    // serialize node
    final ArrayOutput ao = new ArrayOutput();
    node.serialize(Serializer.get(ao));

    // create XML input container
    final IOContent xml = new IOContent(ao.toArray());
    xml.name(io.name());
    return xml;
  }
}
