package org.basex.build;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class defines a parser, which is used to create new databases instances.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class Parser extends Job {
  /** Attributes of currently parsed element. */
  protected final Atts atts = new Atts();
  /** Namespaces of currently parsed element. */
  protected final Atts nsp = new Atts();
  /** Main options. */
  protected final MainOptions options;
  /** Target path (empty, or suffixed with a single slash). */
  protected String target = "";
  /** Parses source or {@code null}. */
  protected IO source;

  /**
   * Constructor without input source.
   * @param options main options
   */
  private Parser(final MainOptions options) {
    this.options = options;
  }

  /**
   * Constructor.
   * @param source input source
   * @param options main options
   */
  protected Parser(final String source, final MainOptions options) {
    this(IO.get(source), options);
  }

  /**
   * Constructor.
   * @param source input source
   * @param options main options
   */
  protected Parser(final IO source, final MainOptions options) {
    this(options);
    this.source = source;
  }

  /**
   * Parses all nodes and sends events to the specified builder.
   * @param build database builder
   * @throws IOException I/O exception
   */
  public void parse(@SuppressWarnings("unused") final Builder build) throws IOException {
    if(source != null) throw new BaseXException("No parser available for supplied source.");
  }

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

  /**
   * Returns the parsed source.
   * @return source or {@code null}
   */
  public IO source() {
    return source;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Returns a parser instance for creating an empty databases.
   * @param options main options
   * @return parser
   */
  public static Parser emptyParser(final MainOptions options) {
    return new Parser(options);
  }

  /**
   * Returns an XML parser instance, using the Java default parser.
   * @param source input source
   * @return xml parser
   */
  public static SAXWrapper xmlParser(final IO source) {
    return new SAXWrapper(source, new MainOptions());
  }

  /**
   * Returns a parser instance, based on the current options.
   * @param source input source
   * @param options main options
   * @param target relative path reference
   * @return parser
   * @throws IOException I/O exception
   */
  public static SingleParser singleParser(final IO source, final MainOptions options,
      final String target) throws IOException {

    // use file specific parser
    final SingleParser p;
    final MainParser mp = options.get(MainOptions.PARSER);
    switch(mp) {
      case HTML: p = new HtmlParser(source, options); break;
      case JSON: p = new JsonParser(source, options); break;
      case CSV:  p = new CsvParser(source, options); break;
      default:   p = options.get(MainOptions.INTPARSE) ? new XMLParser(source, options) :
        new SAXWrapper(source, options); break;
    }
    p.target(target);
    return p;
  }
}
