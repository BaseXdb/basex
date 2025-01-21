(:~
 : Kill sessions.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/sessions';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'sessions';

(:~
 : Kill sessions.
 : @param  $ids  session ids (including names)
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/session-kill')
  %rest:form-param('id', '{$ids}')
function dba:drop(
  $ids  as xs:string*
) as element(rest:response) {
  try {
    for $id in $ids
    return sessions:delete(substring-before($id, '|'), substring-after($id, '|')),
    web:redirect($dba:CAT, { 'info': utils:info($ids, 'session', 'killed') })
  } catch * {
    web:redirect($dba:CAT, { 'error': $err:description })
  }
};
