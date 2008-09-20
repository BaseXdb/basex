package org.basex.build.xml;

import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.io.IO;
 
/**
 * This class parses the tokens that are delivered by the
 * {@link XMLScanner} and sends them to the specified database builder.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DirParser extends Parser {
  /** Parser reference. */
  private Parser parser;

  /**
   * Constructor.
   * @param f file reference
   */
  public DirParser(final IO f) {
    super(f);
  }

  @Override
  public void parse(final Builder b) throws IOException {
    b.meta.filesize = 0;
    parse(b, file);
  }
  
  /**
   * Parse the specified file or parser its children.
   * @param b builder
   * @param path file path
   * @throws IOException I/O exception
   */
  private void parse(final Builder b, final IO path) throws IOException {
    if(path.isDir()) {
      for(final IO f : path.children()) parse(b, f);
    } else {
      file = path;
      while(path.more()) {
        // [CG] Create Collection: how to deal with non-XML documents?
        //if(!f.name().endsWith(IO.XMLSUFFIX)) continue;
        b.meta.filesize += file.length();
        parser = Prop.intparse ? new XMLParser(file) : new SAXWrapper(file);
        parser.parse(b);
      }
    }
  }
  
  
  @Override
  public String head() {
    return parser != null ? parser.head() : "";
  }

  @Override
  public String det() {
    return parser != null ? parser.det() : "";
  }

  @Override
  public double percent() {
    return parser != null ? parser.percent() : 0;
  }
}
