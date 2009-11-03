package org.basex.build.fs;

import static org.basex.build.fs.FSText.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
import org.basex.build.fs.metadata.XMLExtractor;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.proc.CreateFS;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.util.Map;
import org.basex.util.Token;
import org.deepfs.fs.DeepFS;

/**
 * Imports/shreds/parses a file hierarchy into a database.
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
  /** Directory size Stack. */
  private final long[] sizeStack = new long[IO.MAXHEIGHT];
  /** Pre value stack. */
  private final int[] preStack = new int[IO.MAXHEIGHT];
  /** Path to root of the backing store. */
  private final String backingpath;
  /** Meta data index. */
  private final Map<AbstractExtractor> meta = new Map<AbstractExtractor>();
  /** Root flag to parse root node or all partitions (C:, D: ...). */
  private final boolean root;
  /** Reference to the database builder. */
  private Builder builder;
  /** The currently processed file. */
  private File curr;
  /** Level counter. */
  private int lvl;
  /** Do not expect complete file hierarchy, but parse single files. */
  private boolean singlemode;

  /**
   * Constructor.
   * @param path the traversal starts from
   * @param pr database properties
   * on Windows systems. If set to true, the path reference is ignored
   */
  public FSParser(final String path, final Prop pr) {
    super(IO.get(path), pr);
    prop.set(Prop.INTPARSE, true);
    prop.set(Prop.ENTITY, false);
    prop.set(Prop.DTD, false);
    root = path.equals("/");

    backingpath = io.path();

    meta.add(TYPEGIF, new GIFExtractor());
    meta.add(TYPEPNG, new PNGExtractor());
    meta.add(TYPEJPG, new JPGExtractor());
    meta.add(TYPEJPEG, new JPGExtractor());
    meta.add(TYPEBMP, new BMPExtractor());
    meta.add(TYPETIF, new TIFExtractor());
    meta.add(TYPETIFF, new TIFExtractor());
    meta.add(TYPEMP3, new MP3Extractor());
    meta.add(TYPEEML, new EMLExtractor());
    meta.add(TYPEMBS, new EMLExtractor());
    meta.add(TYPEMBX, new EMLExtractor());
    meta.add(TYPEXML, new XMLExtractor());
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
    builder.meta.backing = backingpath;
    builder.meta.deepfs = true;
    builder.startDoc(token(io.name()));

    if(singlemode) {
      file(new File(io.path()).getCanonicalFile());
    } else {
      final byte[] bck = token(backingpath);
      atts.reset();
      atts.add(MOUNTPOINT  , NOTMOUNTED);
      atts.add(SIZE        , Token.EMPTY);
      atts.add(BACKINGSTORE, bck);

      builder.startElem(DEEPFS, atts);

      for(final File f : root ? File.listRoots() :
        new File[] { new File(backingpath).getCanonicalFile() }) {

        sizeStack[0] = 0;
        parse(f);
        builder.setAttValue(preStack[0] + DeepFS.getSizeOffset()
            , token(sizeStack[0]));
      }
      builder.endElem(DEEPFS);
    }
    builder.endDoc();
  }

  /**
   * Visits files in a directory or steps further down.
   * @param d the directory to be visited.
   * @throws IOException on failure
   */
  private void parse(final File d) throws IOException {
    final File[] files = d.listFiles();
    if(files == null) return;

    for(final File f : files) {
      if(!valid(f)) continue;
      if(f.isDirectory()) dir(f);
      else file(f);
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
      Main.debug(f + ": " + ex.getMessage());
      return false;
    }
  }

  /**
   * Invoked when a directory is visited.
   * @param f file name
   * @throws IOException I/O exception
   */
  private void dir(final File f) throws IOException {
    preStack[++lvl] = builder.startElem(DIR, DeepFS.atts(f));
    sizeStack[lvl] = 0;
    parse(f);
    builder.endElem(DIR);

    // calling builder actualization
    // take into account that stored pre value is the one of the
    // element node, not the attributes one!
    final long size = sizeStack[lvl];
    builder.setAttValue(preStack[lvl] + DeepFS.getSizeOffset(), token(size));

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
    if(!singlemode)
      builder.startElem(FILE, DeepFS.atts(f));
    if(f.canRead()) {
      if(prop.is(Prop.FSMETA) && f.getName().indexOf('.') != -1) {
        final String name = f.getName();
        final int dot = name.lastIndexOf('.');
        final byte[] suffix = lc(token(name.substring(dot + 1)));

        final AbstractExtractor index = meta.get(suffix);
        if(index != null && f.length() != 0) {
          try {
            index.extract(builder, f);
          } catch(final MetaDataException ex) {
            Main.debug(ex);
          }
        }
      }

      // import textual content
      if(prop.is(Prop.FSCONT) && f.isFile()) {
        // initialize cache for textual contents
        final int l = (int) Math.min(f.length(), prop.num(Prop.FSTEXTMAX));
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
            builder.nodeAndText(CONTENT, atts.reset(), Arrays.copyOf(cc, c));
          }
        }
      }
    }

    if(!singlemode)
      builder.endElem(FILE);
    // add file size to parent folder
    sizeStack[lvl] += f.length();
    /*
    for (long i : sizeStack)
      System.err.print(i + " ");
    System.err.println("--");
    */
  }

  @Override
  public String tit() {
    return Text.CREATEFSPROG;
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
