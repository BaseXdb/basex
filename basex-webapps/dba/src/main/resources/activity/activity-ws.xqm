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
  (: the sort key of a list, by the block it is filled into :)
  let $sort := fn($id) { string($json?sorts?($id)) }
  (: the number of shown pages of a list, by the block it is filled into :)
  let $page := fn($id) { xs:integer($json?pages?($id) otherwise 1) }
  (: the queries of the DBA itself are only listed on demand :)
  let $dba := $json?dba = true()
  (: every panel is named by the block it is filled into :)
  let $panels := {
    'jobs-panel'   : fn() { panels:jobs($sort('jobs-panel'), $page('jobs-panel'), $dba) },
    (: the connection of this view is pointed out in the list of clients :)
    'clients-panel': fn() { panels:clients(ws:id(), $json?idle = true(), $dba) },
    'db-panel'     : fn() { panels:db-sessions($sort('db-panel'), $page('db-panel')) },
    'caches-panel' : fn() { panels:caches($sort('caches-panel'), $page('caches-panel')) }
  }
  (: a folded panel is not rendered: it is asked for once it is opened :)
  let $open := if (map:contains($json, 'open')) then $json?open?* else map:keys($panels)
  return utils:ws-send({
    'type': 'panels',
    'panels': map:build($open[map:contains($panels, .)],
      value := fn($id) { utils:html($panels($id)()) }),
    (: the details of a job are not a panel: they are shown in a dialog, and are left alone
       once they are final :)
    'job' : utils:html(panels:job-view($job)),
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
