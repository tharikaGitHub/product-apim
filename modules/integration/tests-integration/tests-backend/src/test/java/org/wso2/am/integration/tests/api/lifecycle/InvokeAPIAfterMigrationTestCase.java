/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.am.integration.tests.api.lifecycle;

import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.am.integration.clients.store.api.ApiResponse;
import org.wso2.am.integration.clients.store.api.v1.dto.*;
import org.wso2.am.integration.test.impl.RestAPIStoreImpl;
import org.wso2.am.integration.test.utils.APIManagerIntegrationTestException;
import org.wso2.am.integration.test.utils.base.APIMIntegrationConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Invoke an existing API after migration
 */
public class InvokeAPIAfterMigrationTestCase extends APIManagerLifecycleBaseTest {


    private final String API_NAME = "ADPRestAPI";
    private final String API_CONTEXT = "adp-rest";
    private final String API_END_POINT_METHOD = "/users";
    private final String API_VERSION_1_0_0 = "1.0.0";
    private final String APPLICATION_NAME = "ADPApplicationCS";

    private final String API_SUBSCRIPTION_TIER = "ADPBrass";

    private String CARBON_SUPER_SUBSCRIBER_USERNAME = "adp_sub_user";
    private String CARBON_SUPER_SUBSCRIBER_PASSWORD = "adp_sub_user";

    private String API_SCOPE = "adp-local-scope-without-roles";
    private String applicationId;


    @DataProvider
    public static Object[][] userModeDataProvider() {
        return new Object[][]{
                new Object[]{TestUserMode.SUPER_TENANT_ADMIN},
                //new Object[]{TestUserMode.TENANT_ADMIN},
        };
    }

    @Factory(dataProvider = "userModeDataProvider")
    public InvokeAPIAfterMigrationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws APIManagerIntegrationTestException {
        super.init(userMode);
    }


    @Test(groups = {"wso2.am"}, description = "Test invocation of the API")
    public void testInvokeAPIAfterMigration() throws Exception {

        RestAPIStoreImpl apiStore = getRestAPIStoreForUser(CARBON_SUPER_SUBSCRIBER_USERNAME,
                String.valueOf(CARBON_SUPER_SUBSCRIBER_PASSWORD), user.getUserDomain());

        ApplicationListDTO applicationListDTO = apiStore.getApplications(APPLICATION_NAME);
        if (applicationListDTO != null) {
            for (ApplicationInfoDTO applicationDTO : applicationListDTO.getList()) {
                if (APPLICATION_NAME.equals(applicationDTO.getName())) {
                    applicationId = applicationDTO.getApplicationId();
                }
            }
        }
        ApiResponse<ApplicationKeyDTO> applicationKeys = apiStore.getApplicationKeysByKeyType(applicationId,
                "PRODUCTION");

        if (applicationKeys != null) {
            //get access token
            String requestBody = "grant_type=password&username=" + CARBON_SUPER_SUBSCRIBER_USERNAME + "&password="
                    + CARBON_SUPER_SUBSCRIBER_PASSWORD + "&scope=" + API_SCOPE;
            URL tokenEndpointURL = new URL(keyManagerHTTPSURL + "oauth2/token");
            JSONObject accessTokenGenerationResponse = new JSONObject(
                    apiStore.generateUserAccessKey(applicationKeys.getData().getConsumerKey(),
                            applicationKeys.getData().getConsumerSecret(), requestBody, tokenEndpointURL).getData());
            String userAccessToken = accessTokenGenerationResponse.getString("access_token");

            // Create requestHeaders
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("accept", "application/json");
            requestHeaders.put("Authorization", "Bearer " + userAccessToken);

            //Invoke API
            HttpResponse invokeAPIResponse = HttpRequestUtil.doGet(
                    getAPIInvocationURLHttp(API_CONTEXT, API_VERSION_1_0_0) + API_END_POINT_METHOD,
                    requestHeaders);
            assertEquals(invokeAPIResponse.getResponseCode(), HTTP_RESPONSE_CODE_OK,
                    "Response code mismatched when invoking the API");
        }
    }
}

