package ru.bmstu.rk9.rdo.ui.runtime;

import javax.swing.JFrame;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.bmstu.rk9.rdo.ui.graph.TreeGrapher;

public class RDOGraphBuilderHandler extends AbstractHandler {
		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		System.out.println("qqqqq");
		
		TreeGrapher frame = new TreeGrapher();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.setVisible(true);
		
		return null;
    }

}
