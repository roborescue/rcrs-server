TARGET		= blockadessimulator

SRCS		= blockade.cc main.cc

OBJS		= $(SRCS:.cc=.o)

# Compile flags
CPPFLAGS+=-DNDEBUG -I../librescue

# Flags for linking
LDFLAGS += -L../librescue -lm -lpthread -lrescue

all: $(TARGET)

$(TARGET):	$(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS)

clean:
	rm -f $(TARGET) *.o *core *bak *~

depend:
	makedepend $(SRCS)

