version := 1.50
dirname := yabapi-$(version)

all:	class
class:
	$(MAKE) -C sample/src/sample
.PHONY: clean doc jar tar
clean:
	$(MAKE) -C yab/src/yab clean
	$(MAKE) -C sample/src/sample clean
	rm -f *~
doc:
	javadoc -locale en_US\
		-d doc/\
		-link http://java.sun.com/products/jdk/1.4/docs/api\
	        -windowtitle YabAPI\
		-doctitle YabAPI\
		-sourcepath yab/src/\
		-group "Communication between an RCR agent and the kernel"\
		       "yab.io*"\
		-group "Skeletons of RCR agents"\
		       "yab.agent*"\
		yab.io\
		yab.io.object\
		yab.agent\
		yab.agent.object
jar:
	$(MAKE) -C yab/src/yab jar
	$(MAKE) -C sample/src/sample jar
tar:	doc jar clean
	cd ..;\
	mv yabapi $(dirname);\
	tar zcvf $(dirname).tar.gz $(dirname);\
	mv $(dirname) yabapi
