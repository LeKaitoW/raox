package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Release implements Block {

	public Release(Resource resource) {
		this.resource = resource;
	}

	private Resource resource;
	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (transactStorage.hasTransact())
			return BlockStatus.CHECK_AGAIN;
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;
		if (resource.isAccessible()) {
			throw new ProcessException("Attempting to release unlocked resource");
		}

		ByteBuffer data = ByteBuffer.allocate(TypeSize.INT * 2);
		int resourceTypeNumber = CurrentSimulator.getStaticModelData().getResourceTypeNumber(resource.getTypeName());
		data.putInt(resourceTypeNumber).putInt(resource.getNumber());
		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.RELEASE, transact.getNumber(), data);

		transactStorage.pushTransact(transact);
		resource.put();
		return BlockStatus.SUCCESS;
	}
}
