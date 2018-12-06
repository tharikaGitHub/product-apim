package org.wso2.am.scenario.tests.register.application;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.am.integration.test.utils.APIManagerIntegrationTestException;
import org.wso2.am.integration.test.utils.bean.APPKeyRequestGenerator;
import org.wso2.am.scenario.test.common.APIStoreRestClient;
import org.wso2.am.scenario.test.common.ScenarioDataProvider;
import org.wso2.am.scenario.test.common.ScenarioTestBase;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ApplicationCreationTestCases extends ScenarioTestBase {
    private APIStoreRestClient apiStore;
    private List<String> applicationsList = new ArrayList<>();
    private static final String ADMIN_LOGIN_USERNAME = "admin";
    private static final String ADMIN_LOGIN_PW = "admin";
    private static final String DEFAULT_STORE_URL = "https://localhost:9443/";
    private static final String UTF_8 = "UTF-8";
    private static final String ERROR_APPLICATION_CREATION_FAILED = "Application creation failed for application: ";
    private static final String ERROR_APPLICATION_CREATION_WITH_VALID_INPUT = "Application creation with valid " +
            "input failed for application: ";
    private static final String ERROR_GET_ALL_APPS = "Error when retrieving all apps";
    private static final String ERROR_APPLICATION_CREATION_RESPONSE_CODE = "Response Code is mismatched " +
            "in add application: ";
    private static final String ERROR_APPLICATION_TIER_MISMATCH = "Application tier value mismatch for application: ";
    private static final String ERROR_APPLICATION_DESCRIPTION_MISMATCH = "Application description value mismatch" +
            " for application: ";
    private static final String ERROR_APPLICATION_TOKEN_TYPE_MISMATCH = "Application token type value mismatch" +
            " for application: ";
    private static final String ERROR_GENERATING_PRODUCTION_KEY = "Production key generation failed for application:  ";
    private static final String ERROR_GENERATING_SANDBOX_KEY = "Sandbox key generation failed for application:  ";
    private static final String ERROR = "error";
    private static final String STATUS = "status";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String NAME = "name";
    private static final String TIER = "tier";
    private static final String DESCRIPTION = "description";
    private static final String TOKEN_TYPE = "tokenType";
    private static final String APPLICATIONS = "applications";
    private static final String DATA = "data";
    private static final String KEY = "key";
    private static final String KEY_STATE = "keyState";
    private static final String APP_DETAILS = "appDetails";
    private static final String KEY_TYPE = "key_type";
    private static final String PRODUCTION = "PRODUCTION";
    private static final String SANDBOX = "SANDBOX";


    @BeforeClass(alwaysRun = true)
    public void init() throws APIManagerIntegrationTestException {
        String storeURL;
        Properties infraProperties;

        infraProperties = getDeploymentProperties();
        storeURL = infraProperties.getProperty(STORE_URL);
        if (storeURL == null) {
            storeURL = DEFAULT_STORE_URL;
        }
        setKeyStoreProperties();
        apiStore = new APIStoreRestClient(storeURL);
        apiStore.login(ADMIN_LOGIN_USERNAME, ADMIN_LOGIN_PW);
    }

    @Test(description = "4.1.1.1", dataProvider = "ValidMandatoryApplicationValuesDataProvider",
            dataProviderClass = ScenarioDataProvider.class)
    public void testApplicationCreationWithValidMandatoryValues(String applicationName, String tier, String description)
            throws Exception {
        HttpResponse addApplicationResponse = apiStore
                .addApplication(URLEncoder.encode(applicationName, UTF_8), URLEncoder.encode(tier, UTF_8),
                        "", URLEncoder.encode(description, UTF_8));
        applicationsList.add(applicationName);
        assertEquals(addApplicationResponse.getResponseCode(), Response.Status.OK.getStatusCode(),
                ERROR_APPLICATION_CREATION_RESPONSE_CODE + applicationName);
        JSONObject addApplicationJsonObject = new JSONObject(addApplicationResponse.getData());
        assertFalse(addApplicationJsonObject.getBoolean(ERROR),
                ERROR_APPLICATION_CREATION_WITH_VALID_INPUT + applicationName);
        assertEquals(addApplicationJsonObject.get(STATUS), STATUS_APPROVED,
                ERROR_APPLICATION_CREATION_WITH_VALID_INPUT + applicationName);
        validateApplicationWithValidMandatoryValues(applicationName, tier, description);
    }

    private void validateApplicationWithValidMandatoryValues(String applicationName, String tier, String description)
            throws Exception {
        HttpResponse getAllAppResponse = apiStore.getAllApplications();
        assertEquals(getAllAppResponse.getResponseCode(), Response.Status.OK.getStatusCode(),
                ERROR_GET_ALL_APPS);
        JSONObject getAllAppJsonObject = new JSONObject(getAllAppResponse.getData());
        assertFalse(getAllAppJsonObject.getBoolean(ERROR), ERROR_GET_ALL_APPS);
        JSONArray getAllAppJsonArray = getAllAppJsonObject.getJSONArray(APPLICATIONS);

        for (int i = 0; i < getAllAppJsonArray.length(); i++) {
            if (applicationName.equals(getAllAppJsonArray.getJSONObject(i).getString(NAME))) {
                assertEquals(getAllAppJsonArray.getJSONObject(i).getString(TIER), tier,
                        ERROR_APPLICATION_TIER_MISMATCH + applicationName);
                assertEquals(getAllAppJsonArray.getJSONObject(i).getString(DESCRIPTION), description,
                        ERROR_APPLICATION_DESCRIPTION_MISMATCH + applicationName);
            }
        }
    }

    @Test(description = "4.1.1.2", dataProvider = "MandatoryAndOptionalApplicationValuesDataProvider",
            dataProviderClass = ScenarioDataProvider.class)
    public void testApplicationCreationWithMandatoryAndOptionalValues(String applicationName, String tier,
                                                                      String description, String tokenType)
            throws Exception {
        HttpResponse addApplicationResponse = apiStore
                .addApplicationWithTokenType(URLEncoder.encode(applicationName, UTF_8),
                        URLEncoder.encode(tier, UTF_8), "",
                        URLEncoder.encode(description, UTF_8), URLEncoder.encode(tokenType, UTF_8));
        applicationsList.add(applicationName);
        assertEquals(addApplicationResponse.getResponseCode(), Response.Status.OK.getStatusCode(),
                ERROR_APPLICATION_CREATION_RESPONSE_CODE + applicationName);
        JSONObject addApplicationJsonObject = new JSONObject(addApplicationResponse.getData());
        assertFalse(addApplicationJsonObject.getBoolean(ERROR),
                ERROR_APPLICATION_CREATION_WITH_VALID_INPUT + applicationName);
        assertEquals(addApplicationJsonObject.get(STATUS), STATUS_APPROVED,
                ERROR_APPLICATION_CREATION_WITH_VALID_INPUT + applicationName);
        validateApplicationWithMandatoryAndOptionsValues(applicationName, tier, description, tokenType);
    }

    public void validateApplicationWithMandatoryAndOptionsValues(String applicationName, String tier,
                                                                 String description, String tokenType)
            throws Exception {
        HttpResponse getAllAppResponse = apiStore.getAllApplications();
        assertEquals(getAllAppResponse.getResponseCode(), Response.Status.OK.getStatusCode(),
                ERROR_GET_ALL_APPS);
        applicationsList.add(applicationName);
        JSONObject getAllAppJsonObject = new JSONObject(getAllAppResponse.getData());
        assertFalse(getAllAppJsonObject.getBoolean(ERROR), ERROR_GET_ALL_APPS);
        JSONArray getAllAppJsonArray = getAllAppJsonObject.getJSONArray(APPLICATIONS);

        for (int i = 0; i < getAllAppJsonArray.length(); i++) {
            if (applicationName.equals(getAllAppJsonArray.getJSONObject(i).getString(NAME))) {
                assertEquals(getAllAppJsonArray.getJSONObject(i).getString(TIER), tier,
                        ERROR_APPLICATION_TIER_MISMATCH + applicationName);
                assertEquals(getAllAppJsonArray.getJSONObject(i).getString(DESCRIPTION), description,
                        ERROR_APPLICATION_DESCRIPTION_MISMATCH + applicationName);
                assertEquals(getAllAppJsonArray.getJSONObject(i).getString(TOKEN_TYPE), tokenType,
                        ERROR_APPLICATION_TOKEN_TYPE_MISMATCH + applicationName);
            }
        }
    }

    @Test(description = "4.1.1.3", dataProvider = "ApplicationProductionKeyGenerationDataProvider",
            dataProviderClass = ScenarioDataProvider.class)
    public void testGenerateProductionKeysForApplication(String applicationName, String tier, String description)
            throws Exception {
        createApplicationForKeyGeneration(applicationName, tier, description);
        APPKeyRequestGenerator appKeyRequestGenerator = new APPKeyRequestGenerator(applicationName);
        String responseString = apiStore.generateApplicationKey(appKeyRequestGenerator).getData();
        JSONObject responseStringJson = new JSONObject(responseString);
        assertFalse(responseStringJson.getBoolean(ERROR),
                ERROR_GENERATING_PRODUCTION_KEY + applicationName);
        assertEquals(responseStringJson.getJSONObject(DATA).getJSONObject(KEY).getString(KEY_STATE), STATUS_APPROVED,
                ERROR_GENERATING_PRODUCTION_KEY + applicationName);
        assertEquals(new JSONObject(responseStringJson.getJSONObject(DATA).getJSONObject(KEY).getString(APP_DETAILS))
                .get(KEY_TYPE), PRODUCTION, ERROR_GENERATING_PRODUCTION_KEY + applicationName);
    }

    @Test(description = "4.1.1.4", dataProvider = "ApplicationSandboxKeyGenerationDataProvider",
            dataProviderClass = ScenarioDataProvider.class)
    public void testGenerateSandboxKeysForApplication(String applicationName, String tier, String description)
            throws Exception {
        createApplicationForKeyGeneration(applicationName, tier, description);
        APPKeyRequestGenerator appKeyRequestGenerator = new APPKeyRequestGenerator(applicationName);
        appKeyRequestGenerator.setKeyType(SANDBOX);
        String responseString = apiStore.generateApplicationKey(appKeyRequestGenerator).getData();
        JSONObject responseStringJson = new JSONObject(responseString);
        assertFalse(responseStringJson.getBoolean(ERROR),
                ERROR_GENERATING_PRODUCTION_KEY + applicationName);
        assertEquals(responseStringJson.getJSONObject(DATA).getJSONObject(KEY).getString(KEY_STATE), STATUS_APPROVED,
                ERROR_GENERATING_SANDBOX_KEY + applicationName);
        assertEquals(new JSONObject(responseStringJson.getJSONObject(DATA).getJSONObject(KEY).getString(APP_DETAILS))
                .get(KEY_TYPE), SANDBOX, ERROR_GENERATING_SANDBOX_KEY + applicationName);
    }

    private void createApplicationForKeyGeneration(String applicationName, String tier, String description)
            throws Exception {
        HttpResponse addApplicationResponse = apiStore
                .addApplication(URLEncoder.encode(applicationName, UTF_8),
                        URLEncoder.encode(tier, UTF_8),
                        "", URLEncoder.encode(description, UTF_8));
        applicationsList.add(applicationName);
        assertEquals(addApplicationResponse.getResponseCode(), Response.Status.OK.getStatusCode(),
                ERROR_APPLICATION_CREATION_FAILED + applicationName);
        JSONObject addApplicationJsonObject = new JSONObject(addApplicationResponse.getData());
        assertFalse(addApplicationJsonObject.getBoolean(ERROR),
                ERROR_APPLICATION_CREATION_FAILED + applicationName);
        assertEquals(addApplicationJsonObject.get(STATUS), STATUS_APPROVED,
                ERROR_APPLICATION_CREATION_FAILED + applicationName);
    }

    @AfterMethod(alwaysRun = true)
    public void destroy() throws Exception {
        for (String name : applicationsList) {
            apiStore.removeApplication(URLEncoder.encode(name, UTF_8));
        }
    }
}