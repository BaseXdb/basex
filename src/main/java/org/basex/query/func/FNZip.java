package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.data.SerializerException;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.B64;
import org.basex.query.item.DBNode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.AxisIter;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class FNZip extends Fun {
  /** Function namespace. */
  private static final Uri U_ZIP = Uri.uri(ZIPURI);
  /** Element: zip:file. */
  private static final QNm E_FILE = new QNm(token("file"), U_ZIP);
  /** Element: zip:dir. */
  private static final QNm E_DIR = new QNm(token("dir"), U_ZIP);
  /** Element: zip:entry. */
  private static final QNm E_ENTRY = new QNm(token("entry"), U_ZIP);
  /** Attribute: href. */
  private static final QNm A_HREF = new QNm(token("href"));
  /** Attribute: name. */
  private static final QNm A_NAME = new QNm(token("name"));
  /** Attribute: src. */
  private static final QNm A_SRC = new QNm(token("src"));

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNZip(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    checkAdmin(ctx);
    switch(def) {
      case BENTRY:    return binaryEntry(ctx);
      case TEXTENTRY: return textEntry(ctx);
      case HTMLENTRY: return xmlEntry(ctx);
      case XMLENTRY:  return xmlEntry(ctx);
      case ENTRIES:   return entries(ctx);
      case ZIPFILE:   return zipFile(ctx);
      case UPDATE:    return update();
      default: return super.item(ctx, ii);
    }
  }

  /**
   * Returns a binary entry.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private B64 binaryEntry(final QueryContext ctx) throws QueryException {
    return new B64(entry(ctx));
  }

  /**
   * Returns a binary entry.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private Str textEntry(final QueryContext ctx) throws QueryException {
    return Str.get(entry(ctx));
  }

  /**
   * Returns meta data on a zip file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private ANode entries(final QueryContext ctx) throws QueryException {
    final Uri uri = (Uri) checkType(expr[0].item(ctx, input), Type.URI);
    final String file = IOFile.file(string(uri.atom()));
    if(!IO.get(file).exists()) ZIPNOTFOUND.thrw(input, file);

    // traverse all zip entries and create map (zip entries are not sorted)
    ZipInputStream zis = null;
    try {
      zis = new ZipInputStream(new BufferedInputStream(
          new FileInputStream(file)));
      final TreeMap<String, FElem> map = new TreeMap<String, FElem>();
      ZipEntry ze;
      while((ze = zis.getNextEntry()) != null) {
        final String name = ze.getName();
        final boolean dir = ze.isDirectory();
        final FElem e = new FElem(dir ? E_DIR : E_ENTRY, null);
        e.atts.add(new FAttr(A_NAME, token(name), e));
        map.put(name, e);
      }

      // create result node
      final FElem root = new FElem(E_FILE, ZIP, ZIPURI);
      root.atts.add(new FAttr(A_HREF, token(file), root));

      final Iterator<String> it = map.keySet().iterator();
      createNode(map, it, root, "");
      return root;

    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zis != null) try { zis.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Recursively creates a zip node.
   * @param map map with all nodes
   * @param it iterator
   * @param par parent node
   * @param pref directory prefix
   * @return current prefix
   * @throws IOException I/O exception
   */
  private String createNode(final TreeMap<String, FElem> map,
      final Iterator<String> it, final FElem par, final String pref)
      throws IOException {

    String name = null;
    boolean dir = false;
    while(dir || it.hasNext()) {
      if(!dir) name = it.next();
      if(name == null) break;
      if(!name.startsWith(pref)) return name;
      final FElem e = map.get(name);
      e.parent(par);
      par.children.add(e);
      dir = name.endsWith("/");
      if(dir) name = createNode(map, it, e, name);
    }
    return null;
  }

  /**
   * Creates a new zip file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private Item zipFile(final QueryContext ctx) throws QueryException {
    // check argument
    final ANode elm = (ANode) checkType(expr[0].item(ctx, input), Type.ELM);
    if(!elm.qname().eq(E_FILE)) ZIPUNKNOWN.thrw(input, elm.qname());

    // get file file
    final String file = IOFile.file(attribute(elm, A_HREF, true));
    // write zip file
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(file)));
      createFile(zos, elm, ctx);
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zos != null) try { zos.close(); } catch(final IOException e) { }
    }
    return null;
  }

  /**
   * Recursively fills the zip file.
   * @param zos output stream
   * @param elm element
   * @param ctx query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void createFile(final ZipOutputStream zos, final ANode elm,
      final QueryContext ctx) throws QueryException, IOException {

    final AxisIter ai = elm.children();
    ANode node;
    while((node = ai.next()) != null) {
      // get entry type
      final QNm mode = node.qname();
      if(!mode.eq(E_DIR) && !mode.eq(E_ENTRY)) ZIPUNKNOWN.thrw(input, mode);

      // file path: if null, the zip basename is used
      String path = attribute(node, A_NAME, false);
      // source: if null, the node's children are serialized
      String src = attribute(node, A_SRC, false);
      if(src != null) src = src.replaceAll("\\\\", "/");

      if(path == null) {
        // throw exception if both attributes are null
        if(src == null) ZIPINVALID.thrw(input, node.qname(), A_SRC);
        path = src.replaceAll(".*/", "");
      }

      // add slash to directories
      final boolean dir = mode.eq(E_DIR);
      if(dir && !path.endsWith("/")) path += "/";

      final ZipEntry ze = new ZipEntry(path);
      zos.putNextEntry(ze);
      if(dir) continue;

      if(src != null) {
        // write file to zip archive
        final IO source = IO.get(src);
        if(!source.exists()) ZIPNOTFOUND.thrw(input, source);
        zos.write(source.content());
      } else {
        // serialize child nodes to zip archive
        try {
          final XMLSerializer xml =
            new XMLSerializer(zos, serialPar(node, ctx));
          ANode n;
          final AxisIter ch = node.children();
          while((n = ch.next()) != null) n.serialize(xml);
          xml.close();
        } catch(final SerializerException ex) {
          throw new QueryException(input, ex);
        }
      }
      zos.closeEntry();
    }
  }

  /**
   * Returns serialization parameters.
   * @param node node with parameters
   * @param ctx query context
   * @return properties
   * @throws SerializerException serializer exception
   */
  private SerializerProp serialPar(final ANode node, final QueryContext ctx)
      throws SerializerException {

    // interpret query parameters
    final TokenBuilder tb = new TokenBuilder();
    final AxisIter ati = node.atts();
    ANode at;
    while((at = ati.next()) != null) {
      final QNm name = at.qname();
      if(name.eq(A_NAME) || name.eq(A_SRC)) continue;
      if(tb.size() != 0) tb.add(',');
      tb.add(name.ln()).add('=').add(at.atom());
    }
    return tb.size() == 0 ? ctx.serProp() : new SerializerProp(tb.toString());
  }

  /**
   * Returns the value of the specified attribute.
   * @param elm element node
   * @param name attribute to be found
   * @param force force flag; if {@code true}, an exception is thrown if
   * the attribute is not found
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
   * Updates a zip file.
   * @return binary result
   * @throws QueryException query exception
   */
  private Item update() throws QueryException {
    throw NOTIMPL.thrw(input, def.desc);
  }

  /**
   * Returns an XML entry.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private ANode xmlEntry(final QueryContext ctx) throws QueryException {
    final Prop prop = ctx.context.prop;
    final IO io = new IOContent(entry(ctx));
    try {
      final Parser p = Parser.fileParser(io, prop, "");
      return new DBNode(MemBuilder.build(p, prop, ""), 0);
    } catch(final IOException ex) {
      throw SAXERR.thrw(input, ex);
    }
  }

  /**
   * Returns an entry from a zip file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private byte[] entry(final QueryContext ctx) throws QueryException {
    final Uri uri = (Uri) checkType(expr[0].item(ctx, input), Type.URI);
    final String path = string(checkStr(expr[1], ctx));

    final String file = IOFile.file(string(uri.atom()));
    if(!IO.get(file).exists()) ZIPNOTFOUND.thrw(input, file);

    ZipInputStream zis = null;
    try {
      zis = new ZipInputStream(new FileInputStream(file));
      ZipEntry ze;
      while((ze = zis.getNextEntry()) != null) {
        if(!ze.getName().equals(path)) continue;
        final int s = ze.getSize() == -1 ? IO.BLOCKSIZE : (int) ze.getSize();
        final byte[] data = new byte[Math.min(IO.BLOCKSIZE, s)];
        final ByteList bl = new ByteList(s);
        int c;
        while((c = zis.read(data)) != -1) bl.add(data, 0, c);
        return bl.toArray();
      }
      throw ZIPNOTFOUND.thrw(input, file + '/' + path);
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zis != null) try { zis.close(); } catch(final IOException e) { }
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX;
  }
}
