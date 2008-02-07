
### Makefile for BaseX
### 
### Version: 0.2
### Date: 2006-May-30 13:14:40 CEST
### Author: Stefan Klinger


### Path to the root directory of all sources:
srcPath = src

### Target directory to store the compiled classes:
binPath = bin

### Target directory to store the compiled documentation:
docPath = doc

#####################################################################

### Note that even the target directories are declared PHONY. Thus,
### dependency analysis is done inside the java compiler.

.PHONY : all clean sourcelist help bin doc sourcelist_slim slim
.INTERMEDIATE : sourcelist

all : bin doc

slim : sourcelist_slim all

clean :
	rm -rf sourcelist $(binPath) $(docPath)

help :
	 #
	 #----- Makefile for BaseX ---------------------------------
	 #
	 # Useful targets are:
	 #
	 #   all -- Equal to 'make bin doc'.
	 #
	 #   slim -- Like 'all', but omits building of the gui.
	 #
	 #   bin -- Compile the classes into the directory
	 #     binPath = $(binPath)
	 #     which is specified in the makefile.
	 #
	 #   doc -- Compile documentation into the directory
	 #     docPath = $(docPath)
	 #     which is specified in the makefile. Note that
	 #     documentation will be created for *all* classes.
	 #
	 #   clean -- Remove created files.
	 #
	 #   sourcelist -- Create a listing of all source files in
	 #     srcPath = $(srcPath)
	 #     which is specified in the makefile. The listing is
	 #     stored in a file 'sourcelist' which will be removed
	 #     after each build.
	 #
	 #   help -- Well, you found out about that one. (=
	 #--

#####################################################################

### Create list of source files. Required since number of arguments
### is limited on some machines.
sourcelist :
	test -r $@ || find $(srcPath) -regex '.*/[^./]*\.java' > sourcelist

### the slim version omits the gui, which consumes considerable
### compile time
sourcelist_slim :
	find $(srcPath) -regex '.*/[^./]*\.java' -not -regex '.*basex/gui.*' > sourcelist

bin : sourcelist
	test -e $(binPath) || mkdir $(binPath)
	javac -sourcepath $(srcPath) -d $(binPath) @sourcelist

doc : sourcelist
	test -e $(docPath) || mkdir $(docPath)
	javadoc -private -sourcepath $(srcPath) -d $(docPath) @sourcelist
