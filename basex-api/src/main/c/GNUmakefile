.PHONY: all lib clean clean-depend clean-all

include config.mk

LIBNAME= basexdbc
DBCLIB= lib$(LIBNAME).$(LIBSFX)

VERSION= 0.1

FDEBUG=

CC?= cc
CFLAGS+= -DBUILD="\"$(VERSION)\"" -fPIC
LDFLAGS+= -fPIC
LIBS+=

ifdef FDEBUG
CFLAGS+= -g -ggdb -DDEBUG
CFLAGS+= -Wno-long-long -Wall -W -Wnested-externs -Wformat=2
CFLAGS+= -Wmissing-prototypes -Wstrict-prototypes -Wmissing-declarations
CFLAGS+= -Wwrite-strings -Wshadow -Wpointer-arith -Wsign-compare
CFLAGS+= -Wundef -Wbad-function-cast -Winline -Wcast-align
endif

SRCS= $(shell echo *.c)
OBJS= $(patsubst %.c,%.o,$(SRCS))

all:		lib example

lib:		$(DBCLIB)

example:
		$(CC) -L. -l$(LIBNAME) example.c -o $@

$(DBCLIB):	md5.o basexdbc.o readstring.o
		$(CC) $(LIBS) $(LDFLAGS) -shared -o $@ $+

depend:		$(SRCS)
		$(CC) $(CFLAGS) -MM $(SRCS) >depend

install:	lib
		install -d -m555 /usr/lib/basex
		install -d -m555 /usr/include/basex
		install -m555 $(DBCLIB) /usr/lib/basex/
		install -m444 basexdbc.h /usr/include/basex/

uninstall:
		rm -rf /usr/lib/basex/
		rm -rf /usr/include/basex/

clean:
		rm -f $(DBCLIB)
		rm -f ${OBJS}
		rm -f example

clean-config:
		rm -f config.mk

clean-depend:
		rm -f depend

clean-all:	clean clean-config clean-depend uninstall

include depend
