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
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Organizes icons used all over the GUI.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXImages {
  /** Cached images. */
  private static final HashMap<String, Image> IMAGES = new HashMap<>();
  /** Cached image icons. */
  private static final HashMap<String, ImageIcon> ICONS = new HashMap<>();

  /** File icon cache. */
  private static final HashMap<String, Icon> FILES = new HashMap<>();
  /** System icons. */
  private static final FileSystemView FS = FileSystemView.getFileSystemView();

  /** Private constructor. */
  private BaseXImages() { }

  /**
   * Returns the specified image.
   * @param name name of image
   * @return image
   */
  public static Image get(final String name) {
    if(!IMAGES.containsKey(name)) {
      final int n = 5;
      final Image[] images = new Image[n];
      for(int i = 0; i < n; i++) {
        final String path = "/img/" + name + '-' + i + ".png";
        final URL url = BaseXImages.class.getResource(path);
        if(url == null) throw Util.notExpected("Image missing: " + path);
        try {
          final BufferedImage image = ImageIO.read(url);
          images[i] = GUIConstants.dark ? invert(image) : image;
        } catch(final IOException ex) {
          throw Util.notExpected(ex);
        }
      }
      IMAGES.put(name, new BaseMultiResolutionImage(images));
    }
    return IMAGES.get(name);
  }

  /**
   * Inverts gray-scale images.
   * @param image image
   * @return inverted image, or original image if it contains colors
   */
  private static BufferedImage invert(final BufferedImage image) {
    final BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_INT_ARGB);
    for(int x = 0; x < image.getWidth(); x++) {
      for(int y = 0; y < image.getHeight(); y++) {
        final int rgb = image.getRGB(x, y);
        final int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        if(r != g || r != b) return image;
        tmp.setRGB(x, y, rgb ^ 0x00FFFFFF);
      }
    }
    return tmp;
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
   * Returns a directory icon.
   * @param opened expanded state (open/closed)
   * @return icon
   */
  public static Icon dir(final boolean opened) {
    return icon(opened ? "dir_opened" : "dir_closed");
  }

  /**
   * Returns an icon for the specified text.
   * @param type resource type
   * @return icon
   */
  public static Icon resource(final ResourceType type) {
    return icon(type == ResourceType.XML ? "db_xml" :
      type == ResourceType.BINARY ? "db_bin" : "db_val");
  }

  /**
   * Returns an icon for the specified file.
   * @param file file reference
   * @return icon
   */
  public static Icon file(final IOFile file) {
    String name = "file_text";
    if(file != null) {
      final String path = file.path();
      final MediaType type = MediaType.get(path);
      if(type.isXml()) {
        name = "file_xml";
      } else if(type.isXQuery()) {
        name = "file_xquery";
      } else if(Prop.WIN) {
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
    }
    return icon(name);
  }
}
