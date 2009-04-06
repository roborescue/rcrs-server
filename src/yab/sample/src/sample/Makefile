src := $(wildcard *.java)
yabapidir := ../../../yab
opt := -g:none\
       -deprecation\
       -classpath $(yabapidir)/classes/\
       -d ../../classes
.PHONY: class clean jar
class:
	$(MAKE) -C $(yabapidir)/src/yab/
	javac $(opt) $(src)
clean:
	rm -rf ../../classes/* *.class *~
jar:	clean class
	$(MAKE) -C $(yabapidir)/src/yab/ jar
	cd ../../classes;\
	jar cvmf ../src/sample/manifest ../../sample.jar sample
