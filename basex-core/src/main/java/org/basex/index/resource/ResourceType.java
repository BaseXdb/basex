package org.basex.index.resource;

import java.util.*;

import org.basex.io.*;
import org.basex.util.http.*;

/**
 * Database resource type.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public enum ResourceType {
  /** XML document. */
  XML {
    @Override
    public IOFile dir(final IOFile dbpath) {
      return null;
    }

    @Override
    public MediaType contentType(final String path) {
      final MediaType ct = MediaType.get(path);
      return !ct.isXML() ? MediaType.APPLICATION_XML : ct;
    }

    @Override
    public String path(final String path) {
      return path;
    }
  },

  /** Binary resource. */
  BINARY {
    @Override
    public IOFile dir(final IOFile dbpath) {
      return new IOFile(dbpath, "raw");
    }

    @Override
    public MediaType contentType(final String path) {
      return MediaType.get(path);
    }

    @Override
    public String path(final String path) {
      return path;
    }
  },

  /** Value resource. */
  VALUE {
    @Override
    public IOFile dir(final IOFile dbpath) {
      return new IOFile(dbpath, "values");
    }

    @Override
    public MediaType contentType(final String path) {
      return MediaType.get(path(path));
    }

    @Override
    public String path(final String path) {
      return path.substring(0, path.length() - IO.BASEXSUFFIX.length());
    }
  };

  /**
   * Returns directory in which resources are stored.
   * @param dbpath path to database directory
   * @return directory or {@code null} for XML documents
   */
  public abstract IOFile dir(IOFile dbpath);

  /**
   * Returns the path to a resource.
   * @param path original file path
   * @return path
   */
  public abstract String path(String path);

  /**
   * Returns the content type of a resource.
   * @param path path
   * @return content type
   */
  public abstract MediaType contentType(String path);

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
