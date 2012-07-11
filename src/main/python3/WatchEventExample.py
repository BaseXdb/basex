# -*- coding: utf-8 -*-
# Documentation: http://docs.basex.org/wiki/Events
#
# (C) BaseX Team 2005-12, BSD License

import BaseXClient
import time
from datetime import datetime
from multiprocessing import Process

# event publisher process (child)
def child():
    # create session (listener)
    session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

    # trigger event (first)
    session.execute("""xquery db:event("MY_EVENT", "fired MY_EVENT from child, at %s")""" % str(datetime.now()))

    # Zzz...
    time.sleep(3)

    # trigger event (second)
    session.execute("""xquery db:event("MY_EVENT", "fired MY_EVENT from child, at %s")""" % str(datetime.now()))

    # close session
    session.close()

# listener function
def dump_my_event(data):
    print(data)

# listener process main
def main():
    # create session (listener)
    session = BaseXClient.Session('localhost', 1984, 'admin', 'admin')

    # create event
    session.execute("create event MY_EVENT");

    try:
        # register event watcher
        session.watch("MY_EVENT", dump_my_event)

        # fork child
        chp = Process(target=child, args=())
        chp.start()
        chp.join()

        # unregister event watcher
        session.unwatch("MY_EVENT")

    finally:
        # drop event
        session.execute("drop event MY_EVENT");

    # close session
    session.close()

# test it!
main()
