(:~
 : Drop patterns.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/jobs-users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:SUB := 'user';

(:~
 : Drops patterns.
 : @param  $names    names of users
 : @param  $pattern  database pattern
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/drop-pattern")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("pattern", "{$pattern}")
  %output:method("html")
function dba:drop-pattern(
  $name     as xs:string,
  $pattern  as xs:string
) {
  cons:check(),
  try {
    util:update("user:drop($name, $pattern)", map { 'name': $name, 'pattern': $pattern }),
    db:output(web:redirect($dba:SUB, map { 'name': $name, 'info': 'Pattern dropped: ' || $pattern }))
  } catch * {
    db:output(web:redirect($dba:SUB, map { 'error': $err:description }))
  }
};
