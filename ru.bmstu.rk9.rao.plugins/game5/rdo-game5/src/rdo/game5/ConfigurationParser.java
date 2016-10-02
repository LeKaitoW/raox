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

	public static String parseConfig(JSONObject object) {
		final JSONArray places = (JSONArray) object.get("places");
		final String configuration = String
				.format("resource фишка1 = Фишка.create(1, %1$s)\n"
						+ "resource фишка2 = Фишка.create(2, %2$s)\n"
						+ "resource фишка3 = Фишка.create(3, %3$s)\n"
						+ "resource фишка4 = Фишка.create(4, %4$s)\n"
						+ "resource фишка5 = Фишка.create(5, %5$s)\n"
						+ "resource дырка = Дырка.create(%6$s)\n"
						+ "\n"
						+ "search Расстановка_фишек {\n"
						+ "\tedge перемещение_вправо = new Edge(Перемещение_фишки.create(Место_дырки.СПРАВА, 1), %10$s, ApplyOrder.%9$s)\n"
						+ "\tedge перемещение_влево = new Edge(Перемещение_фишки.create(Место_дырки.СЛЕВА, -1), %12$s, ApplyOrder.%11$s)\n"
						+ "\tedge перемещение_вверх = new Edge(Перемещение_фишки.create(Место_дырки.СВЕРХУ, -3), %14$s, ApplyOrder.%13$s)\n"
						+ "\tedge перемещение_вниз = new Edge(Перемещение_фишки.create(Место_дырки.СНИЗУ, 3), %16$s, ApplyOrder.%15$s)\n"
						+ "\n"
						+ "\tset init() {\n"
						+ "\t\tstartCondition = [Фишка.all.exists[номер != место]]\n"
						+ "\t\tterminateCondition = [Фишка.all.forall[номер == место]]\n"
						+ "\t\tcompareTops = %7$s\n"
						+ "\t\theuristic = [(%8$s) as double]\n"
						+ "\t}\n"
						+ "}\n%17$s",
						places.get(0),
						places.get(1),
						places.get(2),
						places.get(3),
						places.get(4),
						places.get(5),
						object.get("compare"),
						object.get("heuristic"),
						object.get("computeRight").toString().toUpperCase(),
						object.get("costRight"),
						object.get("computeLeft").toString().toUpperCase(),
						object.get("costLeft"),
						object.get("computeUp").toString().toUpperCase(),
						object.get("costUp"),
						object.get("computeDown").toString().toUpperCase(),
						object.get("costDown"),
						object.get("code").equals("") ? "" : "\n"
								+ object.get("code") + "\n");
		return configuration;
	}
}
