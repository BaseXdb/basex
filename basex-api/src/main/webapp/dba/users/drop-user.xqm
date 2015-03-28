(:~
 : Drop users.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/users';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

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
  cons:check(),
  try {
    util:update("$n ! user:drop(.)", map { 'n': $names }),
    db:output(web:redirect("users", map { 'info': 'Dropped users: ' || count($names) }))
  } catch * {
    db:output(web:redirect("users", map { 'error': $err:description }))
  }
};
