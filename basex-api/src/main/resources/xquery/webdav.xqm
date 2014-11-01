(:~
 : This module contains helper functions for locking documents in WebDAV.
 :
 : @author BaseX Team 2005-14, BSD License
 :)
module namespace w = 'http://basex.org/modules/webdav';

(: Lock database name. :)
declare variable $w:locks-db := '~webdav';

(: Lock error. :)
declare variable $w:err-locked := QName('http://basex.org/modules/webdav', 'w:locked_423');

(:~
 : Decomposes a path into segments.
 : @param  $path path
 : @return segments
 :)
declare function w:path-segments(
  $path as xs:string
) as xs:string* {
  tokenize($path, '/')[.]
};

(:~
 : Checks if the specified strings are equal.
 : @param  $x first strings
 : @param  $y second strings
 : @return result of check
 :)
declare function w:is-prefix(
  $x as xs:string*,
  $y as xs:string*
) as xs:boolean {
  every $r in for-each-pair($x, $y, function($a, $b) { $a eq $b })
  satisfies $r
};

(:~
 : Checks if a lock with the given path has locked
 : (possibly indirectly) another resource.
 : @param  $ancestor   ancestor resource
 : @param  $descendant descendant resource
 : @param  $depth      depth
 : @return result of check
 :)
declare function w:has-locked(
  $ancestor   as xs:string,
  $descendant as xs:string,
  $depth      as xs:string
) as xs:boolean {
  let $ancestor-segments := w:path-segments($ancestor),
      $descendant-segments := w:path-segments($descendant)
  return
    switch($depth)
    case '0'
    case '1'
      return
        count($ancestor-segments) + $depth eq count($descendant-segments)
        and
        w:is-prefix($ancestor-segments, $descendant-segments)
    case 'infinite'
      return
        w:is-prefix($ancestor-segments, $descendant-segments)
   default
     return false()
};

(:~
 : Calculates the lock expiration date-time given its timeout in seconds.
 : @param  $timeout time out
 : @return lock expiration
 :)
declare function w:expiry-dateTime(
  $timeout as xs:integer
) as xs:dateTime {
  current-dateTime() + xs:dayTimeDuration('PT' || $timeout || 'S')
};

(:~
 : Checks if the lock database exists.
 : @return result of check
 :)
declare function w:lock-db-exists() as xs:boolean {
  db:exists($w:locks-db)
};

(:~
 : Creates the database for the WebDAV locks if it does not exist.
 :)
declare %updating function w:init-lock-db() {
  if(w:lock-db-exists()) then
    ()
  else
    db:create($w:locks-db, <w:locks/>, $w:locks-db)
};

(:~
 : Opens the lock database if it exists; otherwise, returns an empty sequence.
 : @return database or empty sequence
 :)
declare function w:open-lock-db() as document-node()? {
  if(w:lock-db-exists()) then
    db:open($w:locks-db)
  else
    ()
};

(:~
 : Finds all active locks of the given path.
 : @param  $path path to check
 : @return active lock info elements
 :)
declare function w:locks-on(
  $path as xs:string
) as element(w:lockinfo)* {
  w:open-lock-db()/w:locks/w:lockinfo[
    w:has-locked(w:path, $path, 'infinite')
    and
    xs:dateTime(w:expiry) gt current-dateTime()
  ]
};

(:~
 : Checks if two locks are in conflict.
 : @param  $lock1 first lock
 : @param  $lock2 second lock
 : @return result of check
 :)
declare function w:in-conflict(
  $lock1 as element(w:lockinfo),
  $lock2 as element(w:lockinfo)
) as xs:boolean {
  (w:has-locked($lock1/w:path, $lock2/w:path, $lock1/w:depth) or
   w:has-locked($lock2/w:path, $lock1/w:path, $lock2/w:depth))
  and
  (w:is-exclusive($lock1) or w:is-exclusive($lock2))
  and
  $lock1/w:owner ne $lock2/w:owner
};

(:~
 : Checks if a lock is exclusive.
 : @param  $lock lock to check
 : @return result of check
 :)
declare function w:is-exclusive(
  $lock as element(w:lockinfo)
) as xs:boolean {
  $lock/w:scope eq 'exclusive'
};

(:~
 : Gets all locks which are in conflict with the given one.
 : @param  $requested-lock requested lock
 : @return elements with conflicting locks
 :)
declare function w:conflicting-locks(
  $requested-lock as element(w:lockinfo)
) as element(w:lockinfo)* {
  for $existing-lock in w:locks-on($requested-lock/w:path)
  where w:in-conflict($requested-lock, $existing-lock)
  return $existing-lock
};

(:~
 : Renews a lock with the given token.
 : @param $token lock token
 :)
declare %updating function w:refresh-lock(
  $token as xs:string
) {
  for $lock in w:lock($token)
  return
    replace value of node $lock/w:expiry
    with w:expiry-dateTime($lock/w:timeout)
};

(:~
 : Returns the lock with the given token.
 : @param $token lock token
 : @return lock element
 :)
declare function w:lock(
  $token as xs:string
) as element(w:lockinfo)* {
  w:open-lock-db()/w:locks/w:lockinfo[w:token eq $token]
};

(:~
 : Creates a new lock for a given path.
 : @param $path     path
 : @param $token    token
 : @param $scope    scope
 : @param $type     type
 : @param $depth    depth
 : @param $owner    owner
 : @param $timeout  timeout
 :)
declare %updating function w:create-lock(
  $path     as xs:string,
  $token    as xs:string,
  $scope    as xs:string,
  $type     as xs:string,
  $depth    as xs:string,
  $owner    as xs:string,
  $timeout  as xs:integer
) {
  let $expiry := w:expiry-dateTime($timeout),
      $requested-lock :=
        element w:lockinfo {
          element w:path    { $path },
          element w:token   { $token },
          element w:scope   { $scope },
          element w:type    { $type },
          element w:depth   { $depth },
          element w:owner   { $owner },
          element w:timeout { $timeout },
          element w:expiry  { $expiry }
        }
  return
    if(w:conflicting-locks($requested-lock)) then
      error($w:err-locked, 'Resource has a conflicting lock', $path)
    else
      insert node $requested-lock as last into w:open-lock-db()/w:locks
};

(:~
 : Removes a lock given its token.
 : @param $token lock token
 :)
declare %updating function w:delete-lock(
  $token as xs:string
) {
  delete node w:lock($token)
};
