package org.basex.build;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.regex.Pattern;

import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * This class recursively scans files and directories and parses all
 * relevant files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DirParser extends TargetParser {
  /** Number of skipped files to log. */
  private static final int SKIPLOG = 10;
  /** Skipped files. */
  private final StringList skipped = new StringList();
  /** File pattern. */
  private final Pattern filter;
  /** Properties. */
  private final Prop prop;
  /** Initial file path. */
  private final String root;
  /** Parse archives in directories. */
  private final boolean archives;
  /** Skip corrupt files in directories. */
  private final boolean skip;

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
    final String parent = source.dir();
    root = parent.endsWith("/") ? parent : parent + '/';
    skip = prop.is(Prop.SKIPCORRUPT);
    archives = prop.is(Prop.ADDARCHIVES);
    filter = !source.isDir() ? null :
      Pattern.compile(IOFile.regex(pr.get(Prop.CREATEFILTER)));
  }

  @Override
  public void parse(final Builder build) throws IOException {
    build.meta.filesize = 0;
    build.meta.original = src.path();
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
      if(!archives && src.isArchive()) return;

      // multiple archive files may be parsed in this loop
      while(io.more()) {
        final String nm = Prop.WIN ? io.name().toLowerCase() : io.name();
        if(filter != null && !filter.matcher(nm).matches()) continue;
        b.meta.filesize += src.length();

        // use global target as prefix
        String targ = trg;
        final String name = src.name();
        String path = src.path();
        // add relative path without root (prefix) and file name (suffix)
        if(path.endsWith('/' + name)) {
          path = path.substring(0, path.length() - name.length());
          if(path.startsWith(root)) path = path.substring(root.length());
          targ = (targ + path).replace("//", "/");
        }

        boolean ok = true;
        IO in = io;
        if(skip) {
          // parse file twice to ensure that it is well-formed
          BufferInput bi = null;
          try {
            // cache file contents to allow or speed up a second run
            bi = io.buffer();
            in = new IOContent(bi.readBytes());
            in.name(io.name());
            parser = Parser.fileParser(in, prop, targ);
            MemBuilder.build("", parser, prop);
          } catch(final IOException ex) {
            Util.debug(ex.getMessage());
            skipped.add(io.path());
            ok = false;
          } finally {
            if(bi != null) bi.close();
          }
        }
        parser = Parser.fileParser(in, prop, targ);

        if(ok) parser.parse(b);
        if(Util.debug && (++c & 0x3FF) == 0) Util.err(";");
      }
    }
  }

  @Override
  public String info() {
    final TokenBuilder sb = new TokenBuilder();
    if(skipped.size() != 0) {
      sb.add(SKIPCORRUPT).add(COL).add(NL);
      final int s = skipped.size();
      for(int i = 0; i < s && i < SKIPLOG; i++) {
        sb.add(LI).add(skipped.get(i)).add(NL);
      }
      if(s > SKIPLOG) {
        sb.add(LI).addExt(SKIPINFO, s - SKIPLOG).add(NL);
      }
    }
    return sb.toString();
  };

  @Override
  public String det() {
    return parser != null ? parser.detail() : "";
  }

  @Override
  public double prog() {
    return parser != null ? parser.progress() : 0;
  }

  @Override
  public void close() throws IOException {
    if(parser != null) parser.close();
  }
}
