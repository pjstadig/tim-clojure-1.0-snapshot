This is an example of using Terracotta Integration Module for the Clojure language.  The TIM for Clojure is currently in an alpha state, so should be used at your own risk.

This is the "shared everything" example of using Clojure with Terracotta.  The goal is to have multiple VMs working together in the same shared, global context through Terracotta.  This means that everything in the Clojure environment should be shared by default.  I have worked out the class replacement for a TIM, and I have created a few replacement classes so that Clojure will play nicely with Terracotta.  There are still some issues:

1. A roadblock at the moment is the root bindings for *in*, *out* and *err*.  They cannot be shared, but there is no way to mark the root binding (just for those Vars) as transient.  The workaround for the moment is to just leave the root bindings nil and bind the values manually (hence the 'hello-repl.clj' file).

2. I am not able to connect more than one JVM to Terracotta, nor can I disconnect and reconnect the same JVM.  This is because Clojure dynamically creates classes for each fn that it compiles, but I haven't worked out a way to store those files in Terracotta.  Reconnecting will pull out the compiled functions, but Terracotta can't find the corresponding classes.


How to run this example
=======================
(NOTE: The following instructions have only been tested on Linux.  The scripts are bash scripts, so Windows is out.  This is nothing personal, I just don't use Windows.)

1. Install Terracotta and Clojure.  Please refer to the appropriate websites ([http://www.terracotta.org/](http://www.terracotta.org/) and [http://www.clojure.org/](http://www.clojure.org/)) for installation instructions. (NOTE: You will need to make sure that you are using Clojure [r1310](http://code.google.com/p/clojure/source/browse/trunk/src/jvm/clojure/lang/Keyword.java?r=1310) or later, because the Keyword class needs to have hashCode implemented to play nicely with Terracotta.)

2. Define three environment variables: JAVA_HOME, TC_HOME, and CLOJURE_EXT.  JAVA_HOME should be the directory in which you installed the JDK.  TC_HOME should be the directory in which you installed Terracotta.  CLOJURE_EXT should be the directory in which you have any Clojure code you'd like to be added to the classpath.  For simplicity, I have just linked my clojure jar into my CLOJURE_EXT directory.  If you don't want to do that, then you can also define a CLASSPATH envorinment variable that includes your clojure.jar.

3. Start the Terracotta server.  From this directory type "mvn tc:start".

4. Start the clojure REPL with the Terracotta instrumentation.  From the this directory, type "./bin/dso-clojure hello-repl.clj".

Once in the REPL, you can do anything you would normally do in a Clojure REPL, and you should see objects being modified in the Terracotta cache (type "mvn tc:admin" from this directory to start the Terracotta admin application).

There are certainly some cases that will fail.  I don't expect Agents to work.  Nor Atoms.  As mentioned above, we currently don't have true sharing, because the compiled classes are not shared through Terracotta.  More to come.

For more details about the problems and possible solutions, check out my blog post ["Clojure + Terracotta: the Next Steps"](http://paul.stadig.name/2009/03/clojure-terracotta-next-steps.html).
