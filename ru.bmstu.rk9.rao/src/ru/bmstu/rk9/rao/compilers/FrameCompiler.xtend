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

		import ru.bmstu.rk9.rao.lib.animation.RaoColor.*;
		«Util.putImports»

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

			private BackgroundData backgroundData = new BackgroundData(
				«IF frame.backPicture != null»
					«IF frame.backPicture.size != null»
						«frame.backPicture.size.width», «frame.backPicture.size.height»,
					«ELSE»
						800, 600,
					«ENDIF»
					«frame.backPicture.color.compileFrameColor»
				«ELSE»
				800, 600, RaoColor.COLOR_WHITE
				«ENDIF»
			);

			@Override
			public BackgroundData getBackgroundData()
			{
				return backgroundData;
			}
		}
		'''
	}
}