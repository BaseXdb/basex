package org.basex.query.util;

import static org.basex.util.Token.*;
import org.basex.query.item.ANode;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeMore;
import org.basex.query.util.Package.Component;
import org.basex.query.util.Package.Dependency;

/**
 * Parser for package descriptors.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PackageParser {

  /** <package/> attributes. */
  /** Attribute name. */
  private static final byte[] NAME = token("name");
  /** Attribute abbrev. */
  private static final byte[] ABBREV = token("abbrev");
  /** Attribute version. */
  private static final byte[] VERSION = token("version");
  /** Attribute spec. */
  private static final byte[] SPEC = token("spec");
  /** Attribute title. */
  private static final byte[] TITLE = token("title");
  /** Attribute home. */
  private static final byte[] HOME = token("home");

  /** <package/> children. */
  /** Dependency. */
  private static final byte[] DEPEND = token("dependency");
  /** XQuery module. */
  private static final byte[] XQUERY = token("xquery");

  /** <dependency/> attributes. */
  /** Attribute package. */
  private static final byte[] PKG = token("package");
  /** Attribute processor. */
  private static final byte[] PROC = token("processor");
  /** Attribute vesrions. */
  private static final byte[] VERS = token("versions");
  /** Attribute semver. */
  private static final byte[] SEMVER = token("semver");
  /** Attribute semver-min. */
  private static final byte[] SEMVERMIN = token("semver-min");
  /** Attribute semver-max. */
  private static final byte[] SEMVERMAX = token("semver-max");

  /** <xquery/> attributes. */
  /** Attribute  import-uri. */
  private static final byte[] IMPURI = token("import-uri");
  /** Attribute namespace. */
  private static final byte[] NSPC = token("namespace");
  /** Attribute file. */
  private static final byte[] FILE = token("file");

  /** Constructor. */
  private PackageParser() {

  }

  /**
   * Parses a package descriptor.
   * @param pkgNode <package/>
   * @return package
   */
  public static Package parse(final ANode pkgNode) {
    final Package pkg = new Package();
    parseAttrs(pkgNode, pkg);
    parseChildren(pkgNode, pkg);
    return pkg;
  }

  /**
   * Parses the attributes of <package/>.
   * @param pkgNode package node
   * @param p package container
   */
  private static void parseAttrs(final ANode pkgNode, final Package p) {
    final AxisIter atts = pkgNode.atts();
    ANode next;
    byte[] nextName;
    while((next = atts.next()) != null) {
      nextName = next.nname();
      if(eq(NAME, nextName)) p.name = next.atom();
      else if(eq(ABBREV, nextName)) p.abbrev = next.atom();
      else if(eq(VERSION, nextName)) p.version = next.atom();
      else if(eq(SPEC, nextName)) p.spec = next.atom();
      else { // Error?
      }
      ;
    }
  }

  /**
   * Parses the children of <package/>.
   * @param pkgNode package node
   * @param p package container
   */
  private static void parseChildren(final ANode pkgNode, final Package p) {
    final NodeMore ch = pkgNode.children();
    ANode next;
    byte[] nextName;
    while((next = ch.next()) != null) {
      nextName = next.nname();
      if(eq(TITLE, nextName)) p.title = next.atom();
      else if(eq(HOME, nextName)) p.home = next.atom();
      else if(eq(DEPEND, nextName)) p.dep.add(parseDependency(next));
      else if(eq(XQUERY, nextName)) p.comps.add(parseComp(next));
      else { //?
        }
      }
    }

  /**
   * Parses <dependency/>.
   * @param depNode node <dependency/> to be parsed
   * @return dependency container
   */
  private static Dependency parseDependency(final ANode depNode) {
    final AxisIter attrs = depNode.atts();
    final Dependency dep = new Dependency();
    ANode next;
    byte[] nextName;
    while((next = attrs.next()) != null) {
      nextName = next.nname();
      if(eq(PKG, nextName)) dep.pkg = next.atom();
      else if(eq(PROC, nextName)) dep.processor = next.atom();
      else if(eq(VERS, nextName)) dep.versions = next.atom();
      else if(eq(SEMVER, nextName)) dep.semver = next.atom();
      else if(eq(SEMVERMIN, nextName)) dep.semverMin = next.atom();
      else if(eq(SEMVERMAX, nextName)) dep.semverMax = next.atom();
      else {
        // Error?
      }
    }
    return dep;
  }

  /**
   * Parses <xquery/>.
   * @param comp xquery component
   * @return component container
   */
  private static Component parseComp(final ANode comp) {
    final NodeMore ch = comp.children();
    final Component c = new Component();
    c.type = XQUERY;
    ANode next;
    byte[] nextName;
    while((next = ch.next()) != null) {
      nextName = next.nname();
      if(eq(IMPURI, nextName)) c.importUri = next.atom();
      else if (eq(NSPC, nextName)) c.namespace = next.atom();
      else if (eq(FILE, nextName)) c.file = next.atom();
      else {
        // Error?
      }
    }
    return c;
  }
}
