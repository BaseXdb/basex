package org.basex.index.resource;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>This index organizes the resources of a database (XML documents and raw files).</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Resources implements Index {
  /** Document references. */
  private final Docs docs;
  /** Binary files. */
  private final Binaries bins;

  /**
   * Constructor.
   * @param d data reference
   */
  public Resources(final Data d) {
    docs = new Docs(d);
    bins = new Binaries(d);
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
   * A single dummy node is returned if the database is empty.
   * @return document nodes
   */
  public synchronized IntList docs() {
    return docs.docs();
  }

  /**
   * Initializes the index.
   */
  public synchronized void init() {
    docs.init();
  }

  /**
   * Adds entries to the index and updates subsequent nodes.
   * @param pre insertion position
   * @param d data reference to be inserted
   */
  public void insert(final int pre, final Data d) {
    docs.insert(pre, d);
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
   * Replaces entries in the index.
   * @param pre insertion position
   * @param size number of deleted nodes
   * @param d data reference to be copied
   */
  public void replace(final int pre, final int size, final Data d) {
    docs.replace(pre, size, d);
  }

  /**
   * Returns the pre values of all document nodes matching the specified path.
   * Exact || prefix match!
   * @param path input path
   * @return root nodes
   */
  public synchronized IntList docs(final String path) {
    return docs.docs(path);
  }

  /**
   * Returns the pre value of the node that matches the specified path,
   * or {@code -1}.
   * @param path input path
   * @return pre value
   */
  public int doc(final String path) {
    return doc(path, true);
  }

  /**
   * Returns the pre value of the document node matching the specified path.
   * Exact match! Document paths can be sorted for faster future access or
   * sorting can be disabled as it slows down bulk inserts/deletes/replaces.
   * @param path input path
   * @param sort sort paths before access
   * @return root nodes
   */
  public synchronized int doc(final String path, final boolean sort) {
    return docs.doc(path, sort);
  }

  /**
   * Returns the database paths to all binary files that match the
   * specified path.
   * @param path input path
   * @return root nodes
   */
  public synchronized TokenList binaries(final String path) {
    return bins.bins(path);
  }

  /**
   * Determines whether the given path is the path to a directory.
   * @param path given path (must be normalized, means one leading but
   * no trailing slash.
   * @return path to a directory or not
   */
  public synchronized boolean isDir(final byte[] path) {
    return docs.isDir(path) || bins.isDir(Token.string(path));
  }

  /**
   * Returns the child resources for the given path.
   * @param path path
   * @param dir returns directories
   * @return paths; values of documents will be {@code false}
   */
  public synchronized TokenBoolMap children(final byte[] path, final boolean dir) {
    final TokenBoolMap tbm = new TokenBoolMap();
    docs.children(path, dir, tbm);
    bins.children(path, dir, tbm);
    return tbm;
  }

  // Inherited methods ========================================================

  @Override
  public void close() { }

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public int count(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public byte[] info() {
    throw Util.notexpected();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    throw Util.notexpected();
  }
}
