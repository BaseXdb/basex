package org.basex.query.util.repo;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.repo.PkgText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.NodeMore;
import org.basex.query.util.repo.Package.Component;
import org.basex.query.util.repo.Package.Dependency;
import org.basex.util.InputInfo;

/**
 * Parser for package descriptors.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PkgParser {
  /** Context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param c context
   */
  public PkgParser(final Context c) {
    ctx = c;
  }

  /**
   * Parses package descriptor.
   * @param io IO
   * @param ii input info
   * @return package container
   * @throws QueryException query exception
   */
  public Package parse(final IO io, final InputInfo ii) throws QueryException {
    final Package pkg = new Package();
    try {
      final Parser p = Parser.xmlParser(io, ctx.prop, "");
      final ANode node =
        new DBNode(MemBuilder.build(p, ctx.prop, ""), 0).children().next();
      parseAttrs(node, pkg);
      parseChildren(node, pkg);
      return pkg;
    } catch(final IOException ex) {
      throw PKGREADFAIL.thrw(ii);
    }
  }

  /**
   * Parses the attributes of <package/>.
   * @param pkgNode package node
   * @param p package container
   */
  private void parseAttrs(final ANode pkgNode, final Package p) {
    final AxisIter atts = pkgNode.atts();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] nextName = next.nname();
      if(eq(NAME, nextName)) p.uri = next.atom();
      else if(eq(ABBREV, nextName)) p.abbrev = next.atom();
      else if(eq(VERSION, nextName)) p.version = next.atom();
      else if(eq(SPEC, nextName)) p.spec = next.atom();
    }
  }

  /**
   * Parses the children of <package/>.
   * @param pkgNode package node
   * @param p package container
   */
  private void parseChildren(final ANode pkgNode, final Package p) {
    final NodeMore ch = pkgNode.children();
    for(ANode next; (next = ch.next()) != null;) {
      final byte[] nextName = next.nname();
      if(eq(TITLE, nextName)) p.title = next.atom();
      else if(eq(HOME, nextName)) p.home = next.atom();
      else if(eq(DEPEND, nextName)) p.dep.add(parseDependency(next));
      else if(eq(XQUERY, nextName)) p.comps.add(parseComp(next));
    }
  }

  /**
   * Parses <dependency/>.
   * @param depNode node <dependency/> to be parsed
   * @return dependency container
   */
  private Dependency parseDependency(final ANode depNode) {
    final AxisIter attrs = depNode.atts();
    final Dependency dep = new Dependency();
    for(ANode next; (next = attrs.next()) != null;) {
      final byte[] nextName = next.nname();
      if(eq(PKG, nextName)) dep.pkg = next.atom();
      else if(eq(PROC, nextName)) dep.processor = next.atom();
      else if(eq(VERS, nextName)) dep.versions = next.atom();
      else if(eq(SEMVER, nextName)) dep.semver = next.atom();
      else if(eq(SEMVERMIN, nextName)) dep.semverMin = next.atom();
      else if(eq(SEMVERMAX, nextName)) dep.semverMax = next.atom();
    }
    return dep;
  }

  /**
   * Parses <xquery/>.
   * @param comp xquery component
   * @return component container
   */
  private Component parseComp(final ANode comp) {
    final NodeMore ch = comp.children();
    final Component c = new Component();
    c.type = XQUERY;
    for(ANode next; (next = ch.next()) != null;) {
      final byte[] nextName = next.nname();
      if(eq(IMPURI, nextName)) c.importUri = next.atom();
      else if(eq(NSPC, nextName)) c.namespace = next.atom();
      else if(eq(FILE, nextName)) c.file = next.atom();
    }
    return c;
  }
}
