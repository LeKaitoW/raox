package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*;

import ru.bmstu.rk9.rdo.rdo.Frame
import ru.bmstu.rk9.rdo.generator.RDONaming

class RDOFrameCompiler
{
		def public static compileFrame(Frame frm, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «frm.name» implements AnimationFrame
		{
			@Override
			public String getName()
			{
				return "«RDONaming.getFullyQualifiedName(frm)»";
			}

			public static «frm.name» INSTANCE = new «frm.name»();

			@Override
			public void draw(AnimationContext context)
			{
				«frm.frame.compileStatement»
			}

			private int[] backgroundData = new int[]
			{
				«IF frm.backpicture != null»
					«IF frm.backpicture.size != null»
						«frm.backpicture.size.width», «frm.backpicture.size.height»,
					«ELSE»
						««« TODO handle background picture
						800, 600
					«ENDIF»
					«frm.backpicture.colour.r
					», «frm.backpicture.colour.g
					», «frm.backpicture.colour.b»
				«ELSE»
				800, 600,
				255, 255, 255
				«ENDIF»
			};

			@Override
			public int[] getBackgroundData()
			{
				return backgroundData;
			}
		}
		'''
	}
}