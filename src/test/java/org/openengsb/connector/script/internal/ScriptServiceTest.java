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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.build.BuildFailEvent;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.test.TestDomainEvents;

/* FIXME: These tests run only on Unix systems... */
public class ScriptServiceTest {

    private ScriptServiceImpl scriptService;
    private TestDomainEvents testEvents;
    private BuildDomainEvents buildEvents;
    private DeployDomainEvents deployEvents;

    @Before
    public void setUp() throws Exception {
        System.setProperty("karaf.data", ".");
        deleteLogFile();
        FileUtils.deleteDirectory(new File(getPath("test-unit-success"), "target"));

        buildEvents = mock(BuildDomainEvents.class);
        testEvents = mock(TestDomainEvents.class);
        deployEvents = mock(DeployDomainEvents.class);
        
        scriptService = new ScriptServiceImpl("42");
        scriptService.setBuildEvents(buildEvents);
        scriptService.setTestEvents(testEvents);
        scriptService.setDeployEvents(deployEvents);
        scriptService.setContextService(mock(ContextCurrentService.class));
        scriptService.setSynchronous(true);
        scriptService.setUseLogFile(false);
    }

    @After
    public void deleteLogFile() throws IOException {
        FileUtils.deleteDirectory(new File("log"));
    }

    @Test
    public void build_shouldWork() {
        scriptService.setCommand("/bin/echo");
        scriptService.setParameters("6b481edd-d667-48e1-9042-7072e9c09c24");
        String id = scriptService.build(getFileModel("test-unit-success"));
        ArgumentCaptor<BuildSuccessEvent> argumentCaptor = ArgumentCaptor.forClass(BuildSuccessEvent.class);

        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildSuccessEvent event = argumentCaptor.getValue();
        assertThat(event.getBuildId(), is(id));
        assertThat(event.getOutput(), containsString("6b481edd-d667-48e1-9042-7072e9c09c24"));
    }

    @Test
    public void build_pathTest() {
        scriptService.setCommand("script-connector-pathtest.sh");
        String id = scriptService.build(getFileModel("test-unit-success"));
        ArgumentCaptor<BuildSuccessEvent> argumentCaptor = ArgumentCaptor.forClass(BuildSuccessEvent.class);

        verify(buildEvents).raiseEvent(any(BuildStartEvent.class));
        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildSuccessEvent event = argumentCaptor.getValue();
        assertThat(event.getBuildId(), is(id));
        assertThat(event.getOutput(), containsString("dummy.txt"));
    }

    @Test
    public void build_shouldFail() {
        scriptService.setCommand("/bin/false");
        String id = scriptService.build(getFileModel("test-unit-success"));
        ArgumentCaptor<BuildFailEvent> argumentCaptor = ArgumentCaptor.forClass(BuildFailEvent.class);

        verify(buildEvents).raiseEvent(any(BuildFailEvent.class));
        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildFailEvent event = argumentCaptor.getValue();
        assertThat(event.getBuildId(), is(id));
    }

    @Test
    public void build_nonexistentProgram() {
        scriptService.setCommand("6b481edd-d667-48e1-9042-7072e9c09c24");
        String id = scriptService.build(getFileModel("test-unit-success"));
        ArgumentCaptor<BuildFailEvent> argumentCaptor = ArgumentCaptor.forClass(BuildFailEvent.class);

        verify(buildEvents).raiseEvent(any(BuildFailEvent.class));
        verify(buildEvents).raiseEvent(argumentCaptor.capture());
        BuildFailEvent event = argumentCaptor.getValue();
        assertThat(event.getBuildId(), is(id));
    }

    private String getPath(String folder) {
        return ClassLoader.getSystemResource(folder).getFile();
    }

    private OpenEngSBFileModel getFileModel(String folder) {
        OpenEngSBFileModel m = ModelUtils.createEmptyModelObject(OpenEngSBFileModel.class);
        m.setFile(new File(getPath(folder)));
        return m;
    }
}
