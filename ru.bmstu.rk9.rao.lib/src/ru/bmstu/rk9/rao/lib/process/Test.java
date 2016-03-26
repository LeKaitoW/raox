package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Test implements Block {

	private TransactStorage trueOutputTransactStorage = new TransactStorage();
	private TransactStorage falseOutputTransactStorage = new TransactStorage();
	private InputDock inputDock = new InputDock();
	private OutputDock trueOutputDock = () -> trueOutputTransactStorage.pullTransact();
	private OutputDock falseOutputDock = () -> falseOutputTransactStorage.pullTransact();
	private Supplier<Boolean> condition;

	public Test(Supplier<Boolean> condition) {
		this.condition = condition;
	}

	public Test(double probability) {
		Supplier<Boolean> condition = new Supplier<Boolean>() {

			private final MersenneTwister generator = new MersenneTwister();

			@Override
			public Boolean get() {
				return generator.nextDouble() > probability ? false : true;
			}
		};
		this.condition = condition;
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

		TransactStorage storage;
		if (condition.get()) {
			storage = trueOutputTransactStorage;
			System.out.println(Simulator.getTime() + " : transact goes true " + transact.getNumber());
		} else {
			storage = falseOutputTransactStorage;
			System.out.println(Simulator.getTime() + " : transact goes false " + transact.getNumber());
		}

		if (!storage.pushTransact(transact))
			return BlockStatus.CHECK_AGAIN;

		return BlockStatus.SUCCESS;
	}

}
