package org.basex.query.up.primitives;

/**
 * Update primitive type enumeration.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public enum PrimitiveType {
  // Order is essential - don't change..

  /** Type Insert attribute. */ INSERTATTR,
  /** Type Replace value. */ REPLACEVALUE,
  /** Type Rename. */ RENAME,
  /** Type Insert after. */ INSERTAFTER,
  /** Type Insert into as first. */ INSERTINTOFI,
  /** Type Insert into AND insert into as last. */ INSERTINTO,
  /** Type Replace element content. */ REPLACEELEMCONT,
  /** Type Puts. */ PUT, // put here to reflect updates of descendant-or-self
  /** Type Insert before. */ INSERTBEFORE,
  /** Type Replace node. */ REPLACENODE,
  /** Type Delete. */ DELETE
}