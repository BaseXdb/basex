(:~
 : Page template and shared helpers.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace html = 'dba/lib/html';

import module namespace config = 'dba/lib/config' at 'config.xqm';
import module namespace utils = 'dba/lib/utils' at 'utils.xqm';

(:~ Options of a page; see html:wrap. :)
declare record html:page-options(
  header   as xs:string?,
  error    as xs:string?,
  divided  as xs:boolean?,
  columns  as xs:string*,
  rows     as xs:string*,
  scripts  as xs:string*,
  init     as xs:string?
);

(:~ Options of a link that runs a client function; see html:action. :)
declare record html:action-options(
  href      as xs:string?,
  selected  as xs:boolean?,
  title     as xs:string?,
  class     as xs:string?
);

(:~ Options of a content panel; see html:panel. :)
declare record html:panel-options(
  id           as xs:string?,
  pane         as xs:boolean?,
  label        as xs:string?,
  collapsed    as xs:boolean?,
  hidden       as xs:boolean?,
  fold         as xs:string?,
  extra        as node()*,
  description  as xs:string?
);

(:~
 : Extends the specified content panels with the page template.
 : @param  $panels   content panels, laid out side by side, one grid column each
 : @param  $options  options:
 :   * header: name of the page, which is also the entry marked in the navigation
 :   * error: error string; by default, what the address reports
 :   * divided: whether dividers separate the panels; a resizer separates them otherwise
 :   * columns: grid track widths, one per panel; defaults to equal widths
 :   * rows: grid track heights; the panels then fill the viewport instead of growing with
 :     their content, and each panel scrolls on its own
 :   * scripts: names of the scripts the page needs, besides the shared one
 :   * init: call that prepares the page, evaluated after the shared setup
 : @return page
 :)
declare function html:wrap(
  $panels   as element()*,
  $options  as html:page-options := {}
) as element(html) {
  (: the view is named by its page; the label of its entry is the capitalized name :)
  let $view := $options?header
  let $header := $view ! utils:capitalize(.)
  let $user := session:get($config:SESSION-KEY)
  (: what the last action reported. A page relays it from its address without ever looking at
     it, so the template fetches it instead of every page declaring, accepting and passing it
     on; a page that reports something of its own supplies it :)
  let $query := request:query-map()
  let $info := $query?info[.]
  let $error := ($options?error otherwise $query?error)[.]
  (: only panels get a grid track; a page may supply scripts as well :)
  let $tracks := $panels[tokenize(@class) = 'panel']
  let $columns := string-join($options?columns otherwise ($tracks ! '1fr'), ' ')
  let $rows := string-join($options?rows, ' ')
  (: the client cannot tell a context path from a subdirectory, so the server states it :)
  return <html lang='en' data-context='{ request:context-path() }'
               data-interval='{ min((config:get($config:INTERVAL), $config:MAX-INTERVAL)) }'
               data-xquery='{ $utils:XQUERY-REGEX }'>
    <head>
      <meta charset='utf-8'/>
      <meta http-equiv='Content-Security-Policy'
            content="default-src 'self'; script-src 'self' 'unsafe-inline';
                     style-src 'self' 'unsafe-inline'; img-src 'self' data:;
                     object-src 'none'; base-uri 'self'"/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>DBA{ $header ! (' » ' || .) }</title>
      <meta name='description' content='Database Administration'/>
      <meta name='author' content='BaseX Team, BSD License'/>
      <meta name='robots' content='noindex'/>
      <link rel='icon' href='.static/basex.svg'/>
      <link rel='stylesheet' href='{ html:static('style.css') }'/>
      <script src='{ html:static('js.js') }'/>
      { $options?scripts ! <script src='{ html:static(. || '.js') }'/> }
    </head>
    <body>
      <header>
        <div class='header-main'>
          <div class='header-top'>
            <h1>
              <span class='title-full'>BaseX Database Administration</span>
              <span class='title-short'>BaseX DBA</span>
            </h1>
            {
              if ($user) {
                (: the logout ends the session: it is submitted, not a link that a prefetch
                   or a link scanner could follow :)
                <div>
                  <b>{ $user }</b> · <form method='post' action='logout'>
                    <button type='submit' class='link'>log out</button>
                  </form>
                </div>
              }
            }
          </div>
          <div class='header-nav'>{
            if ($user) then (
              <nav>
                <ul>{
                  for $entry in $config:VIEWS
                  (: the current view is marked by an attribute, which is what a reader is
                     told and what the style picks out; the entry stays a link, and a click
                     on it reloads the starting page of the view :)
                  return <li>{
                    element a {
                      attribute href { $entry },
                      attribute aria-current { 'page' }[$entry = $view],
                      utils:capitalize($entry)
                    }
                  }</li>
                }</ul>
              </nav>
            ) else (
              <div class='note'>
                Please enter your admin credentials:
              </div>
            ),
            (: a status message, not a navigation entry: the client replaces its text, and
               the role is what has the replacement announced. It is spelled out, as not
               every browser maps the implicit role of the element :)
            element output {
              attribute id { 'info' },
              attribute role { 'status' },
              if ($error) then (
                attribute class { 'error' }, $error
              ) else if ($info) {
                attribute class { 'info' }, $info
              }
            }
          }</div>
        </div>
        <a href='/' class='header-logo'>
          <img src='.static/basex.svg' alt='BaseX'/>
        </a>
      </header>
      <main>
        <div class='content{ ' fill'[$rows] }{ ' divided'[$options?divided] }'
             style='--columns: { $columns }{ $rows[.] ! ('; --rows: ' || .) }'>{
          $panels
        }</div>
      </main>
      <hr/>
      <footer><sup>{
        (: the shortcut hint and the version are only shown to administrators :)
        if ($user) {
          <span class='shortcuts-hint'>Press <kbd>?</kbd> for keyboard shortcuts</span>,
          `BaseX { Q{org.basex.util.Prop}VERSION() } ·`
        },
        'BSD License'
      }</sup></footer>
      {
        (: what changes while a view refreshes itself is announced to screen readers; it is
           not shown, as the panels show it already :)
        <div id='announce' class='sr-only' aria-live='polite'/>
      }
      {
        (: the dialogs that replace the browser's confirm and prompt: every page has them,
           and 'method=dialog' hands the clicked button's value back as the answer :)
        (: the answers are supplied by the question, so the row is filled by askQuestion :)
        <dialog id='confirm-dialog'>
          <form method='dialog'>
            <p id='confirm-text'/>
            <p id='confirm-detail' class='note'/>
            <div class='buttons'/>
          </form>
        </dialog>,
        <dialog id='prompt-dialog'>
          <form method='dialog'>
            <p id='prompt-text'/>
            <input type='text' id='prompt-input' class='wide'/>
            <div class='buttons'>
              <button value='ok' id='prompt-submit'>OK</button>
              <button value=''>Cancel</button>
            </div>
          </form>
        </dialog>,
        html:js('buttons(); ready();'),
        (: the page is prepared once the shared setup is done :)
        $options?init ! html:js(.),
        html:js('hideParams("info", "error");')[$info or $error]
      }
    </body>
  </html>
};

(:~
 : Returns the address of a static file of the application, versioned by its modification time.
 : @param  $file  file name
 : @return address
 :)
declare %private function html:static(
  $file  as xs:string
) as xs:string {
  (: the browser caches the files: a changed file is requested under a new address :)
  let $modified := try { file:last-modified(file:base-dir() || '../static/' || $file) } catch * { }
  return `.static/{ $file }{ $modified ! `?v={ replace(string(.), '\D', '') }` }`
};

(:~
 : Creates a content panel of a view: one grid track of the page that html:wrap lays out.
 : @param  $contents  panel contents
 : @param  $options   options:
 :   * id: id of the block that holds the contents. A panel that the server pushes is filled
 :     into it, and the message that carries it names the block
 :   * pane: whether that block scrolls on its own; contents that bring their own block ask for
 :     neither (default: true)
 :   * label: name of the panel, written on the strip it folds to. The empty string leaves the
 :     panel where it is; no name at all falls back to the heading the panel shows
 :   * collapsed: whether the panel opens folded away
 :   * hidden: whether the panel is left out; by default, one with nothing to show is
 :   * fold: 'right' if the panel folds towards the right edge
 :   * extra: content beside the block, which a pushed panel does not replace
 :   * description: what the panel holds, stated by the strip it folds to
 : @return panel
 :)
declare function html:panel(
  $contents  as node()*,
  $options   as html:panel-options := {}
) as element(div) {
  let $id := $options?id
  let $pane := $options?pane otherwise true()
  return <div class='{ string-join((
    'panel',
    'collapsed'[$options?collapsed],
    'hidden'[$options?hidden otherwise empty($contents)]
  ), ' ') }'>{
    $options?label ! attribute data-label { . },
    $options?description ! attribute data-description { . },
    $options?fold ! attribute data-fold { . },
    (: the contents get a block of their own if they scroll or are replaced :)
    if ($pane or $id) then (
      element div {
        $id ! attribute id { . },
        attribute class { 'pane' }[$pane],
        $contents
      }
    ) else (
      $contents
    ),
    $options?extra
  }</div>
};

(:~
 : Creates a symbol that a screen reader reads as a word.
 : @param  $symbol  symbol that is shown
 : @param  $text    text that is read instead
 : @return symbol
 :)
declare function html:symbol(
  $symbol  as xs:string,
  $text    as xs:string
) as element(span) {
  <span><span aria-hidden='true'>{ $symbol }</span><span class='sr-only'>{ $text }</span></span>
};

(:~
 : Creates a note that is appended to an entry, such as the marker of the current user.
 : @param  $text  text of the note
 : @return note
 :)
declare function html:marker(
  $text  as xs:string
) as element(span) {
  <span class='note'>{ html:symbol(' · ', ', '), $text }</span>
};

(:~
 : Creates the heading of a panel or a block, with the controls that belong to it.
 : @param  $text      heading text
 : @param  $controls  controls placed beside the heading
 : @return heading and its controls
 :)
declare function html:heading(
  $text      as xs:string,
  $controls  as item()+
) as element(div) {
  (: the controls sit beside the heading, not inside it: a heading that does not fit is
     clipped, and a button that belongs to it must not be clipped away with it :)
  <div class='pane-title'>{
    <h2>{ $text }</h2>,
    $controls
  }</div>
};

(:~
 : Creates a link to the specified target.
 : @param  $text    link text
 : @param  $href    link reference
 : @param  $params  query parameters
 : @param  $title   tooltip
 : @return link
 :)
declare function html:link(
  $text    as xs:string,
  $href    as xs:string,
  $params  as map(*)* := {},
  $title   as xs:string? := ()
) as element(a) {
  <a href='{ web:create-url($href, map:merge($params)) }'>{
    $title ! attribute title { . },
    $text
  }</a>
};

(:~
 : Creates a link that selects an entry: a deep link naming the whole selection.
 : @param  $label     link label
 : @param  $page      page the link refers to
 : @param  $params    selection the link refers to
 : @param  $selected  whether the link refers to what is shown
 : @param  $key       parameter that names the selected entry
 : @param  $call      client function that selects the entry
 : @return function creating the link
 :)
declare function html:select(
  $label     as xs:string,
  $page      as xs:string,
  $params    as map(*),
  $selected  as xs:boolean,
  $key       as xs:string,
  $call      as xs:string
) as fn() as element(a) {
  (: the link can be followed and bookmarked; a view that refreshes its panels over its own
     connection follows it in place, which is what the supplied call does :)
  let $href := web:create-url($page, $params)
  return fn() {
    html:action($label, $call, { 'select': $params?$key }, { 'href': $href, 'selected': $selected })
  }
};

(:~
 : Creates a link that runs a client function instead of being followed.
 : @param  $label    link label
 : @param  $call     client function
 : @param  $data     values the link names
 : @param  $options  options: 'href' (deep link that the call follows in place; the link refers
 :                   to no page of its own without it), 'selected' (whether the link refers to
 :                   what is shown), 'title' (tooltip), 'class' (further classes)
 : @return link
 :)
declare function html:action(
  $label    as item()*,
  $call     as xs:string,
  $data     as map(*),
  $options  as html:action-options := {}
) as element(a) {
  (: the values are supplied as data attributes: a link that names a single one hands that
     value to the function, one that names several hands over all of them. A link that selects
     an entry names it under 'select', which is what the client points the entry out by :)
  let $class := string-join(('selected'[$options?selected], $options?class), ' ')
  return <a href='{ $options?href otherwise '#' }'>{
    for key $name value $value in $data return attribute { 'data-' || $name } { $value },
    attribute class { $class }[$class],
    $options?title ! attribute title { . },
    (: the function is named as a global: in an inline handler, a plain name is looked up on the
       element first (a function named 'setAttribute' would not be called) :)
    attribute onclick {
      let $arg := `.{ head(map:keys($data)) }`[map:size($data) = 1]
      return `window.{ $call }(this.dataset{ $arg }); return false;`
    },
    $label
  }</a>
};

(:~
 : Returns an exact representation of a dateTime value, as it is forwarded to administrators.
 : @param  $date  date
 : @return string
 :)
declare function html:exact(
  $date  as xs:dateTime
) as xs:string {
  format-dateTime(adjust-dateTime-to-timezone($date), '[Y0000]-[M00]-[D00] [H00]:[m00]:[s00]')
};

(:~
 : Returns a compact representation of a dateTime value with tooltip.
 : @param  $date  date
 : @return element with tooltip
 :)
declare function html:short-date(
  $date  as xs:dateTime
) as element(span) {
  (: the year is only shown if it is not the current one; the time only if it is :)
  html:date($date, '[Y]', '[M00]-[D00] [H00]:[m00]')
};

(:~
 : Returns a formatted time representation of a dateTime value with tooltip.
 : @param  $date  date
 : @return element with tooltip
 :)
declare function html:time(
  $date  as xs:dateTime
) as element(span) {
  (: a value of another day is shown as its date, which would otherwise be lost :)
  html:date($date, '[Y]-[M]-[D]', '[H00]:[m00]:[s00]')
};

(:~
 : Formats a dateTime value with tooltip: as a recent value, or as its date.
 : @param  $date     date
 : @param  $period   picture of the period in which a value is recent
 : @param  $picture  picture of a recent value
 : @return element with tooltip
 :)
declare %private function html:date(
  $date     as xs:dateTime,
  $period   as xs:string,
  $picture  as xs:string
) as element(span) {
  let $adjusted := adjust-dateTime-to-timezone($date)
  let $recent := format-dateTime($adjusted, $period) =
    format-dateTime(current-dateTime(), $period)
  return <span class='nowrap' title='{ html:exact($date) }'>{
    format-dateTime($adjusted, if ($recent) then $picture else '[Y0000]-[M00]-[D00]')
  }</span>
};

(:~
 : Formats a client address, with the logged address as tooltip.
 : @param  $address  address, optionally followed by a port
 : @return element with the address
 :)
declare function html:address(
  $address  as xs:string
) as element(span) {
  <span title='{ $address }'>{ html:localhost($address) }</span>
};

(:~
 : Returns a client address with the loopback address replaced by localhost.
 : @param  $address  address, optionally followed by a port
 : @return address
 :)
declare function html:localhost(
  $address  as xs:string
) as xs:string {
  replace($address, '^(\[?(0:0:0:0:0:0:0:1|::1)\]?|127\.\d+\.\d+\.\d+)(:\d+)?$', 'localhost$3')
};

(:~
 : Formats a duration.
 : @param  $seconds  seconds
 : @return string
 :)
declare function html:duration(
  $seconds  as xs:decimal
) as xs:string {
  `{ format-number($seconds idiv 60, '00') }:{ format-number($seconds mod 60, '00') }`
};

(:~
 : Creates an embedded JavaScript snippet.
 : @param  $js  JavaScript string
 : @return script element
 :)
declare function html:js(
  $js  as xs:string
) as element(script) {
  <script>{ '(function() { ' || $js || ' })();' }</script>
};

(:~
 : Creates a new map with query parameters.
 : @param  $map  predefined parameters
 : @return all current query parameters, and the given ones, prefixed with an underscore
 :)
declare function html:parameters(
  $map  as map(*)? := ()
) as map(*) {
  {
    for $param in request:parameter-names()[not(starts-with(., '_'))]
    return { $param: request:parameter($param) },
    for key $name value $value in $map return { `_{ $name }`: $value }
  }
};
