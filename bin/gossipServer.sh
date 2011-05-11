#!/bin/bash

#java net.sf.appia.xml.utils.ExecuteXML ./config/gossipServer.xml

CP=classes:config/jgcs/open_group:.
for j in `find dist -name *.jar`; do
    CP=$CP:$j;
done

#hehe

java -cp .:classes:lib/log4j-1.2.14.jar net.sf.appia.gossip.GossipServer "$@"
