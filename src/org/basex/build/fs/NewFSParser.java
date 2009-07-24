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
import org.basex.build.fs.parser.ParserUtil;
import org.basex.build.fs.parser.Metadata.Attribute;
import org.basex.build.fs.parser.Metadata.DataType;
import org.basex.build.fs.parser.Metadata.Definition;
import org.basex.build.fs.parser.Metadata.Element;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;
import org.basex.core.Prop;
import org.basex.core.proc.CreateFS;
import org.basex.data.DataText;
import org.basex.io.IO;
import org.basex.util.Atts;

/**
 * Imports/shreds/parses a file hierarchy into a BaseX database.
 *
 * The overall process of importing a file hierarchy can be described as
 * follows:
 * <ol>
 * <li>The import is invoked by the {@link CreateFS} command. To import on the
 * command line type: <tt>$ create fs [path] [dbname]</tt></li>
 * <li>This class {@link NewFSParser} instantiates the needed components for the
 * import process in its {@link NewFSParser#parse(Builder)} method.
 * </ol>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 * @author Bastian Lemke
 */
public final class NewFSParser extends Parser {

  /** Registry for MetadataAdapter implementations. */
  static final Map<String, Class<? extends AbstractParser>> REGISTRY =
    new HashMap<String, Class<? extends AbstractParser>>();

  /**
   * Register a parser implementation with the fs parser.
   * @param suffix the suffix to register the parser implementation for.
   * @param c the parser implementation class.
   */
  public static void register(final String suffix,
      final Class<? extends AbstractParser> c) {
    REGISTRY.put(suffix, c);
  }

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
    } catch(final IOException e) {
      BaseX.errln("Failed to load parsers (%)", e.getMessage());
    }
  }

  // [BL] clean up class ...

  /** If true, verbose debug messages are created (e.g. for corrupt files). */
  public static final boolean VERBOSE = true;
  /** If true, the <code>type=""</code> attributes are added to the XML doc. */
  private static final boolean ADD_TYPE_ATTR = true;

  /** Offset of the size value, as stored in {@link #atts(File, boolean)}. */
  public static final int SIZEOFFSET = 3;
  /** Directory size Stack. */
  private final long[] sizeStack = new long[IO.MAXHEIGHT];
  /** Pre value stack. */
  private final int[] preStack = new int[IO.MAXHEIGHT];
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

  /**
   * Constructor.
   * @param path the traversal starts from
   * @param mp mount point for fuse
   * @param bs path to root of backing store for BLOBs on Windows systems. If
   *          set to true, the path reference is ignored
   */
  public NewFSParser(final String path, final String mp, final String bs) {
    super(IO.get(path));
    Prop.intparse = true;
    Prop.entity = false;
    Prop.dtd = false;
    root = path.equals("/");

    fsimportpath = io.path();
    fsdbname = io.name();
    backingroot = bs;
    mountpoint = mp;
    mybackingpath = backingroot + Prop.SEP + fsdbname;

    final int size = (int) Math.ceil(REGISTRY.size() / 0.75f);
    parserInstances = new HashMap<String, AbstractParser>(size);

    if(Prop.fsmeta || Prop.fscont) {
      buffer = ByteBuffer.allocateDirect(IO.BLOCKSIZE);
    } else {
      buffer = null;
    }
  }

  /**
   * Constructor to parse single file nodes.
   * @param path String to file node to parse
   */
  public NewFSParser(final String path) {
    // [AH] pass mountpoint and backing store args to single parser
    this(path, "/mnt/deepfs", "/var/tmp/deepfs");
    singlemode = true;
  }

  /**
   * Get a parser implementation for given file suffix.
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
      } catch(final InstantiationException e) {
        BaseX.debug("Failed to load parser for suffix " + suffix + " (%)",
            e.getMessage());
      } catch(final IllegalAccessException e) {
        BaseX.debug("Failed to load parser for suffix " + suffix + " (%)",
            e.getMessage());
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
    if(Prop.fuse && !singlemode) {
      final File bs = new File(mybackingpath);
      if(!bs.mkdirs() && bs.exists()) throw new IOException(BACKINGEXISTS
          + mybackingpath);
    }

    builder.startDoc(token(io.name()));

    if(singlemode) {
      file(new File(io.path()).getCanonicalFile());
    } else {
      atts.reset();
      final byte[] mnt = Prop.fuse ? token(mountpoint) : NOTMOUNTED;
      final byte[] bck = Prop.fuse ? token(mybackingpath) : token(fsimportpath);
      atts.add(MOUNTPOINT  , mnt);
      atts.add(SIZE        , EMPTY);
      atts.add(BACKINGSTORE, bck);

      if(ADD_TYPE_ATTR) builder.startNS(token("xsi"),
          token("http://www.w3.org/2001/XMLSchema-instance"));

      builder.startElem(DEEPFS, atts);

      for(final File f : root ? File.listRoots() : new File[] { new File(
          fsimportpath).getCanonicalFile()}) {

        importRootLength = f.getAbsolutePath().length();
        sizeStack[0] = 0;
        parse(f);
        builder.setAttValue(preStack[0] + SIZEOFFSET, token(sizeStack[0]));
      }
      builder.endElem(DEEPFS);
    }
    builder.endDoc();
  }

  /**
   * Visits files in a directory or steps further down.
   * @param d the directory to be visited.
   * @throws IOException I/O exception
   */
  private void parse(final File d) throws IOException {
    final File[] files = d.listFiles();
    if(files == null) return;

    for(final File f : files) {
      if(!valid(f)) continue;

      if(f.isDirectory()) {
        // -- 'copy' directory to backing store
        if(Prop.fuse) new File(mybackingpath
            + f.getAbsolutePath().substring(importRootLength)).mkdir();
        dir(f);
      } else {
        // -- copy file to backing store
        if(Prop.fuse) copy(f.getAbsoluteFile(), new File(mybackingpath
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
    } catch(final IOException e) {
      e.getMessage();
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
    preStack[++lvl] = builder.startElem(DIR, atts(f, false));
    sizeStack[lvl] = 0;
    parse(f);
    builder.endElem(DIR);

    // calling builder actualization
    // take into account that stored pre value is the one of the
    // element node, not the attributes one!
    final long size = sizeStack[lvl];
    builder.setAttValue(preStack[lvl] + SIZEOFFSET, token(size));

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
    if(!singlemode) builder.startElem(FILE, atts(f, false));
    if((Prop.fsmeta || Prop.fscont) && f.canRead() && f.isFile()
        && f.getName().indexOf('.') != -1) {
      final String name = f.getName();
      final int dot = name.lastIndexOf('.');
      final String suffix = name.substring(dot + 1).toLowerCase();

      if(f.length() > 0) {
        final AbstractParser parser = getParser(suffix);
        if(parser != null) {
          final BufferedFileChannel bfc = new BufferedFileChannel(f, buffer);
          try {
            parse0(parser, bfc);
          } catch(final IOException e) {
            BaseX.debug("NewFSParser: Failed to parse file metadata (%).",
                bfc.getFileName());
          } finally {
            try { bfc.close(); } catch(final IOException e1) { /* */ }
          }
        }
      }
    }

    if(!singlemode) builder.endElem(FILE);
    // add file size to parent folder
    sizeStack[lvl] += f.length();
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
    if(Prop.fsmeta || Prop.fscont) {
      final AbstractParser parser = getParser(suffix);
      if(parser == null) return;
      atts.reset();
      if(name != null) {
        final StringBuilder sb = new StringBuilder(name.length() +
            suffix.length() + 1).append(name).append('.').append(suffix);
        atts.add(DataText.NAME, token(sb.toString()));
      }
      if(suffix != null) atts.add(DataText.SUFFIX, token(suffix));
      atts.add(DataText.OFFSET, token(bfc.absolutePosition()));
      atts.add(DataText.SIZE, token(bfc.size()));
      atts.add(DataText.MTIME, ParserUtil.getMTime(curr));
      builder.startElem(DataText.FILE, atts);
      try {
        parse0(parser, bfc);
      } finally {
        builder.endElem(DataText.FILE);
      }
    }
  }

  /**
   * Starts the parser implementation.
   * @param parser the parser instance.
   * @param bfc the {@link BufferedFileChannel} to read from.
   * @throws IOException if any error occurs while reading from the file.
   */
  private void parse0(final AbstractParser parser,
      final BufferedFileChannel bfc) throws IOException {
    if(Prop.fsmeta) {
      atts.reset();
      if(ADD_TYPE_ATTR) atts.add(Attribute.TYPE.get(), DataType.STRING.get());
      builder.nodeAndText(Element.TYPE.get(), atts, parser.getType());
      builder.nodeAndText(Element.FORMAT.get(), atts, parser.getFormat());
      bfc.reset();
      parser.readMeta(bfc, this);
      bfc.finish();
    }
    if(Prop.fscont) {
      bfc.reset();
      parser.readContent(bfc, this);
      bfc.finish();
    }
  }

  /**
   * Constructs attributes for file and directory tags.
   * @param f file name
   * @param r root flag
   * @return attributes as byte[][]
   */
  private Atts atts(final File f, final boolean r) {
    final String name = r ? f.getPath() : f.getName();
    final int s = name.lastIndexOf('.');
    // (values will be smaller than 1GB and will thus be inlined in the storage)
    final byte[] suf = s != -1 ? lc(token(name.substring(s + 1))) : EMPTY;

    atts.reset();
    atts.add(NAME, token(name));
    atts.add(SUFFIX, suf);
    atts.add(SIZE, token(f.length()));
    final byte[] time = ParserUtil.getMTime(f);
    if(time != null) atts.add(MTIME, time);
    return atts;
  }

  @Override
  public String head() {
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

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Generates the xml representation for a name/value pair and adds it to the
   * current file element.
   * @param element the xml element to create.
   * @param t the type of the xml element.
   * @param definition the precise definition of the xml element.
   * @param language the language of the element.
   * @param value the value of the element.
   * @throws IOException if any error occurs while generating the xml code.
   */
  public void metaEvent(final Element element, final DataType t,
      final Definition definition, final byte[] language, final byte[] value)
      throws IOException {
    final byte[] data = trim(value);
    if(ws(data)) return;
    atts.reset();
    if(language != null) atts.add(Attribute.LANGUAGE.get(), language);
    if(definition != Definition.NONE) atts.add(Attribute.DEFINITION.get(),
        definition.get());
    if(ADD_TYPE_ATTR) atts.add(Attribute.TYPE.get(), t.get());
    builder.nodeAndText(element.get(), atts, data);
  }

  /**
   * Generates the xml representation for a new file inside the current file
   * node.
   * @param name the name of the file.
   * @param suffix the suffix of the file.
   * @param type the type of the file.
   * @param format the format of the file.
   * @param absolutePosition the absolute position of the first byte of the file
   *          inside the current file.
   * @param size the size of the file in bytes.
   * @throws IOException if any error occurs while reading from the file.
   */
  public void fileStartEvent(final String name, final String suffix,
      final Type type, final MimeType format, final long absolutePosition,
      final long size) throws IOException {
    atts.reset();
    if(name != null) {
      if(suffix != null) {
        final StringBuilder sb = new StringBuilder(name.length() +
            suffix.length() + 1).append(name).append('.').append(suffix);
        atts.add(DataText.NAME, token(sb.toString()));
        atts.add(DataText.SUFFIX, token(suffix));
      } else {
        atts.add(DataText.NAME, token(name));
      }
    }
    atts.add(DataText.OFFSET, token(absolutePosition));
    atts.add(DataText.SIZE, token(size));
    atts.add(DataText.MTIME, ParserUtil.getMTime(curr));
    builder.startElem(DataText.FILE, atts);
    atts.reset();
    if(ADD_TYPE_ATTR) atts.add(Attribute.TYPE.get(), DataType.STRING.get());
    builder.nodeAndText(Element.TYPE.get(), atts, type.get());
    builder.nodeAndText(Element.FORMAT.get(), atts, format.get());
  }

  /**
   * Closes the last opened file element.
   * @throws IOException if any error occurs while reading from the file.
   */
  public void fileEndEvent() throws IOException {
    builder.endElem(DataText.FILE);
  }

  /**
   * Parse a xml file.
   * @throws IOException if any error occurs while generating the xml code.
   */
  public void parseXML() throws IOException {
    final IO i = IO.get(curr.getPath());
    final Parser parser = Parser.xmlParser(i);
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
