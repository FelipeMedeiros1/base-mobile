package data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * Esta classe tem como objetivo carregar e manter a representação em memória dos arquivos que serão, posteriormente, 
 * utilizados como massa de dados.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoadTestData {
	
	static final Logger logger = LogManager.getLogger(LoadTestData.class);
	
	private Map<String, YamlFileLoader> yaml = new HashMap<String, YamlFileLoader>();
	private Map<String, Object> testData = new HashMap<String, Object>();
	private String url;
	private String endPoint;
	private InternalPropertiesLoader properties;
	
	/**
	 * Construtor padrão:
	 * Carregar todos arquivos YAML do pacote '/resources/massa_dados' do projeto; alocar em MAP(yaml) que mantém todos os arquivos encontrados no pacote em memória.
	 * Carregar arquivo properties do pacote '/resources';
	 * Carregar a URL padrão.
	 * Falha ao encontrar caminho padrão de massa de dados; 
	 * Falha ao encontrar arquivos properties.
	 */
	public LoadTestData() {
		try {
//			loadAllYamlFiles();
			loadProperties();
		} catch (IOException e) {
			logger.error("Erro ao carregar arquivo de massas");
			e.printStackTrace();
		}
		loadUrl();
	}

	/**
	 * Carregar URL padrão do arquivo properties;
	 * o arquivo nome do arquivo padrão é: 'application.properties', <p>a chave que representa o arquivo por padrão é: 'base.url'
	 */
	private void loadUrl() {
		url = properties.getValue("base.url");
	}

	/**
	 * Carregar arquivo padrão properties 'application.properties';
	 *
	 * @throws IOException /Leitura de arquivo
	 */
	private void loadProperties() throws IOException {
		properties = new InternalPropertiesLoader("application.properties");
	}
	
	/**
	 *  associa um par chave valor ao Map que representa os dados carregados
	 * @param key - string que representa a chave no arquivo.
	 * @param value - Object que representa o valor associado a chave
	 */
	public void setData(String key, Object value) {
		testData.put(key, value);
	}

	/**
	 * @return retorna um Map com os arquivos
	 */
	public Map<String, Object> getData() {
		return this.testData;
	}

	/**
	 * @param fileName - nome do arquivo carregado previamente.
	 * @param params - lista de parametros dentro do arquivo indicado pelo 'fileName'
	 * @return String com o valor recuperado do arquivo.
	 * @throws Exception chave não encontrada no arquivo especificado.
	 */
	public String getAsString(String fileName, String... params) throws Exception {
		return String.valueOf(getYamlAttribute(fileName, params));
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - String com o valor recuperado do arquivo.
	 */
	public String getAsString(String key) {
		return String.valueOf(testData.get(key));
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Integer com o valor recuperado do arquivo.
	 */
	public Integer getAsInteger(String key) {
		return (Integer) testData.get(key);
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Double com o valor recuperado do arquivo.(tratado com precisão BigDouble)
	 */
	public Double getAsDouble(String key) {
		return new BigDecimal((Double) testData.get(key)).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
	}
	
	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Long com o valor recuperado do arquivo.
	 */
	public Long getAsLong(String key) {
		return (Long) testData.get(key);
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Vetor de String com os valores recuperados do arquivo.
	 */
	public String[] getAsStringArray(String key) {
		return (String[]) testData.get(key);
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Vetor de Integer com os valores recuperados do arquivo.
	 */
	public Integer[] getAsIntegerArray(String key) {
		return (Integer[]) testData.get(key);
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Vetor de Double com os valores recuperados do arquivo.
	 */
	public Double[] getAsDoubleArray(String key) {
		return (Double[]) testData.get(key);
	}

	/**
	 * @param key -  parametro dentro do arquivo indicado pelo 'arquivo padrão'
	 * @return - Vetor de Long com os valores recuperados do arquivo.
	 */
	public Long[] getAsLongArray(String key) {
		return (Long[]) testData.get(key);
	}

	public void setData(JsonObject json) {
		Set<Map.Entry<String, JsonElement>> entries = json.entrySet();// will return members of your object
		for (Map.Entry<String, JsonElement> entry : entries) {
			this.setData(entry.getKey(), entry.getValue().getAsString());
		}
	}

	/**
	 * @return - url carregada no instante da chamada deste método.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * altera a URL padrão para a indicada pelo parametro url
	 * @param url - String representando a URL base
	 */
	public void setUrl(String url) {
		this.url = url;
		RequestSpecification espc = new RequestSpecBuilder().setBaseUri(url).build();
		RestAssured.given().spec(espc);
	}

	/**
	 * @return - fim de url
	 */
	public String getEndPoint() {
		return endPoint;
	}

	/**
	 * altera o fim da url padrão para a indicada pelo parametro endPoint
	 * @param endPoint - String representando o endpoint
	 */
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	/**
	 * @return - retorna a url completa carregada no instante de chamada deste método
	 */
	public String getFullUrl() {
		return this.url + "/" + this.endPoint;
	}

	/**
	 * Executa o carregamento de todos os arquivos YAML da pasta '/resources/massa_dados' <br>
	 * para o objeto yaml.
	 * @throws FileNotFoundException caso o arquivo nao tenha sido encontrado
	 */
	public void loadYamlFiles() throws FileNotFoundException {
		String basePath = System.getProperty("user.dir") + "\\resources\\massa_dados";
		File folder = new File(basePath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				yaml.put(listOfFiles[i].getName(), new YamlFileLoader(listOfFiles[i].getAbsolutePath()));
			}
		}
	}

	/**
	 * @param fileName - nome do arquivo carregado previamente.
	 * @param params - lista de parametros dentro do arquivo indicado pelo 'fileName'
	 * @return String com o valor recuperado do arquivo.
	 * @throws Exception - chave não encontrada no arquivo especificado.
	 */
	public Object getYamlAttribute(String fileName, String... params) throws Exception {
		return yaml.get(fileName).getAttribute(params);
	}

}
