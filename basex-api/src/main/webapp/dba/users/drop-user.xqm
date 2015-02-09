(:~
 : Drop users.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/users';

import module namespace web = 'dba/web' at '../modules/web.xqm';

(:~
 : Drops users.
 : @param  $names  names of users
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/drop-user")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function _:drop(
  $names  as xs:string*
) {
  web:check(),
  try {
    web:update("$n ! user:drop(.)", map { 'n': $names }),
    web:redirect("users", map { 'info': 'Dropped users: ' || count($names) })
  } catch * {
    web:redirect("users", map { 'error': $err:description })
  }
};
