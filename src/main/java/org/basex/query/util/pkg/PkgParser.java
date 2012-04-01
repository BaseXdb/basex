package org.basex.query.util.pkg;

import static org.basex.query.util.Err.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.pkg.Package.Component;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.util.*;

/**
 * Parses the package descriptors and performs schema checks.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class PkgParser {
  /** Repository context. */
  private final Repo repo;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param r repository context
   * @param ii input info
   */
  public PkgParser(final Repo r, final InputInfo ii) {
    repo = r;
    info = ii;
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
      final byte[] content = io.read();

      final DBNode doc = new DBNode(new IOContent(content), repo.context.prop);
      final ANode node = childElements(doc).next();

      // checks root node
      if(!eqNS(PACKAGE, node.qname()))
        PKGDESCINV.thrw(info, Util.info(WHICHELEM, node.qname()));

      parseAttributes(node, pkg, PACKAGE);
      parseChildren(node, pkg);
      return pkg;
    } catch(final IOException ex) {
      throw PKGREADFAIL.thrw(info, io.name(), ex.getMessage());
    }
  }

  /**
   * Parses the attributes of <package/> or <module/>.
   * @param node package node
   * @param p package container
   * @param root root node
   * @throws QueryException query exception
   */
  private void parseAttributes(final ANode node, final Package p, final byte[] root)
      throws QueryException {

    final AxisIter atts = node.attributes();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] name = next.name();
      if(eq(NAME, name))         p.name = next.string();
      else if(eq(ABBREV, name))  p.abbrev = next.string();
      else if(eq(VERSION, name)) p.version = next.string();
      else if(eq(SPEC, name))    p.spec = next.string();
      else PKGDESCINV.thrw(info, Util.info(WHICHATTR, name));
    }

    // check mandatory attributes
    if(p.name == null)
      PKGDESCINV.thrw(info, Util.info(MISSATTR, NAME, root));
    if(p.version == null)
      PKGDESCINV.thrw(info, Util.info(MISSATTR, VERSION, root));
    if(p.abbrev == null)
      PKGDESCINV.thrw(info, Util.info(MISSATTR, ABBREV, root));
    if(p.spec == null)
      PKGDESCINV.thrw(info, Util.info(MISSATTR, SPEC, root));
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
      final byte[] name = next.name();
      if(eq(PKG, name))            d.pkg = next.string();
      else if(eq(PROC, name))      d.processor = next.string();
      else if(eq(VERS, name))      d.versions = next.string();
      else if(eq(SEMVER, name))    d.semver = next.string();
      else if(eq(SEMVERMIN, name)) d.semverMin = next.string();
      else if(eq(SEMVERMAX, name)) d.semverMax = next.string();
      else PKGDESCINV.thrw(info, Util.info(WHICHATTR, name));
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
      else PKGDESCINV.thrw(info, Util.info(WHICHELEM, name));
    }

    // check mandatory children
    if(c.uri == null) PKGDESCINV.thrw(info, Util.info(MISSCOMP, NSPC));
    if(c.file == null) PKGDESCINV.thrw(info, Util.info(MISSCOMP, FILE));
    return c;
  }

  /**
   * Returns an iterator on all child elements
   * (text and other nodes will be skipped).
   * @param node root node
   * @return child element iterator
   */
  private static AxisIter childElements(final ANode node) {
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
