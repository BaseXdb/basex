(:~
 : Push the panels of the activity view to the client.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/activity-ws';

import module namespace panels = 'dba/lib/panels' at 'panels.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Sends the panels of the activity view to the client. The client requests them one after
 : another, so that a slow answer does not queue up further requests.
 : @param  $message  message
 :)
declare
  %ws:message('/dba/activity', '{$message}')
function dba:ws-message(
  $message  as xs:string
) as empty-sequence() {
  let $json := parse-json($message)
  (: the shown job; the client stops asking for its details once they are done :)
  let $job := $json?job[.]
  return utils:ws-send({
    'type': 'panels',
    'jobs': utils:html(panels:jobs($json?sort)),
    'web' : utils:html(panels:web-sessions()),
    'db'  : utils:html(panels:db-sessions()),
    'caches': utils:html(panels:caches()),
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
