package org.basex.query.util.pkg;

import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class assembles textual information for package handling.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public interface PkgText {
  /** JAR manifest file. */
  String MANIFEST_MF = "META-INF/MANIFEST.MF";

  /** Package descriptor. */
  String DESCRIPTOR = "expath-pkg" + IO.XMLSUFFIX;
  /** Jar descriptor. */
  String JARDESC = Prop.PROJECT + IO.XMLSUFFIX;

  /** EXPath content directory. */
  String CONTENT = "content";

  // <package/> root element

  /** Element package. */
  byte[] E_PACKAGE = token("package");

  // <package/> children.

  /** Element dependency. */
  byte[] E_DEPENDENCY = token("dependency");
  /** Element xquery. */
  byte[] E_XQUERY = token("xquery");

  // <xquery/> children

  /** Attribute namespace. */
  byte[] A_NAMESPACE = token("namespace");
  /** Attribute file. */
  byte[] A_FILE = token("file");

  // Jar descriptor children

  /** jar element. */
  byte[] E_JAR = token("jar");
  /** class element. */
  byte[] E_CLASS = token("class");

  // <package/> attributes

  /** QName. */
  QNm Q_NAME = new QNm("name");
  /** QName. */
  QNm Q_ABBREV = new QNm("abbrev");
  /** QName. */
  QNm Q_VERSION = new QNm("version");
  /** QName. */
  QNm Q_SPEC = new QNm("spec");

  // <dependency/> attributes

  /** QName. */
  QNm Q_PACKAGE = new QNm("package");
  /** QName. */
  QNm Q_PROCESSOR = new QNm("processor");
  /** QName. */
  QNm Q_VERSIONS = new QNm("versions");
  /** QName. */
  QNm Q_SEMVER = new QNm("semver");
  /** QName. */
  QNm Q_SEMVER_MIN = new QNm("semver-min");
  /** QName. */
  QNm Q_SEMVER_MAX = new QNm("semver-max");

  /** Not expected initialization error. */
  String MISSDESC = "Missing package descriptor for package '%'";
  /** Attribute missing. */
  String MISSATTR = "'%' attribute missing in '%' element";
  /** Invalid element. */
  String WHICHELEM = "Invalid element %";
  /** Secondary package missing. */
  String MISSSECOND = "Dependency not completely specified.";
  /** Component missing. */
  String MISSCOMP = "Component '%' not specified";
  /** No jars registered. */
  String NOJARS = "No jars specified";
  /** No public classes registered. */
  String NOCLASSES = "No public classes specified";
  /** No main class found in Java manifest. */
  String MANIFEST = "No 'Main-Class' attribute found: %/META-INF/MANIFEST.MF.";
}
