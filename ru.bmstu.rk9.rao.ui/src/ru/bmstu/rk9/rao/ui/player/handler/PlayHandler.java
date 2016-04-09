package ru.bmstu.rk9.rao.ui.player.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.bmstu.rk9.rao.ui.player.Player;
import ru.bmstu.rk9.rao.ui.player.Player.PlayingDirection;


public class PlayHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Player.play();
		player.runPlayer(1, 1000, PlayingDirection.FORWARD);
		return null;
	}

	private final Player player = new Player();

}
