package org.basex.build.xml;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;

/**
 * This class parses the tokens that are delivered by the
 * {@link XMLScanner} and sends them to the specified database builder.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DirParser extends Parser {
  /** Parser reference. */
  private Parser parser;
  /** File filter. */
  private String filter;
  /** Element counter. */
  private int c;
  /** Initial file path. */
  private final IO root;

  /**
   * Constructor.
   * @param f file reference
   * @param pr database properties
   */
  public DirParser(final IO f, final Prop pr) {
    this(f, pr, "");
  }
  /**
   * Constructor with a explicit root directory set.
   * This method is currently only used by {@link org.basex.core.proc.Add}
   * to keep relative path information associated with the document node.
   * @param f file reference
   * @param pr database properties
   * @param t Explicit target directory for collection handling.
   */
  public DirParser(final IO f, final Prop pr, final String t) {
    super(f, pr, t);
    root = IO.get(f.path());
    checkFilter(f, pr);    
  }
  /**
   * Checks filter parameters.
   * @param f file
   * @param pr properties
   */
  private void checkFilter(final IO f, final Prop pr) {
    if(f.isDir()) {
      filter = pr.get(Prop.CREATEFILTER).replaceAll("\\*", ".*");
      if(!filter.contains(".")) filter = ".*" + filter + ".*";
    } else {
      filter = ".*";
    }
  }

  @Override
  public void parse(final Builder b) throws IOException {
    b.meta.filesize = 0;
    parse(b, io);
    b.meta.file = root;
  }

  /**
   * Parses the specified file or parser its children.
   * @param b builder
   * @param path file path
   * @throws IOException I/O exception
   */
  private void parse(final Builder b, final IO path) throws IOException {
    if(path.isDir()) {
      for(final IO f : path.children()) parse(b, f);
    } else {
      io = path;
      while(path.more()) {
        if(!path.name().matches(filter)) continue;
        b.meta.filesize += io.length();

        // check for relative pathdelet / collections:
        // if relative part is found the document is added with relative
        // path information.
        // Else: the document is added "flat".
        //target.f
        
        // check for: HTTP/IOURL ok, ZIP ok, <XML/>, ...
        final String t2 = buildTargetPath();
        parser = Parser.xmlParser(io, prop, t2);
        parser.doc = doc;
        parser.parse(b);
        if(Prop.debug && ++c % 100 == 0) Main.err(";");
      }
    }
  }
  /**
   * This method returns the path prefix for document nodes.
   * Files/Urls are either added:
   *  flat, if a single file has to be added
   *  or with the prepended target{@link #target} if it is not empty.
   * 
   * In case of adding folders the relative path information is
   * appended to an optionally set target. 
   * @return the target path for adding.
   */
  private String buildTargetPath() {
    final String rd = root.getDir().endsWith("/") ? root.getDir()
        : root.getDir() + "/";
    final String t2 = target
        + io.path().replace(rd, "").replace(io.name(), "");
    return t2.startsWith("/") ? t2.substring(1) : t2;
  }

  @Override
  public String tit() {
    return parser != null ? parser.tit() : PROGCREATE;
  }

  @Override
  public String det() {
    return parser != null ? parser.det() : "";
  }

  @Override
  public double prog() {
    return parser != null ? parser.prog() : 0;
  }
}
