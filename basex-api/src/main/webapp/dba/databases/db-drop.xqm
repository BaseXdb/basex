(:~
 : Drop databases.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/databases';

import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Drops databases.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/db-drop")
  %rest:query-param("name", "{$names}")
function dba:db-drop(
  $names  as xs:string*
) as empty-sequence() {
  try {
    $names ! db:drop(.),
    util:redirect($dba:CAT, map { 'info': util:info($names, 'database', 'dropped') })
  } catch * {
    util:redirect($dba:CAT, map { 'error': $err:description })
  }
};
