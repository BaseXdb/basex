(:~
 : Activity: what the server is running, and for whom.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/activity';

import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace panels = 'dba/lib/panels' at 'panels.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'activity';

(:~
 : Activity.
 : @param  $job       highlighted job
 : @param  $download  job whose result is to be downloaded
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/activity')
  %rest:query-param('job',      '{$job}')
  %rest:query-param('download', '{$download}')
  %output:method('html')
function dba:activity(
  $job       as xs:string?,
  $download  as xs:string?
) as element(html) {
  (
    html:panel(panels:jobs(), { 'id': 'jobs-panel', 'label': 'Jobs' }),
    html:panel(panels:clients(), { 'id': 'clients-panel', 'label': 'Clients' }),
    (: rarely of interest: the panels open on demand. They sit at the right edge, so all fold
       that way; only the last one does so by default :)
    html:panel(panels:caches(),
      { 'id': 'caches-panel', 'label': 'Caches', 'collapsed': true(), 'fold': 'right',
        'description': 'the caches of the server, what they hold, and how often they were of use'
      }),
    html:panel(panels:db-sessions(),
      { 'id': 'db-panel', 'label': 'Database Sessions', 'collapsed': true(),
        'description': 'the clients that are connected via the server protocol' }),
    (: outside the panels: they are replaced by the refresh, the dialogs are not :)
    panels:job-dialog(),
    panels:session-dialog(),
    panels:websocket-dialog(),
    panels:cache-dialog(),
    (: the details of a job are opened over the panels, which are refreshed behind them; a job
       that is selected later is filled in by the refresh. A job that is done does not change
       any more: the client stops asking for it :)
    <dialog id='details-dialog'>
      <form method='post' autocomplete='off' id='job-details'
            data-done='{ empty($job) or panels:job-done($job) }'>{
        panels:job-view($job)
      }</form>
    </dialog>,
    (: the result of a job that was closed: submitted as soon as the page is there, and the
       request that fetches the file is what consumes the job :)
    if ($download) {
      <form id='download-form' method='post' action='job-result'>
        <input type='hidden' name='id' value='{ $download }'/>
      </form>
    }
  ) => html:wrap({
    (: no widths: the panels share the page in equal parts, and each scrolls on its own :)
    'header' : $dba:CAT,
    'divided': true(),
    'rows'   : '1fr',
    'scripts': ('cm6', 'editor', 'activity'),
    'init'   : 'initActivity();'
  })
};
