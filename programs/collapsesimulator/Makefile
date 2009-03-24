TARGET		= collapsesimulator

SRCS		= main.cc collapse.cc

OBJS		= $(SRCS:.cc=.o)

# Compile flags
CPPFLAGS+=-DNDEBUG -I../librescue

# Flags for linking
LDFLAGS += -L../librescue -lm -lpthread -lrescue

all: $(TARGET)

$(TARGET):	$(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS)

clean:
	rm -f $(TARGET) $(OBJS) *core *bak *~

depend:
	makedepend $(SRCS)

