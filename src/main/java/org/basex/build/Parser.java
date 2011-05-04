package org.basex.build;

import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.file.CSVParser;
import org.basex.build.file.HTMLParser;
import org.basex.build.file.MAB2Parser;
import org.basex.build.file.TextParser;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.DataText;
import org.basex.io.IO;
import org.basex.util.Atts;

/**
 * This class defines a parser for creating databases from various sources.
 * If TagSoup is found in the classpath, HTML files are automatically converted
 * to well-formed XML.
 *
 * TagSoup was written by John Cowan and licensed under Apache 2.0
 * {@code http://home.ccil.org/~cowan/XML/tagsoup/}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Parser extends Progress {
  /** Input file. */
  public IO file;

  /** Temporary attribute array.
      To speed up processing, the same instance is used over and over. */
  protected final Atts atts = new Atts();
  /** Target path. */
  protected final String target;

  /**
   * Constructor.
   * @param io parser input
   */
  protected Parser(final String io) {
    this(IO.get(io), "");
  }

  /**
   * Constructor.
   * @param f file reference
   * @param t target path
   */
  public Parser(final IO f, final String t) {
    file = f;
    target = t;
  }

  /**
   * Returns a file parser instance.
   * @param io input
   * @param prop database properties
   * @param target relative path reference
   * @return xml parser
   * @throws IOException I/O exception
   */
  public static FileParser fileParser(final IO io, final Prop prop,
      final String target) throws IOException {

    // use file specific parser
    final String parser = prop.get(Prop.PARSER).toLowerCase();
    if(parser.equals(DataText.M_HTML)) return new HTMLParser(io, target, prop);
    if(parser.equals(DataText.M_TEXT)) return new TextParser(io, target, prop);
    if(parser.equals(DataText.M_CSV)) return new CSVParser(io, target, prop);
    if(parser.equals(DataText.M_MAB2)) return new MAB2Parser(io, target, prop);
    return xmlParser(io, prop, target);
  }

  /**
   * Returns an XML parser instance.
   * @param io input
   * @param prop database properties
   * @param target relative path reference
   * @return xml parser
   * @throws IOException I/O exception
   */
  public static FileParser xmlParser(final IO io, final Prop prop,
      final String target) throws IOException {

    // XML: use internal parser
    if(prop.is(Prop.INTPARSE)) return new XMLParser(io, target, prop);
    // use default parser
    final SAXSource s = new SAXSource(io.inputSource());
    return new SAXWrapper(s, io.name(), target, prop);
  }

  /**
   * Returns a parser instance for creating empty databases.
   * @param f file reference
   * @return parser
   */
  public static Parser emptyParser(final String f) {
    return new Parser(f) {
      @Override
      public void parse(final Builder build) { /* empty */ }
    };
  }

  /**
   * Parses all nodes and sends events to the specified builder.
   * @param build database builder
   * @throws IOException I/O exception
   */
  public abstract void parse(Builder build) throws IOException;
}
