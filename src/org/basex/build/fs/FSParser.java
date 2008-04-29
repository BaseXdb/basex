package org.basex.build.fs;

import static org.basex.build.fs.FSText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.basex.BaseX;
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
import org.basex.core.proc.Create;
import org.basex.io.IO;
import org.basex.util.Array;

/** Imports/shreds/parses a file hierarchy into a BaseX database.
 *
 * The overall process of importing a file hierarchy can be described
 * as follows:
 * <ol>
 * <li>The import is invoked by the {@link Create} command and its
 * {@link Create} fs() function.  To import on the command line type:
 * <tt>$ create fs [dbname] [path]</tt>
 * </li>
 * <li>This class {@link FSParser} instantiates the needed components
 * for the import process in its {@link FSParser#parse(Builder)} method.
 * The components are:
 *  <ol>
 *    <li>the file hierarchy traversal engine ({@link FSWalker} and</li>
 *    <li>as many visitors ({@link FSVisitor}) as needed to receive
 *    events during the traversal.  In this case this class {@link FSParser}
 *    also implements the interface {@link FSVisitor} and as such is
 *    the essential one to shred/import the file hierarchy into an XML
 *    hierarchy.</li>
 *  </ol>
 * </ol>
 *
 * @see FSWalker
 * @see FSVisitor
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Alexander Holupirek
 */
public final class FSParser extends Parser implements FSVisitor {
  /** The current File being processed. */
  private String guimsg = "Importing files ...";
  /** Meta data index. */
  private final ExtractorIndex meta = new ExtractorIndex();
  /** Cache for content indexing. */
  private byte[] cache;
  /** Reference to the database builder. */
  private Builder builder;
  /** Cached attribute array. */
  private static final byte[][] ATS1 = { NAME, null, SUFFIX, null, SIZE, null,
      MTIME, null };
  /** Cached attribute array. */
  private static final byte[][] ATS2 = { NAME, null, SUFFIX, null, SIZE, null };

  /**
   * Constructor.
   * @param path the traversal starts from (enter "/" or leave empty to parse
   * all partitions (C:, D: ...) on Windows)
   */
  public FSParser(final IO path) {
    super(path);

    // initialize cache for textual contents
    if(Prop.fscont) cache = new byte[Prop.fstextmax];

    meta.put(TYPEGIF, new GIFExtractor());
    meta.put(TYPEPNG, new PNGExtractor());
    meta.put(TYPEJPG, new JPGExtractor());
    meta.put(TYPEJPEG, new JPGExtractor());
    meta.put(TYPEBMP, new BMPExtractor());
    meta.put(TYPEGIF, new TIFExtractor());
    meta.put(TYPEMP3, new MP3Extractor());
    meta.put(TYPEEML, new EMLExtractor());
    meta.put(TYPEMBS, new EMLExtractor());
    meta.put(TYPEMBX, new EMLExtractor());
  }

  /**
   * {@inheritDoc}
   */
  public void preTraversal(final String path, final boolean docOpen)
      throws IOException {
    if(docOpen) builder.startNode(DEEPFS, new byte[][] { });
    builder.startNode(DIR, new byte[][] { NAME, token(path) });
  }

  /**
   * {@inheritDoc}
   */
  public void preEvent(final File dir) throws IOException {
    guimsg = dir.toString();
    builder.startNode(DIR, atts(dir));
  }

  /**
   * {@inheritDoc}
   */
  public void postEvent() throws IOException {
    builder.endNode(DIR);
  }

  /**
   * {@inheritDoc}
   */
  public void regfileEvent(final File f) throws IOException {
    guimsg = f.toString();
    builder.startNode(FILE, atts(f));
    if (f.canRead()) {
      if(Prop.fsmeta && f.getName().indexOf('.') != -1) {
        final String name = f.getName();
        final int dot = name.lastIndexOf('.');
        final byte[] suffix = lc(token(name.substring(dot + 1)));

        final AbstractExtractor index = meta.get(suffix);
        if(index != null && f.length() != 0) {
          try {
            index.extract(builder, f);
          } catch(MetaDataException e) {
            BaseX.debug(e);
          }
        }
      }

      // import textual content
      if(Prop.fscont && f.isFile()) {
        final FileInputStream in = new FileInputStream(f);
        final int size = in.read(cache);
        in.close();

        int s = -1;
        while(++s < size) {
          final byte b = cache[s];
          if(b >= 0 && b < ' ' && !ws(b)) break;
        }
        if(s == size) {
          while(--s >= 0 && cache[s] <= 0x20 && cache[s] >= 0);
          if(++s != 0) builder.nodeAndText(CONTENT, Array.finish(cache, s));
        }
      }
    }
    builder.endNode(FILE);
  }

  /**
   * {@inheritDoc}
   */
  public void symlinkEvent(final File link) { }

  /**
   * {@inheritDoc}
   */
  public void postTraversal(final boolean docClose) throws IOException {
    builder.endNode(DIR);
    if(docClose) builder.endNode(DEEPFS);
  }

  @Override
  public String head() {
    return TRAVERSE;
  }

  @Override
  public String det() {
    return guimsg;
  }

  @Override
  public double percent() {
    return 0;
  }
  
  /**
   * Main entry point for the import of a file hierarchy to BaseX.
   * Instantiates fht engine and visitors, and starts the traversal.
   * @param build instance passed by {@link Create} fs().
   * @throws IOException I/O exception
   */
  @Override
  public void parse(final Builder build) throws IOException {
    builder = build;
    builder.encoding(Prop.ENCODING);
    
    final FSWalker f = new FSWalker();
    f.register(this);
    f.fileHierarchyTraversal(file);
  }

  /** Construct attributes for file and directory tags.
   * @param f file name
   * @return attributes as byte[][]
   */
  private byte[][] atts(final File f) {
    
    final String name = f.getName();
    final int s = name.lastIndexOf('.');
    // current time storage: minutes from 1.1.1970
    // (values will be smaller than 1GB and will thus be inlined in the storage)
    final long time = f.lastModified() / 60000;
    final byte[] suf = s != -1 ? lc(token(name.substring(s + 1))) : EMPTY;

    if(time == 0) {
      ATS2[1] = token(name);
      ATS2[3] = suf;
      ATS2[5] = token(f.length());
      return ATS2;
    }
    ATS1[1] = token(name);
    ATS1[3] = suf;
    ATS1[5] = token(f.length());
    ATS1[7] = token(time);
    return ATS1;
  }

}
