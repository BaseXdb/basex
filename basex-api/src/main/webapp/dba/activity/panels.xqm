(:~
 : Panels of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/panels';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Maximum length of a session value that is shown in a table cell. :)
declare %private variable $panels:PREVIEW := 100;
(:~ Time after which a session without connections and jobs is regarded as idle. :)
declare %private variable $panels:IDLE := xs:dayTimeDuration('PT5M');

(:~ What the states of a job mean. :)
declare %private variable $panels:STATES := {
  'scheduled' : 'Waiting for the time at which it starts',
  'queued'    : 'Waiting for locks held by other jobs',
  'running'   : 'Being evaluated',
  'cached'    : 'Finished; the result is kept until it is fetched',
  'registered': 'Registered as service; it is scheduled when the server starts'
};

(:~
 : Indicates whether a job was started by the DBA itself.
 : @param  $id  job id
 : @return result of check
 :)
declare function panels:dba-job(
  $id  as xs:string
) as xs:boolean {
  (: see utils:job-id :)
  starts-with($id, 'dba:')
};

(:~
 : Creates the contents of the jobs panel.
 : @param  $sort  table sort key; empty string for the longest-running jobs first
 : @param  $dba   whether the jobs of the DBA itself are listed as well
 : @return panel contents
 :)
declare function panels:jobs(
  $sort  as xs:string := '',
  $dba   as xs:boolean := false()
) as element()+ {
  let $presort := 'duration'
  let $sort := $sort[.] otherwise $presort
  return <form method='post' autocomplete='off' data-sort='{ $sort }'>
    <h2>Jobs</h2>
    {
      let $headers := (
        { 'key': 'job', 'label': 'ID' },
        { 'key': 'state', 'label': 'State' },
        { 'key': 'duration', 'label': 'Dur.', 'type': 'number', 'order': 'desc' },
        { 'key': 'user', 'label': 'User' },
        { 'key': 'locks', 'label': 'Locks' },
        { 'key': 'time', 'label': 'Created', 'type': 'time', 'order': 'desc' },
        { 'key': 'start', 'label': 'Start', 'type': 'time', 'order': 'desc' }
      )
      let $services := job:services()
      (: the job that renders this table is of no interest :)
      let $jobs := job:list-details()[not(@id = job:current())]
      (: the queries that are run in the DBA itself are only listed on demand :)
      let $hidden := $jobs[panels:dba-job(@id)][not($dba)]
      let $entries := (
        for $details in $jobs except $hidden
        let $id := $details/@id
        let $sec := (xs:dayTimeDuration($details/@duration) div xs:dayTimeDuration('PT1S'))
          otherwise 0
        let $time := data($details/@time)
        let $start := data($details/@start)
        order by $sec descending, $start descending
        return {
          'id': $id,
          'job': panels:job-link($id),
          'state': panels:job-state($details/@state, $services/@id = $id),
          'duration': html:duration($sec),
          (: the address of the client is rarely of interest; the session that the job was
             started from lists it in the Clients panel :)
          'user': panels:job-user($details),
          'locks': panels:job-locks($details),
          'time': $time,
          'start': $start otherwise $time
        },
        (: services without a job are dormant: they are rescheduled when the server is restarted :)
        for $service in $services
        let $id := $service/@id
        where not($id = $jobs/@id)
        return {
          'id': $id,
          'job': panels:job-link($id),
          'state': panels:job-state('registered', false())
        }
      )
      let $buttons := (
        <button type='button' onclick='showDialog("job")'
                title='Start a new job'>New…</button>,
        form:button('jobs/remove', 'Remove', ('CHECK', 'CONFIRM'),
          title := 'Stop the selected jobs and discard their results'),
        form:button('jobs/unregister', 'Unregister', ('CHECK', 'CONFIRM'),
          title := 'Stop the selected services and unregister them, ' ||
            'so they no longer start with the server'),
        <label title='Refresh the view at the interval chosen in the settings'>{
          <input type='checkbox' id='live' data-live='activity' checked=''
                 onchange='liveChanged()'/>, ' Live'
        }</label>,
        (: the state is kept by the client, which states it with every refresh :)
        <label title='Show the queries that are run in the DBA itself'>{
          <input type='checkbox' id='dba-jobs' onchange='dbaJobsChanged()'>{
            attribute checked { }[$dba]
          }</input>, ` DBA jobs ({ count($jobs[panels:dba-job(@id)]) })`
        }</label>
      )
      let $options := { 'sort': $sort, 'presort': $presort, 'select': 'id', 'noun': 'job',
        'empty': 'No jobs are running or scheduled.' }
      return table:create($headers, $entries, $buttons, {}, $options)
    }
  </form>
};

(:~
 : Creates the link to the details of a job.
 : @param  $id  job id
 : @return function creating the link
 :)
declare %private function panels:job-link(
  $id  as xs:string
) as fn() as element(a) {
  (: the details are opened in place; the address names them, so the link can be bookmarked :)
  fn() {
    html:action($id, 'selectJob', { 'job': $id }, { 'href': '?job=' || $id, 'class': 'nowrap' })
  }
};

(:~
 : Creates the contents of the job dialog: the details of a job, or the note that it is gone.
 : @param  $job  job id (can be empty)
 : @return contents; empty if no job is shown
 :)
declare function panels:job-view(
  $job  as xs:string?
) as element()* {
  if ($job) {
    panels:job-details($job) otherwise (
      <h2>{ 'Job: ' || $job }</h2>,
      <p>Job has expired.</p>,
      <div class='buttons'>{ panels:close-button() }</div>
    )
  }
};

(:~
 : Creates the state of a job, with what it means as tooltip.
 : @param  $state    state
 : @param  $service  whether the job is registered as service
 : @return function creating the state
 :)
declare %private function panels:job-state(
  $state    as xs:string,
  $service  as xs:boolean
) as fn() as item()+ {
  fn() {
    (: a queued job waits for the locks of another one: it is what a user asks about :)
    element { if ($state = 'queued') then 'b' else 'span' } {
      attribute title { $panels:STATES?$state },
      $state
    },
    if ($service) {
      html:symbol(' · ', ', '),
      <span title='{ $panels:STATES?registered }'>service</span>
    }
  }
};

(:~
 : Creates the user of a job, with the address of its client as tooltip.
 : @param  $details  job details
 : @return function creating the user
 :)
declare %private function panels:job-user(
  $details  as element(job)
) as fn() as item()* {
  let $user := string($details/@user)
  let $address := $details/@address ! html:localhost(.)
  return fn() {
    if ($address) then <span title='{ $address }'>{ $user }</span> else $user
  }
};

(:~
 : Creates the locks of a job: the databases it reads and writes.
 : @param  $details  job details
 : @return function creating the locks
 :)
declare %private function panels:job-locks(
  $details  as element(job)
) as fn() as item()+ {
  let $locks := (
    ('R: ' || $details/@reads)[not($details/@reads = ('', '(none)'))],
    ('W: ' || $details/@writes)[not($details/@writes = ('', '(none)'))]
  )
  return fn() {
    (
      for $lock at $pos in $locks
      return (html:symbol(' · ', ', ')[$pos > 1], $lock)
    ) otherwise $table:NONE
  }
};

(:~
 : Creates the dialog that starts a new job.
 : @return dialog
 :)
declare function panels:job-dialog() as element(dialog) {
  (: it is not part of a panel: the panels are replaced while the view refreshes, which would
     close the dialog while it is being filled in :)
  form:dialog('job', 'New Job', 'jobs/create', false(), (
    form:editor-field('Query:', 'query', 'job-query'),
    (: what the job is called and when it runs, next to how it repeats and what is kept :)
    <div class='field-columns'>{
      <div>{
        form:field('Start:', <input type='text' name='start' placeholder='PT10S, 01:00:00'/>,
          title := 'When the job first runs: a delay, a time of day, or a dateTime'),
        form:field('End:', <input type='text' name='end' placeholder='P7D'/>,
          title := 'When a repeated job stops: a duration from now, a time of day, or a dateTime'),
        (: last in its column, opposite the flag that registers a service: the id is what a
           service is addressed by later :)
        form:field('ID:', <input type='text' name='id'/>)
      }</div>,
      <div>{
        form:field('Interval:', <input type='text' name='interval' placeholder='P1D'/>,
          title := 'Repeat the job after this duration'),
        form:field('Cron:', <input type='text' name='cron' placeholder='0 6 * * MON-FRI'/>,
          title := 'Repeat the job on a cron schedule, instead of an interval'),
        form:checkbox('cache', 'true', false(), 'Cache the result'),
        (: a service is persisted, so it needs a name to be addressed by later :)
        form:checkbox('service', 'true', false(), 'Register as service')
      }</div>
    }</div>
  ))
};

(:~
 : Creates the details of a job: what is registered for it, what is persisted for it, or both.
 : @param  $job  job id
 : @return details, or empty sequence if the id is unknown
 :)
declare function panels:job-details(
  $job  as xs:string?
) as element()* {
  let $details := $job[.] ! job:list-details(.)
  let $service := job:services()[@id = $job]
  (: an id names a registered job, a persisted definition, or both: a service that has no job
     is dormant, and its definition is all that is left of it :)
  let $registered := exists($details)
  let $persisted := exists($service)
  where $registered or $persisted
  (: what is reported is what runs; what is edited is what is stored, as the string of a job is
     normalized and chopped :)
  let $report := $details otherwise $service
  let $query := string($service otherwise $details)
  (: fetched before the buttons are built: a download is only offered if there is a result :)
  let $cached := $details/@state = 'cached'
  let $output := if ($cached) {
    try {
      utils:preview(job:result($job, { 'keep': true() }), config:get($config:MAXCHARS))
    } catch * {
      utils:error-message($err:module, $err:line-number, $err:column-number,
        $err:description)
    }
  }
  (: only collected if the job was started with the 'info' option :)
  let $info := if ($cached) { job:info($job) }
  return (
    <input type='hidden' name='id' value='{ $job }'/>,
    (: the heading ends after the id, which can be long and is clipped rather than wrapped :)
    <h2>{ (if ($persisted) then 'Service: ' else 'Job: ') || $job }</h2>,
    (: what is known about the job is listed on the left, what it computes on the right :)
    <div class='job-columns'>
      <div class='job-facts'>{
        panels:job-information($report),

        for $bindings in $details ! job:bindings($job)
        where map:size($bindings) > 0
        return (
          <h3>Query Bindings</h3>,
          (: a bound value can be long, and is truncated rather than widening the table :)
          table:pairs(
            for key $key value $value in $bindings
            return <tr>
              <td><b>{ if ($key) then '$' || $key else 'Context' }</b></td>
              <td><code>{ utils:preview($value, 1000) }</code></td>
            </tr>
          )
        ),

        if (exists($info)) {
          <h3>Query Info</h3>,
          <div class='pane'>{ utils:query-info($info) }</div>
        }
      }</div>
      <div class='job-texts'>{
        if ($output) {
          <h3>Result</h3>,
          <textarea id='output' readonly='' spellcheck='false'>{ $output }</textarea>
        },
        (: a stored definition can be replaced; a job string is only shown :)
        <h3>{ if ($persisted) then 'Query' else 'Job String' }</h3>,
        <textarea spellcheck='false'>{
          attribute id { 'job-string' }[$persisted],
          attribute name { 'query' }[$persisted],
          attribute readonly { }[not($persisted)],
          $query
        }</textarea>
      }</div>
    </div>,
    <div class='buttons'>{
      form:button('jobs/remove', 'Remove',
        title := 'Stop the job and discard its result')[$registered],
      (: reading a result closes the job: the action gives it up, and the page it leads to
         fetches the file :)
      form:button('jobs/download', 'Download',
        title := 'Download the result; the job is removed then')[$output],
      form:button('jobs/replace', 'Save',
        title := 'Save the edited query and restart the service')[$persisted],
      form:button('jobs/unregister', 'Unregister',
        title := 'Stop the service and unregister it, ' ||
          'so that it no longer starts with the server')[$persisted],
      panels:close-button()
    }</div>
  )
};

(:~
 : Creates the button that closes the details of a job.
 : @return button
 :)
declare function panels:close-button() as element(button) {
  <button formmethod='dialog' formnovalidate=''>Close</button>
};

(:~
 : Indicates whether the details of a job will not change any more.
 : @param  $job  job id
 : @return result of check
 :)
declare function panels:job-done(
  $job  as xs:string?
) as xs:boolean {
  (: a service is done as well: its query is edited in place, and a refresh would replace the
     editor while it is used :)
  let $details := $job[.] ! job:list-details(.)
  return empty($details) or $details/@state = 'cached' or
    exists(job:services()[@id = $job])
};

(:~
 : Creates the general information of a job or a service definition.
 : @param  $entry  job details or service definition
 : @return table
 :)
declare %private function panels:job-information(
  $entry  as element()
) as element(table) {
  table:pairs(
    for $value in $entry/@*
    for $name in name($value)[. != 'id']
    return <tr>
      <td><b>{ utils:capitalize($name) }</b></td>
      <td>{ string($value) }</td>
    </tr>
  )
};

(:~
 : Creates the contents of the clients panel: the web sessions of the server, each followed by
 : the WebSocket connections and the jobs that were opened in it.
 : @param  $socket  id of the connection of the requesting view (empty if unknown)
 : @param  $idle    whether the idle sessions are listed as well
 : @param  $dba     whether the jobs of the DBA itself are listed as well
 : @return panel contents
 :)
declare function panels:clients(
  $socket  as xs:string? := (),
  $idle    as xs:boolean := false(),
  $dba     as xs:boolean := false()
) as element(form) {
  let $current := session:id()
  let $sessions := sessions:list-details()
  let $sockets := ws:list-details()
  let $jobs := job:list-details()[not(@id = job:current())]
  (: the queries of the DBA are listed as they are in the jobs panel :)
  let $listed-jobs := $jobs[$dba or not(panels:dba-job(@id))]
  let $registered := job:list()
  (: a session without connections and jobs that has not been accessed for a while is idle; the
     session of this view never is :)
  let $idle-ids := $sessions[
    not(@id = ($current, $sockets/@session, $jobs/@session)) and
    xs:dateTime(@accessed) < current-dateTime() - $panels:IDLE
  ]/@id
  let $shown := if ($idle) then $sessions else $sessions[not(@id = $idle-ids)]
  (: the session of this view comes first; the others by creation, so rows do not jump :)
  let $ordered := (
    $shown[@id = $current],
    for $session in $shown[not(@id = $current)]
    order by $session/@created descending
    return $session
  )
  (: a session and what belongs to it are a group of rows, which a screen reader states :)
  let $groups := (
    for $session in $ordered
    let $id := string($session/@id)
    return panels:session-rows($session, $id = $current,
      $sockets[@session = $id], $listed-jobs[@session = $id], $socket, $registered),
    (: connections that were opened without a session, or whose session is gone :)
    let $orphans := $sockets[not(@session = $sessions/@id)]
    where exists($orphans)
    return <tbody>{
      <tr class='group'>
        <th scope='rowgroup' colspan='5'><b>No session</b></th>
      </tr>,
      $orphans ! panels:socket-row(., $socket, $registered)
    }</tbody>
  )
  return <form method='post' autocomplete='off'>
    <h2>Clients</h2>
    <div class='buttons'>{
      form:button('clients/close', 'Close', ('CHECK', 'CONFIRM'),
        title := 'Close the selected sessions and connections; ' ||
          'a closed session logs its users out'),
      (: the state is kept by the client, which states it with every refresh :)
      <label title='{ 'Show the sessions without connections and jobs that were not ' ||
                      'accessed for 5 minutes' }'>{
        <input type='checkbox' id='idle' onchange='idleChanged()'>{
          attribute checked { }[$idle]
        }</input>, ` Show idle ({ count($idle-ids) })`
      }</label>
    }</div>
    <h3>{
      (: worded as the summary of a table :)
      count($sessions) || ' ' || utils:capitalize(utils:plural(count($sessions), 'session')),
      html:symbol(' · ', ', '),
      count($sockets) || ' ' || utils:capitalize(utils:plural(count($sockets), 'connection'))
    }</h3>
    {
      if (empty($groups)) {
        <p class='note'>{
          if (exists($idle-ids)) then 'All sessions are idle.'
          else 'No web sessions or WebSocket connections are open.'
        }</p>
      },
      if (exists($groups)) {
        <div class='scroll'>
          <table class='clients'>
            <thead>
              <tr>
                <th><input type='checkbox' onclick='toggle(this)' aria-label='Select all'/>
                  CLIENT</th>
                <th>ATTRIBUTES</th>
                <th>OPENED</th>
                <th>ACCESS</th>
                <th title='{ 'A session that is not accessed again is discarded at this ' ||
                  'time' }'>EXPIRES</th>
              </tr>
            </thead>
            { $groups }
          </table>
        </div>
      }
    }
  </form>
};

(:~
 : Creates the rows of a web session: the session itself, its connections and its jobs.
 : @param  $session     session details
 : @param  $you         whether it is the session of this view
 : @param  $sockets     details of its connections
 : @param  $jobs        details of its jobs
 : @param  $socket      id of the connection of the requesting view (can be empty)
 : @param  $registered  ids of the registered jobs
 : @return group of rows (empty if the session is gone)
 :)
declare %private function panels:session-rows(
  $session     as element(),
  $you         as xs:boolean,
  $sockets     as element()*,
  $jobs        as element(job)*,
  $socket      as xs:string?,
  $registered  as xs:string*
) as element(tbody)? {
  let $id := string($session/@id)
  (: a session can be gone before it is read: it is skipped, rather than failing the panel. The
     attributes are wrapped in an array, so that the loop runs once :)
  for $attributes in try {
    [ panels:attributes('session', $id,
      map:build(sessions:names($id), value := fn($name) {
        utils:preview(sessions:get($id, $name), $panels:PREVIEW)
      })) ]
  } catch sessions:not-found { }
  return <tbody>{
    <tr class='group' id='{ $id }'>
      <th scope='rowgroup'>
        <input type='checkbox' name='session' value='{ $id }' onclick='buttons(this)'
               aria-label='{ 'Select session ' || $id }'/>
        <b>Session</b>{ ' ' }
        { panels:id($id) }
        { <span class='note'>{ html:symbol(' · ', ', ') }you</span>[$you] }
      </th>
      <td>{ $attributes?* }</td>
      <td>{ html:time($session/@created) }</td>
      <td>{ html:time($session/@accessed) }</td>
      <td>{ html:time($session/@expires) }</td>
    </tr>,
    for $ws in $sockets
    order by $ws/@created
    return panels:socket-row($ws, $socket, $registered),
    for $job in $jobs
    order by $job/@start
    return panels:job-row($job)
  }</tbody>
};

(:~
 : Creates the row of a WebSocket connection.
 : @param  $ws          connection details
 : @param  $socket      id of the connection of the requesting view (can be empty)
 : @param  $registered  ids of the registered jobs
 : @return row (empty if the connection is gone)
 :)
declare %private function panels:socket-row(
  $ws          as element(),
  $socket      as xs:string?,
  $registered  as xs:string*
) as element(tr)? {
  let $id := string($ws/@id)
  let $path := string($ws/@path)
  let $dba := matches($path, '^/?dba(/|$)')
  for $attributes in try {
    [ panels:attributes('websocket', $id,
      map:build(sort(ws:names($id), '?lang=en'), value := fn($name) {
        let $value := ws:get($id, $name)
        (: the jobs that the DBA runs for a connection link to their details :)
        return if ($dba and $name = $utils:JOB) then panels:dba-jobs($value, $registered)
        else utils:preview($value, $panels:PREVIEW)
      })) ]
  } catch ws:not-found { }
  (: the connection is named by its path, a view of the DBA by its name :)
  let $label := if ($dba) then 'DBA: ' || utils:capitalize(replace($path, '^/?dba/?', ''))
    else $path
  return <tr class='child' id='{ $id }'>
    <td>
      <input type='checkbox' name='websocket' value='{ $id }' onclick='buttons(this)'
             aria-label='{ 'Select connection ' || $label }'/>
      <span class='nowrap' title='{ string-join(($id, $ws/@user,
        html:localhost($ws/@address)), ', ') }'>{ $label }</span>
      { <span class='note'>{ html:symbol(' · ', ', ') }this tab</span>[$id = $socket] }
      { <span class='note'>{ html:symbol(' · ', ', '), string($ws/@user) }</span>[
        not($ws/@session)] }
    </td>
    <td>{ $attributes?* }</td>
    <td>{ html:time($ws/@created) }</td>
    <td>{ html:time($ws/@accessed) }</td>
    <td/>
  </tr>
};

(:~
 : Creates the row of a job that was started in a web session.
 : @param  $job  job details
 : @return row
 :)
declare %private function panels:job-row(
  $job  as element(job)
) as element(tr) {
  <tr class='child'>
    <td>
      Job { panels:job-link($job/@id)() }
    </td>
    <td>{
      panels:job-state($job/@state, false())(),
      (: what is locked is only stated if there is anything :)
      if (some $locks in ($job/@reads, $job/@writes) satisfies not($locks = ('', '(none)'))) {
        html:symbol(' · ', ', '), panels:job-locks($job)()
      }
    }</td>
    <td>{ ($job/@start otherwise $job/@time) ! html:time(.) }</td>
    <td/>
    <td/>
  </tr>
};

(:~
 : Creates the attributes of a session or connection: each name links to the dialog that edits it.
 : @param  $kind    what holds the attributes ('session', 'websocket')
 : @param  $id      id of the session or connection
 : @param  $values  previews of the attribute values: strings or nodes
 : @return attributes
 :)
declare %private function panels:attributes(
  $kind    as xs:string,
  $id      as xs:string,
  $values  as map(xs:string, item()*)
) as item()+ {
  (
    for $name at $pos in map:keys($values)
    return (
      html:symbol(' · ', ', ')[$pos > 1],
      (: three values, so the call is handed the whole dataset :)
      html:action($name, 'editAttribute', { 'kind': $kind, 'id': $id, 'name': $name },
        { 'title': 'Edit or delete the attribute' }),
      text { ': ' },
      for $value in $values?$name
      return if ($value instance of node()) then $value
        else if (string-length($value) > 20 and not(contains($value, ' '))) then panels:id($value)
        else text { $value }
    )
  ) otherwise $table:NONE
};

(:~
 : Creates the short form of an id: its end, with the full id as tooltip.
 : @param  $id  id
 : @return element
 :)
declare %private function panels:id(
  $id  as xs:string
) as element(span) {
  (: ids of a servlet container start with a prefix that all of them share :)
  <span class='nowrap' title='{ $id }'>{ '…' || substring($id, string-length($id) - 7) }</span>
};

(:~
 : Creates the links to the jobs that the DBA runs for a connection.
 : @param  $jobs        ids of the query and of the job that pushes its outcome to the client
 : @param  $registered  ids of the registered jobs
 : @return links
 :)
declare %private function panels:dba-jobs(
  $jobs        as map(*),
  $registered  as xs:string*
) as node()* {
  (: the ids are kept after the jobs are done: only a job that is still registered is linked :)
  if ($jobs?query = $registered) then panels:job-link($jobs?query)()
  else text { $jobs?query || ' (finished)' },
  if ($jobs?reader = $registered) {
    text { ' (reader: ' }, panels:job-link($jobs?reader)(), text { ')' }
  }
};

(:~
 : Creates the dialog that assigns a session attribute.
 : @return dialog
 :)
declare function panels:session-dialog() as element(dialog) {
  (: as the dialog that starts a job, it is not part of a panel: the panels are replaced while
     the view refreshes :)
  panels:attribute-dialog('session', 'Session:', 'sessions')
};

(:~
 : Creates the dialog that assigns an attribute of a WebSocket connection.
 : @return dialog
 :)
declare function panels:websocket-dialog() as element(dialog) {
  panels:attribute-dialog('websocket', 'WebSocket:', 'websockets')
};

(:~
 : Creates the dialog that assigns or deletes an attribute.
 : @param  $kind     what holds the attribute ('session', 'websocket')
 : @param  $label    label of the field that names it
 : @param  $actions  endpoint the dialog posts to ('sessions', 'websockets')
 : @return dialog
 :)
declare %private function panels:attribute-dialog(
  $kind     as xs:string,
  $label    as xs:string,
  $actions  as xs:string
) as element(dialog) {
  (: the ids of its fields are derived from what holds the attribute, so that the two dialogs
     of the view do not collide :)
  form:dialog($kind, 'Edit Attribute', $actions || '/set', false(), (
    (: what holds the attribute is chosen in the panel; the name is not, so an attribute that
       it does not hold yet can be assigned as well :)
    form:field('Name:',
      <input type='text' name='name' id='{ $kind }-name' class='wide' required=''
             autofocus=''/>, 'stacked'),
    form:editor-field('Value:', 'value', $kind || '-value'),
    (: what is assigned is stated below what assigns it; it is nothing to fill in, so it is
       written out, and submitted by a field of its own :)
    form:field($label, (
      <span id='{ $kind }-text'/>,
      <input type='hidden' name='id' id='{ $kind }-id'/>
    )),
    (: filled in by the client if the value it fetched cannot be shown :)
    <div id='{ $kind }-note' class='note'/>
  ), <button formaction='{ $actions }/delete'
                 title='Delete the attribute'>Delete</button>)
};

(:~
 : Creates the contents of the caches panel: what the caches of the server hold, and how often
 : they were of use.
 : @param  $sort  table sort key; empty string for the names
 : @return panel contents
 :)
declare function panels:caches(
  $sort  as xs:string := ''
) as element(form) {
  (: a cache is transient and is managed by the server; what can be done with it is to give up
     what it holds :)
  let $presort := 'label'
  let $sort := $sort[.] otherwise $presort
  return <form method='post' autocomplete='off' data-sort='{ $sort }'>
    {
      (: the default cache is addressed by an operation that supplies no name :)
      let $names := distinct-values(('', sort(cache:list(), '?lang=en')))
      let $headers := (
        { 'key': 'label', 'label': 'Name' },
        { 'key': 'entries', 'label': 'Size', 'type': 'number', 'order': 'desc' },
        { 'key': 'max-entries', 'label': 'Max', 'type': 'number', 'order': 'desc' },
        { 'key': 'lifetime', 'label': 'TTL' },
        { 'key': 'lookups', 'label': 'Reads', 'type': 'number', 'order': 'desc' },
        { 'key': 'rate', 'label': 'Hits', 'type': 'percent', 'order': 'desc' },
        { 'key': 'discarded', 'label': 'Dropped', 'type': 'number', 'order': 'desc' }
      )
      let $entries :=
        for $cache in $names
        let $info := cache:info($cache)
        let $lookups := $info?hits + $info?misses
        (: the hit rate is a number, so that it can be sorted :)
        return {
          'cache': $cache,
          (: a cell that a function produces is sorted by its text :)
          'label': fn() {
            html:action(utils:label($cache), 'showCache', { 'name': $cache },
              { 'title': 'Show and edit the entries of the cache' })
          },
          'entries': $info?entries,
          'max-entries': $info?max-entries,
          'lifetime': if ($info?ttl = 0) then 'unlimited' else (
            string(seconds($info?ttl)) => replace('[PT]', '') => lower-case()
          ),
          'lookups': $lookups,
          'rate': if ($lookups > 0) { $info?hits div $lookups },
          'discarded': $info?evictions + $info?expirations
        }
      let $buttons := (
        form:button('caches/delete', 'Delete', ('CHECK', 'CONFIRM'),
          title := 'Delete the selected caches; options assigned with cache:init are kept'),
        form:button('caches/clear', 'Reset All', 'CONFIRM',
          title := 'Delete all caches and the options assigned with cache:init')
      )
      (: the checkbox submits the name: the default cache is addressed by an empty one :)
      return table:create($headers, $entries, $buttons, {},
        { 'sticky': <h2>Caches</h2>, 'select': 'cache', 'sort': $sort, 'presort': $presort,
          'noun': 'cache' })
    }
  </form>
};

(:~
 : Creates the dialog that shows, assigns and removes the entries of a cache.
 : @return dialog
 :)
declare function panels:cache-dialog() as element(dialog) {
  (: the name of the cache, its keys and the chosen value are filled in by the client :)
  form:dialog('cache', 'Cache', 'caches/put', false(), (
    <input type='hidden' name='name' id='cache-name'/>,
    (: the keys to choose from, next to the entry that is shown :)
    <div class='field-columns'>{
      <div>{
        form:field('Keys:', (
          <select id='cache-keys' class='wide' size='13' multiple=''
                  onchange='showCacheEntry()'/>,
          <div id='cache-count' class='note'/>
        ), 'stacked')
      }</div>,
      <div>{
        form:field('Key:',
          <input type='text' name='key' id='cache-key' class='wide' required=''/>, 'stacked'),
        form:editor-field('Value:', 'value', 'cache-value'),
        <div id='cache-note' class='note'/>
      }</div>
    }</div>
  ), <button type='button' id='cache-remove' onclick='removeCacheEntries()' disabled=''
             title='Remove the selected entries'>Remove</button>)
};

(:~
 : Creates the contents of the database sessions panel.
 : @param  $sort  table sort key; empty string for the addresses
 : @return panel contents
 :)
declare function panels:db-sessions(
  $sort  as xs:string := ''
) as element(div) {
  let $presort := 'address'
  let $sort := $sort[.] otherwise $presort
  return <div data-sort='{ $sort }'>{
    <h2>Database Sessions</h2>,
    table:create(
      (
        { 'key': 'address', 'label': 'Address' },
        { 'key': 'user', 'label': 'User' }
      ),
      for $session in admin:sessions()
      let $address := string($session/@address)
      order by $address
      return { 'address': fn() { html:address($address) }, 'user': $session/@user },
      (), {}, { 'sort': $sort, 'presort': $presort, 'noun': 'session',
        'empty': 'No clients are connected via the server protocol.' }
    )
  }</div>
};
