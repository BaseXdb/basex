package org.basex.examples.create;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;

/**
 * This class serves as a simple filesystem parser and creates an XML
 * representation, which resembles the DeepFS markup.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SimpleFSParser extends Parser {
  /** FS Token. */
  private static final byte[] DIR = token("dir");
  /** FS Token. */
  private static final byte[] FILE = token("file");
  /** FS Token. */
  private static final byte[] FSML = token("fsml");
  /** FS Token. */
  private static final byte[] MTIME = token("mtime");
  /** FS Token. */
  private static final byte[] NAME = token("name");
  /** FS Token. */
  private static final byte[] SIZE = token("size");
  /** FS Token. */
  private static final byte[] SUFFIX = token("suffix");

  /**
   * Constructor.
   * @param path file path
   * @param pr database properties
   */
  public SimpleFSParser(final String path, final Prop pr) {
    super(path, pr);
  }

  @Override
  public void parse(final Builder b) throws IOException {
    b.startDoc(token(src.name()));
    b.startElem(FSML, atts.reset());
    parse(new File(src.path()), b);
    b.endElem();
    b.endDoc();
  }

  /**
   * Recursively parses the specified directory.
   * @param dir directory to be parsed
   * @param b builder instance
   * @throws IOException I/O exception
   */
  private void parse(final File dir, final Builder b) throws IOException {
    atts.reset();
    atts.add(NAME, token(dir.getName()));
    atts.add(MTIME, token(dir.lastModified()));
    b.startElem(DIR, atts);
    for(final File f : dir.listFiles()) {
      if(f.isDirectory()) {
        parse(f, b);
      } else {
        atts.reset();
        final String name = f.getName();
        atts.add(NAME, token(name));
        atts.add(SIZE, token(f.length()));
        atts.add(MTIME, token(f.lastModified()));
        int i = name.lastIndexOf('.');
        if(i != -1) atts.add(SUFFIX, token(name.substring(i + 1)));
        b.emptyElem(FILE, atts);
      }
    }
    b.endElem();
  }
}
