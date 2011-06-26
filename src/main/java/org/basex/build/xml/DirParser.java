package org.basex.build.xml;

import java.io.IOException;
import java.util.regex.Pattern;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Util;

/**
 * This class parses the tokens that are delivered by the
 * {@link XMLScanner} and sends them to the specified database builder.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DirParser extends Parser {
  /** File pattern. */
  private final Pattern filter;
  /** Properties. */
  private final Prop prop;
  /** Initial file path. */
  private final String root;

  /** Parser reference. */
  private Parser parser;
  /** Element counter. */
  private int c;

  /**
   * Constructor.
   * @param f file reference
   * @param pr database properties
   */
  public DirParser(final IO f, final Prop pr) {
    this(f, pr, "");
  }

  /**
   * Constructor, specifying a target path.
   * @param path file reference
   * @param pr database properties
   * @param t target path
   */
  public DirParser(final IO path, final Prop pr, final String t) {
    super(path, t);
    prop = pr;
    final String dir = path.dir();
    root = dir.endsWith("/") ? dir : dir + '/';
    filter = !path.isDir() ? null :
      Pattern.compile(IOFile.regex(pr.get(Prop.CREATEFILTER)));
  }

  @Override
  public void parse(final Builder b) throws IOException {
    b.meta.filesize = 0;
    b.meta.path = file.path();
    parse(b, file);
  }

  /**
   * Parses the specified file or its children.
   * @param b builder
   * @param io input
   * @throws IOException I/O exception
   */
  private void parse(final Builder b, final IO io) throws IOException {
    if(io.isDir()) {
      // only {@link IOFile} instances can have children
      for(final IO f : ((IOFile) io).children()) parse(b, f);
    } else {
      file = io;
      if(!b.meta.prop.is(Prop.ADDARCHIVES) && file.archive()) return;

      while(io.more()) {
        final String nm = Prop.WIN ? io.name().toLowerCase() : io.name();
        if(filter != null && !filter.matcher(nm).matches()) continue;
        b.meta.filesize += file.length();

        // use global target as prefix
        String targ = !target.isEmpty() ? target + '/' : "";
        final String name = file.name();
        String path = file.path();
        // add relative path without root (prefix) and file name (suffix)
        if(path.endsWith('/' + name)) {
          path = path.substring(0, path.length() - name.length());
          if(path.startsWith(root)) path = path.substring(root.length());
          targ = (targ + path).replace("//", "/");
        }
        parser = Parser.fileParser(io, prop, targ);
        parser.parse(b);

        if(Util.debug && (++c & 0x3FF) == 0) Util.err(";");
      }
    }
  }

  @Override
  public String det() {
    return parser != null ? parser.detail() : "";
  }

  @Override
  public double prog() {
    return parser != null ? parser.progress() : 0;
  }
}
