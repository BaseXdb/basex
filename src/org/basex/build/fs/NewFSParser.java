package org.basex.build.fs;

import static org.basex.build.fs.FSText.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.basex.BaseX;
import org.basex.Text;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.build.fs.parser.AbstractParser;
import org.basex.build.fs.parser.BufferedFileChannel;
import org.basex.build.fs.parser.Loader;
import org.basex.build.fs.parser.Metadata;
import org.basex.build.fs.parser.ParserUtil;
import org.basex.build.fs.parser.Metadata.IntField;
import org.basex.core.Prop;
import org.basex.core.proc.CreateFS;
import org.basex.data.DataText;
import org.basex.io.IO;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;

/**
 * Imports/shreds/parses a file hierarchy into a database.
 *
 * The overall process of importing a file hierarchy can be described as
 * follows:
 * <ol>
 * <li>The import is invoked by the {@link CreateFS} command. To import on the
 * command line type: <tt>$ create fs [path] [dbname]</tt></li>
 * <li>This class {@link NewFSParser} instantiates the parsers to extract
 * metadata and content from files.
 * </ol>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 * @author Bastian Lemke
 */
public final class NewFSParser extends Parser {

  // ----- Namespaces ----------------------------------------------------------
  /** All namespaces used in {@link NewFSParser}. */
  public enum NS {
    /** XML schema namespace. */
    XS("xs", "http://www.w3.org/2001/XMLSchema"),
    /** XML schema instance namespace. */
    XSI("xsi", "http://www.w3.org/2001/XMLSchema-instance"),
    /** DeepFS filesystem namespace. */
    FS("fs", "http://www.deepfs.org/fs/1.0/"),
    /** DeepFS metadata namespace. */
    FSMETA("fsmeta", "http://www.deepfs.org/fsmeta/1.0/"),
    /** Dublin Core metadata terms namespace. */
    DCTERMS("dcterms", "http://purl.org/dc/terms/");

    /** The namespace prefix. */
    private byte[] prefix;
    /** The namespace URI. */
    private byte[] uri;

    /**
     * Initializes a namespace instance.
     * @param p the prefix.
     * @param u the URI.
     */
    NS(final String p, final String u) {
      prefix = token(p);
      uri = token(u);
    }

    @Override
    public String toString() {
      StringBuilder str = new StringBuilder("xmlns");
      if(prefix.length > 0) {
        str.append(':');
        str.append(string(prefix));
      }
      str.append("=\"");
      str.append(string(uri));
      str.append("\"");
      return str.toString();
    }

    // /**
    // * Returns the namespace's unique URI.
    // * @return the URI.
    // */
    // public byte[] uri() {
    // return uri;
    // }
    //
    // /**
    // * Returns the namespace's prefix.
    // * @return the prefix.
    // */
    // public byte[] prefix() {
    // return prefix;
    // }

    /**
     * Calls {@link Builder#startNS(byte[], byte[])}.
     * @param b the builder instance.
     */
    public void start(final Builder b) {
      b.startNS(prefix, uri);
    }

    /**
     * Converts the xml element into a byte array containing the correct
     * namespace prefix.
     * @param element the xml element to convert.
     * @return the converted element as byte array;
     */
    public byte[] tag(final String element) {
      if(prefix.length == 0) return token(element);
      return concat(prefix, new byte[] { ':'}, token(element));
    }
  }

  // ---------------------------------------------------------------------------

  /** Registry for MetadataAdapter implementations. */
  static final Map<String, Class<? extends AbstractParser>>
  /**/REGISTRY = new HashMap<String, Class<? extends AbstractParser>>();

  /**
   * Registers a parser implementation with the fs parser.
   * @param suffix the suffix to register the parser implementation for.
   * @param c the parser implementation class.
   */
  public static void register(final String suffix,
      final Class<? extends AbstractParser> c) {
    REGISTRY.put(suffix, c);
  }

  /** Spotlight extractor. */
  private SpotlightExtractor spotlight;

  static {
    try {
      final Class<?>[] classes = Loader.load(AbstractParser.class.getPackage(),
          Pattern.compile("^\\w{1,5}Parser$"));
      for(final Class<?> c : classes) {
        final String name = c.getSimpleName();
        if(REGISTRY.containsValue(c)) {
          BaseX.debug("Successfully loaded %", name);
        } else BaseX.debug("Loading % ... FAILED", name);
      }
    } catch(final IOException ex) {
      BaseX.errln("Failed to load parsers (%)", ex.getMessage());
    }
  }

  /** If true, verbose debug messages are created (e.g. for corrupt files). */
  public static final boolean VERBOSE = true;
  /** If true, the <code>type=""</code> attributes are added to the XML doc. */
  private static final boolean ADD_ATTS = true;

  /** Empty attribute array. */
  private static final Atts EMPTY_ATTS = new Atts();
  /** Directory size Stack. */
  private final long[] sizeStack = new long[IO.MAXHEIGHT];
  /** Path to root of the backing store. */
  private final String fsimportpath;
  /** Name of the database and backingroot sub directory. */
  private final String fsdbname;
  /** Path to root of the backing store. */
  private final String backingroot;
  /** Path to FUSE mountpoint. */
  public final String mountpoint;
  /** MetadataAdapter registry. */
  private final Map<String, AbstractParser> parserInstances;
  /** The buffer to use for parsing the file contents. */
  private final ByteBuffer buffer;

  /** Root flag to parse root node or all partitions (C:, D: ...). */
  private final boolean root;
  /** Path to root of the backing store for this import. */
  public final String mybackingpath;
  /** Reference to the database builder. */
  private Builder builder;
  /** The currently processed file. */
  private File curr;
  /** Level counter. */
  private int lvl;
  /**
   * Length of absolute pathname of the root directory the import starts from,
   * i.e. the prefix to be chopped from path and substituted by backingroot.
   */
  private int importRootLength;
  /** Do not expect complete file hierarchy, but parse single files. */
  private boolean singlemode;

  /** Metadata item. */
  Metadata meta = new Metadata();

  /**
   * Constructor.
   * @param path the traversal starts from
   * @param mp mount point for fuse
   * @param bs path to root of backing store for BLOBs on Windows systems. If
   *          set to true, the path reference is ignored
   * @param pr database properties
   */
  public NewFSParser(final String path, final String mp, final String bs,
      final Prop pr) {
    super(IO.get(path), pr);
    prop.set(Prop.INTPARSE, true);
    prop.set(Prop.ENTITY, false);
    prop.set(Prop.DTD, false);
    root = path.equals("/");

    fsimportpath = io.path();
    fsdbname = io.name();
    backingroot = bs;
    mountpoint = mp;
    mybackingpath = backingroot + Prop.SEP + fsdbname;

    if(prop.is(Prop.FSMETA) || prop.is(Prop.FSCONT)) {
      if(prop.is(Prop.SPOTLIGHT)) {
        spotlight = new SpotlightExtractor(this);
      } else {
        final int size = (int) Math.ceil(REGISTRY.size() / 0.75f);
        parserInstances = new HashMap<String, AbstractParser>(size);
        buffer = ByteBuffer.allocateDirect(IO.BLOCKSIZE);
        return;
      }
    }
    parserInstances = null;
    buffer = null;
  }

  /**
   * Constructor to parse single file nodes.
   * @param path String to file node to parse
   * @param pr database properties
   */
  public NewFSParser(final String path, final Prop pr) {
    // [AH] pass mountpoint and backing store args to single parser
    this(path, "/mnt/deepfs", "/var/tmp/deepfs", pr);
    singlemode = true;
  }

  /**
   * Gets a parser implementation for given file suffix.
   * @param suffix the file suffix to get the parser for.
   * @return the parser implementation or <code>null</code> if no implementation
   *         is available.
   */
  private AbstractParser getParser(final String suffix) {
    AbstractParser instance = parserInstances.get(suffix);
    if(instance == null) {
      final Class<? extends AbstractParser> clazz = REGISTRY.get(suffix);
      if(clazz == null) return null;
      try {
        instance = clazz.newInstance();
        BaseX.debug("Successfully initialized parser for ." + suffix
            + " files.");
      } catch(final InstantiationException ex) {
        BaseX.debug("Failed to load parser for suffix " + suffix + " (%)",
            ex.getMessage());
      } catch(final IllegalAccessException ex) {
        BaseX.debug("Failed to load parser for suffix " + suffix + " (%)",
            ex.getMessage());
      }
      // put in hash map ... even if null
      parserInstances.put(suffix, instance);
    }
    return instance;
  }

  /**
   * Main entry point for the import of a file hierarchy. Instantiates the
   * engine and starts the traversal.
   * @param build instance passed by {@link CreateFS}.
   * @throws IOException I/O exception
   */
  @Override
  public void parse(final Builder build) throws IOException {
    builder = build;
    builder.encoding(Prop.ENCODING);

    builder.meta.backingpath = mybackingpath;
    builder.meta.mountpoint = mountpoint;

    // -- create backing store (DeepFS depends on it).
    final boolean fuse = prop.is(Prop.FUSE);
    if(fuse && !singlemode) {
      final File bs = new File(mybackingpath);
      if(!bs.mkdirs() && bs.exists()) throw new IOException(BACKINGEXISTS
          + mybackingpath);
    }

    builder.startDoc(token(io.name()));

    if(singlemode) {
      file(new File(io.path()).getCanonicalFile());
    } else {
      atts.reset();
      final byte[] mnt = fuse ? token(mountpoint) : NOTMOUNTED;
      final byte[] bck = fuse ? token(mybackingpath) : token(fsimportpath);
      atts.add(MOUNTPOINT, mnt);
      atts.add(BACKINGSTORE, bck);

      // define namespaces
      NS.FS.start(builder);
      if(prop.is(Prop.FSMETA)) {
        NS.FSMETA.start(builder);
        NS.DCTERMS.start(builder);
      }
      if(ADD_ATTS) {
        NS.XSI.start(builder);
      }

      builder.startElem(DEEPFS, atts);

      for(final File f : root ? File.listRoots() : new File[] { new File(
          fsimportpath).getCanonicalFile()}) {

        importRootLength = f.getAbsolutePath().length();
        sizeStack[0] = 0;
        parse(f);
        addFSAtts(f, sizeStack[0]);
      }
      builder.endElem(DEEPFS);
    }
    builder.endDoc();
  }

  /**
   * Adds the size node to the current node.
   * @param f the current file.
   * @param size the size to set.
   * @throws IOException I/O exception.
   */
  private void addFSAtts(final File f, final long size) throws IOException {
    meta.setLong(IntField.FS_SIZE, size);
    metaEvent(meta);
    ParserUtil.fireDateEvents(this, meta, f);
  }

  /**
   * Visits files in a directory or steps further down.
   * @param d the directory to be visited.
   * @throws IOException I/O exception
   */
  private void parse(final File d) throws IOException {
    final File[] files = d.listFiles();
    if(files == null) return;

    final boolean fuse = prop.is(Prop.FUSE);
    for(final File f : files) {
      if(!valid(f)) continue;

      if(f.isDirectory()) {
        // -- 'copy' directory to backing store
        if(fuse) new File(mybackingpath
            + f.getAbsolutePath().substring(importRootLength)).mkdir();
        dir(f);
      } else {
        // -- copy file to backing store
        if(fuse) copy(f.getAbsoluteFile(), new File(mybackingpath
            + f.getAbsolutePath().substring(importRootLength)));
        file(f);
      }
    }
  }

  /**
   * Copies a file to the backing store.
   * @param src file source
   * @param dst file destination in backing store
   */
  private void copy(final File src, final File dst) {
    try {
      final FileChannel chIn = new FileInputStream(src).getChannel();
      final FileChannel chOut = new FileOutputStream(dst).getChannel();
      chIn.transferTo(0, chIn.size(), chOut);
      chIn.close();
      chOut.close();
    } catch(final IOException ex) {
      ex.getMessage();
    }
  }

  /**
   * Determines if the specified file is valid and no symbolic link.
   * @param f file to be tested.
   * @return true for a symbolic link
   */
  private static boolean valid(final File f) {
    try {
      return f.getPath().equals(f.getCanonicalPath());
    } catch(final IOException ex) {
      BaseX.debug(f + ": " + ex.getMessage());
      return false;
    }
  }

  /**
   * Invoked when a directory is visited.
   * @param f file name
   * @throws IOException I/O exception
   */
  private void dir(final File f) throws IOException {
    atts.reset();
    atts.add(NAME, token(f.getName()));
    builder.startElem(DIR, atts);
    sizeStack[++lvl] = 0;
    parse(f);

    // calling builder actualization
    // take into account that stored pre value is the one of the
    // element node, not the attributes one!
    final long size = sizeStack[lvl];
    addFSAtts(f, size);

    builder.endElem(DIR);

    // add file size to parent folder
    sizeStack[--lvl] += size;
  }

  /**
   * Invoked when a regular file is visited.
   * @param f file name
   * @throws IOException I/O exception
   */
  private void file(final File f) throws IOException {
    curr = f;
    final long size = f.length();

    if(!singlemode) {
      atts.reset();
      final String name = f.getName();
      atts.add(NAME, token(name));
      builder.startElem(FILE, atts);
      if((prop.is(Prop.FSMETA) || prop.is(Prop.FSCONT)) && f.canRead()
          && f.isFile()) {
        if(prop.is(Prop.SPOTLIGHT)) {
          spotlight.parse(f);
        } else if(name.indexOf('.') != -1) { // internal parser
          final int dot = name.lastIndexOf('.');
          final String suffix = name.substring(dot + 1).toLowerCase();
          if(size > 0) {
            final AbstractParser parser = getParser(suffix);
            if(parser != null) {
              final BufferedFileChannel fc = new BufferedFileChannel(f, buffer);
              try {
                parse0(parser, fc);
              } catch(final IOException e) {
                BaseX.debug("NewFSParser: Failed to parse file metadata (%).",
                    fc.getFileName());
              } finally {
                try {
                  fc.close();
                } catch(final IOException e1) { /* */}
              }
            }
          }
          addFSAtts(f, size); // only for internal parser
        } // end internal parser
      }
      builder.endElem(FILE);
    }
    // add file size to parent folder
    sizeStack[lvl] += size;
  }

  /**
   * Parses a fragment of a file.
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @param name the filename (without suffix!).
   * @param suffix the file suffix.
   * @throws IOException if any error occurs while reading from the file.
   */
  public void parseFileFragment(final BufferedFileChannel bfc,
      final String name, final String suffix) throws IOException {
    if(prop.is(Prop.FSMETA) || prop.is(Prop.FSCONT)) {
      final AbstractParser parser = getParser(suffix);
      final long offset = bfc.absolutePosition();
      final long size = bfc.size();
      fileStartEvent(name, suffix, offset);
      if(parser != null) {
        try {
          parse0(parser, bfc);
        } catch(final IOException ex) {
          BaseX.debug(
              "Failed to parse file fragment (file: %, offset: %, length: %)",
              bfc.getFileName(), offset, size);
          bfc.finish();
        }
      }
      fileEndEvent(size);
    }
  }

  /**
   * Starts the parser implementation.
   * @param parser the parser instance.
   * @param bf the {@link BufferedFileChannel} to read from.
   * @throws IOException if any error occurs while reading from the file.
   */
  private void parse0(final AbstractParser parser, final BufferedFileChannel bf)
      throws IOException {
    bf.reset();
    if(prop.is(Prop.FSMETA)) {
      if(prop.is(Prop.FSCONT)) parser.readMetaAndContent(bf, this);
      else parser.readMeta(bf, this);
    } else if(prop.is(Prop.FSCONT)) parser.readContent(bf, this);
    bf.finish();
  }

  @Override
  public String tit() {
    return Text.IMPORTPROG;
  }

  @Override
  public String det() {
    return curr != null ? curr.toString() : "";
  }

  @Override
  public double prog() {
    return 0;
  }

  /**
   * Deletes a non-empty directory.
   * @param dir to be deleted.
   * @return boolean true for success, false for failure.
   * */
  public static boolean deleteDir(final File dir) {
    if(dir.isDirectory()) {
      for(final String child : dir.list()) {
        if(!deleteDir(new File(dir, child))) return false;
      }
    }
    return dir.delete();
  }

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Generates the xml representation for a key-value pair and adds it to the
   * current file element.
   * @param m the {@link Metadata} object containing all metadata information.
   * @throws IOException if any error occurs while generating the xml code.
   */
  public void metaEvent(final Metadata m) throws IOException {
    final byte[] data = m.getValue();
    if(ws(data)) return;
    builder.nodeAndText(m.getKey(), ADD_ATTS ? m.getAtts() : EMPTY_ATTS, data);
  }

  /**
   * Generates the xml representation for a new file inside the current file
   * node.
   * @param name the name of the file.
   * @param suffix the suffix of the file.
   * @param offset the absolute position of the first byte of the file inside
   *          the current file.
   * @throws IOException if any error occurs while reading from the file.
   */
  public void fileStartEvent(final String name, final String suffix,
      final long offset) throws IOException {
    atts.reset();
    final byte[] n = name == null ? UNKNOWN : token(name);
    final int suffLen = suffix == null ? 0 : suffix.length() + 1;
    final TokenBuilder tb = new TokenBuilder(n.length + suffLen);
    tb.add(n);
    if(suffix != null) tb.add('.').add(suffix);
    atts.add(NAME, tb.finish());
    atts.add(OFFSET, token(offset));
    builder.startElem(FILE, atts);
  }

  /**
   * Closes the last opened file element.
   * @param size the size of the file in bytes.
   * @throws IOException if any error occurs while reading from the file.
   */
  public void fileEndEvent(final long size) throws IOException {
    addFSAtts(curr, size);
    builder.endElem(DataText.FILE);
  }

  /**
   * Parses a xml file.
   * @throws IOException if any error occurs while generating the xml code.
   */
  public void parseXML() throws IOException {
    final IO i = IO.get(curr.getPath());
    final Parser parser = Parser.xmlParser(i, prop);
    parser.doc = false;
    parser.parse(builder);
  }

  /**
   * Checks if a parser for the given suffix is available and the file is in the
   * correct format.
   * @param f the {@link BufferedFileChannel} to check.
   * @param suffix the file suffix.
   * @return true if the data is supported.
   * @throws IOException if any error occurs while reading from the file.
   */
  public boolean isParseable(final BufferedFileChannel f, final String suffix)
      throws IOException {
    final AbstractParser parser = getParser(suffix);
    if(parser == null) return false;
    final long pos = f.position();
    final boolean res = parser.check(f);
    f.position(pos);
    return res;
  }
}
