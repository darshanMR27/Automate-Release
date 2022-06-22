package com.bottomline.automate.release.controller;

import com.bottomline.automate.release.Constants;
import com.bottomline.automate.release.model.AutomatePowerRelease;
import com.bottomline.automate.release.model.AutomateRelease;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.emitter.EmitterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to automate the release branching by executing the Power shell script
 *
 * @author darshan.ramaiah
 *
 */
@RestController
@Slf4j
public class AutomateReleasePowerController {

    @PostMapping("/automate/release/power/execute")
    public ResponseEntity<AutomatePowerRelease> releasePowerAutomation(@RequestParam(Constants.PROJECT) String projectName,
                                                                  @RequestParam(Constants.FIX_VERSION) String fixVersion,
                                                                  @RequestParam(Constants.ISSUE_TYPE) String issueType,
                                                                  @RequestParam(Constants.STATUS) String status) {

        log.info("Project = " + projectName + ", Fix version = " + fixVersion + ", Issue Type = " + issueType + ", Status = " + status);
        PowerShell powerShell = null;
        PowerShellResponse response = null;
        String scriptParams = null;
        try {
            powerShell = PowerShell.openSession();
            Map<String, String> config = new HashMap<>();
            config.put(Constants.POWER_SHELL_MAX_WAIT_CONFIG, Constants.POWER_SHELL_MAX_WAIT_VALUE);
            //Read the resource
            BufferedReader srcReader = new BufferedReader(
                    new InputStreamReader(getClass().getClassLoader().getResourceAsStream(Constants.POWER_SHELL_SCRIPT)));
            srcReader.readLine();

            if (scriptParams != null && !scriptParams.equals("")) {
                response = powerShell.executeScript(srcReader, scriptParams);
            } else {
                response = powerShell.configuration(config).executeScript(srcReader);
            }

            if (response.isError()) {
                throw new EmitterException("WMI operation finished in error: "
                        + response.getCommandOutput());
            }
            log.info("Response Command Output = " + response.getCommandOutput());
        } catch (PowerShellNotAvailableException | IOException ex) {
            log.error("Exception while executing the power shell script", ex);
            return new ResponseEntity<>(buildReponse(response), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return new ResponseEntity<>(buildReponse(response), HttpStatus.OK);
    }

    /**
     * Method to build response
     *
     * @param psResponse
     * @return
     */
    private AutomatePowerRelease buildReponse(PowerShellResponse psResponse) {
        AutomatePowerRelease automateRelease = new AutomatePowerRelease();
        if (null != psResponse) {
            String[] cirNumberArray = psResponse.getCommandOutput().split(Constants.WHITE_SPACE);
            automateRelease.setCirNumberList(Arrays.stream(cirNumberArray).collect(Collectors.toList()));
        }
        return automateRelease;
    }
}
