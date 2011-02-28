package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.file.HTMLParser;
import org.basex.core.Prop;
import org.basex.data.SerializerException;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.io.TextInput;
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
  private static final QNm E_FILE = new QNm(token("zip:file"), U_ZIP);
  /** Element: zip:dir. */
  private static final QNm E_DIR = new QNm(token("zip:dir"), U_ZIP);
  /** Element: zip:entry. */
  private static final QNm E_ENTRY = new QNm(token("zip:entry"), U_ZIP);
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
      case HTMLENTRY: return xmlEntry(ctx, true);
      case XMLENTRY:  return xmlEntry(ctx, false);
      case ENTRIES:   return entries(ctx);
      case ZIPFILE:   return zipFile(ctx);
      case UPDATE:    return update();
      default: return super.item(ctx, ii);
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
      return Str.get(TextInput.content(io, enc).finish());
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
      final Parser p = html ? new HTMLParser(io, "", prop) :
        Parser.xmlParser(io, prop, "");
      return new DBNode(MemBuilder.build(p, prop, ""), 0);
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
    //final Uri uri = (Uri) checkType(expr[0].item(ctx, input), Type.URI);
    final String file = string(checkStr(expr[0], ctx));
    if(!IO.get(file).exists()) ZIPNOTFOUND.thrw(input, file);

    // traverse all zip entries and create intermediate map,
    // as zip entries are not sorted
    final TreeMap<String, FElem> map = new TreeMap<String, FElem>();
    ZipFile zf = null;
    try {
      zf = new ZipFile(file);
      final Enumeration<? extends ZipEntry> en = zf.entries();
      // loop through all files
      while(en.hasMoreElements()) {
        final ZipEntry ze = en.nextElement();
        final FElem e = new FElem(ze.isDirectory() ? E_DIR : E_ENTRY, null);
        final String name = ze.getName();
        e.atts.add(new FAttr(A_NAME, token(name), e));
        map.put(name, e);
      }
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zf != null) try { zf.close(); } catch(final IOException e) { }
    }

    // create result node
    final FElem root = new FElem(E_FILE, ZIP, ZIPURI);
    root.atts.add(new FAttr(A_HREF, token(file), root));
    final Iterator<String> it = map.keySet().iterator();
    createEntries(map, it, root, "");
    return root;
  }

  /**
   * Creates the zip archive nodes in a recursive manner.
   * @param map map with all nodes
   * @param it iterator
   * @param par parent node
   * @param pref directory prefix
   * @return current prefix
   */
  private String createEntries(final TreeMap<String, FElem> map,
      final Iterator<String> it, final FElem par, final String pref) {

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
      if(dir) name = createEntries(map, it, e, name);
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

    // get file
    final String file = IOFile.file(attribute(elm, A_HREF, true));
    // write zip file
    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(file)));
      createFile(zos, elm, ctx);
    } catch(final IOException ex) {
      ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      try {
        if(zos != null) zos.close();
      } catch(final IOException ex) {
        ZIPFAIL.thrw(input, ex.getMessage());
      }
    }
    return null;
  }

  /**
   * Adds a file to the specified zip output.
   * @param zos output stream
   * @param elm element
   * @param ctx query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void createFile(final ZipOutputStream zos, final ANode elm,
      final QueryContext ctx) throws QueryException, IOException {

    final byte[] data = new byte[IO.BLOCKSIZE];
    final AxisIter ai = elm.children();
    ANode node;
    while((node = ai.next()) != null) {
      // get entry type
      final QNm mode = node.qname();
      if(!mode.eq(E_DIR) && !mode.eq(E_ENTRY)) ZIPUNKNOWN.thrw(input, mode);

      // file path: if null, the zip base name is used
      String path = attribute(node, A_NAME, false);
      // source: if null, the node's children are serialized
      String src = attribute(node, A_SRC, false);
      if(src != null) src = src.replaceAll("\\\\", "/");

      if(path == null) {
        // throw exception if both attributes are null
        if(src == null) throw ZIPINVALID.thrw(input, node.qname(), A_SRC);
        path = src.replaceAll(".*/", "");
      }

      // add slash to directories
      final boolean dir = mode.eq(E_DIR);
      if(dir && !path.endsWith("/")) path += "/";

      zos.putNextEntry(new ZipEntry(path));
      if(dir) continue;

      if(src != null) {
        // write file to zip archive
        if(!IO.get(src).exists()) ZIPNOTFOUND.thrw(input, src);

        BufferedInputStream bis = null;
        try {
          bis = new BufferedInputStream(new FileInputStream(src));
          int c;
          while((c = bis.read(data)) != -1) zos.write(data, 0, c);
        } finally {
          if(bis != null) try { bis.close(); } catch(final IOException e) { }
        }
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
   * Returns an entry from a zip file.
   * @param ctx query context
   * @return binary result
   * @throws QueryException query exception
   */
  private byte[] entry(final QueryContext ctx) throws QueryException {
    //final Uri uri = (Uri) checkType(expr[0].item(ctx, input), Type.URI);
    final String file = string(checkStr(expr[0], ctx));
    final String path = string(checkStr(expr[1], ctx));
    if(!IO.get(file).exists()) ZIPNOTFOUND.thrw(input, file);

    ZipFile zf = null;
    try {
      zf = new ZipFile(file);
      final ZipEntry ze = zf.getEntry(path);
      if(ze == null) throw ZIPNOTFOUND.thrw(input, file + '/' + path);

      final InputStream zis = zf.getInputStream(ze);
      final int s = (int) ze.getSize();
      if(s >= 0) {
        // known size: pre-allocate and fill array
        final byte[] data = new byte[s];
        int c, o = 0;
        while(s - o != 0 && (c = zis.read(data, o, s - o)) != -1) o += c;
        return data;
      }
      // unknown size: use byte list
      final byte[] data = new byte[IO.BLOCKSIZE];
      final ByteList bl = new ByteList();
      int c;
      while((c = zis.read(data)) != -1) bl.add(data, 0, c);
      return bl.toArray();
    } catch(final IOException ex) {
      throw ZIPFAIL.thrw(input, ex.getMessage());
    } finally {
      if(zf != null) try { zf.close(); } catch(final IOException e) { }
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX;
  }
}
