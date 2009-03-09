package org.basex.build.fs;

import static org.basex.build.fs.FSText.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.Text;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.build.fs.metadata.AbstractExtractor;
import org.basex.build.fs.metadata.BMPExtractor;
import org.basex.build.fs.metadata.EMLExtractor;
import org.basex.build.fs.metadata.GIFExtractor;
import org.basex.build.fs.metadata.JPGExtractor;
import org.basex.build.fs.metadata.MP3Extractor;
import org.basex.build.fs.metadata.MetaDataException;
import org.basex.build.fs.metadata.PNGExtractor;
import org.basex.build.fs.metadata.TIFExtractor;
import org.basex.core.Prop;
import org.basex.core.proc.CreateFS;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.Atts;
import org.basex.util.Map;

/** Imports/shreds/parses a file hierarchy into a BaseX database.
 *
 * The overall process of importing a file hierarchy can be described
 * as follows:
 * <ol>
 * <li>The import is invoked by the {@link CreateFS} command.
 * To import on the command line type:
 * <tt>$ create fs [path] [dbname]</tt>
 * </li>
 * <li>This class {@link FSParser} instantiates the needed components
 * for the import process in its {@link FSParser#parse(Builder)} method.
 * </ol>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 */
public final class FSParser extends Parser {
  /** Offset of the size value, as stored in {@link #atts(File, boolean)}. */
  public static final int SIZEOFFSET = 3;
  /** Meta data index. */
  private final Map<AbstractExtractor> meta = new Map<AbstractExtractor>();
  /** Reference to the database builder. */
  private Builder builder;
  /** The currently processed file. */
  private File curr;
  /** Root flag. */
  private boolean root;
  /** Level counter. */
  private int lvl;
  /** Directory size Stack. */
  private final long[] sizeStack = new long[IO.MAXHEIGHT];
  /** Pre value stack. */
  private final int[] preStack = new int[IO.MAXHEIGHT];
  /** Do not expect complete file hierarchy, but parse single files. */
  private boolean singlemode;

  /**
   * Constructor.
   * @param path the traversal starts from
   * @param r root flag to parse root node or all partitions (C:, D: ...).
   * on Windows systems. If set to true, the path reference is ignored
   */
  public FSParser(final IO path, final boolean r) {
    super(path);
    root = r;

    meta.add(TYPEGIF, new GIFExtractor());
    meta.add(TYPEPNG, new PNGExtractor());
    meta.add(TYPEJPG, new JPGExtractor());
    meta.add(TYPEJPEG, new JPGExtractor());
    meta.add(TYPEBMP, new BMPExtractor());
    meta.add(TYPEGIF, new TIFExtractor());
    meta.add(TYPEMP3, new MP3Extractor());
    meta.add(TYPEEML, new EMLExtractor());
    meta.add(TYPEMBS, new EMLExtractor());
    meta.add(TYPEMBX, new EMLExtractor());
  }

  /** 
   * Constructor to parse single file nodes.
   * @param path String to file node to parse
   */
  public FSParser(final String path) {
    this(IO.get(path), false);
    singlemode = true;
  }
  
  /**
   * Main entry point for the import of a file hierarchy.
   * Instantiates the engine and starts the traversal.
   * @param build instance passed by {@link CreateFS}.
   * @throws IOException I/O exception
   */
  @Override
  public void parse(final Builder build) throws IOException {
    builder = build;
    builder.encoding(Prop.ENCODING);

    builder.startDoc(token(io.name()));

    if(singlemode) {
      file(new File(io.path()).getCanonicalFile());
    } else {
      builder.startElem(token(DEEPFS), atts.reset());
      
      for(final File f : root ? File.listRoots() :
        new File[] { new File(io.path()).getCanonicalFile() }) {
        
        preStack[0] = builder.startElem(DIR, atts(f, true));
        sizeStack[0] = 0;
        parse(f);
        builder.endElem(DIR);
        builder.setAttValue(preStack[0] + SIZEOFFSET, token(sizeStack[0]));
      }
      
      builder.endElem(token(DEEPFS));
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
        dir(f);
      } else {
        file(f);
      }
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
    } catch(final Exception ex) {
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
    if (!singlemode)
      builder.startElem(FILE, atts(f, false));
    if (f.canRead()) {
      if(Prop.fsmeta && f.getName().indexOf('.') != -1) {
        final String name = f.getName();
        final int dot = name.lastIndexOf('.');
        final byte[] suffix = lc(token(name.substring(dot + 1)));

        final AbstractExtractor index = meta.get(suffix);
        if(index != null && f.length() != 0) {
          try {
            index.extract(builder, f);
          } catch(final MetaDataException ex) {
            BaseX.debug(ex);
          }
        }
      }

      // import textual content
      if(Prop.fscont && f.isFile()) {
        // initialize cache for textual contents
        final int l = (int) Math.min(f.length(), Prop.fstextmax);
        final byte[] cc = new byte[l];
        BufferInput.read(f, cc);
        int c = -1;
        while(++c < l) {
          final byte b = cc[c];
          if(b >= 0 && b < ' ' && !ws(b)) break;
        }
        if(c == l) {
          while(--c >= 0 && cc[c] <= 0x20 && cc[c] >= 0);
          if(++c != 0) {
            builder.nodeAndText(CONTENT, atts.reset(), Array.finish(cc, c));
          }
        }
      }
    }

    if (!singlemode)
      builder.endElem(FILE);
    // add file size to parent folder
    sizeStack[lvl] += f.length();
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
    // current time storage: minutes from 1.1.1970
    // (values will be smaller than 1GB and will thus be inlined in the storage)
    final long time = f.lastModified() / 60000;
    final byte[] suf = s != -1 ? lc(token(name.substring(s + 1))) : EMPTY;

    atts.reset();
    atts.add(NAME, token(name));
    atts.add(SUFFIX, suf);
    atts.add(SIZE, token(f.length()));
    if(time != 0) atts.add(MTIME, token(time));
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
}
