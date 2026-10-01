(:~
 : Panels of the activity view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panels = 'dba/lib/panels';

import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Maximum length of a session value that is shown in a table cell. :)
declare %private variable $panels:PREVIEW := 100;

(:~
 : Creates the contents of the jobs panel.
 : @param  $sort  table sort key
 : @return panel contents
 :)
declare function panels:jobs(
  $sort  as xs:string
) as element()+ {
  <form method='post' autocomplete='off'>
    <h2>Jobs</h2>
    {
      let $headers := (
        { 'key': 'job', 'label': 'ID', 'type': 'dynamic' },
        { 'key': 'state', 'label': 'State', 'type': 'dynamic' },
        { 'key': 'service', 'label': 'Service' },
        { 'key': 'duration', 'label': 'Dur.', 'type': 'number', 'order': 'desc' },
        { 'key': 'user', 'label': 'User', 'type': 'dynamic' },
        { 'key': 'session', 'label': 'Session', 'type': 'dynamic' },
        { 'key': 'locks', 'label': 'Locks', 'type': 'dynamic' },
        { 'key': 'time', 'label': 'Time', 'type': 'time', 'order': 'desc' },
        { 'key': 'start', 'label': 'Start', 'type': 'time', 'order': 'desc' }
      )
      let $services := job:services()
      let $jobs := job:list-details()
      let $entries := (
        (: the job that renders this table is of no interest :)
        let $curr := job:current()
        for $details in $jobs
        let $id := $details/@id
        where not($id = $curr)
        let $sec := (xs:dayTimeDuration($details/@duration) div xs:dayTimeDuration('PT1S'))
          otherwise 0
        let $time := data($details/@time)
        let $start := data($details/@start)
        order by $sec descending, $start descending
        return {
          'id': $id,
          'job': panels:job-link($id),
          'state': panels:job-state($details/@state),
          'service': if ($services/@id = $id) then '✓' else '–',
          'duration': html:duration($sec),
          (: the address of the client is rarely of interest; the session names the row of the
             Web Sessions panel that the job was started from :)
          'user': panels:job-user($details),
          'session': ($details/@session ! panels:session(.)) otherwise '–',
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
          'state': panels:job-state('registered'),
          'service': '✓'
        }
      )
      let $buttons := (
        <button type='button' onclick='showDialog("job")'>New…</button>,
        form:button('jobs/remove', 'Remove', ('CHECK', 'CONFIRM')),
        form:button('jobs/unregister', 'Unregister', ('CHECK', 'CONFIRM')),
        <label title='Refresh the view every second'>{
          <input type='checkbox' id='live' data-live='activity' checked=''
                 onchange='liveChanged()'/>, ' Live'
        }</label>
      )
      let $options := { 'sort': $sort, 'presort': 'duration', 'select': 'id' }
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
  fn() { <a class='nowrap' href='?job={ $id }'>{ $id }</a> }
};

(:~
 : Creates the state of a job.
 : @param  $state  state
 : @return function creating the state
 :)
declare %private function panels:job-state(
  $state  as xs:string
) as fn() as item() {
  (: a queued job waits for the locks of another one: it is what a user asks about :)
  fn() {
    if ($state = 'queued') then <b title='Waiting for locks held by other jobs'>queued</b>
    else $state
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
  let $address := $details/@address ! html:address(.)
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
) as fn() as xs:string {
  let $locks := (
    ('R: ' || $details/@reads)[not($details/@reads = ('', '(none)'))],
    ('W: ' || $details/@writes)[not($details/@writes = ('', '(none)'))]
  )
  return fn() { string-join($locks, ' · ')[.] otherwise '–' }
};

(:~
 : Creates the dialog that starts a new job.
 : @return dialog
 :)
declare function panels:job-dialog() as element(dialog) {
  (: it is not part of a panel: the panels are replaced while the view refreshes, which would
     close the dialog while it is being filled in :)
  form:dialog('job', 'New Job', 'jobs/create', false(), (
    (: no 'required': the editor hides the text area, and a hidden field that fails validation
       cannot be focused, which would block the submit without telling the user why :)
    form:field('Query:',
      <textarea name='query' id='job-query' class='wide' rows='8'/>, 'stacked'),
    (: what the job is called and when it runs, next to how it repeats and what is kept :)
    <div class='field-columns'>{
      <div>{
        form:field('Start:', <input type='text' name='start' placeholder='PT10S, 01:00:00'/>),
        form:field('End:', <input type='text' name='end' placeholder='P7D'/>),
        (: last in its column, opposite the flag that registers a service: the id is what a
           service is addressed by later :)
        form:field('ID:', <input type='text' name='id'/>)
      }</div>,
      <div>{
        form:field('Interval:', <input type='text' name='interval' placeholder='P1D'/>),
        form:field('Cron:', <input type='text' name='cron' placeholder='0 6 * * MON-FRI'/>),
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
      utils:serialize(job:result($job, { 'keep': true() }))
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
    <div class='buttons'>{
      form:button('jobs/remove', 'Remove')[$registered],
      (: reading a result closes the job: the action gives it up, and the page it leads to
         fetches the file :)
      form:button('jobs/download', 'Download')[$output],
      form:button('jobs/unregister', 'Unregister')[$persisted]
    }</div>,

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

    if ($output) {
      <h3>Result</h3>,
      <textarea id='output' readonly='' spellcheck='false'>{ $output }</textarea>
    },

    if (exists($info)) {
      <h3>Query Info</h3>,
      <div class='pane'>{ utils:query-info($info) }</div>
    },

    (: a stored definition can be replaced; a job string is only shown :)
    if ($persisted) then (
      html:heading('Query', form:button('jobs/replace', 'Replace'), 'h3')
    ) else (
      <h3>Job String</h3>
    ),
    <textarea spellcheck='false'>{
      attribute id { 'job-string' }[$persisted],
      attribute name { 'query' }[$persisted],
      attribute readonly { }[not($persisted)],
      $query
    }</textarea>
  )
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
 : Creates a panel that lists what sessions or connections hold.
 : @param  $kind     what holds the attributes ('session', 'websocket')
 : @param  $actions  endpoint the buttons post to ('sessions', 'websockets')
 : @param  $heading  name of the panel
 : @param  $columns  table headers that the panel adds before the shared ones
 : @param  $after    table headers that the panel adds after the shared ones
 : @param  $holders  what is listed: the id, what is read for it (attribute values as previews),
 :                   and its own column values
 : @return panel contents
 :)
declare %private function panels:attribute-panel(
  $kind     as xs:string,
  $actions  as xs:string,
  $heading  as xs:string,
  $columns  as map(*)+,
  $after    as map(*)*,
  $holders  as map(*)*
) as element(form) {
  (: both are addressed by an id and keep named attributes, so both are listed in the same
     way; what tells them apart are the columns of their own and what is asked of the server for
     them :)
  <form method='post' autocomplete='off'>
    <h2>{ $heading }</h2>
    {
      let $headers := (
        (: the columns of the holder come first: the first one carries the checkbox :)
        $columns,
        { 'key': 'attributes', 'label': 'Attributes', 'type': 'dynamic' },
        (: a time is as wide as it will ever be: it is given what it needs, not a share that
           grows with the panel :)
        { 'key': 'access', 'label': 'Access', 'type': 'time', 'order': 'desc',
          'width': '4.5rem' },
        $after
      )
      let $entries :=
        for $holder in $holders
        let $id := $holder?id
        (: what is listed can be gone before it is read; skip it, rather than failing the whole
           panel. Everything that is asked of the server for a holder is asked for here :)
        for $entry in try {
          let $values := map:build($holder?names(), value := $holder?value)
          return {
            {
              'id': $id,
              'attributes': panels:attributes($kind, $id, $values),
              'access': $holder?access()
            },
            $holder?columns
          }
        } catch sessions:not-found | ws:not-found { }
        order by $entry?access descending
        return $entry
      let $buttons := form:button($actions || '/close', 'Close', ('CHECK', 'CONFIRM'))
      return table:create($headers, $entries, $buttons, {}, { 'select': 'id' })
    }
  </form>
};

(:~
 : Creates the contents of the web sessions panel: what the sessions of the server hold.
 : @return panel contents
 :)
declare function panels:web-sessions() as element(form) {
  (: a value that was assigned by a Java application is shown as the object it is :)
  let $current := session:id()
  return panels:attribute-panel('session', 'sessions', 'Web Sessions',
    (
      { 'key': 'session', 'label': 'Session', 'type': 'dynamic', 'width': '7rem' },
      { 'key': 'you', 'label': 'You', 'width': '2.5rem' }
    ),
    (: a session that is not accessed again is discarded at this time :)
    { 'key': 'expires', 'label': 'Expires', 'type': 'time', 'width': '4.5rem' },
    for $session in sessions:list-details()
    let $id := string($session/@id)
    return {
      'id': $id,
      'access': fn() { data($session/@accessed) },
      'names': fn() { sessions:names($id) },
      'value': fn($name) { utils:preview(sessions:get($id, $name), $panels:PREVIEW) },
      'columns': {
        'session': panels:session($id, $session/@created),
        'you': if ($id = $current) then '✓' else '–',
        'expires': data($session/@expires)
      }
    }
  )
};

(:~
 : Creates the label of a web session: the end of its id, which is shown in full as tooltip.
 : @param  $id       session id
 : @param  $created  creation time (can be empty)
 : @return function creating the label
 :)
declare %private function panels:session(
  $id       as xs:string,
  $created  as xs:anyAtomicType? := ()
) as fn() as element(span) {
  (: the id of a servlet container starts with a prefix that all sessions share :)
  let $title := string-join(($id, $created ! ('created ' || panels:exact(.))), ', ')
  return fn() {
    <span class='nowrap' title='{ $title }' data-session='{ $id }'>{
      '…' || substring($id, string-length($id) - 7)
    }</span>
  }
};

(:~
 : Formats a time with its date and seconds, as it is forwarded to administrators.
 : @param  $time  time
 : @return formatted time
 :)
declare %private function panels:exact(
  $time  as xs:anyAtomicType
) as xs:string {
  format-dateTime(html:adjust(xs:dateTime($time)), '[Y0000]-[M00]-[D00] [H00]:[m00]:[s00]')
};

(:~
 : Creates the attributes of a session or connection: each name links to the dialog that edits it.
 : @param  $kind    what holds the attributes ('session', 'websocket')
 : @param  $id      id of the session or connection
 : @param  $values  previews of the attribute values: strings or nodes
 : @return function creating the attributes
 :)
declare %private function panels:attributes(
  $kind    as xs:string,
  $id      as xs:string,
  $values  as map(xs:string, item()*)
) as fn() as item()+ {
  fn() {
    (
      for $name at $pos in map:keys($values)
      return (
        (: text nodes: adjacent strings would be separated by spaces :)
        text { '; ' }[$pos > 1],
        (: three values, so the call is handed the whole dataset :)
        html:action($name, 'editAttribute', { 'kind': $kind, 'id': $id, 'name': $name },
          { 'title': 'Edit or delete the attribute' }),
        text { ': ' },
        for $value in $values?$name
        return if ($value instance of node()) then $value else text { $value }
      )
    ) otherwise '–'
  }
};

(:~
 : Creates the links to the jobs that the DBA runs for a connection.
 : @param  $jobs  ids of the query and of the job that pushes its outcome to the client
 : @return links
 :)
declare %private function panels:dba-jobs(
  $jobs  as map(*)
) as node()* {
  (: the ids are kept after the jobs are done: only a job that is still registered is linked :)
  let $registered := job:list()
  return (
    if ($jobs?query = $registered) then panels:job-link($jobs?query)()
    else text { $jobs?query || ' (finished)' },
    if ($jobs?reader = $registered) {
      text { ' (reader: ' }, panels:job-link($jobs?reader)(), text { ')' }
    }
  )
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
  form:dialog($kind, 'Attribute', $actions || '/set', false(), (
    (: what holds the attribute is chosen in the panel; the name is not, so an attribute that
       it does not hold yet can be assigned as well :)
    form:field('Name:',
      <input type='text' name='name' id='{ $kind }-name' class='wide' required=''
             autofocus=''/>, 'stacked'),
    (: no 'required': the editor hides the text area, and a hidden field that fails validation
       cannot be focused, which would block the submit without telling the user why :)
    form:field('Value:',
      <textarea name='value' id='{ $kind }-value' class='wide' rows='8'/>, 'stacked'),
    (: what is assigned is stated below what assigns it; it is nothing to fill in, so it is
       written out, and submitted by a field of its own :)
    form:field($label, (
      <span id='{ $kind }-text'/>,
      <input type='hidden' name='id' id='{ $kind }-id'/>
    )),
    (: filled in by the client if the value it fetched cannot be shown :)
    <div id='{ $kind }-note' class='note'/>
  ), <button formaction='{ $actions }/delete'>Delete</button>)
};

(:~
 : Creates the contents of the caches panel: what the caches of the server hold, and how often
 : they were of use.
 : @return panel contents
 :)
declare function panels:caches() as element(form) {
  (: a cache is transient and is managed by the server; what can be done with it is to give up
     what it holds :)
  <form method='post' autocomplete='off'>
    {
      (: the default cache is addressed by an operation that supplies no name :)
      let $names := distinct-values(('', sort(cache:list(), '?lang=en')))
      let $headers := (
        { 'key': 'label', 'label': 'Name' },
        { 'key': 'entries', 'label': 'Entries', 'type': 'number', 'order': 'desc' },
        { 'key': 'max-entries', 'label': 'Limit', 'type': 'number', 'order': 'desc' },
        { 'key': 'lifetime', 'label': 'Lifetime' },
        { 'key': 'hits', 'label': 'Hits', 'type': 'number', 'order': 'desc' },
        { 'key': 'misses', 'label': 'Misses', 'type': 'number', 'order': 'desc' },
        { 'key': 'evictions', 'label': 'Evicted', 'type': 'number', 'order': 'desc' },
        { 'key': 'expirations', 'label': 'Expired', 'type': 'number', 'order': 'desc' }
      )
      let $entries :=
        for $cache in $names
        let $info := cache:info($cache)
        let $lookups := $info?hits + $info?misses
        (: the first map takes precedence: its hits are supplemented by the hit rate :)
        return map:merge((
          {
            'cache': $cache,
            'label': $cache[.] otherwise '(default)',
            'lifetime': if ($info?ttl = 0) then 'unlimited' else (
              string(seconds($info?ttl)) => replace('[PT]', '') => lower-case()
            ),
            'hits': if ($lookups = 0) then 0 else (
              $info?hits || ' (' || format-number($info?hits div $lookups, '0%') || ')'
            )
          },
          $info
        ))
      let $buttons := (
        form:button('caches/delete', 'Delete', ('CHECK', 'CONFIRM')),
        form:button('caches/clear', 'Clear All', 'CONFIRM')
      )
      (: the checkbox submits the name: the default cache is addressed by an empty one :)
      return table:create($headers, $entries, $buttons, {},
        { 'sticky': <h2>Caches</h2>, 'select': 'cache' })
    }
  </form>
};

(:~
 : Creates the contents of the WebSockets panel: the connections that are open, and the paths
 : they were opened on.
 : @return panel contents
 :)
declare function panels:websockets() as element(form) {
  (: what a connection holds is its own; what the server can do with it is to close it :)
  panels:attribute-panel('websocket', 'websockets', 'WebSockets',
    (
      { 'key': 'websocket', 'label': 'Connection', 'type': 'dynamic', 'width': '9rem' },
      { 'key': 'session', 'label': 'Session', 'type': 'dynamic', 'width': '7rem' }
    ),
    (),
    for $ws in ws:list-details()
    let $id := string($ws/@id)
    let $path := string($ws/@path)
    let $dba := matches($path, '^/?dba(/|$)')
    return {
      'id': $id,
      'access': fn() { data($ws/@accessed) },
      'names': fn() { sort(ws:names($id), '?lang=en') },
      (: the jobs that the DBA runs for a connection link to their details :)
      'value': fn($name) {
        let $value := ws:get($id, $name)
        return if ($dba and $name = $utils:JOB) then panels:dba-jobs($value)
        else utils:preview($value, $panels:PREVIEW)
      },
      'columns': {
        (: the connection is named by the path it was opened on, a view of the DBA by its
           name. The tooltip carries what a column of its own would cost more than it is
           worth: the id, the user and the address of the client, and the time of the
           handshake :)
        'websocket': fn() {
          <span class='nowrap' title='{ string-join(($id, $ws/@user, html:address($ws/@address),
            'opened ' || panels:exact($ws/@created)), ', ') }'>{
            if ($dba) then 'DBA: ' || utils:capitalize(replace($path, '^/?dba/?', ''))
            else $path
          }</span>
        },
        (: the session that was authenticated for the handshake: it names the row of the
           Web Sessions panel that the connection belongs to :)
        'session': ($ws/@session ! panels:session(.)) otherwise '–'
      }
    }
  )
};

(:~
 : Creates the contents of the database sessions panel.
 : @return panel contents
 :)
declare function panels:db-sessions() as element()+ {
  <h2>Database Sessions</h2>,
  table:create(
    (
      { 'key': 'address', 'label': 'Address' },
      { 'key': 'user', 'label': 'User' }
    ),
    for $session in admin:sessions()
    let $address := string($session/@address)
    return { 'address': fn() { html:address($address) }, 'user': $session/@user }
  )
};
