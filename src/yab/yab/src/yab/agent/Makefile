src := $(wildcard *.java  */*.java)
opt := -classpath ../../
all:
	javac $(opt) $(src)
.PHONY: clean
clean:
	rm -f *.class  */*.class
	rm -f *~       */*~
