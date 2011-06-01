package org.basex.query.util.repo;

import static org.basex.util.Token.*;

/**
 * This class assembles textual information for package handling.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
interface PkgText {
  /** Package descriptor. */
  String DESCRIPTOR = "expath-pkg.xml";

  /** <package/> attributes. */
  /** Attribute name. */
  byte[] NAME = token("name");
  /** Attribute abbrev. */
  byte[] ABBREV = token("abbrev");
  /** Attribute version. */
  byte[] VERSION = token("version");
  /** Attribute spec. */
  byte[] SPEC = token("spec");
  /** Attribute title. */
  byte[] TITLE = token("title");
  /** Attribute home. */
  byte[] HOME = token("home");

  /** <package/> children. */
  /** Dependency. */
  byte[] DEPEND = token("dependency");
  /** XQuery module. */
  byte[] XQUERY = token("xquery");

  /** <dependency/> attributes. */
  /** Attribute package. */
  byte[] PKG = token("package");
  /** Attribute processor. */
  byte[] PROC = token("processor");
  /** Attribute vesrions. */
  byte[] VERS = token("versions");
  /** Attribute semver. */
  byte[] SEMVER = token("semver");
  /** Attribute semver-min. */
  byte[] SEMVERMIN = token("semver-min");
  /** Attribute semver-max. */
  byte[] SEMVERMAX = token("semver-max");

  /** <xquery/> attributes. */
  /** Attribute import-uri. */
  byte[] IMPURI = token("import-uri");
  /** Attribute namespace. */
  byte[] NSPC = token("namespace");
  /** Attribute file. */
  byte[] FILE = token("file");

  /** Not expected initialization error. */
  String NOTEXP = "Missing package descriptor for package '%'.";
  /** Attribute missing. */
  String MISSATTR = "Mandatory attribute '%' is missing.";
  /** Secondary package missing. */
  String MISSSECOND = "Name of secondary package is missing";
  /** Component missing. */
  String MISSCOMP = "Component '%' is not specified.";
}
