package org.basex.query.util.pkg;

import static org.basex.util.Token.*;

import org.basex.core.Text;
import org.basex.io.IO;

/**
 * This class assembles textual information for package handling.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface PkgText {
  /** Package descriptor. */
  String DESCRIPTOR = "expath-pkg" + IO.XMLSUFFIX;
  /**Jar descriptor. */
  String JARDESC = Text.NAMELC + IO.XMLSUFFIX;

  /** <package/> root element. */
  /** Element package. */
  byte[] PACKAGE = token("package");

  /** <package/> attributes. */
  /** Attribute name. */
  byte[] NAME = token("name");
  /** Attribute abbrev. */
  byte[] ABBREV = token("abbrev");
  /** Attribute version. */
  byte[] VERSION = token("version");
  /** Attribute spec. */
  byte[] SPEC = token("spec");

  /** <package/> children. */
  /** Element title. */
  byte[] TITLE = token("title");
  /** Element home. */
  byte[] HOME = token("home");
  /** Element dependency. */
  byte[] DEPEND = token("dependency");
  /** Element xquery. */
  byte[] XQUERY = token("xquery");

  /** <dependency/> attributes. */
  /** Attribute package. */
  byte[] PKG = token("package");
  /** Attribute processor. */
  byte[] PROC = token("processor");
  /** Attribute versions. */
  byte[] VERS = token("versions");
  /** Attribute semver. */
  byte[] SEMVER = token("semver");
  /** Attribute semver-min. */
  byte[] SEMVERMIN = token("semver-min");
  /** Attribute semver-max. */
  byte[] SEMVERMAX = token("semver-max");

  /** <xquery/> children. */
  /** Attribute import-uri. */
  byte[] IMPURI = token("import-uri");
  /** Attribute namespace. */
  byte[] NSPC = token("namespace");
  /** Attribute file. */
  byte[] FILE = token("file");

  /** Jar descriptor children. */
  /** <jar/> element. */
  byte[] JAR = token("jar");
  /** <class/> element. */
  byte[] CLASS = token("class");

  /** Directory. */
  String DIR = "Directory";

  /** Not expected initialization error. */
  String MISSDESC = "Missing package descriptor for package '%'";
  /** Attribute missing. */
  String MISSATTR = "'%' attribute missing in '%' element";
  /** Invalid attribute. */
  String WHICHATTR = "Invalid attribute '%'";
  /** Invalid element. */
  String WHICHELEM = "Invalid element %";
  /** Secondary package missing. */
  String MISSSECOND = "Dependency not completely specified.";
  /** Component missing. */
  String MISSCOMP = "Component '%' not specified";
  /** No jars registered. */
  String NOJARS = "No jars specified";
  /** No public classes registered. */
  String NOCLASS = "No public classes specified";
}
