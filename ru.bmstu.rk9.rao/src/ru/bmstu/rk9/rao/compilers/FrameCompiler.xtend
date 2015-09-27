package ru.bmstu.rk9.rao.compilers

import static extension ru.bmstu.rk9.rao.generator.RaoStatementCompiler.*;

import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.generator.RaoNaming

class FrameCompiler
{
	def static compileFrame(Frame frame, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.animation.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		@SuppressWarnings("all")

		public class «frame.name» implements AnimationFrame
		{
			@Override
			public String getName()
			{
				return "«RaoNaming.getFullyQualifiedName(frame)»";
			}

			public static «frame.name» INSTANCE = new «frame.name»();

			@Override
			public void draw(AnimationContext context)
			{
				«frame.frame.compileStatement»
			}

			private int[] backgroundData = new int[]
			{
				«IF frame.backPicture != null»
					«IF frame.backPicture.size != null»
						«frame.backPicture.size.width», «frame.backPicture.size.height»,
					«ELSE»
						800, 600,
					«ENDIF»
					«frame.backPicture.color.r
					», «frame.backPicture.color.g
					», «frame.backPicture.color.b»
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