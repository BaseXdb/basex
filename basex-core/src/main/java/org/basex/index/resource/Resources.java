package org.basex.index.resource;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.util.index.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This index organizes the resources of a database.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Resources implements Index {
  /** Binary resource types. */
  public static final ResourceType[] BINARIES = { ResourceType.BINARY, ResourceType.VALUE };
  /** Document references. */
  private final Docs docs;
  /** Binary files. */
  private final Binaries bins;

  /**
   * Constructor.
   * @param data data reference
   */
  public Resources(final Data data) {
    docs = new Docs(data);
    bins = new Binaries(data);
  }

  /**
   * Reads information on database resources from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public synchronized void read(final DataInput in) throws IOException {
    docs.read(in);
  }

  /**
   * Writes information on database resources to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    docs.write(out);
  }

  /**
   * Returns the {@code pre} values of all document nodes.
   * @return document nodes (internal representation!)
   */
  public synchronized IntList docs() {
    return docs.docs();
  }

  /**
   * Adds entries to the index and updates subsequent nodes.
   * @param pre insertion position
   * @param clip data clip
   */
  public void insert(final int pre, final DataClip clip) {
    docs.insert(pre, clip);
  }

  /**
   * Deletes the specified entry and updates subsequent nodes.
   * @param pre pre value
   * @param size number of deleted nodes
   */
  public void delete(final int pre, final int size) {
    docs.delete(pre, size);
  }

  /**
   * Updates the index after a document has been renamed.
   * @param pre pre value of updated document
   * @param value new name
   */
  public void rename(final int pre, final byte[] value) {
    docs.rename(pre, value);
  }

  /**
   * Returns the pre values of all document nodes that start with the specified path.
   * @param path input path
   * @return pre values (internal representation!)
   */
  public synchronized IntList docs(final String path) {
    return docs(path, false);
  }

  /**
   * Returns the pre values of all document nodes that start with the specified path.
   * @param path input path
   * @param dir directory view
   * @return pre values (internal representation!)
   */
  public synchronized IntList docs(final String path, final boolean dir) {
    return docs.docs(path, dir);
  }

  /**
   * Returns the pre value of the document node that matches the specified path.
   * @param path input path
   * @return pre value or {@code -1}
   */
  public int doc(final String path) {
    return docs.doc(path);
  }

  /**
   * Returns the database paths to all file resources that start with the specified path.
   * @param type resource type
   * @param path input path
   * @return paths
   */
  public synchronized StringList paths(final String path, final ResourceType type) {
    return bins.paths(path, type);
  }

  /**
   * Determines whether the given path is the path to a directory.
   * @param path given path
   * @return result of check
   */
  public synchronized boolean isDir(final String path) {
    return docs.isDir(path) || bins.isDir(path, ResourceType.BINARY) ||
        bins.isDir(path, ResourceType.VALUE);
  }

  /**
   * Returns the child resources for the given path.
   * @param path path
   * @param dir returns directories
   * @return paths with resource types
   */
  public synchronized TokenObjMap<ResourceType> children(final String path, final boolean dir) {
    final TokenObjMap<ResourceType> map = new TokenObjMap<>();
    docs.children(path, dir, map);
    bins.children(path, dir, map);
    return map;
  }

  // Inherited methods ============================================================================

  @Override
  public boolean drop() {
    throw Util.notExpected();
  }

  @Override
  public void close() { }

  @Override
  public IndexIterator iter(final IndexSearch search) {
    throw Util.notExpected();
  }

  @Override
  public IndexCosts costs(final IndexSearch search) {
    throw Util.notExpected();
  }

  @Override
  public byte[] info(final MainOptions options) {
    throw Util.notExpected();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    throw Util.notExpected();
  }
}
