package org.basex.build;

import java.io.IOException;
import java.util.regex.Pattern;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Util;

/**
 * This class recursively scans files and directories and parses all
 * relevant files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DirParser extends TargetParser {
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
   * @param source source path
   * @param pr database properties
   */
  public DirParser(final IO source, final Prop pr) {
    this(source, "", pr);
  }

  /**
   * Constructor, specifying a target path.
   * @param source source path
   * @param target target path
   * @param pr database properties
   */
  public DirParser(final IO source, final String target, final Prop pr) {
    super(source, target);
    prop = pr;
    final String dir = source.dir();
    root = dir.endsWith("/") ? dir : dir + '/';
    filter = !source.isDir() ? null :
      Pattern.compile(IOFile.regex(pr.get(Prop.CREATEFILTER)));
  }

  @Override
  public void parse(final Builder build) throws IOException {
    build.meta.filesize = 0;
    build.meta.path = src.path();
    parse(build, src);
  }

  /**
   * Parses the specified file or its children.
   * @param b builder
   * @param io current input
   * @throws IOException I/O exception
   */
  private void parse(final Builder b, final IO io) throws IOException {
    if(io.isDir()) {
      // only {@link IOFile} instances can have children
      for(final IO f : ((IOFile) io).children()) parse(b, f);
    } else {
      src = io;
      if(!b.meta.prop.is(Prop.ADDARCHIVES) && src.archive()) return;

      while(io.more()) {
        final String nm = Prop.WIN ? io.name().toLowerCase() : io.name();
        if(filter != null && !filter.matcher(nm).matches()) continue;
        b.meta.filesize += src.length();

        // use global target as prefix
        String targ = !trg.isEmpty() ? trg + '/' : "";
        final String name = src.name();
        String path = src.path();
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
