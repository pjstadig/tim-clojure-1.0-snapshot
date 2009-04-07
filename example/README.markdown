How to run this example
=======================
(NOTE: The following instructions have only been tested on Linux.  The scripts are bash scripts, so Windows is out.  This is nothing personal, I just don't use Windows.)

1. Install Maven, Terracotta, and Clojure.  Please refer to the appropriate websites ([http://maven.apache.org/](http://maven.apache.org/), [http://www.terracotta.org/](http://www.terracotta.org/), and [http://www.clojure.org/](http://www.clojure.org/)) for installation instructions. (NOTE: You will need to make sure that you are using Clojure [r1310](http://code.google.com/p/clojure/source/browse/trunk/src/jvm/clojure/lang/Keyword.java?r=1310) or later, because the Keyword class needs to have hashCode implemented to play nicely with Terracotta.  This TIM has been tested with r1335 of Clojure, YMMV.)

2. Define three environment variables: JAVA_HOME, TC_HOME, and CLOJURE_EXT.  JAVA_HOME should be the directory in which you installed the JDK.  TC_HOME should be the directory in which you installed Terracotta.  CLOJURE_EXT should be the directory in which you have any Clojure code you'd like to be added to the classpath.  For simplicity, I have just linked my clojure-slim.jar into my CLOJURE_EXT directory.  If you don't want to do that, then you can also define a CLASSPATH envorinment variable that includes your clojure-slim.jar.
   (**\*\*IMPORTANT NOTE\*\* You must use clojure-slim.jar.  This TIM makes compiler changes, and the full clojure.jar includes precompiled versions of core.clj, main.clj, etc.  You need to let the TIM compile those files for you.**)

3. You need to install the Clojure Terracotta Integration Module.  To do this you need to go to the parent of this example directory (should be something like tim-clojure-1.0-snapshot), and run the `mvn install` command.  This will install the TIM into your local maven repository, and make it available to the Terracotta server.

4. Start the Terracotta server.  Back in this directory, run the `mvn tc:start` command.  You may also want to start the Terracotta administration application, so you can see what objects are being added to the cache.  Type `mvn tc:admin` from this directory.

5. Start the clojure REPL with the Terracotta instrumentation.  From the this directory, type `./bin/dso-clojure @tc-repl.clj`.  If you'd like to run it with a debugger, then type `./bin/dso-clojure-debug @tc-repl.clj`.  You can then connect a Java debugger (JSwat has worked for me) to localhost:8888.

Once in the REPL, you can do anything you would normally do in a Clojure REPL.  Here is a transcript of a session:

JVM #1
======
    paul@pstadig-laptop:~/tim-clojure/tim-clojure-1.0-SNAPSHOT/example$ ./bin/dso-clojure @tc-repl.clj
    Starting BootJarTool...
    2009-03-06 16:47:17,066 INFO - Terracotta 2.7.3, as of 20090129-100125 (Revision 11424 by cruise@su10mo5 from 2.7)
    2009-03-06 16:47:17,653 INFO - Configuration loaded from the file at '/home/paul/tim-clojure/tim-clojure-1.0-SNAPSHOT/example/tc-config.xml'.

    Starting Terracotta client...
    2009-03-06 16:47:21,102 INFO - Terracotta 2.7.3, as of 20090129-100125 (Revision 11424 by cruise@su10mo5 from 2.7)
    2009-03-06 16:47:21,703 INFO - Configuration loaded from the file at '/home/paul/tim-clojure/tim-clojure-1.0-SNAPSHOT/example/tc-config.xml'.
    2009-03-06 16:47:21,904 INFO - Log file: '/home/paul/terracotta/client-logs/org.terracotta.modules.sample/20090306164721884/terracotta-client.log'.
    2009-03-06 16:47:24,030 INFO - Statistics buffer: '/home/paul/tim-clojure/tim-clojure-1.0-SNAPSHOT/example/statistics-127.0.1.1'.
    2009-03-06 16:47:24,591 INFO - Connection successfully established to server at 127.0.1.1:9510
    user=> (defn foo [] 42)
    #'user/foo
    user=>

JVM #2
======
    paul@pstadig-laptop:~/tim-clojure/tim-clojure-1.0-SNAPSHOT/example$ ./bin/dso-clojure @tc-repl.clj
    Starting BootJarTool...
    2009-03-06 16:48:36,894 INFO - Terracotta 2.7.3, as of 20090129-100125 (Revision 11424 by cruise@su10mo5 from 2.7)
    2009-03-06 16:48:37,466 INFO - Configuration loaded from the file at '/home/paul/tim-clojure/tim-clojure-1.0-SNAPSHOT/example/tc-config.xml'.

    Starting Terracotta client...
    2009-03-06 16:48:40,909 INFO - Terracotta 2.7.3, as of 20090129-100125 (Revision 11424 by cruise@su10mo5 from 2.7)
    2009-03-06 16:48:41,415 INFO - Configuration loaded from the file at '/home/paul/tim-clojure/tim-clojure-1.0-SNAPSHOT/example/tc-config.xml'.
    2009-03-06 16:48:41,609 INFO - Log file: '/home/paul/terracotta/client-logs/org.terracotta.modules.sample/20090306164841585/terracotta-client.log'.
    2009-03-06 16:48:45,386 WARN -
    **************************************************************************************
    The statistics buffer couldn't be opened at
    '/home/paul/tim-clojure/tim-clojure-1.0-SNAPSHOT/example/statistics-127.0.1.1'.
    The CVT system will not be active for this node.

    A common reason for this is that you're launching several Terracotta L1
    clients on the same machine. The default directory for the statistics buffer
    uses the IP address of the machine that it runs on as the identifier.
    When several clients are being executed on the same machine, a typical solution
    to properly separate these directories is by using a JVM property at startup
    that is unique for each client.

    For example:
      dso-java.sh -Dtc.node-name=node1 your.main.Class

    You can then adapt the tc-config.xml file so that this JVM property is picked
    up when the statistics directory is configured by using %(tc.node-name) in the
    statistics path.
    **************************************************************************************

    2009-03-06 16:48:45,924 INFO - Connection successfully established to server at 127.0.1.1:9510
    user=> (foo)
    42
    user=>

This is beta state, so there are some cases that may fail.

To keep track of my progress integrating Clojure and Terracotta, check out my blog [http://paul.stadig.name/](http://paul.stadig.name/).

Clojure code is copyright Rich Hickey.

    Copyright (c) Rich Hickey. All rights reserved.
    The use and distribution terms for this software are covered by the
    Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
    which can be found in the file epl-v10.html at the root of this distribution.
    By using this software in any fashion, you are agreeing to be bound by
    the terms of this license.
    You must not remove this notice, or any other, from this software.

    Modifications are copyright Stadig Technologies, LLC, and released under the same license.

Terracotta Integration Module code is copyright Stadig Technologies, LLC.

    Copyright (c) Stadig Technologies, LLC. All rights reserved.
    The use and distribution terms for this software are covered by the
    Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
    which can be found in the file epl-v10.html at the root of this distribution.
    By using this software in any fashion, you are agreeing to be bound by
    the terms of this license.
    You must not remove this notice, or any other, from this software.
