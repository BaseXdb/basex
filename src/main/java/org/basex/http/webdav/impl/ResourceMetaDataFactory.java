package org.basex.http.webdav.impl;

public interface ResourceMetaDataFactory<T> {
  T file(final WebDAVService<T> s, final ResourceMetaData d);
  T folder(final WebDAVService<T> s, final ResourceMetaData d);
  T database(final WebDAVService<T> s, final ResourceMetaData d);
}
