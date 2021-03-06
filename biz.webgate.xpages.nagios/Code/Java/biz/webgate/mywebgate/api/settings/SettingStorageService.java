/*
 * � Copyright WebGate Consulting AG, 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package biz.webgate.mywebgate.api.settings;

import biz.webgate.xpages.util.logging.LogSettings;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Session;
import lotus.domino.View;

public class SettingStorageService {
	private static SettingStorageService m_Service;

	private SettingStorageService() {

	}

	public static SettingStorageService getInstance() {
		if (m_Service == null) {
			m_Service = new SettingStorageService();
		}
		return m_Service;
	}

	public ApplicationSettings getApplicationSettings() {
		ApplicationSettings asRC = new ApplicationSettings();
		MyWebGateTargetDB mwgSetting = getMWGDbSettings();
		if (mwgSetting.isAvailable()) {
			asRC.setPath(mwgSetting.getPath());
			asRC.setServer(mwgSetting.getServer());
			asRC.setAvailable(mwgSetting.isAvailable());
			try {
				Database ndbMWG = ExtLibUtil.getCurrentSession().getDatabase(mwgSetting.getServer(), mwgSetting.getPath());
				getSettingsFromMyWebGate(ndbMWG, asRC);
				ndbMWG.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return asRC;
	}

	private void getSettingsFromMyWebGate(Database ndbMyWebGate, ApplicationSettings apSettings) {
		try {
			View viwSetup = ndbMyWebGate.getView("vwLUPSetup");
			Document docSetup = viwSetup.getFirstDocument();
			if (docSetup != null) {
				apSettings.setExternalCertifier(docSetup.getItemValueString("ExternalCertifierT"));
				apSettings.setInternalCertifier(docSetup.getItemValueString("InternalCertifierT"));
				apSettings.setGlobalID(docSetup.getItemValueString("GlobalIDT"));
				apSettings.setSystemPrefix(docSetup.getItemValueString("SystemPrefixT"));
				apSettings.setFQDN(docSetup.getItemValueString("FQDNT"));
				apSettings.setSFN(docSetup.getItemValueString("SendFriendRequestT"));
				docSetup.recycle();
			}
			viwSetup.recycle();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public MyWebGateTargetDB getMWGDbSettings() {
		MyWebGateTargetDB mwgTarget = new MyWebGateTargetDB();
		try {
		//	System.out.println("LENA: " +  ExtLibUtil.getCurrentSessionAsSigner());
			View viwSettings = ExtLibUtil.getCurrentSession().getCurrentDatabase().getView("vwLUPSettings");
			Document docSettings = viwSettings.getDocumentByKey("MWGDbSettings", true);
			//Document docProfile = ExtLibUtil.getCurrentSessionAsSigner().getCurrentDatabase().getProfileDocument("myWebGate", "default");
			if (docSettings != null){
				if (docSettings.hasItem("ServerT") && docSettings.hasItem("PathT")) {
					mwgTarget.setServer(docSettings.getItemValueString("ServerT"));
					mwgTarget.setPath(docSettings.getItemValueString("PathT"));
					Database ndbMyWebGate = ExtLibUtil.getCurrentSession().getDatabase(mwgTarget.getServer(), mwgTarget.getPath());
					if (ndbMyWebGate != null && ndbMyWebGate.isOpen()) {
						mwgTarget.setAvailable(true);
						ndbMyWebGate.recycle();
					}
				}
				docSettings.recycle();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mwgTarget;
	}

	public void saveMWGDbSettings(MyWebGateTargetDB mwgTarget) {
		try {
			//Document docProfile = ExtLibUtil.getCurrentSessionAsSigner().getCurrentDatabase().getProfileDocument("myWebGate", "default");
			View viwSettings = ExtLibUtil.getCurrentSessionAsSigner().getCurrentDatabase().getView("vwLUPSettings");
			Document docSettings = viwSettings.getDocumentByKey("MWGDbSettings", true);
			if(docSettings == null){
				docSettings = ExtLibUtil.getCurrentSessionAsSigner().getCurrentDatabase().createDocument();
				docSettings.replaceItemValue("Form", "frmSettings");
				docSettings.replaceItemValue("Type", "MWGDbSettings");
			}
			
			docSettings.replaceItemValue("ServerT", mwgTarget.getServer());
			docSettings.replaceItemValue("PathT", mwgTarget.getPath());
			docSettings.save(true, false, true);
			docSettings.recycle(); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LogSettings getLogSettings(Session sesCurrent) {
		LogSettings logSettings = new LogSettings();
		try {
			//Document docSetup = sesCurrent.getCurrentDatabase().getProfileDocument("myWebGate", "logging");
			View viwSettings = ExtLibUtil.getCurrentSession().getCurrentDatabase().getView("vwLUPSettings");
			Document docSettings = viwSettings.getFirstDocument();
			if (docSettings != null){
				if (docSettings.hasItem("logLevelN")) {
					logSettings.setLogLevel(docSettings.getItemValueInteger("logLevelN"));
					logSettings.setLogLevelConsole(docSettings.getItemValueInteger("logLevelConsoleN"));
					logSettings.setLogLevelFile(docSettings.getItemValueInteger("logLevelFileN"));
				}
				docSettings.recycle();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return logSettings;
	}

	public boolean saveLogSettings(LogSettings logCurrent, Session sesCurrent) {
		boolean rc = false;
		try {
			//Document docSetup = sesCurrent.getCurrentDatabase().getProfileDocument("myWebGate", "logging");
			View viwSettings = ExtLibUtil.getCurrentSessionAsSigner().getCurrentDatabase().getView("vwLUPSettings");
			Document docSettings = viwSettings.getDocumentByKey("Logging", true);
			if(docSettings == null){
				docSettings = ExtLibUtil.getCurrentSessionAsSigner().getCurrentDatabase().createDocument();
				docSettings.replaceItemValue("Form", "frmSettings");
				docSettings.replaceItemValue("Type", "Logging");
			}
			
			docSettings.replaceItemValue("logLevelN", new Integer(logCurrent.getLogLevel()));
			docSettings.replaceItemValue("logLevelConsoleN", new Integer(logCurrent.getLogLevelConsole()));
			docSettings.replaceItemValue("logLevelFileN", new Integer(logCurrent.getLogLevelFile()));

			rc = docSettings.save(true, false, true);
			docSettings.recycle();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return rc;

	}

}
