package utils;

import com.google.gson.JsonObject;
import io.cucumber.datatable.DataTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public final class Utils {
	 
	static final Logger logger = LogManager.getLogger(Utils.class);

	/**
	 * Remove caracteres especiais (tudo que não for alfanumerico)
	 * @param text texto para ser removido caracteres especiais
	 * @return String
	 */
	public static String removeSpecialCharacters(String text) {
		return text.replaceAll("[^A-Za-z0-9]","");
	}

	/**
	 * Remove os caracteres informados de uma String especifica
	 * @param text texto alvo para ser removido os caracteres
	 * @param  chars caracteres para ser removidos do texto alvo
	 * @return String
	 */
	public static String removeCharacters(String text, String... chars ) {
		for (String s : chars) {
			text = text.replace(s, "");
		}
		return text;
	}
	
	/**
	 * Converte um objeto DataTable em um objeto JsonObject
	 * @param data DataTable para ser convertido em JsonObject
	 * @return JsonObject
	 */
	public static JsonObject DataTableToJson(DataTable data) {
		JsonObject json = new JsonObject();
		for(Map<String, String> line: data.asMaps()) {
			for (String key : line.keySet()) {
				json.addProperty(key, line.get(key).toString().replace("'", ""));
			}
		}
		return json;
	}

	public static void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}
}
