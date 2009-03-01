This is an example of using Terracotta Integration Module for the Clojure language.  The TIM for Clojure is currently in an alpha state, so should be used at your own risk.

How to run this example
=======================
(NOTE: The following instructions have only been tested on Linux.  The scripts are bash scripts, so Windows is out.  This is nothing personal, I just don't use Windows.)

1. Install Terracotta and Clojure.  Please refer to the appropriate websites ([http://www.terracotta.org/](http://www.terracotta.org/) and [http://www.clojure.org/](http://www.clojure.org/)) for installation instructions. (NOTE: You will need to make sure that you are using Clojure [r1310](http://code.google.com/p/clojure/source/browse/trunk/src/jvm/clojure/lang/Keyword.java?r=1310) or later, because the Keyword class needs to have hashCode implemented to play nicely with Terracotta.)

2. Define three environment variables: JAVA_HOME, TC_HOME, and CLOJURE_EXT.  JAVA_HOME should be the directory in which you installed the JDK.  TC_HOME should be the directory in which you installed Terracotta.  CLOJURE_EXT should be the directory in which you have any Clojure code you'd like to be added to the classpath.  For simplicity, I have just linked my clojure jar into my CLOJURE_EXT directory.  If you don't want to do that, then you can also define a CLASSPATH envorinment variable that includes your clojure.jar.

3. Build the application.  From this directory type "mvn package".

4. Start the Terracotta server.  From this directory type "mvn tc:start".

5. Start the clojure REPL with the Terracotta instrumentation.  From the this directory, type "./bin/dso-clojure".

Once in the REPL, try the following:

    user=> (require 'stadigtech.terraclojure)
    nil
    user=> @stadigtech.terraclojure/*hash*
    {}
    user=> @stadigtech.terraclojure/*count*
    0
    user=> (dosync (stadigtech.terraclojure/add :a :b))
    1
    user=> @stadigtech.terraclojure/*hash*
    {:a :b}
    user=> @stadigtech.terraclojure/*count*
    1

Without closing that REPL you can start one in another terminal, then try:

    user=> (require 'stadigtech.terraclojure)
    nil
    user=> @stadigtech.terraclojure/*hash*
    {:a :b}
    user=> (dosync (stadigtech.terraclojure/add :a :b))
    2
    user=> @stadigtech.terraclojure/*hash*
    {:a :b}
    user=> @stadigtech.terraclojure/*count*
    2

Notice that it did not add any new data to the hash, because the Keywords are distributed.  Back in the original now:

    user=> @stadigtech.terraclojure/*count*
    2

Congratulations!  You now have a networked, virtual, heap, with distributed transactions using a modern, concurrent Lisp on the JVM!  ;)

For more details about this setup, check out my blog post ["Clojure + Terracotta = Yeah, Baby!"](http://paul.stadig.name/2009/02/clojure-terracotta-yeah-baby.html).
