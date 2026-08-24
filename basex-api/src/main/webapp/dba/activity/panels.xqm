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
        { 'key': 'id', 'label': 'ID' },
        { 'key': 'state', 'label': 'State' },
        { 'key': 'service', 'label': 'Service' },
        { 'key': 'duration', 'label': 'Dur.', 'type': 'number', 'order': 'desc' },
        { 'key': 'user', 'label': 'User' },
        { 'key': 'time', 'label': 'Time', 'type': 'time', 'order': 'desc' },
        { 'key': 'start', 'label': 'Start', 'type': 'time', 'order': 'desc' }
      )
      let $services := job:services()
      let $jobs := job:list-details()
      let $entries := (
        let $curr := job:current()
        for $details in $jobs
        let $id := $details/@id
        (: the job that renders this table is of no interest :)
        where not($id = $curr)
        let $sec := (xs:dayTimeDuration($details/@duration) div xs:dayTimeDuration('PT1S'))
          otherwise 0
        let $time := data($details/@time)
        let $start := data($details/@start)
        order by $sec descending, $start descending
        return {
          'id': $id,
          'state': $details/@state,
          'service': if ($services/@id = $id) then '✓' else '–',
          'duration': html:duration($sec),
          'user': $details/@user,
          'time': $time,
          'start': $start otherwise $time
        },
        (: services without a job are dormant: they are rescheduled when the server is restarted :)
        for $service in $services
        let $id := $service/@id
        where not($id = $jobs/@id)
        return {
          'id': $id,
          'state': 'registered',
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
      let $options := { 'sort': $sort, 'presort': 'duration' }
      return table:create($headers, $entries, $buttons, {}, $options) update {
        (: replace job ids with links; the separator after the checkbox stays outside :)
        for $tr in descendant::tr[not(th)]
        for $text in $tr/td[1]/text()
        for $id in data($tr/@id)
        return replace node $text with (
          substring-before($text, $id),
          <a href='?job={ $id }'>{ $id }</a>
        )
      }
    }
  </form>
};

(:~
 : Creates the dialog that starts a new job. It is not part of a panel: the panels are replaced
 : while the view refreshes, which would close the dialog while it is being filled in.
 : @return dialog
 :)
declare function panels:job-dialog() as element(dialog) {
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
  let $output := if ($details/@state = 'cached') {
    try {
      utils:serialize(job:result($job, { 'keep': true() }))
    } catch * {
      'Stopped at ' || $err:module || ', ' || $err:line-number || '/' ||
        $err:column-number || ':' || char('\n') || $err:description
    }
  }
  return <form method='post' autocomplete='off'>{
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
      <table>{
        map:for-each($bindings, fn($key, $value) {
          <tr>
            <td><b>{ if ($key) then '$' || $key else 'Context' }</b></td>
            <td><code>{
              utils:chop(serialize($value, { 'method': 'basex' }), 1000)
            }</code></td>
          </tr>
        })
      }
      </table>
    ),

    if ($output) {
      <h3>Result</h3>,
      <textarea id='output' rows='8' readonly='' spellcheck='false'>{ $output }</textarea>
    },

    (: a stored definition can be replaced; a job string is only shown :)
    <h3>{
      if ($persisted) {
        'Query', '&#xa0;', form:button('jobs/replace', 'Replace')
      } else {
        'Job String'
      }
    }</h3>,
    <textarea rows='8' spellcheck='false'>{
      attribute id { 'job-string' }[$persisted],
      attribute name { 'query' }[$persisted],
      attribute readonly { }[not($persisted)],
      $query
    }</textarea>
  }</form>
};

(:~
 : Indicates whether the details of a job will not change any more. A service is done as well:
 : its query is edited in place, and a refresh would replace the editor while it is used.
 : @param  $job  job id
 : @return result of check
 :)
declare function panels:job-done(
  $job  as xs:string?
) as xs:boolean {
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
  <table>{
    for $value in $entry/@*
    for $name in name($value)[. != 'id']
    return <tr>
      <td><b>{ utils:capitalize($name) }</b></td>
      <td>{ string($value) }</td>
    </tr>
  }</table>
};

(:~
 : Creates the contents of the web sessions panel: what the sessions of the server hold. A
 : value that was assigned by a Java application is shown as the object it is.
 : @return panel contents
 :)
declare function panels:web-sessions() as element()+ {
  <form method='post' autocomplete='off'>
    <h2>Web Sessions</h2>
    {
      let $current := session:id()
      let $headers := (
        (: fixed widths: a value can be long, and is truncated rather than widening the table :)
        { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '17%' },
        { 'key': 'value', 'label': 'Value', 'width': '21%' },
        (: a time and a flag are as wide as they will ever be: they are given what they need,
           not a share that grows with the panel. 'Access' rather than 'Last Access': a label
           that does not fit its column is truncated as a value is :)
        { 'key': 'access', 'label': 'Access', 'type': 'time', 'order': 'desc',
          'width': '4.5rem' },
        { 'key': 'you', 'label': 'You', 'width': '2.5rem' },
        (: last, and with no width of its own: a session id is long, so it takes what the other
           columns leave :)
        { 'key': 'session', 'label': 'Session' }
      )
      let $entries :=
        for $id in sessions:ids()
        (: a session can be dropped between being listed and being read; skip the ones that
           are gone by then, rather than failing the whole panel :)
        for $entry in try {
          let $access := sessions:accessed($id)
          (: a session that holds nothing gets a row of its own: it can be closed like any
             other, and a session that is listed by none of its attributes could not be :)
          for $name in (sessions:names($id) otherwise '')
          let $value := if ($name) {
            (: serialize more characters than requested, as the limit represents number of
               bytes :)
            utils:chop(serialize(sessions:get($id, $name), {
              'method': 'basex', 'limit': $panels:PREVIEW * 2 + 1
            }), $panels:PREVIEW)
          }
          return {
            'id': $id || '|' || $name,
            'session': $id,
            'name': if ($name) { panels:attribute('session', $id, $name) } else { '–' },
            'value': $value otherwise '–',
            'access': $access,
            'you': if ($id = $current) then '✓' else '–'
          }
        } catch sessions:not-found { }
        (: the attributes of a session are listed in one block: they share its access time :)
        order by $entry?access descending
        return $entry
      let $buttons := (
        form:button('sessions/delete', 'Delete', ('CHECK', 'CONFIRM')),
        form:button('sessions/close', 'Close', ('CHECK', 'CONFIRM'))
      )
      (: the checkbox submits the attribute: a session is not addressed by what it shows :)
      return table:create($headers, $entries, $buttons, {}, { 'select': 'id' })
    }
  </form>
};

(:~
 : Creates the link of an attribute: the name that assigns its value.
 : @param  $kind  what holds the attribute ('session', 'websocket')
 : @param  $id    id of the session or connection
 : @param  $name  attribute name
 : @return function creating the link
 :)
declare %private function panels:attribute(
  $kind  as xs:string,
  $id    as xs:string,
  $name  as xs:string
) as fn() as element(a) {
  fn() {
    <a href='#' data-kind='{ $kind }' data-id='{ $id }' data-name='{ $name }'
       title='Assign a new value' onclick='setAttribute(this.dataset); return false;'>{
      $name
    }</a>
  }
};

(:~
 : Creates the dialog that assigns a session attribute. As the dialog that starts a job, it is
 : not part of a panel: the panels are replaced while the view refreshes.
 : @return dialog
 :)
declare function panels:session-dialog() as element(dialog) {
  panels:attribute-dialog('session', 'Session:', 'sessions/set')
};

(:~
 : Creates the dialog that assigns an attribute of a WebSocket connection.
 : @return dialog
 :)
declare function panels:websocket-dialog() as element(dialog) {
  panels:attribute-dialog('websocket', 'WebSocket:', 'websockets/set')
};

(:~
 : Creates the dialog that assigns an attribute. The ids of its fields are derived from what
 : holds the attribute, so that the two dialogs of the view do not collide.
 : @param  $kind    what holds the attribute ('session', 'websocket')
 : @param  $label   label of the field that names it
 : @param  $action  action the dialog posts to
 : @return dialog
 :)
declare %private function panels:attribute-dialog(
  $kind    as xs:string,
  $label   as xs:string,
  $action  as xs:string
) as element(dialog) {
  form:dialog($kind, 'Set Attribute', $action, false(), (
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
  ))
};

(:~
 : Creates the contents of the caches panel: what the caches of the server hold, and how often
 : they were of use. A cache is transient and is managed by the server; what can be done with
 : it is to give up what it holds.
 : @return panel contents
 :)
declare function panels:caches() as element(form) {
  <form method='post' autocomplete='off'>
    {
      (: the default cache is addressed by an operation that supplies no name :)
      let $names := distinct-values(('', sort(cache:list(), '?lang=en')))
      let $headers := (
        { 'key': 'label', 'label': 'Name' },
        { 'key': 'entries', 'label': 'Entries', 'type': 'number', 'order': 'desc' },
        { 'key': 'hits', 'label': 'Hits', 'type': 'number', 'order': 'desc' },
        { 'key': 'misses', 'label': 'Misses', 'type': 'number', 'order': 'desc' },
        { 'key': 'evictions', 'label': 'Evicted', 'type': 'number', 'order': 'desc' },
        { 'key': 'expirations', 'label': 'Expired', 'type': 'number', 'order': 'desc' }
      )
      let $entries :=
        for $cache in $names
        return map:merge((
          { 'cache': $cache, 'label': $cache[.] otherwise '(default)' },
          cache:info($cache)
        ))
      let $buttons := (
        form:button('caches/delete', 'Delete', ('CHECK', 'CONFIRM')),
        form:button('caches/clear', 'Clear All', 'CONFIRM')
      )
      (: the checkbox submits the name: the default cache is addressed by an empty one :)
      return table:create($headers, $entries, $buttons, {},
        { 'sticky': <h2>Caches</h2>, 'compact': true(), 'select': 'cache' })
    }
  </form>
};

(:~
 : Creates the contents of the WebSockets panel: the connections that are open, and the paths
 : they were opened on. What a connection holds is its own; what the server can do with it is
 : to close it.
 : @return panel contents
 :)
declare function panels:websockets() as element(form) {
  <form method='post' autocomplete='off'>
    <h2>WebSockets</h2>
    {
      let $headers := (
        (: fixed widths: a value can be long, and is truncated rather than widening the table :)
        { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '16%' },
        { 'key': 'value', 'label': 'Value', 'width': '22%' },
        (: how long a connection has been idle; what it was opened on, by whom and from where
           is stated by the connection itself, which has no column of its own :)
        { 'key': 'accessed', 'label': 'Access', 'type': 'time', 'order': 'desc',
          'width': '4.5rem' },
        { 'key': 'websocket', 'label': 'ID', 'type': 'dynamic', 'width': '14%' },
        (: last, and with no width of its own: a session id is long, so it takes what the
           other columns leave :)
        { 'key': 'session', 'label': 'Session' }
      )
      let $entries :=
        for $ws in ws:list-details()
        let $id := string($ws/@id)
        (: a connection can be closed between being listed and being read; skip the ones that
           are gone by then, rather than failing the whole panel :)
        for $entry in try {
          (: a connection that holds nothing gets a row of its own: it can be closed like any
             other, and a connection that is listed by none of its attributes could not be :)
          for $name in (sort(ws:names($id), '?lang=en') otherwise '')
          let $value := if ($name) {
            (: serialize more characters than requested, as the limit represents number of
               bytes :)
            utils:chop(serialize(ws:get($id, $name), {
              'method': 'basex', 'limit': $panels:PREVIEW * 2 + 1
            }), $panels:PREVIEW)
          }
          return {
            'id': $id || '|' || $name,
            (: the tooltip carries what a column of its own would cost more than it is worth:
               the path, the user and the address of the client, and the time of the handshake :)
            'websocket': fn() {
              <span title='{ string-join(($ws/@path, $ws/@user, $ws/@address,
                'opened ' || html:date(xs:dateTime($ws/@created))), ', ') }'>{ $id }</span>
            },
            'name': if ($name) { panels:attribute('websocket', $id, $name) } else { '–' },
            'value': $value otherwise '–',
            'accessed': data($ws/@accessed),
            (: the session that was authenticated for the handshake: it names the row of the
               Web Sessions panel that the connection belongs to :)
            'session': data($ws/@session) otherwise '–'
          }
        } catch ws:not-found { }
        (: the attributes of a connection are listed in one block: they share its times :)
        order by $entry?accessed descending
        return $entry
      let $buttons := (
        form:button('websockets/delete', 'Delete', ('CHECK', 'CONFIRM')),
        form:button('websockets/close', 'Close', ('CHECK', 'CONFIRM'))
      )
      (: the checkbox submits the attribute: a connection is not addressed by what it shows :)
      return table:create($headers, $entries, $buttons, {}, { 'select': 'id' })
    }
  </form>
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
    admin:sessions() ! { 'address': @address, 'user': @user }
  )
};
