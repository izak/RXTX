
CFLAGS=-Wall -g -O2 -D_POSIX_SOURCE -D_BSD_SOURCE -D__need_timespec -O2 -D_POSIX_SOURCE -D_BSD_SOURCE -D__need_timespec

javafiles=$( wildcard *.java )
classfiles=$( patsubst %.java,javax.comm/%.class,$( wildcard *.java ) )
objects=$( patsubst %.java,javax.comm/%.o,$( wildcard *.java ) )

all:  Simple

Simple: librxtx.so librxtx.a
	#gcj *.o librxtx.a -static  -lgcj -lpthread -ldl -lz -o Simple --main=Simple
	ln -s Simple.Java Simple.java
	gcj --classpath RXTX.jar  -d . -C Simple.java
	rm Simple.java
	gcj -g -classpath RXTX.jar -d . -c Simple.class
	gcj -g *.o javax.comm/*.o -static  -lgcj -lpthread -ldl -lz -o Simple --main=Simple


librxtx.a:  javax.comm/SerialImp.o javax.comm/fuserImp.o objects
	ar rs librxtx.a  javax.comm/*.o *.o

librxtx.so:  javax.comm/SerialImp.o javax.comm/fuserImp.o objects
	g++ -g -shared javax.comm/*.o *.o -lpthread  -Wl,-soname -Wl,librxtx-0.0-1.so -o librxtx-0.0-1.so
	rm -f librxtx.so && ln -s librxtx-0.0-1.so librxtx.so

objects: classfiles
	(cd javax.comm && gcj -g -classpath ../.. -d javax.comm -c *.class )

javax.comm/SerialImp.o:  classfiles
	g++ $(CFLAGS) -I . -c SerialImp.c

javax.comm/fuserImp.o:  classfiles
	g++ $(CFLAGS) -I . -c fuserImp.c

classfiles:
	rm -f Simple.java
	gcj -d . -C *.java
	for i in javax.comm/*.class;do gcjh -d . `echo $$i|sed s/.class//g`;done
	jar -cf RXTX.jar javax.comm/*.class

clean:
	rm -f javax.comm/*
	rm -f *.a *.o *.so *.class Simple *.jar
	rmdir javax.comm
	rmdir gnu
