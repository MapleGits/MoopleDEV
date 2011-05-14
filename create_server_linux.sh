#!/bin/sh

export CLASSPATH=.:dist\*

-Djavax.net.ssl.keyStore=filename.keystore \
-Djavax.net.ssl.keyStorePassword=passwd \
-Djavax.net.ssl.trustStore=filename.keystore \
-Djavax.net.ssl.trustStorePassword=passwd \
java -Xmx10m \
net.server.CreateINI
