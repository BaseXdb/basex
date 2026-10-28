package org.basex.index.resource;

/**
 * Scope of the documents returned for a database path.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum DocScope {
  /** All documents, including those in subdirectories. */
  ALL,
  /** Documents without those in subdirectories. */
  CHILDREN,
  /** Documents without those in subdirectories, and one document per subdirectory. */
  DIRECTORY
}
