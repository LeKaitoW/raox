package ru.bmstu.rk9.rao.lib.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TeleportTransactStorage extends TransactStorage {

	List<Integer> linkedIds;
	int currentIdx = 0;

	public TeleportTransactStorage(Collection<Integer> linkedIds) {
		this.linkedIds = new ArrayList<>(linkedIds);
		Collections.sort(this.linkedIds);
	}

	public Transact pullTransact(int id) {
		if (currentTransact == null)
			return null;

		if (id != linkedIds.get(currentIdx))
			return null;

		currentIdx = (currentIdx + 1) % linkedIds.size();

		Transact transact = currentTransact;
		currentTransact = null;
		return transact;
	}

}
