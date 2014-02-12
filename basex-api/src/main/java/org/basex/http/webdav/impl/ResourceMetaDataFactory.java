package org.basex.http.webdav.impl;

/**
 * Factory interface for generating different WebDAV resources.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 * @param <T> generated resource type
 */
public interface ResourceMetaDataFactory<T> {
  /**
   * Creates a new resource representing a file.
   * @param s service instance
   * @param d file meta data
   * @return object representing the file
   */
  T file(final WebDAVService<T> s, final ResourceMetaData d);

  /**
   * Creates a new resource representing a folder.
   * @param s service instance
   * @param d folder meta data
   * @return object representing the folder
   */
  T folder(final WebDAVService<T> s, final ResourceMetaData d);

  /**
   * Creates a new resource representing a database.
   * @param s service instance
   * @param d database meta data
   * @return object representing the database
   */
  T database(final WebDAVService<T> s, final ResourceMetaData d);
}
