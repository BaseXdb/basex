(:~
 : Kill sessions.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/jobs-users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $_:CAT := 'jobs-users';

(:~
 : Kills DBA sessions.
 : @param  $id session ids (including names)
 :)
declare
  %rest:GET
  %rest:path("/dba/kill-session")
  %rest:query-param("id", "{$ids}")
  %output:method("html")
function _:drop(
  $ids as xs:string*
) {
  cons:check(),
  try {
    for $id in $ids
    return Sessions:delete(substring-before($id, '|'), substring-after($id, '|')),
    web:redirect($_:CAT, map { 'info': 'Killed sessions: ' || count($ids) })
  } catch * {
    web:redirect($_:CAT, map { 'error': $err:description })
  }
};
