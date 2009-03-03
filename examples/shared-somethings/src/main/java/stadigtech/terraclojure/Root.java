package stadigtech.terraclojure;

import clojure.lang.Ref;
import clojure.lang.PersistentHashMap;

public class Root {
    public static Ref hash;
    public static Ref count;

    static {
        try {
            hash = new Ref(PersistentHashMap.EMPTY);
            count = new Ref(0);
        } catch (Exception e) {
            System.err.println("Could not create root");
            System.exit(0);
        }
    }
}
