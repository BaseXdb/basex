(:~
 : This module contains some basic examples for RESTXQ annotations.
 : @author BaseX Team
 :)
module namespace page = 'http://basex.org/examples/web-page';

(:~
 : Generates a welcome page.
 : @return HTML page
 :)
declare
  %rest:GET
  %rest:path('')
  %output:method('html')
function page:start(
) as element(html) {
  <html lang='en'>
    <head>
      <meta charset='utf-8'/>
      <meta name='viewport' content='width=device-width, initial-scale=1'/>
      <title>BaseX HTTP Services</title>
      <link rel='icon' href='static/basex.svg'/>
      <link rel='stylesheet' type='text/css' href='static/style.css'/>
    </head>
    <body>
      <header>
        <div class='header-main'>
          <div class='header-top'>
            <h1>BaseX HTTP Services</h1>
          </div>
          <nav class='ellipsis'>
            <a href='dba'>DBA</a> · <a href='chat'>Chat</a>
          </nav>
          <hr/>
        </div>
        <a href='/' class='header-logo'><img src='static/basex.svg' alt='BaseX'/></a>
      </header>
      <main>
        <p>Welcome to the BaseX HTTP Services: sample web applications written in
          XQuery, and a REST interface to your databases.</p>
        <p>For the full picture, see the
          <a href='https://docs.basex.org/main/Web_Application'>Web Application</a>
          documentation.</p>

        <h2>RESTXQ &amp; WebSockets</h2>
        <p>These sample applications are built entirely in XQuery with
          <a href='https://docs.basex.org/main/RESTXQ'>RESTXQ</a>.
          Their source is yours to read: a starting point for your own
          web applications.</p>

        <h3><a href='dba'>DBA: Database Administration</a></h3>
        <p><b>A complete web front-end for your server</b></p>
        <p>Browse and edit databases and their resources, manage users,
        run queries in a live editor, and keep an eye on jobs,
        sessions and logs, all updating in real time.</p>

        <h3><a href='chat'>Chat Application</a></h3>
        <p><b>A live, multi-room chat</b></p>
        <p>Switch between channels, see who is online and where they are, and post to the whole room
          or privately to a single person. Every message pushed instantly over WebSockets.</p>
        <p>For the full effect, create multiple users in the DBA, open the chat in two browsers,
          and log in as different users.</p>

        <h2>REST</h2>
        <p><b>Work with your databases straight over HTTP</b></p>
        <p>Read, add, replace and delete documents, and run queries.
          All with ordinary web requests, no extra tooling required. Try it <a href='rest'>here</a>.</p>
      </main>
      <hr/>
      <footer class='right'><sup>BaseX Team, BSD License</sup></footer>
    </body>
  </html>
};
