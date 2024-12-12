package data;

public class JsonDataStore {

	private static JsonDataStore instance;
	private String jsonContent;

	private JsonDataStore() {
		// Construtor privado para impedir instância direta
	}

	public static JsonDataStore getInstance() {
		if (instance == null) {
			instance = new JsonDataStore();
		}
		return instance;
	}

	public void setJsonContent(String jsonContent) {
		this.jsonContent = jsonContent;
	}

	public String getJsonContent() {
		return jsonContent;
	}
}
