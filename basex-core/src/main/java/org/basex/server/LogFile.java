package org.basex.server;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.list.*;

/**
 * Log file instance.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LogFile {
  /** File reference. */
  private final IOFile file;
  /** Output stream. */
  private FileOutputStream fos;

  /**
   * Creates a new writable log file for the specified date.
   * @param name name of log file
   * @param dir log directory
   * @return log file
   * @throws IOException I/O exception
   */
  static LogFile create(final String name, final IOFile dir) throws IOException {
    final LogFile lf = new LogFile(name, dir);
    dir.md();
    lf.fos = new FileOutputStream(lf.file.file(), true);
    return lf;
  }

  /**
   * Constructor.
   * @param name name of log file
   * @param dir log directory
   */
  LogFile(final String name, final IOFile dir) {
    file = new IOFile(dir, name + IO.LOGSUFFIX);
  }

  /**
   * Indicates if this is the current log file.
   * @return result of check
   */
  public boolean current() {
    return fos != null;
  }

  /**
   * Returns the entries of the specified log file.
   * @return log entries
   * @throws IOException I/O exception
   */
  public StringList read() throws IOException {
    synchronized(file) {
      final StringList list = new StringList();
      try(NewlineInput nli = new NewlineInput(file)) {
        for(String line; (line = nli.readLine()) != null;) list.add(line);
      }
      return list;
    }
  }

  /**
   * Deletes the specified log file.
   * @return result of check
   */
  public boolean delete() {
    synchronized(file) {
      return file.delete();
    }
  }

  /**
   * Checks if the specified log file exists.
   * @return result of check
   */
  boolean exists() {
    synchronized(file) {
      return file.exists();
    }
  }

  /**
   * Writes new line to the log file.
   * @param line line to be written
   * @throws IOException I/O exception
   */
  void write(final byte[] line) throws IOException {
    synchronized(file) {
      fos.write(line);
      fos.flush();
    }
  }

  /**
   * Closes the current log file.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    synchronized(file) {
      if(fos != null) {
        fos.close();
        fos = null;
      }
    }
  }

  /**
   * Checks if the log file is still valid.
   * @param name name of log file
   * @return result of check
   */
  boolean valid(final String name) {
    return file.name().equals(name + IO.LOGSUFFIX);
  }
}
