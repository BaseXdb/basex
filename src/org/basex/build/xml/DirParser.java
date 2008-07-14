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
  public void parse(final Builder build) throws IOException {
    build.meta().filesize = 0;
    parse(build, file);
  }
  
  /**
   * Parse the specified file or parser its children.
   * @param b builder
   * @param f file reference
   * @throws IOException I/O exception
   */
  private void parse(final Builder b, final IO f) throws IOException {
    if(f.isDir() && !f.isLink()) {
      for(final IO ch : f.children()) parse(b, ch);
    } else {
      file = f;
      while(f.more()) {
        if(!f.name().endsWith(IO.XMLSUFFIX)) continue;
        b.meta().filesize += file.length();
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
