/*
 *Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.am.scenario.test.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.integration.test.utils.APIManagerIntegrationTestException;
import org.wso2.am.integration.test.utils.http.HTTPSClientUtils;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardRestClient {

    private static final Log log = LogFactory.getLog(AdminDashboardRestClient.class);
    private String backendURL;
    private Map<String, String> requestHeaders = new HashMap<String, String>();

    public AdminDashboardRestClient(String backendURL) {
        this.backendURL = backendURL;
        if (requestHeaders.get("Content-Type") == null) {
            this.requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");
        }
    }

    /**
     * Login to API Admin Portal
     *
     * @param userName - username to login
     * @param password - password to login
     * @return - http response
     * @throws org.wso2.am.integration.test.utils.APIManagerIntegrationTestException - Throws if login to Admin Portal
     *                                                                               fails
     */
    public HttpResponse login(String userName, String password) throws APIManagerIntegrationTestException {
        HttpResponse response;
        log.info("Login to Admin Portal " + backendURL + " as the user " + userName);
        try {
            response = HTTPSClientUtils.doPost(new URL(backendURL + "/site/blocks/user/login/ajax/login.jag"),
                    "action=login&username=" + userName + "&password=" + password + "", requestHeaders);
        } catch (Exception e) {
            throw new APIManagerIntegrationTestException("Unable to login to the store app ", e);
        }

        String session = getSession(response.getHeaders());

        if (session == null) {
            throw new APIManagerIntegrationTestException("No session cookie found with response");
        }

        setSession(session);
        return response;
    }

    /**
     * Add new subscription policy
     *
     * @param subscriptionThrottlePolicyRequest
     * @return http response
     * @throws APIManagerIntegrationTestException
     */
    public HttpResponse addSubscriptionPolicy(SubscriptionThrottlePolicyRequest subscriptionThrottlePolicyRequest)
            throws APIManagerIntegrationTestException {
        try {
            checkAuthentication();
            return HTTPSClientUtils.doPost(new URL(
                            backendURL + "/site/blocks/policy/subscription/edit/ajax/subscription-policy-edit.jag"),
                    subscriptionThrottlePolicyRequest.generateRequestParameters(), requestHeaders);
        } catch (Exception e) {
            throw new APIManagerIntegrationTestException("Add new subscription policy failed", e);
        }
    }

    /**
     * Delete subscription policy
     *
     * @param policyName
     * @return http response
     * @throws APIManagerIntegrationTestException
     */
    public HttpResponse deleteSubscriptionPolicy(String policyName) throws APIManagerIntegrationTestException {
        try {
            checkAuthentication();
            return HTTPSClientUtils.doPost(new URL(
                            backendURL + "/site/blocks/policy/subscription/manage/ajax/subscription-policy-manage.jag"),
                    "action=deleteSubscriptionPolicy&policy=" + policyName, requestHeaders);
        } catch (Exception e) {
            throw new APIManagerIntegrationTestException("Delete subscription policy failed", e);
        }
    }

    private String getSession(Map<String, String> responseHeaders) {
        return responseHeaders.get("Set-Cookie");
    }

    private String setSession(String session) {
        return requestHeaders.put("Cookie", session);
    }

    /**
     * Check whether the user is logged in
     *
     * @throws org.wso2.am.integration.test.utils.APIManagerIntegrationTestException - If session cookie not found in
     *                                                                               the request header
     */
    private void checkAuthentication() throws APIManagerIntegrationTestException {
        if (requestHeaders.get("Cookie") == null) {
            throw new APIManagerIntegrationTestException("No Session Cookie found. Please login first");
        }
    }
}
