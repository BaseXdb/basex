(:~
 : Kill sessions.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/jobs-users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'jobs-users';

(:~
 : Kills DBA sessions.
 : @param  $id session ids (including names)
 :)
declare
  %rest:GET
  %rest:path("/dba/kill-session")
  %rest:query-param("id", "{$ids}")
  %output:method("html")
function dba:drop(
  $ids as xs:string*
) as element(rest:response) {
  cons:check(),
  try {
    for $id in $ids
    return Sessions:delete(substring-before($id, '|'), substring-after($id, '|')),
    web:redirect($dba:CAT, map { 'info': 'Killed sessions: ' || count($ids) })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
