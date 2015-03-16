package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;

public class GraphControl {
	
	public static ArrayList<String> openedGraphList = new ArrayList<String>(); 
	
	private static Integer lastAddedVertexIndex;
	
	public static Integer getLastAddedVertexIndex() {
		return lastAddedVertexIndex;
	}

	public static void setLastAddedVertexIndex(Integer lastAddedVertex) {
		GraphControl.lastAddedVertexIndex = lastAddedVertex;
	}
	
	private static Integer dptNumOfLastAddedVertex;
	
	public static Integer getDptNumOfLastAddedVertex() {
		return dptNumOfLastAddedVertex;
	}

	public static void setDptNumOfLastAddedVertex(Integer dptNumOflastAddedVertex) {
		GraphControl.dptNumOfLastAddedVertex = dptNumOflastAddedVertex;
	}
}
