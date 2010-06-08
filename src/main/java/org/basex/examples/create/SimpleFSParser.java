package org.basex.examples.create;

import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.deepfs.fs.DeepFS;

/**
 * This class recursively parses a directory in the filesystem
 * and sends events to the specified database builder.
 * The resulting XML representation resembles the DeepFS syntax.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SimpleFSParser extends Parser {
  /**
   * Constructor.
   * @param path file path
   */
  public SimpleFSParser(final String path) {
    super(path);
  }

  @Override
  public void parse(final Builder b) throws IOException {
    b.startDoc(token(file.name()));
    b.startElem(DeepFS.FSML, atts.reset());
    atts.add(DeepFS.BACKINGSTORE, token(file.toString()));
    b.startElem(DeepFS.DEEPFS, atts);
    parse(new File(file.path()), b);
    b.endElem(DeepFS.DEEPFS);
    b.endElem(DeepFS.FSML);
    b.endDoc();
    b.meta.deepfs = true;
  }

  /**
   * Recursively parses the specified directory.
   * @param dir directory to be parsed
   * @param b builder instance
   * @throws IOException I/O exception
   */
  private void parse(final File dir, final Builder b) throws IOException {
    atts.reset();
    atts.add(DeepFS.NAME, token(dir.getName()));
    atts.add(DeepFS.MTIME, token(dir.lastModified()));
    b.startElem(DeepFS.DIR, atts);
    for(final File f : dir.listFiles()) {
      if(f.isDirectory()) {
        parse(f, b);
      } else {
        atts.reset();
        final String name = f.getName();
        atts.add(DeepFS.NAME, token(name));
        atts.add(DeepFS.SIZE, token(f.length()));
        atts.add(DeepFS.MTIME, token(f.lastModified()));
        int i = name.lastIndexOf('.');
        if(i != -1) atts.add(DeepFS.SUFFIX, token(name.substring(i + 1)));
        b.emptyElem(DeepFS.FILE, atts);
      }
    }
    b.endElem(DeepFS.DIR);
  }
}
