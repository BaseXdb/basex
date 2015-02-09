package org.basex.index.resource;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
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
 * @author BaseX Team 2005-15, BSD License
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
   * @return document nodes (internal representation!)
   */
  public synchronized IntList docs() {
    return docs.docs();
  }

  @Override
  public synchronized void init() {
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
   * Replaces entries in the index.
   * @param pre insertion position
   * @param size number of deleted nodes
   * @param clip data clip
   */
  public void replace(final int pre, final int size, final DataClip clip) {
    docs.replace(pre, size, clip);
  }

  /**
   * Returns the pre values of all document nodes that start with the specified path.
   * @param path input path
   * @return pre values (internal representation!)
   */
  public synchronized IntList docs(final String path) {
    return docs.docs(path, false);
  }

  /**
   * Returns the pre value of the document node that matches the specified path, or {@code -1}.
   * @param path input path
   * @return pre value, or {@code -1}
   */
  public int doc(final String path) {
    return docs.doc(path);
  }

  /**
   * Returns the database paths to all binary files that start with the specified path.
   * @param path input path
   * @return root nodes
   */
  public synchronized TokenList binaries(final String path) {
    return bins.bins(path);
  }

  /**
   * Determines whether the given path is the path to a directory.
   * @param path given path
   * @return result of check
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
  public boolean drop() {
    throw Util.notExpected();
  }

  @Override
  public void close() { }

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notExpected();
  }

  @Override
  public int costs(final IndexToken token) {
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
