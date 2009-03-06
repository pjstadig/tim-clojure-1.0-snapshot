package clojure.lang;

import clojure.asm.Label;
import clojure.asm.commons.GeneratorAdapter;
import clojure.asm.commons.Method;
import clojure.lang.Compiler.C;
import clojure.lang.Compiler.Expr;
import clojure.lang.Compiler.FnExpr;
import clojure.lang.Compiler.IParser;
import clojure.lang.Compiler.MaybePrimitiveExpr;

public class IfExprTC implements Expr {
	public final Expr testExpr;

	public final Expr thenExpr;

	public final Expr elseExpr;

	public final int line;

	public IfExprTC(int line, Expr testExpr, Expr thenExpr, Expr elseExpr) {
		this.testExpr = testExpr;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
		this.line = line;
	}

	public Object eval() throws Exception {
		Object t = testExpr.eval();
		if (t != null
				&& ((Boolean) t).booleanValue() != Boolean.FALSE.booleanValue())
			return thenExpr.eval();
		return elseExpr.eval();
	}

	public void emit(C context, FnExpr fn, GeneratorAdapter gen) {
		Method booleanValueMethod = Method.getMethod("boolean booleanValue()");
		Label nullLabel = gen.newLabel();
		Label falseLabel = gen.newLabel();
		Label endLabel = gen.newLabel();
		Label booleanLabel = gen.newLabel();
		Label notBooleanLabel = gen.newLabel();

		gen.visitLineNumber(line, gen.mark());

		try {
			if (testExpr instanceof MaybePrimitiveExpr
					&& testExpr.hasJavaClass()
					&& testExpr.getJavaClass() == boolean.class) {
				((MaybePrimitiveExpr) testExpr).emitUnboxed(C.EXPRESSION, fn,
						gen);
				gen.ifZCmp(gen.EQ, falseLabel);
			} else {
				testExpr.emit(C.EXPRESSION, fn, gen);
				gen.dup();
				gen.ifNull(nullLabel);
				gen.dup();
				gen.instanceOf(Compiler.BOOLEAN_OBJECT_TYPE);
				gen.ifZCmp(gen.EQ, notBooleanLabel);
				gen.checkCast(Compiler.BOOLEAN_OBJECT_TYPE);
				gen.invokeVirtual(Compiler.BOOLEAN_OBJECT_TYPE,
						booleanValueMethod);
				gen.ifZCmp(gen.EQ, falseLabel);
				gen.goTo(booleanLabel);
				gen.mark(notBooleanLabel);
				gen.pop();
				gen.mark(booleanLabel);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		thenExpr.emit(context, fn, gen);
		gen.goTo(endLabel);
		gen.mark(nullLabel);
		gen.pop();
		gen.mark(falseLabel);
		elseExpr.emit(context, fn, gen);
		gen.mark(endLabel);
	}

	public boolean hasJavaClass() throws Exception {
		return thenExpr.hasJavaClass()
				&& elseExpr.hasJavaClass()
				&& (thenExpr.getJavaClass() == elseExpr.getJavaClass()
						|| thenExpr.getJavaClass() == null || elseExpr
						.getJavaClass() == null);
	}

	public Class getJavaClass() throws Exception {
		Class thenClass = thenExpr.getJavaClass();
		if (thenClass != null)
			return thenClass;
		return elseExpr.getJavaClass();
	}

	static class Parser implements IParser {
		public Expr parse(C context, Object frm) throws Exception {
			ISeq form = (ISeq) frm;
			// (if test then) or (if test then else)
			if (form.count() > 4)
				throw new Exception("Too many arguments to if");
			else if (form.count() < 3)
				throw new Exception("Too few arguments to if");
			return new IfExprTC((Integer) Compiler.LINE.deref(), Compiler
					.analyze(context == C.EVAL ? context : C.EXPRESSION, RT
							.second(form)), Compiler.analyze(context, RT
					.third(form)), Compiler.analyze(context, RT.fourth(form)));
		}
	}
}
