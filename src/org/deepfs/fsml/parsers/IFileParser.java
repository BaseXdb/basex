package org.deepfs.fsml.parsers;

import java.io.IOException;

import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;

/**
 * Interface for metadata extractors / file parsers.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public interface IFileParser {

  /**
   * <p>
   * Checks if there is a File in correct format and can be read by the parser.
   * Checks e.g. header bytes.
   * </p>
   * @param bfc the {@link BufferedFileChannel} to read from
   * @return true if the file is supported
   * @throws IOException if an error occurs while reading from the file
   */
  boolean check(final BufferedFileChannel bfc)
      throws IOException;

  /**
   * Extracts metadata and file contents.
   * @param deepFile the {@link DeepFile} to save metadata and content to
   * @throws IOException if any error occurs while reading from the file
   */
  void extract(final DeepFile deepFile) throws IOException;

  /**
   * Propagates the metadata and file contents back to the file in the file
   * system.
   * @param deepFile the {@link DeepFile} that contains the metadata and file
   *          contents
   * @throws IOException if any error occurs
   */
  void propagate(final DeepFile deepFile) throws IOException;
}
