src := $(wildcard *.java  */*.java */*/*.java)
opt := -g:none\
       -deprecation\
       -d ../../classes
.PHONY: class clean jar
class:
	javac $(opt) $(src)
clean:
	rm -rf ../../classes/*\
	       *.class  */*.class  */*/*.class\
	       *~       */*~       */*/*~
jar:	clean class
	cd ../../classes;  jar cmvf ../src/yab/manifest ../../yab.jar yab
