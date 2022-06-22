package com.bottomline.automate.release.controller;

import com.bottomline.automate.release.Constants;
import com.bottomline.automate.release.model.AutomateRelease;
import com.bottomline.automate.release.model.IssueInfo;
import com.bottomline.automate.release.service.AutomateReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;

/**
 * Class is to fetch JIRA project issue list by Automate release branching
 *
 * @author darshan.ramaiah
 */
@RestController
@Slf4j
public class AutomateReleaseController {

    @Autowired
    private AutomateReleaseService automateReleaseService;

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String secretKey;

    /**
     * Method to execute the release automation using the JQL query
     *
     * @param project
     * @param fixVersion
     * @param issueType
     * @param status
     * @return
     */
    @GetMapping(Constants.AUTOMATE_RELEASE_URL)
    public ResponseEntity<AutomateRelease> automateRelease(@QueryParam(Constants.PROJECT) String project,
                                                           @QueryParam(Constants.FIX_VERSION) String fixVersion,
                                                           @QueryParam(Constants.ISSUE_TYPE) String issueType,
                                                           @QueryParam(Constants.STATUS) String status) throws IOException {
        log.info("AutomateReleaseController: releaseAutomation: Entry");
        AutomateRelease response = new AutomateRelease();
        log.debug("Project = " + project + ", Fix Version = " + fixVersion + ", Issue Type = " + issueType + ", Status = " + status);
        try {
            List<IssueInfo> cirIssueList = automateReleaseService
                    .automateRelease(project, fixVersion, issueType, status, username, secretKey);
            if (!CollectionUtils.isEmpty(cirIssueList)) {
                if (cirIssueList.size() == 1) {
                    if (cirIssueList.get(0).getTickets() == null) {
                        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
                    } else {
                        response.setCirNumberList(cirIssueList);
                    }
                } else {
                    response.setCirNumberList(cirIssueList);
                }
            } else {
                return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
            }
        } catch (Exception ex) {
            log.error("Exception while automating the release checklist", ex);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("AutomateReleaseController: releaseAutomation: Exit");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
