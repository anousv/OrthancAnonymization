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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import ij.plugin.PlugIn;

public class ImportDCM extends JFrame implements PlugIn{
	private static final long serialVersionUID = 1L;
	private String ip;
	private String port;
	private String authentication;
	private String url;
	private String fullAddress;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");
	private JLabel state;

	public ImportDCM(){

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////// CONNECTION SETUP ////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////// END CONNECTION SETUP ////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		this.setTitle("Import DICOM files");
		JPanel mainPanel = new JPanel(new GridBagLayout());
		JLabel labelPath = new JLabel("DICOM files path");
		JTextField path = new JTextField(jpreferPerso.get("filesLocation", System.getProperty("user.dir")));
		path.setMinimumSize(new Dimension(250, 27));
		path.setMaximumSize(new Dimension(250, 27));
		path.setEditable(false);
		JButton browse = new JButton("...");
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File(jpreferPerso.get("filesLocation", System.getProperty("user.dir"))));
				chooser.setDialogTitle("Export zip to...");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					path.setText(chooser.getSelectedFile().toPath().toString());
					jpreferPerso.put("filesLocation", path.getText());
				}
			}
		});

		state = new JLabel("");

		JButton importBtn = new JButton("Import");
		importBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(path.getText().length() > 0){
					SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

						@Override
						protected Void doInBackground() throws Exception { 
							importFiles(Paths.get(jpreferPerso.get("filesLocation", System.getProperty("user.dir"))));
							return null;
						}
					};
					worker.execute();
				}
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(20, 20, 20, 20);
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainPanel.add(labelPath, gbc);

		gbc.insets = new Insets(20, 0, 20, 0);
		gbc.gridx = 1;
		gbc.gridy = 0;
		mainPanel.add(path, gbc);

		gbc.insets = new Insets(20, 0, 20, 20);
		gbc.gridx = 2;
		gbc.gridy = 0;
		mainPanel.add(browse, gbc);

		gbc.insets = new Insets(0, 0, 20, 20);
		gbc.gridx = 1;
		gbc.gridy = 1;
		mainPanel.add(state, gbc);
		
		gbc.insets = new Insets(0, 0, 20, 20);
		gbc.gridx = 1;
		gbc.gridy = 2;
		mainPanel.add(importBtn, gbc);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Image image = new ImageIcon(ClassLoader.getSystemResource("OrthancIcon.png")).getImage();
		this.setIconImage(image);
		this.getContentPane().add(mainPanel);
	}

	public void importFiles(Path path){
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				int successCount = 0;
				long totalFiles = 0;
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					System.out.println("Importing " + file);
					setUrl("instances");
					URL url2 = new URL(url);
					HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
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

					conn = (HttpURLConnection) url2.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("content-length", Integer.toString(Files.readAllBytes(file).length));
					conn.setRequestProperty("content-type", "application/dicom");

					OutputStream os = conn.getOutputStream();
					os.write((Files.readAllBytes(file)));

					conn.disconnect();
					if(conn.getResponseCode() == 200){
						System.out.println("=> Success \n");
						successCount++;
					}else{
						System.out.println("=> Failure (Is it a DICOM file ? is there a password ?)\n");
					}
					totalFiles++;
					state.setText(successCount + "/" + totalFiles + " files were imported. (Fiji>Window>Console)");
					System.out.println(successCount + "/" + totalFiles + " files were imported.");
					return FileVisitResult.CONTINUE;
				}
			});		

		} catch (MalformedURLException e) {
			System.out.println("Bad URL");
		} catch (IOException e) {
			System.out.println("=> Unable to connect (Is Orthanc running ? Is there a password ?)\n");
		}
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


	public static void main(String... args){
		ImportDCM vue = new ImportDCM();
		vue.setSize(1200,640);
		vue.setVisible(true);
		vue.pack();
	}

	@Override
	public void run(String arg0) {
		ImportDCM vue = new ImportDCM();
		vue.setSize(1200, 400);
		vue.pack();
		vue.setVisible(true);
	}
}
