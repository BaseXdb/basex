package org.basex.query.up.primitives;

/**
 * Update primitive type enumeration.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public enum PrimitiveType {
  /** Type Insert attribute. */ INSERTATTR,
  /** Type Replace value. */ REPLACEVALUE,
  /** Type Rename. */ RENAME,
  /** Type Insert after. */ INSERTAFTER,
  /** Type Insert into as first. */ INSERTINTOFI,
  /** Type Insert into AND insert into as last. */ INSERTINTO,
  /** Type Replace element content. */ REPLACEELEMCONT,
  /** Type Delete. */ PUT,
  /** Type Insert before. */ INSERTBEFORE,
  /** Type Replace node. */ REPLACENODE,
  /** Type Delete. */ DELETE
}