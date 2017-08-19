(:~
 : Kill web sessions.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/users';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Kills web sessions.
 : @param  $ids  session ids (including names)
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/kill-session")
  %rest:query-param("id", "{$ids}")
  %output:method("html")
function dba:drop(
  $ids  as xs:string*
) as element(rest:response) {
  cons:check(),
  try {
    for $id in $ids
    return Sessions:delete(substring-before($id, '|'), substring-after($id, '|')),
    web:redirect($dba:CAT, map { 'info': util:info($ids, 'session', 'killed') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
