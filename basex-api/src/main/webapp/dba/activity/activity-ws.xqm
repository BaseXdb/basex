(:~
 : Push the panels of the activity view to the client.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/activity-ws';

import module namespace panels = 'dba/lib/panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Sends the panels of the activity view to the client.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/activity', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  (: they are pushed in one message: the view refreshes as a whole, and the client asks for
     the next refresh once this one has arrived, so that a slow answer does not queue up further
     requests :)
  let $json := parse-json($message)
  (: the shown job; the client stops asking for its details once they are done :)
  let $job := $json?job[.]
  return utils:ws-send({
    'type': 'panels',
    (: every panel is named by the block it is filled into :)
    'panels': {
      'jobs-panel'  : utils:html(panels:jobs($json?sort)),
      'web-panel'   : utils:html(panels:web-sessions()),
      'db-panel'    : utils:html(panels:db-sessions()),
      'ws-panel'    : utils:html(panels:websockets()),
      'caches-panel': utils:html(panels:caches())
    },
    (: the details of a job are not a panel of their own: they are inserted before the
       reports, and are left alone once they are final :)
    'job' : utils:html(panels:job-details($job)),
    'done': panels:job-done($job)
  })
};

(:~
 : Reports an error to the client.
 : @param  $message  error message
 :)
declare
  %ws:error('/dba/activity', '{$message}')
function dba:ws-error(
  $message  as xs:string
) as empty-sequence() {
  utils:ws-error('Activity', $message)
};
