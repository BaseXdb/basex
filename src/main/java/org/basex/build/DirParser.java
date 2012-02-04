package org.basex.build;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

import org.basex.core.Prop;
import org.basex.core.cmd.Store;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * This class recursively scans files and directories and parses all
 * relevant files.
 *
 * @author BaseX Team 2005-12, BSD License
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
  /** Add remaining files as raw files. */
  private final boolean raw;

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
    raw = prop.is(Prop.ADDRAW);
    archives = prop.is(Prop.ADDARCHIVES);
    filter = !source.isDir() && !source.isArchive() ? null :
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

      while(io.more(archives)) {
        final String nm = Prop.WIN ? io.name().toLowerCase(Locale.ENGLISH) :
          io.name();

        final long l = src.length();
        if(l != -1) b.meta.filesize += l;

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

        if(filter != null && !filter.matcher(nm).matches()) {
          if(raw) {
            Store.store(src.inputSource(), b.meta.binary(targ + name));
          }
        } else {
          boolean ok = true;
          IO in = io;
          if(skip) {
            // parse file twice to ensure that it is well-formed
            try {
              // cache file contents to allow or speed up a second run
              in = new IOContent(io.read());
              in.name(io.name());
              parser = Parser.fileParser(in, prop, targ);
              MemBuilder.build("", parser, prop);
            } catch(final IOException ex) {
              Util.debug(ex.getMessage());
              skipped.add(io.path());
              ok = false;
            }
          }

          if(ok) {
            parser = Parser.fileParser(in, prop, targ);
            parser.parse(b);
          } else {
            parser = null;
          }
          if(Util.debug && (++c & 0x3FF) == 0) Util.err(";");
        }
      }
    }
  }

  @Override
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    if(skipped.size() != 0) {
      tb.add(SKIPPED).add(COL).add(NL);
      final int s = skipped.size();
      for(int i = 0; i < s && i < SKIPLOG; i++) {
        tb.add(LI).add(skipped.get(i)).add(NL);
      }
      if(s > SKIPLOG) {
        tb.add(LI).addExt(MORE_SKIPPED_X, s - SKIPLOG).add(NL);
      }
    }
    return tb.toString();
  }

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
