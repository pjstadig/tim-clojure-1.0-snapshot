package stadigtech.terracotta.modules.clojure_1_0_SNAPSHOT;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.terracotta.modules.configuration.TerracottaConfiguratorModule;

public class ClojureTerracottaConfigurator extends TerracottaConfiguratorModule {
	@Override
	protected void addInstrumentation(BundleContext context) {
		super.addInstrumentation(context);
		Bundle bundle = getExportedBundle(context,
				"stadigtech.terracotta.modules.tim-clojure-1.0-SNAPSHOT");
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
		addClassReplacement(bundle, "clojure.lang.DynamicClassLoader", "clojure.lang.DynamicClassLoaderTC");
		//addClassReplacement(bundle, "clojure.lang.ProxyHandler", "clojure.lang.ProxyHandlerTC");
		addClassReplacement(bundle, "clojure.lang.Compiler$IfExpr", "clojure.lang.IfExprTC");
		addClassReplacement(bundle, "clojure.lang.Compiler$IfExpr$Parser", "clojure.lang.IfExprTC$Parser");
		addClassReplacement(bundle, "clojure.lang.PersistentArrayMap", "clojure.lang.PersistentArrayMapTC");
		addClassReplacement(bundle, "clojure.lang.PersistentHashMap", "clojure.lang.PersistentHashMapTC");
		addClassReplacement(bundle, "clojure.lang.PersistentHashMap$LeafNode", "clojure.lang.PersistentHashMapTC$LeafNode");
		addClassReplacement(bundle, "clojure.lang.PersistentHashMap$FullNode", "clojure.lang.PersistentHashMapTC$FullNode");
		addClassReplacement(bundle, "clojure.lang.PersistentHashMap$BitmapIndexedNode", "clojure.lang.PersistentHashMapTC$BitmapIndexedNode");
		addClassReplacement(bundle, "clojure.lang.PersistentTreeMap", "clojure.lang.PersistentTreeMapTC");
		addClassReplacement(bundle, "clojure.lang.Util", "clojure.lang.UtilTC");
		addClassReplacement(bundle, "clojure.lang.LazilyPersistentVector", "clojure.lang.LazilyPersistentVectorTC");
		addClassReplacement(bundle, "clojure.lang.PersistentStructMap", "clojure.lang.PersistentStructMapTC");
		addClassReplacement(bundle, "clojure.lang.PersistentStructMap$1", "clojure.lang.PersistentStructMapTC$1");
		addClassReplacement(bundle, "clojure.lang.PersistentVector", "clojure.lang.PersistentVectorTC");
		addClassReplacement(bundle, "clojure.lang.Agent", "clojure.lang.AgentTC");
		addClassReplacement(bundle, "clojure.lang.Agent$Action", "clojure.lang.AgentTC$Action");
		addClassReplacement(bundle, "clojure.lang.Atom", "clojure.lang.AtomTC");
	}
}
