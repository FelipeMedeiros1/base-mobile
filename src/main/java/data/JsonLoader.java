package data;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import exceptions.AutomationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;


/**
 * Responsável por carregar arquivos escritos em padrão JSON. O caminho padrão é
 * '/src/test/resources/json'.
 */
public class JsonLoader {
    static final Logger logger = LogManager.getLogger(JsonLoader.class);
    private String JSON_DEFAULT_PATH = System.getProperty("user.dir") + "/src/test/resources/json";
    private String FILE_NAME;
    private JsonObject json;

    /**
     * Construtor padrão vazio. Permite inicializar o JsonLoader sem parâmetros.
     */
    public JsonLoader() {
        // Construtor vazio, sem ação inicial.
    }

    /**
     * Construtor que inicializa o carregamento do arquivo JSON com o nome do
     * arquivo.
     *
     * @param fileName - Nome do arquivo que deseja carregar. <br>
     *                 Exemplo: 'exemplo.json'
     */
    public JsonLoader(String fileName) {
        try {
            json = (JsonObject) JsonParser.parseReader(new FileReader(JSON_DEFAULT_PATH + (FILE_NAME = fileName)));
        } catch (Exception e) {
            logger.error("Erro ao realizar a leitura do arquivo identificado: path -> " + getFullPathJson());
            throw new AutomationException(e);
        }
    }

    /**
     * Construtor que inicializa o carregamento do arquivo JSON com o nome do
     * arquivo e o caminho completo.
     *
     * @param path     - Caminho completo do arquivo que deseja carregar.
     * @param fileName - Nome do arquivo que deseja carregar. <br>
     *                 Exemplo: 'exemplo.json'
     */
    public JsonLoader(String path, String fileName) {
        try {
            json = (JsonObject) JsonParser
                    .parseReader(new FileReader((JSON_DEFAULT_PATH = path) + (FILE_NAME = fileName)));
        } catch (Exception e) {
            logger.error("Erro ao realizar a leitura do arquivo identificado: path -> " + getFullPathJson());
            throw new AutomationException(e);
        }
    }

    /**
     * Retorna o valor correspondente à chave especificada no arquivo JSON
     * carregado.
     *
     * @param key - Chave do parâmetro que deseja capturar do arquivo carregado.
     * @return String - Conteúdo referente à chave especificada.
     */
    public String getValue(String key) {
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            throw new AutomationException(e);
        }
    }

    /**
     * Retorna o caminho completo do arquivo JSON especificado.
     *
     * @return String - Caminho completo do arquivo especificado.
     */
    public String getFullPathJson() {
        return JSON_DEFAULT_PATH + FILE_NAME;
    }

    /**
     * Retorna o arquivo JSON carregado no formato {@link JsonObject}.
     *
     * @return JsonObject - Objeto JSON carregado.
     */
    public JsonObject getJsonFile() {
        return json;
    }

    /**
     * Carrega o conteúdo de um arquivo JSON específico e o armazena no
     * {@link JsonDataStore}.
     *
     * @param fileName - Nome do arquivo JSON que deseja carregar.
     * @throws IOException - Se ocorrer um erro durante a leitura do arquivo.
     */
    public void loadJsonFile(String fileName) throws IOException {
        String filePath = JSON_DEFAULT_PATH + "/" + fileName;
        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        JsonDataStore.getInstance().setJsonContent(jsonContent);
    }

    /**
     * Retorna o valor de uma expressão JSONPath no conteúdo JSON carregado.
     *
     * @param jsonPath - Expressão JSONPath para o valor desejado.
     * @return String - Valor correspondente à expressão JSONPath.
     * @throws AutomationException - Se o conteúdo JSON não estiver carregado.
     */
    public String getValueFromJson(String jsonPath) {
        String jsonContent = JsonDataStore.getInstance().getJsonContent();
        if (jsonContent != null) {
            return JsonPath.read(jsonContent, jsonPath);
        }
        throw new AutomationException("O conteúdo JSON não está carregado.");
    }
}
