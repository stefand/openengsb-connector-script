/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.script.internal;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.test.TestDomainEvents;

public class ScriptServiceInstanceFactoryTest {

	@Test
	public void testCreateSeviceImplInjects() throws Exception {
		BuildDomainEvents build = mock(BuildDomainEvents.class);
		TestDomainEvents test = mock(TestDomainEvents.class);
		DeployDomainEvents deploy = mock(DeployDomainEvents.class);
		ContextCurrentService context = mock(ContextCurrentService.class);

		ScriptServiceInstanceFactory factory = createFactory(build, test,
				deploy, context);

		ScriptServiceImpl scriptService = (ScriptServiceImpl) factory
				.createNewInstance("id");

		Assert.assertEquals(build, scriptService.getBuildEvents());
		Assert.assertEquals(test, scriptService.getTestEvents());
		Assert.assertEquals(deploy, scriptService.getDeployEvents());
		Assert.assertEquals(context, scriptService.getContextService());

	}

	@Test
	public void testCreatePlaintextReportService() throws Exception {
		System.setProperty("karaf.data", FileUtils.getTempDirectoryPath());

		ScriptServiceInstanceFactory factory = createFactory(
				mock(BuildDomainEvents.class),
				mock(TestDomainEvents.class),
				mock(DeployDomainEvents.class),
				mock(ContextCurrentService.class));

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("projectPath", "someValue");

		ScriptServiceImpl mavenService = (ScriptServiceImpl) factory
				.createNewInstance("id");
		factory.applyAttributes(mavenService, attributes);

		Assert.assertNotNull(mavenService);
	}

	private static ScriptServiceInstanceFactory createFactory(
			BuildDomainEvents build, TestDomainEvents test,
			DeployDomainEvents deploy, ContextCurrentService context) {
		
	    ScriptServiceInstanceFactory factory = new ScriptServiceInstanceFactory();
		factory.setBuildEvents(build);
		factory.setTestEvents(test);
		factory.setDeployEvents(deploy);
		factory.setContextService(context);

		return factory;
	}

}
