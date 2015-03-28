(:~
 : Kill sessions.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~
 : Kills DBA sessions.
 : @param  $id  session ids
 :)
declare
  %rest:GET
  %rest:path("dba/kill-dba")
  %rest:query-param("id", "{$ids}")
  %output:method("html")
function _:drop(
  $ids  as xs:string*
) {
  cons:check(),
  try {
    Sessions:ids()[. = $ids] ! Sessions:close(.),
    web:redirect("users", map { 'info': 'Killed sessions: ' || count($ids) })
  } catch * {
    web:redirect("users", map { 'error': $err:description })
  }
};
