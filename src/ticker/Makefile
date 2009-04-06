TARGET		= ticker

SRCS		= main.cc

OBJS		= $(SRCS:.cc=.o)

# Compile flags
CPPFLAGS+=

# Flags for linking
LDFLAGS += -lX11

all: $(TARGET)

$(TARGET):	$(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS)

clean:
	rm -f $(TARGET) $(OBJS) *core *bak *~

depend:
	makedepend $(SRCS)

