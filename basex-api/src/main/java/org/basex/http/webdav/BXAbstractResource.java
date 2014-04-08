package org.basex.http.webdav;

import static org.basex.http.webdav.impl.Utils.*;
import static com.bradmcevoy.http.LockResult.*;
import java.io.*;
import java.util.*;

import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.WebDAVService;
import org.basex.server.LoginException;
import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * WebDAV resource representing an abstract folder within a collection database.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXAbstractResource implements CopyableResource, DeletableResource, MoveableResource,
    LockableResource {

  /** Resource meta data. */
  final ResourceMetaData meta;
  /** WebDAV service implementation. */
  final WebDAVService<BXAbstractResource> service;

  /**
   * Constructor.
   * @param m resource meta data
   * @param s service
   */
  BXAbstractResource(final ResourceMetaData m, final WebDAVService<BXAbstractResource> s) {
    meta = m;
    service = s;
  }

  @Override
  public Object authenticate(final String user, final String pass) {
    if(user == null) return null;
    return new BXCode<Object>(this) {
      @Override
      public String get() throws LoginException {
        service.authenticate(user, pass);
        return user;
      }
    }.evalNoEx();
  }

  @Override
  public boolean authorise(final Request request, final Request.Method method, final Auth auth) {
    return auth != null && auth.getTag() != null && service.authorize(auth.getUser(), "any", meta.db, meta.path);
  }

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }

  @Override
  public String getRealm() {
    return Prop.NAME;
  }

  @Override
  public String getUniqueId() {
    return null;
  }

  @Override
  public String getName() {
    return name(meta.path);
  }

  @Override
  public Date getModifiedDate() {
    return meta.mdate;
  }

  @Override
  public void delete() throws BadRequestException, NotAuthorizedException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        del();
      }
    }.eval();
  }

  @Override
  public void copyTo(final CollectionResource target, final String name) throws BadRequestException,
      NotAuthorizedException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof BXRoot)
          copyToRoot(name);
        else if(target instanceof BXFolder)
          copyTo((BXFolder) target, name);
      }
    }.eval();
  }

  @Override
  public void moveTo(final CollectionResource target, final String name) throws BadRequestException,
      NotAuthorizedException {

    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        if(target instanceof BXRoot)
          moveToRoot(name);
        else if(target instanceof BXFolder)
          moveTo((BXFolder) target, name);
      }
    }.eval();
  }

  /**
   * Lock this resource and return a token.
   *
   * @param timeout - in seconds, or null
   * @param lockInfo lock info
   * @return - a result containing the token representing the lock if successful,
   * otherwise a failure reason code
   */
  @Override
  public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo) throws NotAuthorizedException,
      PreConditionFailedException, LockedException {
    return new BXCode<LockResult>(this) {
      @Override
      public LockResult get() throws IOException {
        return lockResource(timeout, lockInfo);
      }
    }.evalNoEx();
  }

  /**
   * Renew the lock and return new lock info.
   *
   * @param token lock token
   * @return lock result
   */
  @Override
  public LockResult refreshLock(final String token) throws NotAuthorizedException,
      PreConditionFailedException {
    return new BXCode<LockResult>(this) {
      @Override
      public LockResult get() throws IOException {
        return refresh(token);
      }
    }.evalNoEx();
  }

  /**
   * If the resource is currently locked, and the tokenId  matches the current
   * one, unlock the resource.
   *
   * @param tokenId lock token
   */
  @Override
  public void unlock(final String tokenId) throws NotAuthorizedException,
      PreConditionFailedException {
    new BXCode<Object>(this) {
      @Override
      public void run() throws IOException {
        service.locking.unlock(tokenId);
      }
    }.evalNoEx();
  }

  /**
   * Get the active lock for the current resource.
   * @return - the current lock, if the resource is locked, or null
   */
  @Override
  public LockToken getCurrentLock() {
    return new BXCode<LockToken>(this) {
      @Override
      public LockToken get() throws IOException {
        return getCurrentActiveLock();
      }
    }.evalNoEx();
  }

  /**
   * Delete document or folder.
   * @throws IOException I/O exception
   */
  void del() throws IOException {
    service.delete(meta.db, meta.path);
  }

  /**
   * Rename document or folder.
   * @param n new name
   * @throws IOException I/O exception
   */
  void rename(final String n) throws IOException {
    service.rename(meta.db, meta.path, n);
  }

  /**
   * Copy folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  protected abstract void copyToRoot(final String n) throws IOException;

  /**
   * Copy folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  protected abstract void copyTo(final BXFolder f, final String n) throws IOException;

  /**
   * Move folder to the root, creating a new database.
   * @param n new name of the folder (database)
   * @throws IOException I/O exception
   */
  void moveToRoot(final String n) throws IOException {
    // folder is moved to the root: create new database with it
    copyToRoot(n);
    del();
  }

  /**
   * Move folder to another folder.
   * @param f target folder
   * @param n new name of the folder
   * @throws IOException I/O exception
   */
  final void moveTo(final BXFolder f, final String n) throws IOException {
    if(f.meta.db.equals(meta.db)) {
      // folder is moved to a folder in the same database
      rename(f.meta.path + SEP + n);
    } else {
      // folder is moved to a folder in another database
      copyTo(f, n);
      del();
    }
  }

  /**
   * Lock the current resource.
   * @param timeout lock timeout
   * @param lockInfo lock info
   * @return lock result
   * @throws IOException I/O exception
   */
  final LockResult lockResource(final LockTimeout timeout, final LockInfo lockInfo) throws IOException {

    final String tokenId = service.locking.lock(meta.db, meta.path,
      lockInfo.scope.name().toLowerCase(Locale.ENGLISH),
      lockInfo.type.name().toLowerCase(Locale.ENGLISH),
      lockInfo.depth.name().toLowerCase(Locale.ENGLISH),
      lockInfo.lockedByUser,
      timeout.getSeconds());

    // TODO failed(failureReason);
    return success(new LockToken(tokenId, lockInfo, timeout));
  }

  /**
   * Get the active lock on the current resource.
   * @return the token of the active lock or {@code null} if resource is not locked
   * @throws IOException I/O exception
   */
  final LockToken getCurrentActiveLock() throws IOException {
    final String lockInfoStr = service.locking.lock(meta.db, meta.path);
    return lockInfoStr == null ? null : parseLockInfo(lockInfoStr);
  }

  /**
   * Renew a lock with the given token.
   * @param token lock token
   * @return lock result
   * @throws IOException I/O exception
   */
  final LockResult refresh(final String token) throws IOException {
    service.locking.refreshLock(token);
    final String lockInfoStr = service.locking.lock(token);
    final LockToken lockToken = lockInfoStr == null ? null : parseLockInfo(lockInfoStr);
    // TODO failed(failureReason);
    return success(lockToken);
  }

  /**
   * Parse the lock info.
   * @param lockInfo lock info as a string
   * @return parsed lock info bean
   * @throws IOException I/O exception
   */
  private static LockToken parseLockInfo(final String lockInfo) throws IOException {
    try {
      final XMLReader reader = XMLReaderFactory.createXMLReader();
      final LockTokenSaxHandler handler = new LockTokenSaxHandler();
      reader.setContentHandler(handler);
      reader.parse(new InputSource(new StringReader(lockInfo)));
      return handler.lockToken;
    } catch(final SAXException ex) {
      Util.err("Error while parsing lock info.");
      Util.debug(ex);
      return null;
    }
  }

  /** SAX handler for lock token. */
  private static final class LockTokenSaxHandler extends DefaultHandler {
    /** Parsed lock token. */
    public final LockToken lockToken = new LockToken(null, new LockInfo(), null);
    /** Current element name. */
    private String elementName;

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes attributes)
        throws SAXException {
      elementName = localName;
      super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
      elementName = null;
      super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
      final String v = String.valueOf(ch, start, length);
      if (elementName != null) {
        switch (elementName) {
          case "token":
            lockToken.tokenId = v;
            break;
          case "scope":
            lockToken.info.scope = LockInfo.LockScope.valueOf(v.toUpperCase(Locale.ENGLISH));
            break;
          case "type":
            lockToken.info.type = LockInfo.LockType.valueOf(v.toUpperCase(Locale.ENGLISH));
            break;
          case "depth":
            lockToken.info.depth = LockInfo.LockDepth.valueOf(v.toUpperCase(Locale.ENGLISH));
            break;
          case "owner":
            lockToken.info.lockedByUser = v;
            break;
          case "timeout":
            lockToken.timeout = LockTimeout.parseTimeout(v);
            break;
        }
      }
    }
  }
}
