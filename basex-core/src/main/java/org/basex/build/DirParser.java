package org.basex.build;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class recursively scans files and directories and parses all
 * relevant files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DirParser extends Parser {
  /** Number of skipped files to log. */
  private static final int SKIPLOG = 10;
  /** Skipped files. */
  private final StringList skipped = new StringList();
  /** File pattern. */
  private final Pattern filter;
  /** Initial file path. */
  private final String root;

  /** Parse archives in directories. */
  private final boolean archives;
  /** Skip corrupt files in directories. */
  private final boolean skipCorrupt;
  /** Add ignored files as raw files. */
  private final boolean addRaw;
  /** DTD parsing. */
  private final boolean dtd;
  /** Raw parsing. */
  private final boolean rawParser;
  /** Database path for storing binary files. */
  private IOFile rawPath;

  /** Last source. */
  private IO lastSrc;
  /** Parser reference. */
  private Parser parser;
  /** Element counter. */
  private int c;

  /**
   * Constructor.
   * @param source source path
   * @param options main options
   */
  public DirParser(final IO source, final MainOptions options) {
    super(source, options);

    final String dir = source.dir();
    root = dir.endsWith("/") ? dir : dir + '/';
    skipCorrupt = options.get(MainOptions.SKIPCORRUPT);
    archives = options.get(MainOptions.ADDARCHIVES);
    addRaw = options.get(MainOptions.ADDRAW);
    dtd = options.get(MainOptions.DTD);
    rawParser = options.get(MainOptions.PARSER) == MainParser.RAW;
    filter = !source.isDir() && !source.isArchive() ? null :
      Pattern.compile(IOFile.regex(options.get(MainOptions.CREATEFILTER)));
  }

  /**
   * Constructor.
   * @param source source path
   * @param context database context
   * @param path future database path
   */
  public DirParser(final IO source, final Context context, final IOFile path) {
    this(source, context.options);
    if(path != null && (addRaw || rawParser)) rawPath = new IOFile(path, IO.RAW);
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
    if(io instanceof IOFile && io.isDir()) {
      for(final IO f : ((IOFile) io).children()) parse(b, f);
    } else if(archives && io.isArchive()) {
      final String name = io.name().toLowerCase(Locale.ENGLISH);
      InputStream in = io.inputStream();
      if(name.endsWith(IO.TARSUFFIX) || name.endsWith(IO.TGZSUFFIX) ||
          name.endsWith(IO.TARGZSUFFIX)) {
        // process TAR files
        if(!name.endsWith(IO.TARSUFFIX)) in = new GZIPInputStream(in);
        try(final TarInputStream is = new TarInputStream(in)) {
          for(TarEntry ze; (ze = is.getNextEntry()) != null;) {
            if(ze.isDirectory()) continue;
            src = new IOStream(is, ze.getName());
            src.length(ze.getSize());
            parseResource(b);
          }
        }
      } else if(name.endsWith(IO.GZSUFFIX)) {
        // process GZIP archive
        try(final GZIPInputStream is = new GZIPInputStream(in)) {
          src = new IOStream(is, io.name().replaceAll("\\..*", IO.XMLSUFFIX));
          parseResource(b);
        }
      } else {
        // process ZIP archive
        try(final ZipInputStream is = new ZipInputStream(in)) {
          for(ZipEntry ze; (ze = is.getNextEntry()) != null;) {
            if(ze.isDirectory()) continue;
            src = new IOStream(is, ze.getName());
            src.length(ze.getSize());
            parseResource(b);
          }
        }
      }
    } else {
      // process regular file
      src = io;
      parseResource(b);
    }
  }

  /**
   * Parses the current source.
   * @param b builder instance
   * @throws IOException I/O exception
   */
  private void parseResource(final Builder b) throws IOException {
    b.checkStop();

    // add file size for database meta information
    final long l = src.length();
    if(l != -1) b.meta.filesize += l;

    // use global target as path prefix
    String targ = target;
    String path = src.path();

    // add relative path without root (prefix) and file name (suffix)
    final String name = src.name();
    if(path.endsWith('/' + name)) {
      path = path.substring(0, path.length() - name.length());
      if(path.startsWith(root)) path = path.substring(root.length());
      targ = (targ + path).replace("//", "/");
    }

    // check if file passes the name filter pattern
    boolean exclude = false;
    if(filter != null) {
      final String nm = Prop.CASE ? src.name() : src.name().toLowerCase(Locale.ENGLISH);
      exclude = !filter.matcher(nm).matches();
    }

    if(exclude) {
      // exclude file: check if will be added as raw file
      if(addRaw && rawPath != null) {
        Store.store(src.inputSource(), new IOFile(rawPath, targ + name));
      }
    } else {
      if(rawParser) {
        // store input in raw format if database path is known
        if(rawPath != null) {
          Store.store(src.inputSource(), new IOFile(rawPath, targ + name));
        }
      } else {
        // store input as XML
        boolean ok = true;
        IO in = src;
        if(skipCorrupt) {
          // parse file twice to ensure that it is well-formed
          try {
            // cache file contents to allow or speed up a second run
            if(!(src instanceof IOContent || dtd)) {
              in = new IOContent(src.read());
              in.name(src.name());
            }
            parser = Parser.singleParser(in, options, targ);
            MemBuilder.build("", parser);
          } catch(final IOException ex) {
            Util.debug(ex);
            skipped.add(src.path());
            ok = false;
          }
        }

        // parse file
        if(ok) {
          parser = Parser.singleParser(in, options, targ);
          parser.parse(b);
        }
        parser = null;
        // dump debug data
        if(Prop.debug && (++c & 0x3FF) == 0) Util.err(";");
      }
    }
  }

  @Override
  public String info() {
    final TokenBuilder tb = new TokenBuilder();
    if(!skipped.isEmpty()) {
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
    return parser != null ? parser.detail() : src.path();
  }

  @Override
  public double prog() {
    if(parser != null) return parser.progress();
    if(lastSrc == src) return 1;
    lastSrc = src;
    return Math.random();
  }

  @Override
  public void close() throws IOException {
    if(parser != null) parser.close();
  }
}
