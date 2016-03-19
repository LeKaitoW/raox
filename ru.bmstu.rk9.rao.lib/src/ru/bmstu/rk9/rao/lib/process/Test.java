package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Test implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock trueOutputDock = new OutputDock();
	private OutputDock falseOutputDock = new OutputDock();
	private Supplier<Boolean> test;

	public Test(Supplier<Boolean> test) {
		this.test = test;
	}

	public Test(double probability) {
		Supplier<Boolean> test = new Supplier<Boolean>() {

			private final MersenneTwister generator = new MersenneTwister();

			@Override
			public Boolean get() {
				return generator.nextDouble() > probability ? false : true;
			}
		};
		this.test = test;
	}

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getTrueOutputDock() {
		return trueOutputDock;
	}

	public OutputDock getFalseOutputDock() {
		return falseOutputDock;
	}

	@Override
	public BlockStatus check() {
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		OutputDock outputDock;
		if (test.get()) {
			outputDock = trueOutputDock;
			System.out.println(Simulator.getTime() + " : transact goes true " + transact.getNumber());
		} else {
			outputDock = falseOutputDock;
			System.out.println(Simulator.getTime() + " : transact goes false " + transact.getNumber());
		}

		if (outputDock.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}

		outputDock.pushTransact(transact);

		return BlockStatus.SUCCESS;
	}

}
