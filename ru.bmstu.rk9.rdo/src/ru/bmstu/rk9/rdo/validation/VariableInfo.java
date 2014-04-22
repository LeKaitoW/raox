package ru.bmstu.rk9.rdo.validation;

import java.util.LinkedList;
import java.util.HashMap;

import ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler;
import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence;
import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic;
import ru.bmstu.rk9.rdo.rdo.FunctionList;
import ru.bmstu.rk9.rdo.rdo.FunctionParameter;
import ru.bmstu.rk9.rdo.rdo.FunctionParameters;
import ru.bmstu.rk9.rdo.rdo.FunctionTable;
import ru.bmstu.rk9.rdo.rdo.HistogramSequence;
import ru.bmstu.rk9.rdo.rdo.RegularSequence;
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration;
import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration;
import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter;
import ru.bmstu.rk9.rdo.rdo.Sequence;
import ru.bmstu.rk9.rdo.rdo.Function;
import ru.bmstu.rk9.rdo.rdo.SequenceType;


public class VariableInfo
{
	public class RTP
	{
		public ResourceType origin;

		public HashMap<String, String> parameters;

		public RTP(ResourceType rtp)
		{
			origin = rtp;
			parameters = new HashMap<String, String>();
			for (ResourceTypeParameter p : rtp.getParameters())
				parameters.put(p.getName(), RDOExpressionCompiler.compileType(p));
		}
	}

	public class RSS
	{
		public ResourceDeclaration origin;

		public String reference;

		public RSS(ResourceDeclaration rss)
		{
			origin = rss;
			ResourceType rtp = rss.getReference();
			restypes.put(rtp.getName(), new RTP(rtp));
			reference = rtp.getName();
		}
	}

	public class SEQ
	{
		public Sequence origin;

		public String type;
		public int parameters;

		public SEQ(Sequence seq)
		{
			origin = seq;
			type = RDOExpressionCompiler.compileType(seq.getReturntype());
			SequenceType type = seq.getType();
			if(type instanceof EnumerativeSequence || type instanceof HistogramSequence)
				parameters = 0;
			else
				switch (((RegularSequence)type).getType())
				{
					case EXPONENTIAL:
						parameters = 1;

					case NORMAL:
						parameters = 2;

					case TRIANGULAR:
						parameters = 3;

					case UNIFORM:
						parameters = 2;
				}
		}
	}

	public class CON
	{
		public ConstantDeclaration origin;

		public String type;

		public CON(ConstantDeclaration con)
		{
			origin = con;
			type = RDOExpressionCompiler.compileType(con.getType());
		}
	}

	public class FUN
	{
		public Function origin;

		public LinkedList<String> parameters = new LinkedList<String>();

		public FUN(Function fun)
		{
			origin = fun;

			FunctionParameters parameters = null;

			if(fun.getType() instanceof FunctionAlgorithmic)
				parameters = ((FunctionAlgorithmic)fun.getType()).getParameters();
			if(fun.getType() instanceof FunctionTable)
				parameters = ((FunctionTable)fun.getType()).getParameters();
			if(fun.getType() instanceof FunctionList)
				parameters = ((FunctionList)fun.getType()).getParameters();

			if(parameters != null)
				for (FunctionParameter p : parameters.getParameters())
					this.parameters.addLast(RDOExpressionCompiler.compileType(p.getType()));
		}
	}

	public RSS newRSS(ResourceDeclaration rss)
	{
		return new RSS(rss);
	}

	public SEQ newSEQ(Sequence seq)
	{
		return new SEQ(seq);
	}

	public CON newCON(ConstantDeclaration con)
	{
		return new CON(con);
	}

	public FUN newFUN(Function fun)
	{
		return new FUN(fun);
	}

	public HashMap<String, RTP> restypes  = new HashMap<String, RTP>();
	public HashMap<String, RSS> resources = new HashMap<String, RSS>();
	public HashMap<String, SEQ> sequences = new HashMap<String, SEQ>();
	public HashMap<String, CON> constants = new HashMap<String, CON>();
	public HashMap<String, FUN> functions = new HashMap<String, FUN>();
}
