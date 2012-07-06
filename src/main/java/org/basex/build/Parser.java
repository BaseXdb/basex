package org.basex.build;

import static org.basex.core.Text.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.file.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class defines a parser, which is used to create new databases instances.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Parser extends Progress {
  /** Source document, or {@code null}. */
  public IO src;
  /** Temporary attribute array.
      To speed up processing, the same instance is used over and over. */
  protected final Atts atts = new Atts();
  /** Database properties. */
  protected final Prop prop;
  /** Target path (empty, or suffixed with a single slash). */
  protected String target = "";

  /**
   * Constructor.
   * @param source document source, or {@code null}
   * @param pr database properties
   */
  protected Parser(final String source, final Prop pr) {
    this(source == null ? null : IO.get(source), pr);
  }

  /**
   * Constructor.
   * @param source document source, or {@code null}
   * @param pr database properties
   */
  protected Parser(final IO source, final Prop pr) {
    src = source;
    prop = pr;
  }

  /**
   * Parses all nodes and sends events to the specified builder.
   * @param build database builder
   * @throws IOException I/O exception
   */
  public abstract void parse(final Builder build) throws IOException;

  /**
   * Closes the parser.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }

  /**
   * Returns parser information.
   * @return info string
   */
  public String info() {
    return "";
  }

  /**
   * Sets the target path.
   * @param path target path
   * @return self reference
   */
  public Parser target(final String path) {
    target = path.isEmpty() ? "" : (path + '/').replaceAll("//+", "/");
    return this;
  }

  // STATIC METHODS ===========================================================

  /**
   * Returns a parser instance for creating empty databases.
   * @param pr database properties
   * @return parser
   */
  public static Parser emptyParser(final Prop pr) {
    return new Parser((IO) null, pr) {
      @Override
      public void parse(final Builder build) { /* empty */ }
    };
  }

  /**
   * Returns an XML parser instance.
   * @param in input source
   * @param prop database properties
   * @return xml parser
   * @throws IOException I/O exception
   */
  public static SingleParser xmlParser(final IO in, final Prop prop)
      throws IOException {
    // use internal or default XML parser
    return prop.is(Prop.INTPARSE) ? new XMLParser(in, prop) : new SAXWrapper(in, prop);
  }

  /**
   * Returns a parser instance, based on the current options.
   * @param in input source
   * @param prop database properties
   * @param target relative path reference
   * @return parser
   * @throws IOException I/O exception
   */
  public static SingleParser singleParser(final IO in, final Prop prop,
      final String target) throws IOException {

    // use file specific parser
    final String parser = prop.get(Prop.PARSER).toLowerCase(Locale.ENGLISH);
    final SingleParser p;
    if(parser.equals(DataText.M_HTML)) {
      p = new HTMLParser(in, prop);
    } else if(parser.equals(DataText.M_TEXT)) {
      p = new TextParser(in, prop);
    } else if(parser.equals(DataText.M_MAB2)) {
      p = new MAB2Parser(in, prop);
    } else if(parser.equals(DataText.M_JSON)) {
      p = new JSONParser(in, prop);
    } else if(parser.equals(DataText.M_CSV)) {
      p = new CSVParser(in, prop);
    } else if(parser.equals(DataText.M_XML)) {
      p = xmlParser(in, prop);
    } else {
      throw new BuildException(UNKNOWN_PARSER_X, parser);
    }
    p.target(target);
    return p;
  }

  /**
   * Returns an XQuery item for the specified content type.
   * @param in input source
   * @param prop database properties
   * @param type content type (media type)
   * @return xml parser
   * @throws IOException I/O exception
   */
  @SuppressWarnings("resource")
  public static Item item(final IO in, final Prop prop, final String type)
      throws IOException {

    Item it = null;
    if(type != null) {
      if(Token.eq(type, APP_JSON, APP_JSONML)) {
        final String options = ParserProp.JSONML[0] + "=" + eq(type, APP_JSONML);
        it = new DBNode(new JSONParser(in, prop, options));
      } else if(TEXT_CSV.equals(type)) {
        it = new DBNode(new CSVParser(in, prop));
      } else if(TEXT_HTML.equals(type)) {
        it = new DBNode(new HTMLParser(in, prop));
      } else if(MimeTypes.isXML(type)) {
        it = new DBNode(in, prop);
      } else if(MimeTypes.isText(type)) {
        it = Str.get(new TextInput(in).content());
      }
    }
    return it == null ? new B64(in.read()) : it;
  }
}
