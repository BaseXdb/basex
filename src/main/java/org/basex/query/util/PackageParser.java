package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeMore;
import org.basex.query.util.Package.Component;
import org.basex.query.util.Package.Dependency;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;

/**
 * Parser for package descriptors.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PackageParser {

  /** Package descriptor. */
  private static final String PKGDESC = "expath-pkg.xml";

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
  /** Attribute import-uri. */
  private static final byte[] IMPURI = token("import-uri");
  /** Attribute namespace. */
  private static final byte[] NSPC = token("namespace");
  /** Attribute file. */
  private static final byte[] FILE = token("file");

  /** Constructor. */
  private PackageParser() {

  }

  /**
   * Parses package descriptor.
   * @param io IO
   * @param ctx context
   * @param ii input info
   * @return package container
   * @throws QueryException query exception
   */
  public static Package parse(
      final IO io, final Context ctx, final InputInfo ii)
      throws QueryException {
    final Package pkg = new Package();
    try {
      final Parser p = Parser.xmlParser(io, ctx.prop, "");
      final ANode pkgNode =
        new DBNode(MemBuilder.build(p, ctx.prop, ""), 0).children().next();
      parseAttrs(pkgNode, pkg);
      parseChildren(pkgNode, pkg);
      return pkg;
    } catch(IOException ex) {
      throw PKGREADFAIL.thrw(ii);
    }
  }

  /**
   * Reads package descriptor directly from .xar archive. Logic is copied from
   * method entry() in org.basex.query.func.FNZip
   * @param zf package archive
   * @param ii input info
   * @return contents of package descriptor
   * @throws QueryException query exception
   */
  protected static byte[] readPkgDesc(final ZipFile zf, final InputInfo ii)
      throws QueryException {

    try {
      final ZipEntry ze = zf.getEntry(PKGDESC);
      if(ze == null) PKGDESCMISS.thrw(ii);
      final InputStream zis = zf.getInputStream(ze);
      final int s = (int) ze.getSize();
      if(s >= 0) {
        // known size: pre-allocate and fill array
        final byte[] data = new byte[s];
        int c, o = 0;
        while(s - o != 0 && (c = zis.read(data, o, s - o)) != -1)
          o += c;
        return data;
      }
      // unknown size: use byte list
      final byte[] data = new byte[IO.BLOCKSIZE];
      final ByteList bl = new ByteList();
      int c;
      while((c = zis.read(data)) != -1)
        bl.add(data, 0, c);
      return bl.toArray();

    } catch(IOException ex) {
      throw PKGREADFAIL.thrw(ii);
    }
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
      if(eq(NAME, nextName)) p.uri = next.atom();
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
      else { // ?
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
      else if(eq(NSPC, nextName)) c.namespace = next.atom();
      else if(eq(FILE, nextName)) c.file = next.atom();
      else {
        // Error?
      }
    }
    return c;
  }

}
