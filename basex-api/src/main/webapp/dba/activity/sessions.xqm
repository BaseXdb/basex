(:~
 : Session actions of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/sessions';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Runs a session action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/sessions/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'kill': fn($args) { {
      'info': utils:info($args?id, 'session', 'killed'),
      'run' : %updating fn() {
        $args?id ! sessions:delete(substring-before(., '|'), substring-after(., '|'))
      }
    } }
  })
};
