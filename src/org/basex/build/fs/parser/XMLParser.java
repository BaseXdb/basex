package org.basex.build.fs.parser;

import java.io.IOException;
import java.util.TreeMap;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IOFile;

/**
 * Parser for XML files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class XMLParser extends AbstractParser {

  /** Suffixes of all file formats, this parser is able to parse. */
  private static final TreeMap<String, MimeType> suffixes =
      new TreeMap<String, MimeType>();

  static {
    suffixes.put("xml", MimeType.XML);
    suffixes.put("kml", MimeType.KML);
    suffixes.put("rng", MimeType.XML);
    suffixes.put("webloc", MimeType.XML);
    suffixes.put("mailtoloc", MimeType.XML);
    for(String suf : suffixes.keySet())
      NewFSParser.register(suf, XMLParser.class);
  }

  @Override
  public boolean check(final BufferedFileChannel f) {
    final String name = f.getFileName();
    final String suf = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    if(!suffixes.keySet().contains(suf)) return false;
    return true;
  }

  /**
   * Checks if the document is well-formed.
   * @param f the {@link BufferedFileChannel} to read the xml document from.
   * @param parser the parser to get properties from.
   * @return true if the document is well-formed, false otherwise.
   * @throws IOException if any error occurs.
   */
  private boolean check(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(f.size() > parser.prop.num(Prop.FSTEXTMAX)) return false;
    // parsing of xml fragments inside file is not supported
    if(f.isSubChannel()) return false;
    if(!check(f)) return false;
    try {
      final Parser p = Parser.xmlParser(
          new IOFile(f.getFileName()), new Prop());
      new MemBuilder(p).build();
    } catch(final IOException ex) {
      // XML parsing exception...
      return false;
    }
    return true;
  }

  @Override
  protected void content(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(!check(f, parser)) {
      // try to extract content as plaintext
      parser.parseWithFallbackParser(f, true);
      return;
    }
    parse(f, parser);
  }

  /**
   * Parses the XML content (without well-formedness check)
   * @param f the {@link BufferedFileChannel} to read from.
   * @param parser the parser to write the xml to.
   * @throws IOException if any error occurs.
   */
  private void parse(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    parser.startXMLContent(f.absolutePosition(), f.size());
    parser.parseXML();
    parser.endXMLContent();
  }

  @Override
  protected void meta(final BufferedFileChannel bfc, final NewFSParser parser)
      throws IOException {
    if(!check(bfc)) return;
    setTypeAndFormat(bfc, parser);
  }

  /**
   * Sets {@link MetaType} and {@link MimeType}.
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param parser the {@link NewFSParser}.
   * @throws IOException if any error occurs.
   */
  private void setTypeAndFormat(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    final Metadata meta = new Metadata();
    parser.metaEvent(meta.setMetaType(MetaType.XML));
    final String name = bfc.getFileName();
    final String suf = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    final MimeType mime = suffixes.get(suf);
    if(mime == null) Main.notexpected();
    parser.metaEvent(meta.setMimeType(mime));
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    if(!check(bfc, parser)) {
      // try to extract content as plaintext
      parser.parseWithFallbackParser(bfc, true);
      return true;
    }
    setTypeAndFormat(bfc, parser);
    parse(bfc, parser);
    return true;
  }
}
