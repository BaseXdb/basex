(:~
 : Drop patterns.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
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
  %output:method("html")
function dba:pattern-drop(
  $name      as xs:string,
  $patterns  as xs:string*
) as empty-sequence() {
  cons:check(),
  try {
    $patterns ! user:drop($name, .),
    cons:redirect($dba:SUB, map {
      'name': $name, 'info': util:info($patterns, 'pattern', 'dropped') })
  } catch * {
    cons:redirect($dba:SUB, map { 'error': $err:description })
  }
};
