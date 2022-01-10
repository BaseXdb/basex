(:~
 : Kill web sessions.
 :
 : @author Christian Grün, BaseX Team 2005-22, BSD License
 :)
module namespace dba = 'dba/sessions';

import module namespace util = 'dba/util' at '../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'sessions';

(:~
 : Kills web sessions.
 : @param  $ids  session ids (including names)
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path('/dba/session-kill')
  %rest:query-param('id', '{$ids}')
function dba:drop(
  $ids  as xs:string*
) as element(rest:response) {
  try {
    for $id in $ids
    return sessions:delete(substring-before($id, '|'), substring-after($id, '|')),
    web:redirect($dba:CAT, map { 'info': util:info($ids, 'session', 'killed') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
