package org.basex.query.util;

import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.io.IOFile;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodeMore;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;

/**
 * Repositiry manager.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class RepoManager {

  /** Package name. */
  private static final byte[] NAME = token("name");
  /** Package direcorty. */
  private static final byte[] DIR = token("dir");
  private static final byte[] XQUERY = token("xquery");
  private static final byte[] NMSPC = token("namespace");
  private static final byte[] ABBREV = token("abbrev");
  private static final String REPO = "/home/hermione/workspace/basex/lib";

  /** Constructor. */
  private RepoManager() {

  }

  /**
   * Installs a new package.
   * @throws IOException IO exeception
   * @throws ZipException Zip exception
   */
  public static void installPackage(final String pkg) throws ZipException,
      IOException {
    final ZipFile xar = new ZipFile(new File(pkg));
    
  }

  /***/
  public static void removePackage() {

  }

  /**
   * Reads the contents of the package repository and creates a dictionary with
   * <key, value> pairs consisting of namespace and package names where the
   * namespace is found.
   * @param ctx context
   * @return repository dictionary
   * @throws IOException IO exception
   */
  public static TokenObjMap<TokenList> readRepo(final Context ctx,
      final TokenMap pkgsInst) throws IOException {
    final TokenObjMap<TokenList> repoDict = new TokenObjMap<TokenList>();
    // TODO: repo location shall be kept as constant somewhere
    final File repoDir = new File(REPO);
    final File[] pkgs = repoDir.listFiles();
    for(int i = 0; i < pkgs.length; i++) {
      if(pkgs[i].isDirectory()) {
        final File pkgDesc = new File(pkgs[i].getPath() + "/expath-pkg.xml");
        if(!pkgDesc.exists()) {
          // TODO: Error: Missing package descriptor.
        } else {
          readPkg(pkgDesc, repoDict, ctx, pkgsInst);
        }
      }
    }
    return repoDict;
  }

  /**
   * Reads the package descriptor of a package and adds the components'
   * namespaces the repository dictionary.
   * @param pkgDesc package descriptor
   * @param repoDict repository dictionary
   * @param ctx context
   * @throws IOException IO exception
   */
  private static void readPkg(final File pkgDesc,
      final TokenObjMap<TokenList> repoDict, final Context ctx,
      final TokenMap pkgsInst) throws IOException {
    final IOFile io = new IOFile(pkgDesc);
    final Parser p = Parser.xmlParser(io, ctx.prop, "");
    final ANode pkg = new DBNode(MemBuilder.build(p, ctx.prop, ""), 0).children().next();
    final ANode xqueryComp = getChild(pkg, XQUERY);
    if(xqueryComp == null) {
      // TODO: error: no xquery module specified - this check shall be done
      // during package installation
    } else {
      final ANode ns = getChild(xqueryComp, NMSPC);
      if(ns == null) {
        // TODO: this check shall be done during package installation
      } else {
        if(repoDict.get(ns.atom()) != null) {
          repoDict.get(ns.atom()).add(pkg.attribute(new QNm(NAME)));
        } else {
          final TokenList vals = new TokenList();
          vals.add(pkg.attribute(new QNm(NAME)));
          repoDict.add(ns.atom(), vals);
        }
        final TokenBuilder tb = new TokenBuilder();
        tb.add(token(REPO)).add(token("/")).add(pkg.attribute(new QNm(ABBREV)));
        // TODO: use later pkg name + version as a key
        pkgsInst.add(pkg.attribute(new QNm(NAME)), tb.finish());
      }
    }
  }

  /**
   * Returns the child node with the specified name.
   * @param node node
   * @param name child name
   * @return child node
   */
  private static ANode getChild(final ANode node, final byte[] name) {
    final NodeMore ch = node.children();
    while(true) {
      final ANode next = ch.next();
      if(ch == null) return null;
      if(eq(name, next.nname())) return next;
    }
  }

  /**
   * Searches for an installed package in the repository descriptor.
   * @param pkgName package uri
   * @param ctx context
   * @return package
   * @throws IOException IO exception
   */
  public static Package searchPackage(final byte[] pkgName, final Context ctx)
      throws IOException {
    // Look for requested package in package repository
    final IOFile io = new IOFile(
        "/home/hermione/workspace/basex/lib/.expath-pkg/packages.xml");
    final Parser p = Parser.xmlParser(io, ctx.prop, "");
    final DBNode dbNode = new DBNode(MemBuilder.build(p, ctx.prop, ""), 0);
    // children of <packages/>
    final NodeMore pkgs = dbNode.children().next().children();
    ANode nextPkg;
    byte[] dir;
    while((nextPkg = pkgs.next()) != null) {
      if(eq(nextPkg.attribute(new QNm(NAME)), pkgName)) {
        dir = nextPkg.attribute(new QNm(DIR));
        return extractPkg("/home/hermione/workspace/basex/lib/" + string(dir),
            ctx);
      }
    }
    return null;
  }

  /**
   * Looks in the package descriptor of the given dir and extracts a package
   * container for the given package.
   * @param dir package directory
   * @param ctx context
   * @return package container
   * @throws IOException IO exception
   */
  private static Package extractPkg(final String dir, final Context ctx)
      throws IOException {
    // Get package descriptor
    final IOFile io = new IOFile(dir + "/expath-pkg.xml");
    final Parser p = Parser.xmlParser(io, ctx.prop, "");
    final DBNode dbNode = new DBNode(MemBuilder.build(p, ctx.prop, ""), 0);
    // Parse package
    return PackageParser.parse(dbNode.children().next());
  }
  
  /**
   * Checks package consistency.
   */
  private static void check(final ZipFile pkg, final Context ctx) {
    
  }
}
