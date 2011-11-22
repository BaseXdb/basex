package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.AxisIter;
import org.basex.query.util.pkg.Package.Component;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Parses the package descriptors and performs schema checks.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class PkgParser {
  /** Repository context. */
  private final Repo repo;
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   * @param r repository context
   * @param ii input info
   */
  public PkgParser(final Repo r, final InputInfo ii) {
    repo = r;
    input = ii;
  }

  /**
   * Parses package descriptor.
   * @param io XML input
   * @return package container
   * @throws QueryException query exception
   */
  public Package parse(final IO io) throws QueryException {
    final Package pkg = new Package();
    try {
      final DBNode doc = new DBNode(io, repo.context.prop);
      final ANode node = childElements(doc).next();

      // checks root node
      if(!eqNS(PACKAGE, node.qname()))
        PKGDESCINV.thrw(input, Util.info(WHICHELEM, node.qname()));

      parseAttributes(node, pkg, PACKAGE);
      parseChildren(node, pkg);
      return pkg;
    } catch(final IOException ex) {
      throw PKGREADFAIL.thrw(input, io.name(), ex.getMessage());
    }
  }

  /**
   * Parses the attributes of <package/> or <module/>.
   * @param node package node
   * @param p package container
   * @param root root node
   * @throws QueryException query exception
   */
  private void parseAttributes(final ANode node, final Package p,
      final byte[] root) throws QueryException {

    final AxisIter atts = node.attributes();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] name = next.nname();
      if(eq(NAME, name))         p.name = next.string();
      else if(eq(ABBREV, name))  p.abbrev = next.string();
      else if(eq(VERSION, name)) p.version = next.string();
      else if(eq(SPEC, name))    p.spec = next.string();
      else PKGDESCINV.thrw(input, Util.info(WHICHATTR, name));
    }

    // check mandatory attributes
    if(p.name == null)
      PKGDESCINV.thrw(input, Util.info(MISSATTR, NAME, root));
    if(p.version == null)
      PKGDESCINV.thrw(input, Util.info(MISSATTR, VERSION, root));
    if(p.abbrev == null)
      PKGDESCINV.thrw(input, Util.info(MISSATTR, ABBREV, root));
    if(p.spec == null)
      PKGDESCINV.thrw(input, Util.info(MISSATTR, SPEC, root));
  }

  /**
   * Parses the children of <package/>.
   * @param node package node
   * @param p package container
   * @throws QueryException query exception
   */
  private void parseChildren(final ANode node, final Package p)
      throws QueryException {

    final AxisIter ch = childElements(node);
    for(ANode next; (next = ch.next()) != null;) {
      final QNm name = next.qname();
      if(eqNS(DEPEND, name)) p.dep.add(parseDependency(next));
      else if(eqNS(XQUERY, name)) p.comps.add(parseComp(next));
    }
  }

  /**
   * Parses <dependency/>.
   * @param node node <dependency/> to be parsed
   * @return dependency container
   * @throws QueryException query exception
   */
  private Dependency parseDependency(final ANode node) throws QueryException {
    final AxisIter atts = node.attributes();
    final Dependency d = new Dependency();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] name = next.nname();
      if(eq(PKG, name))            d.pkg = next.string();
      else if(eq(PROC, name))      d.processor = next.string();
      else if(eq(VERS, name))      d.versions = next.string();
      else if(eq(SEMVER, name))    d.semver = next.string();
      else if(eq(SEMVERMIN, name)) d.semverMin = next.string();
      else if(eq(SEMVERMAX, name)) d.semverMax = next.string();
      else PKGDESCINV.thrw(input, Util.info(WHICHATTR, name));
    }
    return d;
  }

  /**
   * Parses <xquery/>.
   * @param node xquery component
   * @return component container
   * @throws QueryException query exception
   */
  private Component parseComp(final ANode node) throws QueryException {
    final AxisIter ch = childElements(node);
    final Component c = new Component();
    for(ANode next; (next = ch.next()) != null;) {
      final QNm name = next.qname();
      if(eqNS(NSPC, name)) c.uri = next.string();
      else if(eqNS(FILE, name)) c.file = next.string();
      else PKGDESCINV.thrw(input, Util.info(WHICHELEM, name));
    }

    // check mandatory children
    if(c.uri == null) PKGDESCINV.thrw(input, Util.info(MISSCOMP, NSPC));
    if(c.file == null) PKGDESCINV.thrw(input, Util.info(MISSCOMP, FILE));
    return c;
  }

  /**
   * Returns an iterator on all child elements
   * (text and other nodes will be skipped).
   * @param node root node
   * @return child element iterator
   */
  private AxisIter childElements(final ANode node) {
    return new AxisIter() {
      /** Child iterator. */
      final AxisIter ch = node.children();

      @Override
      public ANode next() {
        while(true) {
          final ANode n = ch.next();
          if(n == null || n.type == NodeType.ELM) return n;
        }
      }
    };
  }

  /**
   * Checks if the specified name equals the qname and if it uses the packaging
   * namespace.
   * @param cmp input
   * @param name name to be compared
   * @return result of check
   */
  private static boolean eqNS(final byte[] cmp, final QNm name) {
    return name.eq(new QNm(cmp, QueryText.PKGURI));
  }
}
