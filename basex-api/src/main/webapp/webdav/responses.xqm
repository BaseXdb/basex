(:~
 : Generation of WebDAV responses: hrefs, live properties and multistatus bodies.
 :
 : @author BaseX Team, BSD License
 :)
module namespace resp = 'webdav/responses';

import module namespace res = 'webdav/resources' at 'resources.xqm';
import module namespace lock = 'webdav/locks' at 'locks.xqm';

declare namespace DAV = 'DAV:';

(:~ Status lines used in multistatus responses. :)
declare variable $resp:OK := 'HTTP/1.1 200 OK';
declare variable $resp:FORBIDDEN := 'HTTP/1.1 403 Forbidden';
declare variable $resp:NOT-FOUND := 'HTTP/1.1 404 Not Found';

(:~
 : Returns the URI of a resource. Collections are given a trailing slash.
 : @param  $rc  resource description
 : @return percent-encoded href
 :)
declare function resp:href(
  $rc  as res:any
) as xs:string {
  resp:encode(res:path($rc)) || (if ($rc?kind = 'resource') { '' } else { '/' })
};

(:~
 : Percent-encodes a path below the WebDAV root. web:encode-url cannot be used
 : here: it applies form encoding, which would turn spaces into plus signs.
 : @param  $path  path below the WebDAV root
 : @return href
 :)
declare function resp:encode(
  $path  as xs:string
) as xs:string {
  $res:ROOT || string-join(tokenize($path, '/')[.] ! ('/' || encode-for-uri(.)))
};

(:~
 : Formats a timestamp as an HTTP date (RFC 1123).
 : @param  $dt  timestamp
 : @return date string
 :)
declare function resp:http-date(
  $dt  as xs:dateTime
) as xs:string {
  format-dateTime(adjust-dateTime-to-timezone($dt, xs:dayTimeDuration('PT0S')),
    '[FNn,*-3], [D01] [MNn,*-3] [Y0001] [H01]:[m01]:[s01] GMT', 'en', (), ())
};

(:~
 : Returns the entity tag of a resource.
 : @param  $rc  resource description
 : @return entity tag
 :)
declare function resp:etag(
  $rc  as res:resource
) as xs:string {
  '"' || replace(string($rc?modified), '\D', '') || '-' || $rc?size || '"'
};

(:~
 : Returns the live properties of a resource.
 : @param  $rc     resource description
 : @param  $locks  active locks
 : @return properties
 :)
declare function resp:properties(
  $rc     as res:any,
  $locks  as lock:lock*
) as element()* {
  <DAV:displayname>{ $rc?name }</DAV:displayname>,
  <DAV:creationdate>{
    adjust-dateTime-to-timezone($rc?modified, xs:dayTimeDuration('PT0S'))
  }</DAV:creationdate>,
  <DAV:getlastmodified>{ resp:http-date($rc?modified) }</DAV:getlastmodified>,
  <DAV:supportedlock>{
    for $scope in ('exclusive', 'shared')
    return <DAV:lockentry>
      <DAV:lockscope>{ element { QName('DAV:', 'DAV:' || $scope) } {} }</DAV:lockscope>
      <DAV:locktype><DAV:write/></DAV:locktype>
    </DAV:lockentry>
  }</DAV:supportedlock>,
  <DAV:lockdiscovery>{
    lock:covering($locks, res:path($rc)) ! resp:activelock(.)
  }</DAV:lockdiscovery>,
  if ($rc?kind = 'resource') {
    <DAV:resourcetype/>,
    (: for XML resources, the reported size is a node count, not a byte length :)
    if ($rc?type != 'xml') { <DAV:getcontentlength>{ $rc?size }</DAV:getcontentlength> },
    <DAV:getcontenttype>{ $rc?content-type }</DAV:getcontenttype>,
    <DAV:getetag>{ resp:etag($rc) }</DAV:getetag>
  } else {
    <DAV:resourcetype><DAV:collection/></DAV:resourcetype>
  }
};

(:~
 : Creates the response element for a single resource.
 : @param  $rc       resource description
 : @param  $request  parsed PROPFIND request body (empty for allprop)
 : @param  $locks    active locks
 : @return response element
 :)
declare function resp:response(
  $rc       as res:any,
  $request  as element(DAV:propfind)?,
  $locks    as lock:lock*
) as element(DAV:response) {
  let $properties := resp:properties($rc, $locks)
  return <DAV:response>{
    <DAV:href>{ resp:href($rc) }</DAV:href>,
    if ($request/DAV:propname) {
      resp:propstat($properties ! element { node-name(.) } {}, $resp:OK)
    } else if (empty($request) or $request/DAV:allprop) {
      resp:propstat($properties, $resp:OK)
    } else {
      let $requested := $request/DAV:prop/*
      let $names := $properties ! node-name(.)
      return (
        resp:propstat($properties[node-name(.) = $requested ! node-name(.)], $resp:OK),
        resp:propstat($requested[not(node-name(.) = $names)] ! element { node-name(.) } {},
          $resp:NOT-FOUND)
      )
    }
  }</DAV:response>
};

(:~
 : Creates the response element for a property update. All properties are rejected:
 : live properties are derived from the stored resources, dead ones are not supported.
 : @param  $rc       resource description
 : @param  $request  parsed PROPPATCH request body
 : @return response element
 :)
declare function resp:propertyupdate(
  $rc       as res:any,
  $request  as element(DAV:propertyupdate)
) as element(DAV:response) {
  <DAV:response>{
    <DAV:href>{ resp:href($rc) }</DAV:href>,
    resp:propstat($request/(DAV:set | DAV:remove)/DAV:prop/* ! element { node-name(.) } {},
      $resp:FORBIDDEN)
  }</DAV:response>
};

(:~
 : Reports a list of properties with a status line.
 : @param  $properties  properties
 : @param  $status      status line
 : @return propstat element, or empty sequence if no properties are supplied
 :)
declare %private function resp:propstat(
  $properties  as element()*,
  $status      as xs:string
) as element(DAV:propstat)? {
  if ($properties) {
    <DAV:propstat>
      <DAV:prop>{ $properties }</DAV:prop>
      <DAV:status>{ $status }</DAV:status>
    </DAV:propstat>
  }
};

(:~
 : Renders a lock as an activelock element.
 : @param  $lock  lock
 : @return activelock element
 :)
declare function resp:activelock(
  $lock  as lock:lock
) as element(DAV:activelock) {
  <DAV:activelock>
    <DAV:locktype><DAV:write/></DAV:locktype>
    <DAV:lockscope>{ element { QName('DAV:', 'DAV:' || $lock?scope) } {} }</DAV:lockscope>
    <DAV:depth>{ $lock?depth }</DAV:depth>
    { $lock?owner }
    <DAV:timeout>Second-{ $lock?timeout }</DAV:timeout>
    <DAV:locktoken><DAV:href>{ $lock?token }</DAV:href></DAV:locktoken>
    <DAV:lockroot><DAV:href>{ resp:encode($lock?path) }</DAV:href></DAV:lockroot>
  </DAV:activelock>
};

(:~
 : Wraps a lock in the body returned by a LOCK request.
 : @param  $lock  lock
 : @return prop element
 :)
declare function resp:lockinfo(
  $lock  as lock:lock
) as element(DAV:prop) {
  <DAV:prop>
    <DAV:lockdiscovery>{ resp:activelock($lock) }</DAV:lockdiscovery>
  </DAV:prop>
};
