package org.basex.examples.create;

import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.FileParser;
import org.deepfs.fs.DeepFS;

/**
 * This class recursively parses a directory in the filesystem
 * and sends events to the specified database builder.
 * The resulting XML representation resembles the DeepFS syntax.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class SimpleFSParser extends FileParser {
  /**
   * Constructor.
   * @param path file path
   */
  public SimpleFSParser(final String path) {
    super(path);
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(DeepFS.FSML, atts.reset());
    atts.add(DeepFS.BACKINGSTORE, token(file.toString()));
    builder.startElem(DeepFS.DEEPFS, atts);
    parse(new File(file.path()));
    builder.endElem(DeepFS.DEEPFS);
    builder.endElem(DeepFS.FSML);
    builder.meta.deepfs = true;
  }

  /**
   * Recursively parses the specified directory.
   * @param dir directory to be parsed
   * @throws IOException I/O exception
   */
  private void parse(final File dir) throws IOException {
    atts.reset();
    atts.add(DeepFS.NAME, token(dir.getName()));
    atts.add(DeepFS.MTIME, token(dir.lastModified()));
    builder.startElem(DeepFS.DIR, atts);
    for(final File f : dir.listFiles()) {
      if(f.isDirectory()) {
        parse(f);
      } else {
        atts.reset();
        final String name = f.getName();
        atts.add(DeepFS.NAME, token(name));
        atts.add(DeepFS.SIZE, token(f.length()));
        atts.add(DeepFS.MTIME, token(f.lastModified()));
        int i = name.lastIndexOf('.');
        if(i != -1) atts.add(DeepFS.SUFFIX, token(name.substring(i + 1)));
        builder.emptyElem(DeepFS.FILE, atts);
      }
    }
    builder.endElem(DeepFS.DIR);
  }
}
