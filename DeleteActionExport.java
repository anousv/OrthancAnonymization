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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.SwingWorker;

public class DeleteActionExport{

	private String url;
	private String fullAddress;
	private String ip;
	private String port;
	private String authentication;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");
	private JTable tableauExportStudies;
	private TableDataExportStudies modeleExportStudies;
	private JTable tableauExportSeries;
	private TableDataExportSeries modeleExportSeries;

	public DeleteActionExport(JTable tableauExportStudies, TableDataExportStudies modeleExportStudies){
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
		this.tableauExportStudies = tableauExportStudies;
		this.modeleExportStudies = modeleExportStudies;
	}

	public DeleteActionExport(JTable tableauExportSeries, TableDataExportSeries modeleExportSeries){
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
		this.tableauExportSeries = tableauExportSeries;
		this.modeleExportSeries = modeleExportSeries;
	}

	public void delete() {
		if(tableauExportStudies != null){
			setUrl("studies/" + modeleExportStudies.getValueAt(tableauExportStudies.convertRowIndexToModel(tableauExportStudies.getSelectedRow()), 5));
		}else{
			setUrl("series/" + modeleExportSeries.getValueAt(tableauExportSeries.convertRowIndexToModel(tableauExportSeries.getSelectedRow()), 4));
		}
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() {
				URL url2;
				HttpURLConnection conn;
				try {
					url2  = new URL(url);
					conn = (HttpURLConnection) url2.openConnection();
					conn.setDoOutput(true);
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
					conn.setRequestMethod("DELETE");
					conn.getResponseCode();
					conn.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		worker.execute();
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
