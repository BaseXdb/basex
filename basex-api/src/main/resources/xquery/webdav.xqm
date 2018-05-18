(:~
 : This module contains helper functions for locking documents in WebDAV.
 :
 : @author BaseX Team 2005-18, BSD License
 :)
module namespace w = 'http://basex.org/modules/webdav';

(: Lock database name. :)
declare variable $w:webdav-db := db:open('~webdav~')/webdav;
(: Lock error. :)
declare variable $w:err-locked := QName('http://basex.org/modules/webdav', 'w:locked_423');

(:~
 : Decomposes a path into segments.
 : @param  $path  path
 : @return segments
 :)
declare function w:path-segments(
  $path  as xs:string
) as xs:string* {
  tokenize($path, '/')[.]
};

(:~
 : Checks if the specified strings are equal.
 : @param  $x  first strings
 : @param  $y  second strings
 : @return result of check
 :)
declare function w:is-prefix(
  $x  as xs:string*,
  $y  as xs:string*
) as xs:boolean {
  every $r in for-each-pair($x, $y, function($a, $b) { $a = $b })
  satisfies $r
};

(:~
 : Checks if a lock with the given path has locked (possibly indirectly) another resource.
 : @param  $ancestor    ancestor resource
 : @param  $descendant  descendant resource
 : @param  $depth       depth
 : @return result of check
 :)
declare function w:has-locked(
  $ancestor    as xs:string,
  $descendant  as xs:string,
  $depth       as xs:string
) as xs:boolean {
  let $ancestor-segments := w:path-segments($ancestor),
      $descendant-segments := w:path-segments($descendant)
  return switch($depth)
    case '0'
    case '1'
      return count($ancestor-segments) + $depth = count($descendant-segments) and
             w:is-prefix($ancestor-segments, $descendant-segments)
    case 'infinity'
      return w:is-prefix($ancestor-segments, $descendant-segments)
    default
      return false()
};

(:~
 : Calculates the lock expiration date-time given its timeout in seconds.
 : @param  $timeout  time out
 : @return lock expiration
 :)
declare function w:expiry-dateTime(
  $timeout  as xs:integer
) as xs:dateTime {
  current-dateTime() + xs:dayTimeDuration('PT' || $timeout || 'S')
};

(:~
 : Finds all active locks of the given path.
 : @param  $path  path to check
 : @return active lock info elements
 :)
declare function w:locks-on(
  $path  as xs:string
) as element(lockinfo)* {
  $w:webdav-db/lockinfo
    [w:has-locked(path, $path, 'infinity')]
    [xs:dateTime(expiry) > current-dateTime()]
};

(:~
 : Checks if two locks are in conflict.
 : @param  $lock1  first lock
 : @param  $lock2  second lock
 : @return result of check
 :)
declare function w:in-conflict(
  $lock1  as element(lockinfo),
  $lock2  as element(lockinfo)
) as xs:boolean {
  (w:has-locked($lock1/path, $lock2/path, $lock1/depth) or
   w:has-locked($lock2/path, $lock1/path, $lock2/depth)) and
  ($lock1, $lock2)/scope = 'exclusive'
};

(:~
 : Gets all locks which are in conflict with the given one.
 : @param  $lock  requested lock
 : @return elements with conflicting locks
 :)
declare function w:conflicting-locks(
  $lock  as element(lockinfo)
) as element(lockinfo)* {
  w:locks-on($lock/path)[w:in-conflict($lock, .)]
};

(:~
 : Renews a lock with the given token.
 : @param $token  lock token
 :)
declare %updating function w:refresh-lock(
  $token  as xs:string
) as empty-sequence() {
  for $lock in w:lock($token)
  return
    replace value of node $lock/expiry
    with w:expiry-dateTime($lock/timeout)
};

(:~
 : Returns the lock with the given token.
 : @param  $token  lock token
 : @return lock element
 :)
declare function w:lock(
  $token  as xs:string
) as element(lockinfo)* {
  $w:webdav-db/lockinfo[token = $token]
};

(:~
 : Creates a new lock for a given path.
 : @param  $path     path
 : @param  $token    token
 : @param  $scope    scope
 : @param  $type     type
 : @param  $depth    depth
 : @param  $owner    owner
 : @param  $timeout  timeout
 :)
declare %updating function w:create-lock(
  $path     as xs:string,
  $token    as xs:string,
  $scope    as xs:string,
  $type     as xs:string,
  $depth    as xs:string,
  $owner    as xs:string,
  $timeout  as xs:string
) as empty-sequence() {
  let $lock := element lockinfo {
    element path    { $path },
    element token   { $token },
    element scope   { $scope },
    element type    { $type },
    element depth   { $depth },
    element owner   { $owner },
    element timeout { $timeout },
    element expiry  { w:expiry-dateTime(xs:integer($timeout)) }
  }
  return if(w:conflicting-locks($lock)) then (
    error($w:err-locked, 'Resource has a conflicting lock', $path)
  ) else (
    insert node $lock into $w:webdav-db
  )
};

(:~
 : Removes a lock given its token.
 : @param  $token  lock token
 :)
declare %updating function w:unlock(
  $token  as xs:string
) as empty-sequence() {
  delete node w:lock($token)
};
