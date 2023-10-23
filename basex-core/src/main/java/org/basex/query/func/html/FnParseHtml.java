package org.basex.query.func.html;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.html.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.xml.sax.*;

import nu.validator.htmlparser.common.*;
import nu.validator.htmlparser.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class FnParseHtml extends StandardFunc {
  // TODO: handle second argument (method, html-version, encoding), produce error code FODC0012

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    return value.isEmpty() ? Empty.VALUE : parse(new IOContent(toBytes(value)));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the input and creates an XML document.
   * @param io input data
   * @return node
   * @throws QueryException query exception
   */
  protected final Item parse(final IO io) throws QueryException {
    try {
      if (!ParserImpl.available()) {
        // reader could not be initialized; fall back to html:parse
        final HtmlOptions htmlOptions = new HtmlOptions();
        htmlOptions.set(HtmlOptions.LEXICAL, true);
        htmlOptions.set(HtmlOptions.NONS, false);
        return new DBNode(new org.basex.build.html.HtmlParser(io, new MainOptions(), htmlOptions));
      }
      return new DBNode(new ParserImpl(io, new MainOptions()));
    } catch(final IOException ex) {
      throw INVHTML_X.get(info, ex);
    }
  }

  /**
   * Parser implementation.
   */
  private static class ParserImpl extends XMLParser {

    /**
     * Checks if Validator.nu is available.
     * @return result of check
     */
    public static boolean available() {
      return Reflect.available("nu.validator.htmlparser.sax.HtmlParser");
    }

    /**
     * Constructor.
     * @param source document source
     * @param options main options
     * @throws IOException I/O exception
     */
    ParserImpl(final IO source, final MainOptions options)
        throws IOException {
      super(toXml(source), options);
    }

    /**
     * Converts an HTML document to XML.
     * @param io io reference
     * @return parser
     * @throws IOException I/O exception
     */
    private static IO toXml(final IO io) throws IOException {
      try(TextInput ti = new TextInput(io)) {

        // tries to extract the encoding from the input
        // TODO: remove this, in favor of encoding from options, or constant for string input
        String enc = ti.encoding();
        final byte[] content = ti.content();
        // looks for a charset definition
        final byte[] encoding = token("charset=");
        int cs = indexOf(content, encoding);
        if(cs > 0) {
          // extracts the encoding string
          cs += encoding.length;
          int ce = cs;
          final int cl = content.length;
          while(++ce < cl && content[ce] > 0x28);
          enc = string(substring(content, cs, ce));
        }

        // define output
        final StringWriter sw = new StringWriter();
        final nu.validator.htmlparser.sax.HtmlParser reader =
            new nu.validator.htmlparser.sax.HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

        final ContentHandler writer = new XmlSerializer(sw);
        reader.setContentHandler(writer);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);

        // define input
        final InputSource is = new InputSource(new ArrayInput(content));
        is.setEncoding(Strings.supported(enc) ? Strings.normEncoding(enc) : Strings.UTF8);
        reader.parse(is);
        return new IOContent(token(sw.toString()), io.name());

      } catch(final SAXException ex) {
        Util.errln(ex);
        throw INVHTML_X.getIO(ex.getLocalizedMessage());
      }
    }
  }
}
