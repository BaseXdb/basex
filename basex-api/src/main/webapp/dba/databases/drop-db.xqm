(:~
 : Drop databases.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace web = 'dba/web' at '../modules/web.xqm';

(:~
 : Drops databases.
 : @param  $names  names of databases
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/drop-db")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function _:drop(
  $names  as xs:string*
) {
  web:check(),
  try {
    web:update("$n ! db:drop(.)", map { 'n': $names }),
    web:redirect("databases", map { 'info': 'Dropped databases: ' || count($names) })
  } catch * {
    web:redirect("databases", map { 'error': $err:description })
  }
};
