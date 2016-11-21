(:~
 : Drop databases.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Drops databases.
 : @param  $names  names of databases
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/drop-db")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:drop(
  $names  as xs:string*
) {
  cons:check(),
  try {
    util:update("$names ! db:drop(.)", map { 'names': $names }),
    db:output(web:redirect($dba:CAT, map { 'info': 'Dropped databases: ' || count($names) }))
  } catch * {
    db:output(web:redirect($dba:CAT, map { 'error': $err:description }))
  }
};
