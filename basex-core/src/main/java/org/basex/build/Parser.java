package org.basex.build;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.text.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class defines a parser, which is used to create new databases instances.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Parser extends Proc {
  /** Source document or {@code null}. */
  public IO source;
  /** Attributes of currently parsed element. */
  protected final Atts atts = new Atts();
  /** Namespaces of currently parsed element. */
  protected final Atts nsp = new Atts();
  /** Main options. */
  protected final MainOptions options;
  /** Target path (empty, or suffixed with a single slash). */
  String target = "";

  /**
   * Constructor.
   * @param source document source or {@code null}
   * @param options main options
   */
  protected Parser(final String source, final MainOptions options) {
    this(source == null ? null : IO.get(source), options);
  }

  /**
   * Constructor.
   * @param source document source or {@code null}
   * @param options main options
   */
  protected Parser(final IO source, final MainOptions options) {
    this.source = source;
    this.options = options;
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
   * @param options database options
   * @return parser
   */
  public static Parser emptyParser(final MainOptions options) {
    return new Parser((IO) null, options) {
      @Override
      public void parse(final Builder build) { /* empty */ }
    };
  }

  /**
   * Returns an XML parser instance, using the Java default parser.
   * @param source input source
   * @return xml parser
   */
  public static SAXWrapper xmlParser(final IO source) {
    final MainOptions opts = new MainOptions();
    opts.set(MainOptions.CHOP, false);
    return new SAXWrapper(source, opts);
  }

  /**
   * Returns a parser instance, based on the current options.
   * @param source input source
   * @param options database options
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
      case TEXT: p = new TextParser(source, options); break;
      case JSON: p = new JsonParser(source, options); break;
      case CSV:  p = new CsvParser(source, options); break;
      default:   p = options.get(MainOptions.INTPARSE) ? new XMLParser(source, options) :
        new SAXWrapper(source, options); break;
    }
    p.target(target);
    return p;
  }
}
