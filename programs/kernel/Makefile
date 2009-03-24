TARGET		= kernel

SRCS = kernel.cc main.cc perception.cc

OBJS		= $(SRCS:.cc=.o)

# Compile flags
CPPFLAGS+=-DNDEBUG -I../librescue

# Flags for linking
LDFLAGS += -L../librescue -lm -lrescue

all:	$(TARGET)

$(TARGET):	$(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS)

clean:
	rm -f $(TARGET) $(OBJS) *core *bak *~
