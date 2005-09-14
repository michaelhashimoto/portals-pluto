/*
 * Copyright 2003,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.testsuite.test;

import org.apache.pluto.testsuite.TestConfig;
import org.apache.pluto.testsuite.PortletTest;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestResults;

import javax.portlet.PortletResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="ddewolf@apache.org">David H. DeWolf</a>
 */
public abstract class AbstractReflectivePortletTest implements PortletTest {

    private Map initParameters;
    private TestConfig config;

    public TestResults doTest(PortletConfig config,
                              PortletContext context,
                              PortletRequest req,
                              PortletResponse res) {
        TestResults results = new TestResults(getTestSuiteName());

        Class klass = getClass();
        Method[] methods = klass.getDeclaredMethods();

        for(int i = 0; i<methods.length;i++) {
            if(methods[i].getName().startsWith("check")) {
                try {
                    results.add(invoke(methods[i], config, context, req, res));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    TestResult result = new TestResult();
                    result.setName(methods[i].getName());
                    result.setDesc("Unknown");
                    result.setReturnCode(TestResult.FAILED);
                    result.setResults(e.getMessage());
                    results.add(result);
                }
            }
        }
        return results;
    }

    public void init(TestConfig config) {
        this.initParameters = config.getInitParameters();
        this.config = config;
    }

    private TestResult invoke(Method method, PortletConfig config,
                              PortletContext context,
                              PortletRequest req, PortletResponse res)
    throws IllegalAccessException, InvocationTargetException {

        TestResult result = null;
        Class[] paramTypes= method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];

        for(int i=0;i<paramTypes.length;i++) {
            if(paramTypes[i].equals(PortletContext.class)) {
                paramValues[i] = context;
            }
            if(paramTypes[i].equals(PortletRequest.class)) {
                paramValues[i] = req;
            }
            if(paramTypes[i].equals(PortletResponse.class)) {
                paramValues[i] = res;
            }
            if(paramTypes[i].equals(PortletSession.class)) {
                paramValues[i] = req.getPortletSession();
            }
            if(paramTypes[i].equals(PortletConfig.class)) {
                paramValues[i] = config;
            }
        }
        result = (TestResult)method.invoke(this, paramValues);
        return result;
    }

    public Map getRenderParameters(PortletRequest req) {
        return new HashMap();
    }

    public Map getInitParameters() {
        return initParameters;
    }

    public TestConfig getConfig() {
        return config;
    }
}