package ru.bmstu.rk9.raox.plugin.game5;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConfigurationParser {
	public static JSONObject parseObject(IFile configIFile) {
		JSONObject object = null;
		try {
			final JSONParser parser = new JSONParser();
			object = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(ResourcesPlugin.getWorkspace()
					.getRoot().getLocation().append(configIFile.getFullPath()).toString()), StandardCharsets.UTF_8));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
					"Failed to parse configuration template:\n" + e.getMessage());
			throw new Game5Exception(e);
		}
		return object;
	}

	public static String getResourcesCode(JSONObject object) {
		final JSONArray places = (JSONArray) object.get("places");
		String code = "";

		code += "resource фишка1 = Фишки.create(1, " + places.get(0) + ")\n";
		code += "resource фишка2 = Фишки.create(2, " + places.get(1) + ")\n";
		code += "resource фишка3 = Фишки.create(3, " + places.get(2) + ")\n";
		code += "resource фишка4 = Фишки.create(4, " + places.get(3) + ")\n";
		code += "resource фишка5 = Фишки.create(5, " + places.get(4) + ")\n";
		code += "resource дырка = Дырка.create(" + places.get(5) + ")\n";

		return code;
	}

	public static String getSearchCode(JSONObject object) {
		String costRight = object.get("costRight").toString();
		String computeRight = object.get("computeRight").toString().toUpperCase();

		String costLeft = object.get("costLeft").toString();
		String computeLeft = object.get("computeLeft").toString().toUpperCase();

		String costUp = object.get("costUp").toString();
		String computeUp = object.get("computeUp").toString().toUpperCase();

		String costDown = object.get("costDown").toString();
		String computeDown = object.get("computeDown").toString().toUpperCase();

		String code = "";

		code += "search Расстановка_фишек {\n";
		code += "\t" + "edge перемещение_вправо = new Edge(Перемещение_фишки.create(Место_дырки.СПРАВА, 1), "
				+ costRight + ", ApplyOrder." + computeRight + ")\n";
		code += "\t" + "edge перемещение_влево = new Edge(Перемещение_фишки.create(Место_дырки.СЛЕВА, -1), " + costLeft
				+ ", ApplyOrder." + computeLeft + ")\n";
		code += "\t" + "edge перемещение_вверх = new Edge(Перемещение_фишки.create(Место_дырки.СВЕРХУ, -3), " + costUp
				+ ", ApplyOrder." + computeUp + ")\n";
		code += "\t" + "edge перемещение_вниз = new Edge(Перемещение_фишки.create(Место_дырки.СНИЗУ, 3), " + costDown
				+ ", ApplyOrder." + computeDown + ")\n";
		code += "\n";
		code += "\t" + "def init() {\n";
		code += "\t\t" + "startCondition = [Фишки.all.exists[номер != место]]\n";
		code += "\t\t" + "terminateCondition = [Фишки.all.forall[номер == место]]\n";
		code += "\t\t" + "compareTops = " + object.get("compare") + "\n";
		code += "\t\t" + "heuristic = [(" + object.get("heuristic") + ") as double]\n";
		code += "\t" + "}\n";
		code += "}\n";

		return code;
	}
}
