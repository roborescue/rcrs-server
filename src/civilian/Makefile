TARGET		= civilian

SRCS = civilian.cc main.cc

OBJS		= $(SRCS:.cc=.o)

# Compile flags
CPPFLAGS+=-I../librescue

# Flags for linking
LDFLAGS += -L../librescue -lm -lrescue

all:	$(TARGET)

$(TARGET):	$(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS)

clean:
	rm -f $(TARGET) $(OBJS) *core *bak *~