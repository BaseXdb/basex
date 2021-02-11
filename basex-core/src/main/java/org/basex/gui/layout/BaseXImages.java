package org.basex.gui.layout;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Organizes icons used all over the GUI.
 *
 * @author BaseX Team 2005-21, BSD License
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
    return ICONS.computeIfAbsent(name, n -> new ImageIcon(get(n)));
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
    final MediaType type = MediaType.get(path);
    if(type.isXML()) return XML;
    if(type.isXQuery()) return XQUERY;
    if(path.contains(IO.BASEXSUFFIX)) return BASEX;

    if(Prop.WIN) {
      // retrieve system icons (only supported on Windows)
      final int p = path.lastIndexOf(path, '.');
      final String suffix = p == -1 ? null : path.substring(p + 1);
      Icon icon = null;
      if(suffix != null) icon = FILES.get(suffix);
      if(icon == null) {
        icon = FS.getSystemIcon(file.file());
        if(suffix != null) FILES.put(suffix, icon);
      }
      return icon;
    }
    // default icon chooser
    return type.isText() ? TEXT : UNKNOWN;
  }
}
