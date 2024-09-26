package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class SelectPath extends Block {

	private TransactStorage trueOutputTransactStorage = new TransactStorage();
	private TransactStorage falseOutputTransactStorage = new TransactStorage();
	private InputDock inputDock = new InputDock();
	private OutputDock trueOutputDock = (int ID) -> trueOutputTransactStorage.pullTransact();
	private OutputDock falseOutputDock = (int ID) -> falseOutputTransactStorage.pullTransact();
	private Supplier<Boolean> condition;

	public static enum SelectPathOutputs {
		TRUE("true"), FALSE("false");

		private SelectPathOutputs(final String output) {
			this.output = output;
		}

		public String getString() {
			return output;
		}

		private final String output;
	}

	public enum SelectPathMode {
		PROBABILITY, CONDITION
	}

	public SelectPath(int ID, Supplier<Boolean> condition) {
		super(ID);
		this.condition = condition;
	}

	public SelectPath(int ID, double probability) {
		super(ID);
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
		Transact transact = inputDock.pullTransact(ID);
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		ByteBuffer data = ByteBuffer.allocate(TypeSize.BYTE);
		TransactStorage storage;
		if (condition.get()) {
			storage = trueOutputTransactStorage;
			data.put((byte) SelectPathOutputs.TRUE.ordinal());
		} else {
			storage = falseOutputTransactStorage;
			data.put((byte) SelectPathOutputs.FALSE.ordinal());
		}
		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.SELECT_PATH, transact.getNumber(), data);

		if (!storage.pushTransact(transact))
			return BlockStatus.CHECK_AGAIN;

		return BlockStatus.SUCCESS;
	}
}
