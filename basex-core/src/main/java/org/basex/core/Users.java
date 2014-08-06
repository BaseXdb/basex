package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.basex.io.*;
//import org.basex.io.parse.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class organizes all users.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Users {
  /** User array. */
  private final ArrayList<User> list = new ArrayList<>(0);
  /** Filename; set to {@code null} if the instance handles local users. */
  private IOFile file;

  /**
   * Constructor for global users.
   * @param ctx database context ({@code null} if instance is local)
   */
  public Users(final Context ctx) {
    if(ctx == null) return;

    // try to find permission file in database and home directory
    final String perm = "permissions.xml";
    file = new IOFile(ctx.globalopts.dbpath(), perm);

    if(!file.exists()) file = new IOFile(Prop.HOME, perm);

    if(file.exists()) {
      try {
        File in = new File("/home/tejus/permissions.xml");
        read(in);
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else {
      // define default admin user with all rights
      list.add(new User(S_ADMIN, md5(S_ADMIN), md5(D_ADMIN), Perm.ADMIN));
    }
  }

  /**
   * Reads users from disk.
   * @param in input stream
   * @throws Exception I/O exception
   */

  public synchronized void read(final File in) throws Exception {

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(in);
    doc.getDocumentElement().normalize();

    NodeList nList = doc.getElementsByTagName("user");

    for(int temp = 0; temp < nList.getLength(); temp++) {
      Node nNode = nList.item(temp);
      if(nNode.getNodeType() == Node.ELEMENT_NODE)
      ;
      Element eElement = (Element) nNode;
      if(eElement.hasAttribute("name")) // && eElement.getAttribute("name").equals("user"))
      {
        String n, md5, dig, perm;
        n = eElement.getAttribute("name");
        perm = eElement.getAttribute("perm");
        md5 = eElement.getElementsByTagName("password").item(0).getTextContent();
        dig = eElement.getElementsByTagName("password").item(1).getTextContent();
        // System.out.println(n+" "+perm+" "+md5+" "+dig);
        // System.out.println(Perm.get(perm));
        list.add(new User(n, md5, dig, Perm.get(perm)));
      }
    }
  }

  /**
   * Writes global permissions to disk.
   */
  public synchronized void write() {
    if(file == null) return;
    try {
      File out = new File("/home/tejus/permissions.xml");
      write(out);
    } catch(Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Stores a user and encrypted password.
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public synchronized boolean create(final String usern, final String pass) {
    // check if user already exists
    return get(usern) == null && create(new User(usern, pass, pass, Perm.NONE));
  }

  /**
   * Adds the specified user.
   * @param user user to be added
   * @return success of operation
   */
  public synchronized boolean create(final User user) {
    list.add(user);
    write();
    return true;
  }

  /**
   * Changes the password of a user.
   * @param usern user name
   * @param pass password
   * @return success of operation
   */
  public synchronized boolean alter(final String usern, final String pass) {
    // check if user already exists
    final User user = get(usern);
    if(user == null) return false;

    user.password = pass.toLowerCase(Locale.ENGLISH);
    write();
    return true;
  }

  /**
   * Drops a user from the list.
   * @param user user reference
   * @return success flag
   */
  public synchronized boolean drop(final User user) {
    if(!list.remove(user)) return false;
    write();
    return true;
  }

  /**
   * Returns a user reference with the specified name.
   * @param usern user name
   * @return success of operation
   */
  public synchronized User get(final String usern) {
    for(final User user : list)
      if(user.name.equals(usern)) return user;
    return null;
  }

  /**
   * Returns all users that match the specified pattern.
   * @param pattern user pattern
   * @return user list
   */

  public synchronized String[] find(final Pattern pattern) {
    final StringList sl = new StringList();
    for(final User u : list) {
      if(pattern.matcher(u.name).matches()) sl.add(u.name);
    }
    return sl.toArray();
  }

  /**
   * Writes permissions to disk.
   * @param out output stream; if set to null, the global rights are written
   * @throws IOException I/O exception
   * @throws SAXException SAX exception
   */
  public synchronized void write(final File out) throws IOException, SAXException {

    // skip writing of local rights
    // TODO local permissions
    try {

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

      Document doc = docBuilder.parse(out);

      Element rootElement = doc.getDocumentElement();

      // int index = list.size() + 1;

      for(final User user : list) {

        // user elements
        Element usr = doc.createElement("user");
        rootElement.appendChild(usr);

        // set attribute to user element
        Attr attr = doc.createAttribute("name");
        Attr att = doc.createAttribute("perm");
        attr.setValue(user.name);
        att.setValue(user.perm.toString());

        // att.setValue(user.perm.NONE.toString());
        // att.setValue(Perm.NONE.toString());
        usr.setAttributeNode(attr);
        usr.setAttributeNode(att);

        // md5 password element
        Element password = doc.createElement("password");
        password.appendChild(doc.createTextNode(user.password));
        usr.appendChild(password);

        // digest password element
        Element pass = doc.createElement("password");
        pass.appendChild(doc.createTextNode(user.digest));
        usr.appendChild(pass);
      }
      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      // StreamResult result = new StreamResult(new File("/home/tejus/permissions.xml"));
      StreamResult result = new StreamResult(out);
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(source, result);

      // System.out.println("File saved!");

    } catch(ParserConfigurationException pce) {
      pce.printStackTrace();
    } catch(TransformerException tfe) {
      tfe.printStackTrace();
    }

  }

  /**
   * Returns information on all users.
   * @param users optional global user list (for ignoring obsolete local users)
   * @return user information
   */
  public synchronized Table info(final Users users) {
    final Table table = new Table();
    table.description = USERS_X;

    final int sz = file == null ? 3 : 5;
    for(int u = 0; u < sz; ++u)
      table.header.add(S_USERINFO[u]);

    for(final User user : users(users)) {
      final TokenList tl = new TokenList();
      tl.add(user.name);
      tl.add(user.has(Perm.READ) ? "X" : "");
      tl.add(user.has(Perm.WRITE) ? "X" : "");
      if(sz == 5) {
        tl.add(user.has(Perm.CREATE) ? "X" : "");
        tl.add(user.has(Perm.ADMIN) ? "X" : "");
      }
      table.contents.add(tl);
    }
    return table.sort().toTop(token(S_ADMIN));
  }

  /**
   * Returns all users.
   * @param users optional second list
   * @return user information
   */
  public synchronized User[] users(final Users users) {
    final ArrayList<User> al = new ArrayList<>();
    for(final User user : list) {
      if(users == null || users.get(user.name) != null) al.add(user);
    }
    return al.toArray(new User[al.size()]);
  }
}
