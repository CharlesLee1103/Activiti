package org.activiti.spring.boot.process;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNSignalEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.event.impl.BPMNSignalReceivedEventImpl;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeEvents {

    private static final String SINGLE_TASK_PROCESS = "SingleTaskProcess";
    
    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Before
    public void init() {
        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;
        //Reset event collections
        RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance.clear();
        RuntimeTestConfiguration.sequenceFlowTakenEvents.clear();
        RuntimeTestConfiguration.signalReceivedEvents.clear();
    }

    @Test
    public void shouldGetSameProcessInstanceIfForAllSequenceFlowTakenEvents(){

        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name",
                        "peter")
                .build());
        //then
        assertThat(RuntimeTestConfiguration.sequenceFlowTakenEvents)
                .extracting(event -> event.getProcessInstanceId())
                .contains(categorizeProcess.getId());
    }

    @Test
    public void shouldGetSameProcessInstanceIfForAllVariableCreatedEvents(){

        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name",
                        "peter")
                .build());
        //then
        assertThat(RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance)
                .extracting(event -> event.getProcessInstanceId())
                .contains(categorizeProcess.getId());
    }

    @Test
    public void shouldGetJustOneVariableCreatedEvent(){
        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name",
                        "peter")
                .build());
        //then
        assertThat(RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance)
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    public void shouldGetJustThreeVariableCreatedEvent(){
        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariables(new HashMap<String, Object>() {{
                    put("name", "peter");
                    put("surname", "peterson");
                    put("age", 25);
                }})
                .build());
        //then
        assertThat(RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance)
                .isNotEmpty()
                .hasSize(3);
    }
    
    @Test
    public void shouldGetSameProcessInstanceIfForAllSignalReceivedEvents(){

        //given
        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey("processWithSignalStart1")
                .build());
        
        SignalPayload signalPayload = new SignalPayload("The Signal", null);
        processRuntime.signal(signalPayload);
        
        
        //then
        assertThat(RuntimeTestConfiguration.signalReceivedEvents)
        .isNotEmpty()
        .hasSize(1);
        
        BPMNSignalReceivedEvent event = RuntimeTestConfiguration.signalReceivedEvents.iterator().next();
        
        assertThat(event.getEventType()).isEqualTo(BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED);
        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo("The Signal");     
        
    }
    
}
