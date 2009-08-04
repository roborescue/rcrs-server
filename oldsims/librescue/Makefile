PROGRAMS=test
LIBRARY=librescue.so.1.0.1
SYMLINKS=librescue.so.1\
	librescue.so
OBJECTS=objects.o\
	error.o\
	input.o\
	output.o\
	connection.o\
	udp.o\
	tcp.o\
	config.o\
	objectpool.o\
	handy.o\
	connection_manager.o\
	command.o\
	agent_commands.o\
	simulator_commands.o\
	viewer_commands.o\
	gis_commands.o\
	simulator.o\
	agent.o\
	container.o\
	args.o\
	property_filter.o

TESTOBJECTS=test.o

# Flags for linking the library
LFLAGS=-lpthread -fPIC -shared -Wl,-soname,librescue.so.1

all:	$(LIBRARY) $(PROGRAMS)

$(LIBRARY): CPPFLAGS+=-fPIC
$(LIBRARY):	$(OBJECTS)
	$(CXX) $(LDFLAGS) $(LFLAGS) -o $(LIBRARY) $(OBJECTS)
	rm -f $(SYMLINKS)
	ln -s librescue.so.1.0.1 librescue.so.1
	ln -s librescue.so.1 librescue.so

test:	$(LIBRARY) $(TESTOBJECTS)
	$(CXX) $(LDFLAGS) -L. -lrescue -o test test.o

clean:
	rm -f $(LIBRARY) $(SYMLINKS) $(PROGRAMS) *.o