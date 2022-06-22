package com.bottomline.automate.release.controller;

import com.bottomline.automate.release.Constants;
import com.bottomline.automate.release.config.AbstractTest;
import com.bottomline.automate.release.model.AutomateRelease;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
class AutomateReleaseControllerTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    void test_automateRelease_Success() throws Exception {
        String project = "CIR";
        String fixVersion = "\"Apollo 2.7\"";
        String issueType = "Story,bug";
        String jqlStatus = "Closed,Open,\"In Progress\"";

        String uri = Constants.AUTOMATE_RELEASE_URL;
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(uri)
                .queryParam("project", project)
                        .queryParam("fixVersion" , fixVersion)
                        .queryParam("issueType", issueType)
                        .queryParam("status", jqlStatus)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status);
        String content = mvcResult.getResponse().getContentAsString();
        AutomateRelease automateRelease = super.mapFromJson(content, AutomateRelease.class);
        assertTrue(automateRelease.getCirNumberList().size() > 0);
    }

    @Test
    void test_automateRelease_NoContent() throws Exception {
        String project = "CIR";
        String fixVersion = "\"Apollo 2.7\"";
        String issueType = "Story,bug";
        String jqlStatus = "Closed";

        String uri = Constants.AUTOMATE_RELEASE_URL;
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(uri)
                .queryParam("project", project)
                .queryParam("fixVersion" , fixVersion)
                .queryParam("issueType", issueType)
                .queryParam("status", jqlStatus)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.NO_CONTENT.value(), status);
        String content = mvcResult.getResponse().getContentAsString();
        AutomateRelease automateRelease = super.mapFromJson(content, AutomateRelease.class);
        assertTrue(automateRelease.getCirNumberList() == null);
    }

    @Test
    void test_automateRelease_InternalServerError() throws Exception {
        String project = "CIR";
        String fixVersion = "Apollo 2.7";
        String issueType = "Story,bug";
        String jqlStatus = "Closed";

        String uri = Constants.AUTOMATE_RELEASE_URL;
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(uri)
                .queryParam("project", project)
                .queryParam("fixVersion" , fixVersion)
                .queryParam("issueType", issueType)
                .queryParam("status", jqlStatus)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status);
    }
}