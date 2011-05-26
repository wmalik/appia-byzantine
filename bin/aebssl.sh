#!/bin/bash

CP=classes:config/jgcs/open_group:.
for j in `find dist -name *.jar`; do
    CP=$CP:$j;
done

echo "Classpath: $CP";



#java -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl -cp $CP net.sf.appia.test.broadcast.ssl.AEB $@
java -cp $CP net.sf.appia.test.broadcast.ssl.AEB $@
