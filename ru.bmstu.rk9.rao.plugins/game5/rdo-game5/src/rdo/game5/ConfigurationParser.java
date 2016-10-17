package rdo.game5;

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
		final String code = "resource фишка1 = Фишка.create(1, " + places.get(0) + ")\n"
				+ "resource фишка2 = Фишка.create(2, " + places.get(1) + ")\n" + "resource фишка3 = Фишка.create(3, "
				+ places.get(2) + ")\n" + "resource фишка4 = Фишка.create(4, " + places.get(3) + ")\n"
				+ "resource фишка5 = Фишка.create(5, " + places.get(4) + ")\n" + "resource дырка = Дырка.create("
				+ places.get(5) + ")\n";
		return code;
	}

	public static String getSearchCode(JSONObject object) {
		final String code = "\nsearch Расстановка_фишек {\n"
				+ "\tedge перемещение_вправо = new Edge(Перемещение_фишки.create(Место_дырки.СПРАВА, 1), "
				+ object.get("costRight") + ", ApplyOrder." + object.get("computeRight").toString().toUpperCase()
				+ ")\n" + "\tedge перемещение_влево = new Edge(Перемещение_фишки.create(Место_дырки.СЛЕВА, -1), "
				+ object.get("costLeft") + ", ApplyOrder." + object.get("computeLeft").toString().toUpperCase() + ")\n"
				+ "\tedge перемещение_вверх = new Edge(Перемещение_фишки.create(Место_дырки.СВЕРХУ, -3), "
				+ object.get("costUp") + ", ApplyOrder." + object.get("computeUp").toString().toUpperCase() + ")\n"
				+ "\tedge перемещение_вниз = new Edge(Перемещение_фишки.create(Место_дырки.СНИЗУ, 3), "
				+ object.get("costDown") + ", ApplyOrder." + object.get("computeDown").toString().toUpperCase() + ")\n"
				+ "\n" + "\tdef init() {\n" + "\t\tstartCondition = [Фишка.all.exists[номер != место]]\n"
				+ "\t\tterminateCondition = [Фишка.all.forall[номер == место]]\n" + "\t\tcompareTops = "
				+ object.get("compare") + "\n" + "\t\theuristic = [(" + object.get("heuristic") + ") as double]\n"
				+ "\t}\n" + "}\n";

		return code;
	}
}
