package org.basex.build;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class recursively scans files and directories and parses all
 * relevant files.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DirParser extends Parser {
  /** Number of skipped files to log. */
  private static final int SKIPLOG = 10;
  /** Skipped files. */
  private final StringList skipped = new StringList();
  /** File pattern. */
  private final Pattern filter;
  /** Root directory. */
  private final String dir;
  /** Original path. */
  private final String original;

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
  /** Archive name. */
  private final boolean archiveName;

  /** Last source. */
  private IO lastSrc;
  /** Parser reference. */
  private Parser parser;
  /** Resource counter. */
  private int resources;

  /**
   * Constructor.
   * @param source source path
   * @param options main options
   */
  public DirParser(final IO source, final MainOptions options) {
    super(source, options);

    final boolean isDir = source.isDir();
    if(isDir) {
      dir = source.path().replaceAll("/$", "") + '/';
      original = dir;
    } else {
      dir = source.dir();
      original = source.path();
    }
    skipCorrupt = options.get(MainOptions.SKIPCORRUPT);
    archives = options.get(MainOptions.ADDARCHIVES);
    archiveName = options.get(MainOptions.ARCHIVENAME);
    addRaw = options.get(MainOptions.ADDRAW);
    dtd = options.get(MainOptions.DTD);
    rawParser = options.get(MainOptions.PARSER) == MainParser.RAW;
    filter = !isDir && !source.isArchive() ? null :
      Pattern.compile(IOFile.regex(options.get(MainOptions.CREATEFILTER)));
  }

  @Override
  public void parse(final Builder build) throws IOException {
    build.meta.inputsize = 0;
    build.meta.original = original;
    parse(build, source);
  }

  /**
   * Parses the specified file or its children.
   * @param builder builder
   * @param input current input
   * @throws IOException I/O exception
   */
  private void parse(final Builder builder, final IO input) throws IOException {
    if(input instanceof IOFile && input.isDir()) {
      for(final IO f : ((IOFile) input).children()) parse(builder, f);
    } else if(archives && input.isArchive()) {
      String name = input.name().toLowerCase(Locale.ENGLISH);
      InputStream in = input.inputStream();
      if(name.endsWith(IO.TARSUFFIX) || name.endsWith(IO.TGZSUFFIX) ||
          name.endsWith(IO.TARGZSUFFIX)) {
        // process TAR files
        if(!name.endsWith(IO.TARSUFFIX)) in = new GZIPInputStream(in);
        try(TarInputStream is = new TarInputStream(in)) {
          for(TarEntry te; (te = is.getNextEntry()) != null;) {
            if(te.isDirectory()) continue;
            source = newStream(is, te.getName(), input);
            source.length(te.getSize());
            parseResource(builder);
          }
        }
      } else if(name.endsWith(IO.GZSUFFIX)) {
        // process GZIP archive
        try(GZIPInputStream is = new GZIPInputStream(in)) {
          // generate filename (the optional filename cannot be retrieve from the input stream):
          // drop archive suffix, add .xml if no suffix remains
          name = input.name().replaceAll("\\.[^.]+$", "");
          if(!Strings.contains(name, '.')) name += IO.XMLSUFFIX;
          source = newStream(is, name, input);
          parseResource(builder);
        }
      } else {
        // process ZIP archive
        try(ZipInputStream is = new ZipInputStream(in)) {
          for(ZipEntry ze; (ze = is.getNextEntry()) != null;) {
            if(ze.isDirectory()) continue;
            source = newStream(is, ze.getName(), input);
            source.length(ze.getSize());
            parseResource(builder);
          }
        } catch(final IllegalArgumentException ex) {
          // GH-1351: catch invalid archive encodings
          throw new IOException(ex);
        }
      }
    } else {
      // process regular file
      source = input;
      parseResource(builder);
    }
  }

  /**
   * Creates a new stream.
   * @param is input stream
   * @param path path inside archive
   * @param input input
   * @return stream
   */
  private IOStream newStream(final InputStream is, final String path, final IO input) {
    return new IOStream(is, archiveName ? (input.path() + '/' + path) : path);
  }

  /**
   * Parses the current source.
   * @param builder builder instance
   * @throws IOException I/O exception
   */
  private void parseResource(final Builder builder) throws IOException {
    builder.checkStop();

    // use global target as path prefix
    final String name = source.name();
    String targ = target;

    // add relative path without root (prefix) and file name (suffix)
    String path = source.path();
    if(path.endsWith('/' + name)) {
      path = path.substring(0, path.length() - name.length());
      if(path.startsWith(dir)) path = path.substring(dir.length());
      targ = (targ + path).replace("//", "/");
    }

    // check if file passes the name filter pattern
    final boolean include = filter == null ||
        filter.matcher(Prop.CASE ? name : name.toLowerCase(Locale.ENGLISH)).matches();

    if(include ? rawParser : addRaw) {
      // store input in raw format if raw parser was chosen, or if file was included otherwise
      builder.binary(targ + name, source);
    } else if(include) {
      // store input as XML
      boolean ok = true;
      IO in = source;
      if(skipCorrupt) {
        // parse file twice to ensure that it is well-formed
        try {
          // cache file contents to allow or speed up a second run
          if(!(source instanceof IOContent || dtd)) {
            in = new IOContent(source.read());
            in.name(name);
          }
          parser = Parser.singleParser(in, options, targ);
          MemBuilder.build("", parser);
        } catch(final IOException ex) {
          Util.debug(ex);
          skipped.add(source.path());
          ok = false;
        }
      }

      // parse file
      if(ok) {
        parser = Parser.singleParser(in, options, targ);
        parser.parse(builder);
      }
      parser = null;
    }

    // sum meta data file size
    final long l = source.length();
    if(l != -1) builder.meta.inputsize += l;

    // debugging: increment number of processed resources
    if(Prop.debug && (++resources & 0x3FF) == 0) Util.err(";");
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
  public String detailedInfo() {
    return parser != null ? parser.detailedInfo() : source.path();
  }

  @Override
  public double progressInfo() {
    if(parser != null) return parser.progressInfo();
    if(lastSrc == source) return 1;
    lastSrc = source;
    return Math.random();
  }

  @Override
  public void close() throws IOException {
    if(parser != null) parser.close();
  }
}
