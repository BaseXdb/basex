package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.build.*;
import org.basex.build.file.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNZip extends StandardFunc {
  /** Element: zip:file. */
  private static final QNm E_FILE = new QNm("zip:file", ZIPURI);
  /** Element: zip:dir. */
  private static final QNm E_DIR = new QNm("zip:dir", ZIPURI);
  /** Element: zip:entry. */
  private static final QNm E_ENTRY = new QNm("zip:entry", ZIPURI);
  /** Attribute: href. */
  private static final QNm A_HREF = new QNm("href");
  /** Attribute: name. */
  private static final QNm A_NAME = new QNm("name");
  /** Attribute: src. */
  private static final QNm A_SRC = new QNm("src");
  /** Attribute: src. */
  private static final QNm A_METHOD = new QNm("method");
  /** Method "base64". */
  private static final String M_BASE64 = "base64";
  /** Method "hex". */
  private static final String M_HEX = "hex";

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNZip(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _ZIP_BINARY_ENTRY:     return binaryEntry(ctx);
      case _ZIP_TEXT_ENTRY:       return textEntry(ctx);
      case _ZIP_HTML_ENTRY:       return xmlEntry(ctx, true);
      case _ZIP_XML_ENTRY:        return xmlEntry(ctx, false);
      case _ZIP_ENTRIES:          return entries(ctx);
      case _ZIP_ZIP_FILE:         return zipFile(ctx);
      case _ZIP_UPDATE_ENTRIES:   return updateEntries(ctx);
      default:                    return super.item(ctx, ii);
    }
  }

  /**
   * Returns a xs:base64Binary item, created from a binary file.
   * Returns a binary entry.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private B64 binaryEntry(final QueryContext ctx) throws QueryException {
    return new B64(entry(ctx));
  }

  /**
   * Returns a string, created from a text file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private Str textEntry(final QueryContext ctx) throws QueryException {
    final String enc = expr.length < 3 ? null : string(checkStr(expr[2], ctx));
    final IO io = new IOContent(entry(ctx));
    try {
      return Str.get(new NewlineInput(io).encoding(enc).content());
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    }
  }

  /**
   * Returns a document node, created from an XML or HTML file.
   * @param ctx query context
   * @param html html flag
   * @return binary result
   * @throws QueryException query exception
   */
  private ANode xmlEntry(final QueryContext ctx, final boolean html)
      throws QueryException {

    final Prop prop = ctx.context.prop;
    final IO io = new IOContent(entry(ctx));
    try {
      return new DBNode(html ? new HTMLParser(io, prop) : Parser.xmlParser(io, prop));
    } catch(final IOException ex) {
      throw SAXERR.thrw(input, ex);
    }
  }

  /**
   * Returns a zip archive description.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private ANode entries(final QueryContext ctx) throws QueryException {
    final String file = string(checkStr(expr[0], ctx));

    // check file path
    final IOFile path = new IOFile(file);
    if(!path.exists()) ZIPNOTFOUND.thrw(input, file);
    // loop through file
    ZipFile zf = null;
    try {
      zf = new ZipFile(file);
      // create result node
      final FElem root = new FElem(E_FILE, new Atts(ZIP, ZIPURI));
      root.add(new FAttr(A_HREF, token(path.path())));
      createEntries(paths(zf).iterator(), root, "");
      return root;
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zf != null) try { zf.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Creates the zip archive nodes in a recursive manner.
   * @param it iterator
   * @param par parent node
   * @param pref directory prefix
   * @return current prefix
   */
  private static String createEntries(final Iterator<String> it, final FElem par,
      final String pref) {

    String path = null;
    boolean curr = false;
    while(curr || it.hasNext()) {
      if(!curr) {
        path = it.next();
        curr = true;
      }
      if(path == null) break;
      // current entry is located in a higher/other directory
      if(!path.startsWith(pref)) return path;

      // current file starts with new directory
      final int i = path.lastIndexOf('/');
      final String dir = i == -1 ? path : path.substring(0, i);
      final String name = path.substring(i + 1);

      if(name.isEmpty()) {
        // path ends with slash: create directory
        path = createEntries(it, createDir(par, dir), dir);
      } else {
        // create file
        createFile(par, name);
        curr = false;
      }
    }
    return null;
  }

  /**
   * Creates a directory element.
   * @param par parent node
   * @param name name of directory
   * @return element
   */
  private static FElem createDir(final FElem par, final String name) {
    final FElem e = new FElem(E_DIR);
    e.add(new FAttr(A_NAME, token(name)));
    par.add(e);
    return e;
  }

  /**
   * Creates a file element.
   * @param par parent node
   * @param name name of directory
   */
  private static void createFile(final FElem par, final String name) {
    final FElem e = new FElem(E_ENTRY);
    e.add(new FAttr(A_NAME, token(name)));
    par.add(e);
  }

  /**
   * Creates a new zip file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private Item zipFile(final QueryContext ctx) throws QueryException {
    // check argument
    final ANode elm = (ANode) checkType(expr[0].item(ctx, input), NodeType.ELM);
    if(!elm.qname().eq(E_FILE)) ZIPUNKNOWN.thrw(input, elm.qname());
    // get file
    final String file = attribute(elm, A_HREF, true);

    // write zip file
    FileOutputStream fos = null;
    boolean ok = true;
    try {
      fos = new FileOutputStream(file);
      final ZipOutputStream zos =
        new ZipOutputStream(new BufferedOutputStream(fos));
      create(zos, elm.children(), "", null, ctx);
      zos.close();
    } catch(final IOException ex) {
      ok = false;
      ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(fos != null) {
        try { fos.close(); } catch(final IOException ex) { }
        if(!ok) new IOFile(file).delete();
      }
    }
    return null;
  }

  /**
   * Adds files to the specified zip output, or copies files from the
   * specified file.
   * @param zos output stream
   * @param ai axis iterator
   * @param root root path
   * @param ctx query context
   * @param zf original zip file (or {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void create(final ZipOutputStream zos, final AxisIter ai, final String root,
      final ZipFile zf, final QueryContext ctx) throws QueryException, IOException {

    final byte[] data = new byte[IO.BLOCKSIZE];
    for(ANode node; (node = ai.next()) != null;) {
      // get entry type
      final QNm mode = node.qname();
      final boolean dir = mode.eq(E_DIR);
      if(!dir && !mode.eq(E_ENTRY)) ZIPUNKNOWN.thrw(input, mode);

      // file path: if null, the zip base name is used
      String name = attribute(node, A_NAME, false);
      // source: if null, the node's children are serialized
      String src = attribute(node, A_SRC, false);
      if(src != null) src = src.replaceAll("\\\\", "/");

      if(name == null) {
        // throw exception if both attributes are null
        if(src == null) throw ZIPINVALID.thrw(input, node.qname(), A_SRC);
        name = src;
      }
      name = name.replaceAll(".*/", "");

      // add slash to directories
      if(dir) name += '/';
      zos.putNextEntry(new ZipEntry(root + name));

      if(dir) {
        create(zos, node.children(), root + name, zf, ctx);
      } else {
        if(src != null) {
          // write file to zip archive
          if(!new IOFile(src).exists()) ZIPNOTFOUND.thrw(input, src);

          BufferedInputStream bis = null;
          try {
            bis = new BufferedInputStream(new FileInputStream(src));
            for(int c; (c = bis.read(data)) != -1;) zos.write(data, 0, c);
          } finally {
            if(bis != null) try { bis.close(); } catch(final IOException e) { }
          }
        } else {
          // no source reference: the child nodes are treated as file contents
          final AxisIter ch = node.children();
          final String m = attribute(node, A_METHOD, false);
          // retrieve first child (might be null)
          ANode n = ch.next();

          // access original zip file if available, and if no children exist
          ZipEntry ze = null;
          if(zf != null && n == null) ze = zf.getEntry(root + name);

          if(ze != null) {
            // add old zip entry
            final InputStream zis = zf.getInputStream(ze);
            for(int c; (c = zis.read(data)) != -1;) zos.write(data, 0, c);
          } else if(n != null) {
            // write new binary content to archive
            final boolean hex = M_HEX.equals(m);
            if(hex || M_BASE64.equals(m)) {
              // treat children as base64/hex
              final ByteList bl = new ByteList();
              do bl.add(n.string()); while((n = ch.next()) != null);
              final byte[] bytes = bl.toArray();
              zos.write((hex ? new Hex(bytes) : new B64(bytes)).toJava());
            } else {
              // serialize new nodes
              try {
                final Serializer ser = Serializer.get(zos, serPar(node, ctx));
                do {
                  DataBuilder.stripNS(n, ZIPURI, ctx.context).serialize(ser);
                } while((n = ch.next()) != null);
                ser.close();
              } catch(final SerializerException ex) {
                throw ex.getCause(input);
              }
            }
          }
        }
        zos.closeEntry();
      }
    }
  }

  /**
   * Returns serialization parameters.
   * @param node node with parameters
   * @param ctx query context
   * @return properties
   * @throws SerializerException serializer exception
   */
  private static SerializerProp serPar(final ANode node, final QueryContext ctx)
      throws SerializerException {

    // interpret query parameters
    final TokenBuilder tb = new TokenBuilder();
    final AxisIter ati = node.attributes();
    for(ANode at; (at = ati.next()) != null;) {
      final QNm name = at.qname();
      if(name.eq(A_NAME) || name.eq(A_SRC)) continue;
      if(!tb.isEmpty()) tb.add(',');
      tb.add(name.local()).add('=').add(at.string());
    }
    return tb.isEmpty() ? ctx.serParams(true) : new SerializerProp(tb.toString());
  }

  /**
   * Updates a zip archive.
   * @param ctx query context
   * @return empty result
   * @throws QueryException query exception
   */
  private Item updateEntries(final QueryContext ctx) throws QueryException {
    // check argument
    final ANode elm = (ANode) checkType(expr[0].item(ctx, input), NodeType.ELM);
    if(!elm.qname().eq(E_FILE)) ZIPUNKNOWN.thrw(input, elm.qname());

    // sorted paths in original file
    final String in = attribute(elm, A_HREF, true);

    // target and temporary output file
    final IOFile target = new IOFile(string(checkStr(expr[1], ctx)));
    IOFile out;
    do {
      out = new IOFile(target.path() + new Random().nextInt(0x7FFFFFFF));
    } while(out.exists());

    // open zip file
    if(!new IOFile(in).exists()) ZIPNOTFOUND.thrw(input, in);
    ZipFile zf = null;
    boolean ok = true;
    try {
      zf = new ZipFile(in);
      // write zip file
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(out.path());
        final ZipOutputStream zos =
          new ZipOutputStream(new BufferedOutputStream(fos));
        // fill new zip file with entries from old file and description
        create(zos, elm.children(), "", zf, ctx);
        zos.close();
      } catch(final IOException ex) {
        ok = false;
        ZIPFAIL.thrw(input, ex.getMessage());
      } finally {
        if(fos != null) try { fos.close(); } catch(final IOException ex) { }
      }
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zf != null) try { zf.close(); } catch(final IOException e) { }
      if(ok) {
        // rename temporary file to final target
        target.delete();
        out.rename(target);
      } else {
        // remove temporary file
        out.delete();
      }
    }
    return null;
  }

  /**
   * Returns a list of all file paths.
   * @param zf zip file file to be parsed
   * @return binary result
   */
  private static StringList paths(final ZipFile zf) {
    // traverse all zip entries and create intermediate map,
    // as zip entries are not sorted
    //final StringList paths = new StringList();
    final TreeSet<String> paths = new TreeSet<String>();

    final Enumeration<? extends ZipEntry> en = zf.entries();
    // loop through all files
    while(en.hasMoreElements()) {
      final ZipEntry ze = en.nextElement();
      final String name = ze.getName();
      final int i = name.lastIndexOf('/');
      // add directory
      if(i > -1 && i + 1 < name.length()) paths.add(name.substring(0, i + 1));
      paths.add(name);
    }
    final StringList sl = new StringList();
    for(final String path : paths) sl.add(path);
    return sl;
  }

  /**
   * Returns the value of the specified attribute.
   * @param elm element node
   * @param name attribute to be found
   * @param force if set to {@code true}, an exception is thrown if the
   * attribute is not found
   * @return attribute value
   * @throws QueryException query exception
   */
  private String attribute(final ANode elm, final QNm name, final boolean force)
      throws QueryException {

    final byte[] val = elm.attribute(name);
    if(val == null && force) throw ZIPINVALID.thrw(input, elm.qname(), name);
    return val == null ? null : string(val);
  }

  /**
   * Returns an entry from a zip file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private byte[] entry(final QueryContext ctx) throws QueryException {
    final IOFile file = new IOFile(string(checkStr(expr[0], ctx)));
    final String path = string(checkStr(expr[1], ctx));
    if(!file.exists()) ZIPNOTFOUND.thrw(input, file);

    try {
      return new Zip(file).read(path);
    } catch(final FileNotFoundException ex) {
      throw ZIPNOTFOUND.thrw(input, file + "/" + path);
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT || super.uses(u);
  }
}
