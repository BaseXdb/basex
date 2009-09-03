package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.core.Prop;
import org.basex.io.IOFile;

/**
 * Parser for XML files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class XMLParser extends AbstractParser {

  static {
    NewFSParser.register("xml", XMLParser.class);
  }

  /** Standard constructor. */
  public XMLParser() {
    super(MetaType.XML, MimeType.XML);
  }

  @Override
  public boolean check(final BufferedFileChannel f) {
    // [BL] check if the document is well-formed
    return true;
  }

  @Override
  protected void content(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    final long size = f.size();

    // parsing of xml fragments inside file is not supported
    if(size <= parser.prop.num(Prop.FSTEXTMAX) && !f.isSubChannel()) {
      try {
        final Parser p = Parser.xmlParser(new IOFile(parser.curr), parser.prop);
        new MemBuilder(p).build();
  
        parser.startXMLContent(f.absolutePosition(), f.size());
        parser.parseXML();
        parser.endXMLContent();
        return;
      } catch(final IOException ex) {
        // XML parsing exception...
      }
      parser.parseWithFallbackParser(f, true);
    }
  }

  @Override
  protected void meta(final BufferedFileChannel bfc, final NewFSParser parser) {
  // no metadata to read...
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    content(bfc, parser);
    return true;
  }
}
