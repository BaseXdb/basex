(:~
 : Drop patterns.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/users';

import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:SUB := 'user';

(:~
 : Drops a pattern.
 : @param  $name      user name
 : @param  $patterns  database patterns
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/pattern-drop")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("pattern", "{$patterns}")
function dba:pattern-drop(
  $name      as xs:string,
  $patterns  as xs:string*
) as empty-sequence() {
  try {
    $patterns ! user:drop($name, .),
    util:redirect($dba:SUB, map {
      'name': $name, 'info': util:info($patterns, 'pattern', 'dropped') })
  } catch * {
    util:redirect($dba:SUB, map { 'error': $err:description })
  }
};
