This is a Terracotta Integration Module (TIM) for the Clojure language.  The TIM for Clojure is currently in an beta state (meaning fully functional, but may still have bugs). As such, it may not be suitable for production applications.  I welcome any feedback and experience you have running this example, or using the TIM.  You can contact me at [mailto:paul@stadig.name](paul@stadig.name).

The goal is to have multiple VMs working together in the same shared, global context through Terracotta.  This means that everything in the Clojure environment should be shared by default.  In order to get this to function properly I am using class replacement to insert Terracotta compatible versions of some Clojure classes.  This includes a change to the Clojure compiler, so you must use the clojure-slim.jar (see notes with the example).

Does it work?
=============
I have done limited testing with all of the important features of Clojure including agents, atoms, refs, transactions, def'ing vars and functions, etc.  I have also run the clojure.contrib.test-clojure suite successfully.  None of these tests are exhaustive, so there may still be some unexplored corners.

According to my testing, everything is functional in a shared context, but there is still one outstanding issue and a workaround. At the moment is the root bindings for \*in\*, \*out\* and \*err\* cannot be shared, but there is no way to mark the root binding (just for those Vars) as transient.  The workaround for the moment is to just leave the root bindings nil and bind the values manually in a custom REPL (hence the "tc-repl.clj" file in the example application).

What is left do to?
===================
There is still an opportunity to tune the Terracotta configuration.  The Terracotta configuration includes locking and instrumentation configuration options. This is an initial proof-of-concept, so the locking and instrumentation configurations are broad, sweeping, and indiscriminate.  Tuning this configuration may improve the performance slightly.

The TIM uses class replacement to insert Terracotta compatible versions of some Clojure classes at runtime. The runtime class insertion would not be necessary if those changes were rolled back into the Clojure code base.  Rich Hickey and others in the Clojure community are evaluating the changes and considering whether it would make sense to roll the changes back into Clojure.

Additionally, some of the changes that were made to the Clojure classes were done so as a result of bugs in Terracotta, or to work around the use of some classes that Terracotta considers non-portable.  I have filed some bug reports with the Terracotta team, and in the latest version of Terracotta some of the non-portable classes will be made portable.  These changes and updates should be transparent to one who is using the TIM.  The point is that the TIM is functional, but still a work-in-progress.

How do I use it?
================
For more information about how to use the module, see the example application in the "example" directory.

Boring copyright stuff
======================
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
