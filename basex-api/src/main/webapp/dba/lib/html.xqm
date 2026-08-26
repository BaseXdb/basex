(:~
 : Page template and shared helpers.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace html = 'dba/lib/html';

import module namespace config = 'dba/lib/config' at 'config.xqm';
import module namespace utils = 'dba/lib/utils' at 'utils.xqm';

(:~
 : Extends the specified content panels with the page template.
 : @param  $panels   content panels, laid out side by side, one grid column each
 : @param  $options  options:
 :   * header: name of the page, which is also the entry marked in the navigation
 :   * error: error string; by default, what the address reports
 :   * info: info string; by default, what the address reports
 :   * columns: grid track widths, one per panel; defaults to equal widths
 :   * rows: grid track heights; the panels then fill the viewport instead of growing with
 :     their content, and each panel scrolls on its own
 :   * panels: name of the subview, if the page shows different panels in different subviews;
 :     the collapsed state is then remembered for each of them, as it is kept by position
 :   * scripts: names of the scripts the page needs, besides the shared one
 :   * init: call that prepares the page, evaluated after the shared setup
 : @return page
 :)
declare function html:wrap(
  $panels   as element()*,
  $options  as map(*) := {}
) as element(html) {
  (: the view is named by its page; the label of its entry is the capitalized name :)
  let $view := $options?header
  let $header := $view ! utils:capitalize(.)
  let $user := session:get($config:SESSION-KEY)
  (: what the last action reported. A page relays it from its address without ever looking at
     it, so the template fetches it instead of every page declaring, accepting and passing it
     on; a page that reports something of its own supplies it :)
  let $info := ($options?info otherwise request:parameter('info'))[.]
  let $error := ($options?error otherwise request:parameter('error'))[.]
  (: only panels get a grid track; a page may supply scripts as well :)
  let $tracks := $panels[tokenize(@class) = 'panel']
  let $columns := string-join($options?columns otherwise ($tracks ! '1fr'), ' ')
  let $rows := string-join($options?rows, ' ')
  (: the client cannot tell a context path from a subdirectory, so the server states it :)
  return <html lang='en' data-context='{ request:context-path() }'
               data-interval='{ config:get($config:INTERVAL) }'
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
      <link rel='stylesheet' href='.static/style.css'/>
      <script src='.static/js.js'/>
      { $options?scripts ! <script src='.static/{ . }.js'/> }
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
                    <button type='submit' class='link'>logout</button>
                  </form>
                </div>
              }
            }
          </div>
          <div class='header-nav'>{
            if ($user) {
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
            } else {
              <div class='note'>
                Please enter your admin credentials:
              </div>
            },
            (: a status message, not a navigation entry: the client replaces its text, and
               the role is what has the replacement announced. It is spelled out, as not
               every browser maps the implicit role of the element :)
            element output {
              attribute id { 'info' },
              attribute role { 'status' },
              if ($error) {
                attribute class { 'error' }, $error
              } else if ($info) {
                attribute class { 'info' }, $info
              }
            }
          }</div>
        </div>
        <a href='{ request:context-path() }/dba' class='header-logo'>
          <img src='.static/basex.svg' alt='BaseX'/>
        </a>
      </header>
      <main>
        <div class='content{ ' fill'[$rows] }'
             style='--columns: { $columns }{ $rows[.] ! ('; --rows: ' || .) }'>{
          $options?panels ! attribute data-panels { . },
          $panels
        }</div>
      </main>
      <hr/>
      <footer><sup>BaseX Team, BSD License</sup></footer>
      {
        (: the dialogs that replace the browser's confirm and prompt: every page has them,
           and 'method=dialog' hands the clicked button's value back as the answer :)
        (: the answers are supplied by the question, so the row is filled by askQuestion :)
        <dialog id='confirm-dialog'>
          <form method='dialog'>
            <p id='confirm-text'/>
            <div class='buttons'/>
          </form>
        </dialog>,
        <dialog id='prompt-dialog'>
          <form method='dialog'>
            <p id='prompt-text'/>
            <input type='text' id='prompt-input' class='wide'/>
            <div class='buttons'>
              <button value='ok'>OK</button>
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
 : Creates a content panel of a view: one grid track of the page that html:wrap lays out.
 : @param  $contents  panel contents
 : @param  $options   options:
 :   * id: id of the block that holds the contents. A panel that the server pushes is filled
 :     into it, and the message that carries it names the block
 :   * pane: whether that block scrolls on its own; a string adds further classes to it, and
 :     contents that bring their own block ask for neither (default: true)
 :   * label: name of the panel, written on the strip it folds to. The empty string leaves the
 :     panel where it is; no name at all falls back to the heading the panel shows
 :   * collapsed: whether the panel opens folded away
 :   * hidden: whether the panel is left out; by default, one with nothing to show is
 :   * fold: 'right' if the panel folds towards the right edge
 :   * divider: whether a divider separates the panel from the one before it
 :   * class: further classes of the panel
 :   * style: grid placement of the panel
 :   * panel-id: id of the panel itself
 :   * extra: content beside the block, which a pushed panel does not replace
 : @return panel
 :)
declare function html:panel(
  $contents  as node()*,
  $options   as map(*) := {}
) as element(div) {
  let $id := $options?id
  (: the block that holds the contents; a string names the classes it carries besides 'pane' :)
  let $pane := $options?pane otherwise true()
  let $pane-class := if ($pane instance of xs:string) {
    'pane ' || $pane
  } else if ($pane) {
    'pane'
  }
  return <div class='{ string-join((
    'panel',
    'no-divider'[not($options?divider)],
    'collapsed'[$options?collapsed],
    'hidden'[$options?hidden otherwise empty($contents)],
    $options?class
  ), ' ') }'>{
    $options?panel-id ! attribute id { . },
    $options?style ! attribute style { . },
    attribute data-label { $options?label }[map:contains($options, 'label')],
    $options?fold ! attribute data-fold { . },
    (: the contents get a block of their own if they scroll or are replaced :)
    if (exists($pane-class) or exists($id)) {
      element div {
        $id ! attribute id { . },
        attribute class { $pane-class }[$pane-class],
        $contents
      }
    } else {
      $contents
    },
    $options?extra
  }</div>
};

(:~
 : Creates the heading of a panel or a block, with the controls that belong to it.
 : @param  $text      heading text
 : @param  $controls  controls placed beside the heading
 : @param  $level     name of the heading element
 : @return heading and its controls
 :)
declare function html:heading(
  $text      as xs:string,
  $controls  as item()+,
  $level     as xs:string := 'h2'
) as element(div) {
  (: the controls sit beside the heading, not inside it: a heading that does not fit is
     clipped, and a button that belongs to it must not be clipped away with it :)
  <div class='pane-title'>{
    element { $level } { $text },
    $controls
  }</div>
};

(:~
 : Creates a link to the specified target.
 : @param  $text    link text
 : @param  $href    link reference
 : @param  $params  query parameters
 : @return link
 :)
declare function html:link(
  $text    as xs:string,
  $href    as xs:string,
  $params  as map(*)* := {}
) as element(a) {
  <a href='{ web:create-url($href, map:merge($params)) }'>{ $text }</a>
};

(:~
 : Creates a link that selects an entry: a deep link naming the whole selection.
 : @param  $label     link label
 : @param  $page      page the link refers to
 : @param  $params    selection the link refers to
 : @param  $selected  whether the link refers to what is shown
 : @param  $key       parameter that names the selected entry; empty if the link is followed by
 :                    loading the page it names
 : @param  $call      client function that selects the entry
 : @return function creating the link
 :)
declare function html:select(
  $label     as xs:string,
  $page      as xs:string,
  $params    as map(*),
  $selected  as xs:boolean,
  $key       as xs:string? := (),
  $call      as xs:string? := ()
) as fn() as element(a) {
  (: the link can be followed and bookmarked; a view that refreshes its panels over its own
     connection follows it in place, which is what the supplied call does :)
  let $href := web:create-url($page, $params)
  return fn() {
    if ($key) {
      html:action($label, $call, { 'select': $params?($key) },
        { 'href': $href, 'selected': $selected })
    } else {
      <a href='{ $href }'>{ attribute class { 'selected' }[$selected], $label }</a>
    }
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
  $options  as map(*) := {}
) as element(a) {
  (: the values are supplied as data attributes: a link that names a single one hands that
     value to the function, one that names several hands over all of them. A link that selects
     an entry names it under 'select', which is what the client points the entry out by :)
  let $class := string-join(('selected'[$options?selected], $options?class), ' ')
  return <a href='{ $options?href otherwise '#' }'>{
    map:for-each($data, fn($name, $value) { attribute { 'data-' || $name } { $value } }),
    attribute class { $class }[$class],
    $options?title ! attribute title { . },
    attribute onclick { $call ||
      '(this.dataset' || ('.' || head(map:keys($data)))[map:size($data) = 1] ||
      '); return false;' },
    $label
  }</a>
};

(:~
 : Returns a formatted representation of a dateTime value.
 : @param  $date  date
 : @return string
 :)
declare function html:date(
  $date  as xs:dateTime
) as xs:string {
  (: seconds are dropped: the value tells when a file or database was last touched, which
     no one counts in seconds :)
  format-dateTime(html:adjust($date), '[Y0000]-[M00]-[D00] [H00]:[m00]')
};

(:~
 : Returns a formatted time representation of a dateTime value with tooltip.
 : @param  $date  date
 : @return element with tooltip
 :)
declare function html:time(
  $date  as xs:dateTime
) as element(span) {
  let $adjusted := html:adjust($date)
  let $formatted := format-dateTime($adjusted, '[H00]:[m00]:[s00]')
  return <span title='{ $adjusted }'>{ $formatted }</span>
};

(:~
 : Returns a dateTime value adjusted to the current time zone.
 : @param  $date  date
 : @return adjusted value
 :)
declare function html:adjust(
  $date  as xs:dateTime
) as xs:dateTime {
  let $zone := timezone-from-dateTime(current-dateTime())
  return adjust-dateTime-to-timezone(xs:dateTime($date), $zone)
};

(:~
 : Formats a duration.
 : @param  $seconds  seconds
 : @return string
 :)
declare function html:duration(
  $seconds  as xs:decimal
) as xs:string {
  let $min := $seconds idiv 60
  let $sec := $seconds - $min * 60
  return (format-number($min, '00') || ':' || format-number($sec, '00'))
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
 : Creates a new map with the current query parameters.
 : @return map with query parameters
 :)
declare function html:parameters() as map(*) {
  map:merge(
    for $param in request:parameter-names()[not(starts-with(., '_'))]
    return { $param: request:parameter($param) }
  )
};

(:~
 : Creates a new map with query parameters.
 : @param  $map  predefined parameters
 : @return all current query parameters, and the given ones, prefixed with an underscore
 :)
declare function html:parameters(
  $map  as map(*)?
) as map(*) {
  map:merge((
    html:parameters(),
    map:for-each($map, fn($name, $value) {
      map:entry('_' || $name, $value)
    })
  ))
};
