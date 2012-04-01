package org.basex.query.util.pkg;

import static org.basex.util.Token.*;

import org.basex.core.Text;
import org.basex.io.IO;

/**
 * This class assembles textual information for package handling.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface PkgText {
  /** JAR manifest file. */
  String MANIFEST_MF = "META-INF/MANIFEST.MF";

  /** Package descriptor. */
  String DESCRIPTOR = "expath-pkg" + IO.XMLSUFFIX;
  /** Jar descriptor. */
  String JARDESC = Text.NAMELC + IO.XMLSUFFIX;

  /** EXPath type. */
  String EXPATH = "EXPath";
  /** Internal type. */
  String INTERNAL = "Internal";

  /** <package/> root element. */
  /** Element package. */
  byte[] PACKAGE = token("package");

  /** <package/> children. */
  /** Element dependency. */
  byte[] DEPEND = token("dependency");
  /** Element xquery. */
  byte[] XQUERY = token("xquery");

  /** <xquery/> children. */
  /** Attribute namespace. */
  byte[] A_NAMESPACE = token("namespace");
  /** Attribute file. */
  byte[] A_FILE = token("file");

  /** Jar descriptor children. */
  /** <jar/> element. */
  byte[] JAR = token("jar");
  /** <class/> element. */
  byte[] CLASS = token("class");

  /** <package/> attributes. */
  /** Attribute name. */
  byte[] A_NAME = token("name");
  /** Attribute abbrev. */
  byte[] A_ABBREV = token("abbrev");
  /** Attribute version. */
  byte[] A_VERSION = token("version");
  /** Attribute spec. */
  byte[] A_SPEC = token("spec");

  /** <dependency/> attributes. */
  /** Attribute package. */
  byte[] A_PACKAGE = token("package");
  /** Attribute processor. */
  byte[] A_PROCESSOR = token("processor");
  /** Attribute versions. */
  byte[] A_VERSIONS = token("versions");
  /** Attribute semver. */
  byte[] A_SEMVER = token("semver");
  /** Attribute semver-min. */
  byte[] A_SEMVER_MIN = token("semver-min");
  /** Attribute semver-max. */
  byte[] A_SEMVER_MAX = token("semver-max");

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
