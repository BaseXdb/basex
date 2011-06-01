package org.basex.query.util.repo;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.repo.PkgText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.AxisIter;
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
  private final Context context;

  /**
   * Constructor.
   * @param ctx context
   */
  public PkgParser(final Context ctx) {
    context = ctx;
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
      final ANode node = new DBNode(io, context.prop).children().next();
      parseAttrs(node, pkg);
      parseChildren(node, pkg);
      return pkg;
    } catch(final IOException ex) {
      throw PKGREADFAIL.thrw(ii, ex.getMessage());
    }
  }

  /**
   * Parses the attributes of <package/>.
   * @param pkgNode package node
   * @param p package container
   */
  private void parseAttrs(final ANode pkgNode, final Package p) {
    final AxisIter atts = pkgNode.attributes();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] name = next.nname();
      if(eq(NAME, name)) p.uri = next.atom();
      else if(eq(ABBREV, name)) p.abbrev = next.atom();
      else if(eq(VERSION, name)) p.version = next.atom();
      else if(eq(SPEC, name)) p.spec = next.atom();
    }
  }

  /**
   * Parses the children of <package/>.
   * @param pkgNode package node
   * @param p package container
   */
  private void parseChildren(final ANode pkgNode, final Package p) {
    final AxisIter ch = pkgNode.children();
    for(ANode next; (next = ch.next()) != null;) {
      final byte[] name = next.nname();
      if(eq(TITLE, name)) p.title = next.atom();
      else if(eq(HOME, name)) p.home = next.atom();
      else if(eq(DEPEND, name)) p.dep.add(parseDependency(next));
      else if(eq(XQUERY, name)) p.comps.add(parseComp(next));
    }
  }

  /**
   * Parses <dependency/>.
   * @param depNode node <dependency/> to be parsed
   * @return dependency container
   */
  private Dependency parseDependency(final ANode depNode) {
    final AxisIter attrs = depNode.attributes();
    final Dependency dep = new Dependency();
    for(ANode next; (next = attrs.next()) != null;) {
      final byte[] name = next.nname();
      if(eq(PKG, name)) dep.pkg = next.atom();
      else if(eq(PROC, name)) dep.processor = next.atom();
      else if(eq(VERS, name)) dep.versions = next.atom();
      else if(eq(SEMVER, name)) dep.semver = next.atom();
      else if(eq(SEMVERMIN, name)) dep.semverMin = next.atom();
      else if(eq(SEMVERMAX, name)) dep.semverMax = next.atom();
    }
    return dep;
  }

  /**
   * Parses <xquery/>.
   * @param comp xquery component
   * @return component container
   */
  private Component parseComp(final ANode comp) {
    final AxisIter ch = comp.children();
    final Component c = new Component();
    c.type = XQUERY;
    for(ANode next; (next = ch.next()) != null;) {
      final byte[] name = next.nname();
      if(eq(IMPURI, name)) c.importUri = next.atom();
      else if(eq(NSPC, name)) c.namespace = next.atom();
      else if(eq(FILE, name)) c.file = next.atom();
    }
    return c;
  }
}
