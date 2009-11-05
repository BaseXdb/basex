package org.deepfs.fsml.extractors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.basex.build.Builder;
import org.basex.util.Atts;

/**
 * This is an abstract class for defining meta data extractors.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class AbstractExtractor {
  /** Attribute container. */
  final Atts atts = new Atts();

  /**
   * Extracts the file content.
   * @param build reference to the database builder
   * @param f file to be visited
   * @throws IOException I/O exception
   */
  public abstract void extract(Builder build, File f) throws IOException;

  /**
   * Skips the number of specified bytes.
   * @param in input stream
   * @param skip bytes to be skipped
   * @throws IOException I/O exception
   */
  final void skip(final InputStream in, final int skip) throws IOException {
    int s = skip;
    while(s > 0) s -= in.skip(s);
  }
}
