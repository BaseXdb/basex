package org.basex.build;

import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.util.Atts;

/**
 * This class defines a parser for creating databases from various sources.
 * If TagSoup is found in the classpath, HTML files are automatically converted
 * to well-formed XML.
 * 
 * TagSoup was written by John Cowan and licensed under Apache 2.0
 * http://home.ccil.org/~cowan/XML/tagsoup/
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Parser extends Progress {
  /** Temporary attribute array. */
  protected final Atts atts = new Atts();
  /** Optional HTML parser. */
  private static HTMLParser html;
  /** Document flag; if true, a document node is added. */
  public boolean doc = true;
  /** Database properties. */
  public Prop prop;
  /** Input file. */
  public IO io;

  // Check for existence of TagSoup.
  static {
    try { html = new HTMLParser(); } catch(final Exception ex) { }
  }
  
  /**
   * Constructor.
   * @param f file reference
   * @param p database properties
   */
  protected Parser(final IO f, final Prop p) {
    io = f;
    prop = p;
  }

  /**
   * Returns an XML parser instance.
   * @param in input
   * @param prop database properties
   * @return xml parser
   * @throws IOException I/O exception
   */
  public static Parser xmlParser(final IO in, final Prop prop)
      throws IOException {

    // optionally convert HTML input to well-formed xml
    final IO io = html != null ? html.toXML(in) : in;
    // use internal parser
    if(prop.is(Prop.INTPARSE)) return new XMLParser(io, prop);
    // use default parser
    final SAXSource s = new SAXSource(io.inputSource());
    if(s.getSystemId() == null) s.setSystemId(io.name());
    return new SAXWrapper(s, prop);
  }

  /**
   * Returns a parser instance for creating empty databases.
   * @param io io reference
   * @param pr database properties
   * @return parser
   */
  public static Parser emptyParser(final IO io, final Prop pr) {
    return new Parser(io, pr) {
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
