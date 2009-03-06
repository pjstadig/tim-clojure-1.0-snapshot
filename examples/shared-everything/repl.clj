(binding [*err* java.lang.System/err
          *in* (clojure.lang.LineNumberingPushbackReader. (java.io.InputStreamReader. System/in))
          *out* (java.io.PrintWriter. System/out)]
  (clojure.main/repl))
