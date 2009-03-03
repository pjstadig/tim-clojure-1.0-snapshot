package stadigtech.clojure.terracotta.clojure_1_0_SNAPSHOT;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.terracotta.modules.configuration.TerracottaConfiguratorModule;

public class ClojureTerracottaConfigurator extends TerracottaConfiguratorModule {
	@Override
	protected void addInstrumentation(BundleContext context) {
		super.addInstrumentation(context);
		Bundle bundle = getExportedBundle(context,
				"stadigtech.clojure.terracotta.tim-clojure-1.0-SNAPSHOT");
		addClassReplacement(bundle, "clojure.lang.Namespace",
				"clojure.lang.NamespaceTC");
		addClassReplacement(bundle, "clojure.lang.Var", "clojure.lang.VarTC");
		addClassReplacement(bundle, "clojure.lang.Var$1",
				"clojure.lang.VarTC$1");
		addClassReplacement(bundle, "clojure.lang.Var$2",
				"clojure.lang.VarTC$2");
		addClassReplacement(bundle, "clojure.lang.RT", "clojure.lang.RTTC");
		addClassReplacement(bundle, "clojure.lang.RT$1", "clojure.lang.RTTC$1");
		addClassReplacement(bundle, "clojure.lang.RT$2", "clojure.lang.RTTC$2");
		addClassReplacement(bundle, "clojure.lang.RT$3", "clojure.lang.RTTC$3");
		addClassReplacement(bundle, "clojure.lang.RT$4", "clojure.lang.RTTC$4");
		addClassReplacement(bundle, "clojure.lang.RT$5", "clojure.lang.RTTC$5");
		addClassReplacement(bundle, "clojure.lang.RT$6", "clojure.lang.RTTC$6");
		addClassReplacement(bundle, "clojure.lang.RT$7", "clojure.lang.RTTC$7");
		addClassReplacement(bundle, "clojure.lang.Symbol", "clojure.lang.SymbolTC");
	}
}
