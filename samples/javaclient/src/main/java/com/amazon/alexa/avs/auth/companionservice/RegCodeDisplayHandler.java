/** 
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License"). You may not use this file 
 * except in compliance with the License. A copy of the License is located at
 *
 *   http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package com.amazon.alexa.avs.auth.companionservice;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

import javax.swing.JOptionPane;

import com.amazon.alexa.avs.config.DeviceConfig;
import com.amazon.alexa.avs.ui.DialogFactory;

public class RegCodeDisplayHandler {

    private final DialogFactory dialogFactory;
    private final DeviceConfig deviceConfig;
    
    private static final String TITLE = "Login to Register/Authenticate your Device";
    private static final String VISIT_URL = "Please register your device by visiting the following URL in a web browser and follow the instructions:\n";
    private static final String CONTINUE_WHEN_DONE = "Continue only once you've authenticated with AVS.";

    public RegCodeDisplayHandler(DialogFactory dialogFactory, DeviceConfig deviceConfig) {
        this.dialogFactory = dialogFactory;
        this.deviceConfig = deviceConfig;
    }

    public void displayRegCode(String regCode) {
        String regUrl = deviceConfig.getCompanionServiceInfo().getServiceUrl() + "/provision/" + regCode;
        
        if (!handleAuthenticationOpenInBrowser(TITLE, regUrl)) {
            handleAuthenticationCopyToClipboard(TITLE, regUrl);
            dialogFactory.showInformationalDialog(TITLE, VISIT_URL + regUrl + CONTINUE_WHEN_DONE);
        }
    }
    
    private boolean isDesktopBrowserCapable() {
        return (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
    }
    
    private boolean handleAuthenticationOpenInBrowser(String title, String regUrl) {
        if (isDesktopBrowserCapable()) {
            int selected = dialogFactory.showYesNoDialog(title, VISIT_URL + regUrl
                + "\n\n Would you like to open the URL automatically in your default browser?");
            if (selected == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().browse(new URI(regUrl));
                } catch (Exception e) {
                    // Ignore and proceed
                }
                dialogFactory.showInformationalDialog(title,
                    "If a browser window did not open, please copy and paste the below URL into a "
                        + "web browser, and follow the instructions:\n" + regUrl + "\n\n " + CONTINUE_WHEN_DONE);
                return true;
            }
        }
        return false;
    }
    
    private boolean isClipboardCapable() {
        return !deviceConfig.canOnlyDoHeadless();
    }

    private boolean handleAuthenticationCopyToClipboard(String title, String regUrl) {
        if (isClipboardCapable()) {
            int selected = dialogFactory.showYesNoDialog(title, VISIT_URL + regUrl + "\n\n Would you like the URL copied to your clipboard?");
            if (selected == JOptionPane.YES_OPTION) {
                copyToClipboard(regUrl);
                return true;
            }
        }
        return false;
    }

    private void copyToClipboard(String text) {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Clipboard systemClipboard = defaultToolkit.getSystemClipboard();
        systemClipboard.setContents(new StringSelection(text), null);
    }
}