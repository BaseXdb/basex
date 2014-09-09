package org.basex.gui.layout;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import org.basex.gui.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Organizes icons used all over the GUI.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXImages {
  /** Cached image icons. */
  private static final HashMap<String, ImageIcon> ICONS = new HashMap<>();

  /** File icon cache. */
  private static final HashMap<String, Icon> FILES = new HashMap<>();
  /** System icons. */
  private static final FileSystemView FS = FileSystemView.getFileSystemView();

  /** Icon for xml files. */
  private static final Icon XMLTEXT = icon("text_xml");
  /** Icon for raw files. */
  private static final Icon RAWTEXT = icon("text_raw");

  /** Large icons. */
  private static final boolean large = GUIConstants.HEIGHT > 16;
  /** Icon for closed directories. */
  private static final Icon DIR1 = icon("file_dir1");
  /** Icon for opened directories. */
  private static final Icon DIR2 = icon("file_dir2");
  /** Icon for textual files. */
  private static final Icon TEXT = icon("file_text");
  /** Icon for XML/XQuery file types. */
  private static final Icon XML = icon("file_xml");
  /** Icon for XML/XQuery file types. */
  private static final Icon XQUERY = icon("file_xquery");
  /** Icon for BaseX file types. */
  private static final Icon BASEX = icon("file_basex");
  /** Icon for unknown file types. */
  private static final Icon UNKNOWN = icon("file_unknown");

  /** Private constructor. */
  private BaseXImages() { }

  /**
   * Returns the specified image.
   * @param name name of image
   * @return image
   */
  public static Image get(final String name) {
    return get(url(name));
  }

  /**
   * Returns the specified image.
   * @param url image url
   * @return image
   */
  public static Image get(final URL url) {
    try {
      return ImageIO.read(url);
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns the specified image as icon.
   * @param name name of icon
   * @return icon
   */
  public static ImageIcon icon(final String name) {
    ImageIcon ii = ICONS.get(name);
    if(ii != null) return ii;

    Image img;
    if(GUIConstants.SCALE > 1) {
      // choose large image or none
      final URL url = large ? BaseXImages.class.getResource("/img/" + name + "_32.png") : null;
      if(url == null) {
        // resize low-res image
        img = get(url(name));
        final int w = (int) (img.getWidth(null) * GUIConstants.SCALE);
        final int h = (int) (img.getHeight(null) * GUIConstants.SCALE);
        final BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(img, 0, 0, w, h, null);
        g2.dispose();
        img = tmp;
      } else {
        img = get(url);
      }
    } else {
      img = get(name);
    }
    ii = new ImageIcon(img);
    ICONS.put(name, ii);
    return ii;
  }

  /**
   * Returns the image url.
   * @param name name of image
   * @return url
   */
  private static URL url(final String name) {
    final String path = "/img/" + name + ".png";
    URL url = BaseXImages.class.getResource(path);
    if(url == null) {
      Util.stack("Image not found: " + path);
      url = BaseXImages.class.getResource("/img/warn.png");
    }
    return url;
  }

  /**
   * Returns a directory icon.
   * @param expanded expanded state (open/closed)
   * @return icon
   */
  public static Icon dir(final boolean expanded) {
    return expanded ? DIR2 : DIR1;
  }

  /**
   * Returns an icon for the specified text.
   * @param raw raw/xml text
   * @return icon
   */
  public static Icon text(final boolean raw) {
    return raw ? RAWTEXT : XMLTEXT;
  }

  /**
   * Returns an icon for the specified file.
   * @param file file reference
   * @return icon
   */
  public static Icon file(final IOFile file) {
    if(file == null) return UNKNOWN;

    // fallback code for displaying icons
    final String path = file.path();
    final String mime = MimeTypes.get(path);
    if(MimeTypes.isXML(mime)) return XML;
    if(MimeTypes.isXQuery(mime)) return XQUERY;
    if(path.contains(IO.BASEXSUFFIX)) return BASEX;

    // only works with standard dpi (https://bugs.openjdk.java.net/browse/JDK-6817929)
    if(Prop.WIN && !large) {
      // retrieve system icons (only supported on Windows)
      final int p = path.lastIndexOf(path, '.');
      final String suffix = p != -1 ? path.substring(p + 1) : null;
      Icon icon = null;
      if(suffix != null) icon = FILES.get(suffix);
      if(icon == null) {
        icon = FS.getSystemIcon(file.file());
        if(suffix != null) FILES.put(suffix, icon);
      }
      return icon;
    }
    // default icon chooser
    return MimeTypes.isText(mime) ? TEXT : UNKNOWN;
  }
}
