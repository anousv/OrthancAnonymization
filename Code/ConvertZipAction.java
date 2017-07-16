/**
Copyright (C) 2017 VONGSALAT Anousone

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.imagej;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConvertZipAction{
	private StringBuilder ids;
	private Path path;
	private String url;
	private String ip;
	private String port;
	private String fullAddress;
	private String authentication;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");
	private Preferences jpreferAnon = Preferences.userRoot().node("<unnamed>/anonPlugin");
	private ArrayList<String> zipContent;
	private JButton exportZip;
	private JLabel state;
	private JButton removeFromZip;
	private JButton storeBtn;
	private JButton displayAnonTool;
	private JPanel oToolRight;
	private JPanel anonTablesPanel;
	private boolean[] choix = {false};
	private JComboBox<Object> zipShownContent;
	private ArrayList<String> zipShownContentList;
	private String setupPath;
	private File f;
	private boolean[] temporary = {false};
	
	public ConvertZipAction(String setupPath, ArrayList<String> zipContent, boolean temporary){
		int curDb = jprefer.getInt("current database", 0);
		int typeDb = jprefer.getInt("db type" + curDb, 5);
		if(typeDb == 5){
			if(!jprefer.get("db path" + curDb, "none").equals("none")){
				String pathBrut = jprefer.get("db path" + curDb, "none") + "/";
				int index = ordinalIndexOf(pathBrut, "/", 3);
				this.fullAddress = pathBrut.substring(0, index);
			}else{
				String address = jprefer.get("ODBC" + curDb, "localhost");
				String pattern1 = "@";
				String pattern2 = ":";
				Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
				Matcher m = p.matcher(address);
				while(m.find()){
					this.ip = "http://" + m.group(1);
				}
			}
			if(jprefer.get("db user" + curDb, null) != null && jprefer.get("db pass" + curDb, null) != null){
				authentication = Base64.getEncoder().encodeToString((jprefer.get("db user" + curDb, null) + ":" + jprefer.get("db pass" + curDb, null)).getBytes());
			}
		}else{
			this.ip = jpreferPerso.get("ip", "http://localhost");
			this.port = jpreferPerso.get("port", "8042");
			if(jpreferPerso.get("username", null) != null && jpreferPerso.get("username", null) != null){
				authentication = Base64.getEncoder().encodeToString((jpreferPerso.get("username", null) + ":" + jpreferPerso.get("password", null)).getBytes());
			}
		}
		this.setupPath = setupPath;
		this.zipContent = zipContent;
		this.temporary[0] = temporary;
	}

	public ConvertZipAction(JComboBox<Object> zipShownContent, ArrayList<String> zipShownContentList, 
			ArrayList<String> zipContent, JButton exportZip, JLabel state, JPanel oToolRight,
			JButton displayAnonTool, JPanel anonTablesPanel, JButton removeFromZip, JButton storeBtn){
		int curDb = jprefer.getInt("current database", 0);
		int typeDb = jprefer.getInt("db type" + curDb, 5);
		if(typeDb == 5){
			if(!jprefer.get("db path" + curDb, "none").equals("none")){
				String pathBrut = jprefer.get("db path" + curDb, "none") + "/";
				int index = ordinalIndexOf(pathBrut, "/", 3);
				this.fullAddress = pathBrut.substring(0, index);
			}else{
				String address = jprefer.get("ODBC" + curDb, "localhost");
				String pattern1 = "@";
				String pattern2 = ":";
				Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
				Matcher m = p.matcher(address);
				while(m.find()){
					this.ip = "http://" + m.group(1);
				}
			}
			if(jprefer.get("db user" + curDb, null) != null && jprefer.get("db pass" + curDb, null) != null){
				authentication = Base64.getEncoder().encodeToString((jprefer.get("db user" + curDb, null) + ":" + jprefer.get("db pass" + curDb, null)).getBytes());
			}
		}else{
			this.ip = jpreferPerso.get("ip", "http://localhost");
			this.port = jpreferPerso.get("port", "8042");
			if(jpreferPerso.get("username", null) != null && jpreferPerso.get("password", null) != null){
				authentication = Base64.getEncoder().encodeToString((jpreferPerso.get("username", null) + ":" + jpreferPerso.get("password", null)).getBytes());
			}
		}
		this.oToolRight = oToolRight;
		this.storeBtn = storeBtn;
		this.removeFromZip = removeFromZip;
		this.displayAnonTool = displayAnonTool;
		this.anonTablesPanel = anonTablesPanel;
		this.zipShownContent = zipShownContent;
		this.zipShownContentList = zipShownContentList;
		this.ids = new StringBuilder();
		this.zipContent = zipContent;
		this.exportZip = exportZip;
		this.state = state;
		this.setupPath = null;
	}

	public void generateZip() throws IOException {
		// storing the IDs in a stringbuilder
		this.ids = new StringBuilder();
		this.ids.append("[");
		for(int i = 0; i < zipContent.size(); i++){
			this.ids.append("\"" + zipContent.get(i) + "\",");
		}
		ids.replace(ids.length()-1, ids.length(), "]");

		// the absence of a setupPath or not, will define whether or not a jfilechooser will be used
		DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss");
		if(setupPath != null){
			if(temporary[0]){
				f = File.createTempFile(setupPath + File.separator + df.format(new Date()), ".zip");
				f.deleteOnExit();
			}else{
				f = new File(setupPath + File.separator + df.format(new Date()) + ".zip");
			}
		}
		if(setupPath == null){
			this.choix[0] = this.fileChooser();
			f = new File(path + File.separator + df.format(new Date()) + ".zip");
		}
		
		if(!zipContent.isEmpty() && (this.setupPath != null || choix[0])){
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				if(setupPath == null){
					exportZip.setText("Generating Zip...");
					exportZip.setEnabled(false);
					storeBtn.setEnabled(false);
					removeFromZip.setEnabled(false);
					state.setText("Generating Zip...");
				}

				setUrl("tools/create-archive");
				URL url2 = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				if((fullAddress != null && fullAddress.contains("https")) || (ip != null && ip.contains("https"))){
					try{
						HttpsTrustModifier.Trust(conn);
					}catch (Exception e){
						throw new IOException("Cannot allow self-signed certificates");
					}
				}
				if(authentication != null){
					conn.setRequestProperty("Authorization", "Basic " + authentication);
				}

				OutputStream os = conn.getOutputStream();
				os.write((ids.toString()).getBytes());
				os.flush();

				is = conn.getInputStream();
				fos = new FileOutputStream(f);
				int bytesRead = -1;
				byte[] buffer = new byte[1024];
				while ((bytesRead = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if(setupPath == null){
				state.setText("<html><font color='green'>The data have successfully been exported to zip</font></html>");
				exportZip.setText("Export list to zip");
				exportZip.setEnabled(true);
				storeBtn.setEnabled(true);
				removeFromZip.setEnabled(true);
				zipShownContent.removeAllItems();
				zipShownContentList.removeAll(zipShownContentList);
				zipContent.removeAll(zipContent);
				oToolRight.setVisible(false);
				displayAnonTool.setVisible(true);
				displayAnonTool.setText("Display anonymization tool");
				anonTablesPanel.setVisible(false);
			}
		}
	}

	private boolean fileChooser(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(jpreferAnon.get("zipLocation", System.getProperty("user.dir"))));
		chooser.setDialogTitle("Export zip to...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			this.path = chooser.getSelectedFile().toPath();
			jpreferAnon.put("zipLocation", this.path.toString());
			return true;
		}
		return false;
	}

	public String getGeneratedZipPath(){
		return this.f.getAbsolutePath();
	}

	public String getGeneratedZipName(){
		return this.f.getName();
	}

	/*
	 * This method sets the url
	 */
	public void setUrl(String url) {
		if(this.fullAddress != null && !this.fullAddress.equals("none")){
			this.url = this.fullAddress + "/" + url;
		}else{
			if(this.ip != null && port != null){
				this.url = ip + ":" + port + "/" + url;
			}
		}
	}

	public int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}
}
