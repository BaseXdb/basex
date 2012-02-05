package org.basex.build;

import java.io.IOException;
import java.util.Locale;
import org.basex.build.file.*;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.DataText;
import org.basex.io.IO;
import org.basex.util.Atts;

/**
 * This class defines a parser, which is used to create new databases instances.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Parser extends Progress {
  /** Empty parser. */
  private static final Parser DUMMY = new Parser((IO) null) {
    @Override
    public void parse(final Builder build) { /* empty */ }
  };
  /** Source document, or {@code null}. */
  public IO src;
  /** Temporary attribute array.
      To speed up processing, the same instance is used over and over. */
  protected final Atts atts = new Atts();

  /**
   * Constructor.
   * @param source document source, or {@code null}
   */
  protected Parser(final String source) {
    this(source == null ? null : IO.get(source));
  }

  /**
   * Constructor.
   * @param source document source, or {@code null}
   */
  protected Parser(final IO source) {
    src = source;
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

  // STATIC METHODS ===========================================================

  /**
   * Returns a parser instance for creating empty databases.
   * @return parser
   */
  public static Parser emptyParser() {
    return DUMMY;
  }

  /**
   * Returns an XML parser instance.
   * @param source input
   * @param prop database properties
   * @return xml parser
   * @throws IOException I/O exception
   */
  public static SingleParser xmlParser(final IO source, final Prop prop)
      throws IOException {
    return xmlParser(source, prop, "");
  }

  /**
   * Returns an XML parser instance.
   * @param source input
   * @param prop database properties
   * @param target relative path reference
   * @return xml parser
   * @throws IOException I/O exception
   */
  private static SingleParser xmlParser(final IO source, final Prop prop,
      final String target) throws IOException {

    // XML: use internal parser
    if(prop.is(Prop.INTPARSE)) return new XMLParser(source, target, prop);
    // use default parser
    return new SAXWrapper(source, target, prop);
  }

  /**
   * Returns a file parser instance.
   * @param source document source
   * @param prop database properties
   * @param target relative path reference
   * @return xml parser
   * @throws IOException I/O exception
   */
  static SingleParser fileParser(final IO source, final Prop prop,
      final String target) throws IOException {

    // use file specific parser
    final String parser = prop.get(Prop.PARSER).toLowerCase(Locale.ENGLISH);
    if(parser.equals(DataText.M_HTML))
      return new HTMLParser(source, target, prop);
    if(parser.equals(DataText.M_TEXT))
      return new TextParser(source, target, prop);
    if(parser.equals(DataText.M_MAB2))
      return new MAB2Parser(source, target, prop);
    if(parser.equals(DataText.M_JSON))
      return new JSONParser(source, target, prop);
    if(parser.equals(DataText.M_CSV))
      return new CSVParser(source, target, prop);
    return xmlParser(source, prop, target);
  }
}
