package ru.bmstu.rk9.rao.lib.process;

public interface BlockWithOutput extends Block {
	public void setNextBlock(BlockWithInput nextBlock);

}
