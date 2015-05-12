package ru.bmstu.rk9.rdo.generator;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;

import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ParameterType;
import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement;
import ru.bmstu.rk9.rdo.rdo.Constant;
import ru.bmstu.rk9.rdo.rdo.Function;
import ru.bmstu.rk9.rdo.rdo.FunctionParameter;
import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic;
import ru.bmstu.rk9.rdo.rdo.FunctionList;
import ru.bmstu.rk9.rdo.rdo.FunctionTable;
import ru.bmstu.rk9.rdo.rdo.Sequence;

public class GlobalContext {
	public class RTP {
		public ResourceType origin;

		public HashMap<String, String> parameters;

		public RTP(ResourceType rtp) {
			origin = rtp;
			parameters = new HashMap<String, String>();
			for (ParameterType p : rtp.getParameters())
				parameters.put(p.getParam().getName(),
						RDOExpressionCompiler.compileType(p));
		}
	}

	public class RSS {
		public ResourceCreateStatement origin;

		public String reference;

		public RSS(ResourceCreateStatement rss) {
			origin = rss;
			ResourceType rtp = rss.getReference();
			restypes.put(rtp.getName(), new RTP(rtp));
			reference = rtp.getName();
		}
	}

	public class SEQ {
		public Sequence origin;

		public String type;
		public int parameters = 0;

		public SEQ(Sequence seq) {
			origin = seq;
			type = RDOExpressionCompiler.compileType(seq.getReturntype());
		}
	}

	public class CON {
		public Constant origin;

		public String type;

		public CON(Constant con) {
			origin = con;
			type = RDOExpressionCompiler.compileType(con.getType());
		}
	}

	public class FUN {
		public Function origin;

		public LinkedList<String> parameters = new LinkedList<String>();

		public String type;

		public FUN(Function fun) {
			origin = fun;

			List<FunctionParameter> parameters = null;

			type = RDOExpressionCompiler.compileType(fun.getReturntype());

			if (fun.getType() instanceof FunctionAlgorithmic)
				parameters = ((FunctionAlgorithmic) fun.getType())
						.getParameters();
			if (fun.getType() instanceof FunctionTable)
				parameters = ((FunctionTable) fun.getType()).getParameters();
			if (fun.getType() instanceof FunctionList)
				parameters = ((FunctionList) fun.getType()).getParameters();

			if (parameters != null)
				for (FunctionParameter p : parameters)
					this.parameters.addLast(RDOExpressionCompiler.compileType(p
							.getType()));
		}
	}

	public RSS newRSS(ResourceCreateStatement rss) {
		return new RSS(rss);
	}

	public SEQ newSEQ(Sequence seq) {
		return new SEQ(seq);
	}

	public CON newCON(Constant con) {
		return new CON(con);
	}

	public FUN newFUN(Function fun) {
		return new FUN(fun);
	}

	public HashMap<String, RTP> restypes = new HashMap<String, RTP>();
	public HashMap<String, RSS> resources = new HashMap<String, RSS>();
	public HashMap<String, SEQ> sequences = new HashMap<String, SEQ>();
	public HashMap<String, CON> constants = new HashMap<String, CON>();
	public HashMap<String, FUN> functions = new HashMap<String, FUN>();
}
