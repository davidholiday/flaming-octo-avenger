import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.print.DocFlavor.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

public class ServerMonitor {
	//console output field, port number, ServerSocket, and packet size variables
	JTextArea cOutArea = new JTextArea();
	String portS = new String();
	int portI;
	ServerSocket serverSock;
	int packetSizeI = 128;
		
	//boolean to stop erroneous error message from popping up on the server console
	boolean closeRequested = false;

		
	
	
	/*
	 * builds GUI and displays it to the user
	 */
	public void buildGUI()  {
		//create console window 
		JFrame frame = new JFrame("Server Monitor");
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setLocation(0, 0);
		
		//create consoleOUT panel
		JPanel cOutPanel = new JPanel();
		cOutPanel.setLayout(new BorderLayout());
		cOutArea.setBackground(Color.black);
		cOutArea.setForeground(Color.GREEN);
		cOutArea.setEditable(false);
		cOutArea.setLineWrap(true);
		cOutArea.setFont(new Font ("Andale Mono", Font.PLAIN, 12));
	
		//because stupid windows stopped supporting Andale Mono
		if (System.getProperty("os.name").contains("Windows")) {
			cOutArea.setFont(new Font ("Lucida Console", Font.PLAIN, 12));
		}
		
		cOutArea.setWrapStyleWord(true);
		DefaultCaret caret = (DefaultCaret)cOutArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		cOutArea.append(
				"-----------------------------------------------------\n" +	
				"Server Monitor\n" +
				"v1 31JAN14\n" +
				"David Holiday (Neuburger) \n" +
				"TYPE [HELP] FOR ASSITANCE \n" +
				"-----------------------------------------------------\n\n"			
				);
		
		JScrollPane cOutScroll = new JScrollPane(cOutArea);
		cOutPanel.add(cOutScroll, BorderLayout.CENTER);
		
		//create consoneIN panel
		JPanel cInPanel = new JPanel();
		cInPanel.setLayout(new BorderLayout());
		final JTextArea cInArea = new JTextArea();
		cInArea.setBackground(Color.black);
		cInArea.setForeground(Color.GREEN);
		cInArea.setFont(new Font ("Andale Mono", Font.PLAIN, 12));
		
		//because stupid windows stopped supporting Andale Mono
		if (System.getProperty("os.name").contains("Windows")) {
			cInArea.setFont(new Font ("Lucida Console", Font.PLAIN, 12));
		}
		
		cInPanel.add(cInArea, BorderLayout.CENTER);
		JButton subButton = new JButton("SUBMIT");	
		frame.getRootPane().setDefaultButton(subButton);
		subButton.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
					String input = cInArea.getText();
					cInArea.setText("");
					String response = inputHandler(input);
					start.ssm.writeToConsole(response);
					cInArea.requestFocus();
				}
			
		});
		
		cInPanel.add(subButton, BorderLayout.EAST);
			
		
		//add console panel to new split pane
		JSplitPane jsPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cOutScroll, cInPanel);
		jsPane.setDividerLocation(400);
		
		//add it all to the frame and display frame to user
		frame.add(jsPane);
		frame.setVisible(true);
		cInArea.requestFocus();	
		
	}
	
	
	
	
	/*
	 * handles input from the user
	 */
	public String inputHandler(String input) {
		commands cmd = commands.valueOf("UNKNOWN");
		
		if (commands.isMember(input) == true) {
			int spaceIndex = input.indexOf(" ");	
			String inputFirstWord = input;
			
			if (spaceIndex > -1) 
				inputFirstWord = input.substring(0, input.indexOf(" "));
			
			cmd = commands.valueOf(inputFirstWord.toUpperCase());
		}		
	
		switch (cmd) {
		
			//display help message
			case HELP: 
				return ("\n\tLISTEN: tells the server to start listening at the specified port number.\n" +
						"\t\t syntax: LISTEN [PORT NUMBER]\n\n" +
						"\tCLOSE: tells the server to stop listening and close whatever port may have been opened.\n\n" +
						"\tLIST: displays a list of files available to download.\n\n" +
						"\tSETPS: sets the packet size used when serving up files from 1 - 255 bytes.\n" +
						"\t\t syntax: SETPS [PACKET SIZE IN BYTES]\n\n" +
						"\tHELP: displays this helpful message.\n\n"	
						);
			
				
			//open a port and listen for connections
			case LISTEN: 
				int returnVal = openPort(input);
			
				if (returnVal == -1) {
					return ("ERROR PARSING " + "'" + input + "'" + ". ARE THE PARAMETERS VALID AND PROPERLLY FORMATTED?");
				}
				else {
					return ("LISTENING AT: " + portS);
				}
				
			
			//return a list of available files
			case LIST:
			
			try {
				File folder = new File( new File(".").getCanonicalPath() + "//file_repository");
				File[] fileNameArr = folder.listFiles();
				StringBuffer returnSB = new StringBuffer();
				
				for (int i = 0; i < fileNameArr.length; i++) {
					returnSB.append("\n\t" + fileNameArr[i].getName() + " ... " + util.readableFileSize(fileNameArr[i].length()));
				}
				return returnSB.toString();
				
			} catch (IOException e) {
				return e.toString();
			}

				
			//closes any ports that may have been opened
			case CLOSE:
				
				if (serverSock != null && serverSock.isClosed() == false) {
					try {
						closeRequested = true;
						serverSock.close();
						return ("SOCKET AT " + portS + " CLOSED!");
					} catch (IOException e) {
						return (e.toString());
					}
				}
				else {
					return ("ALL PORTS ALREADY CLOSED!");
				}
				
				
			case SETPS: 
				
				try {
					int proposedPacketSizeI = Integer.parseInt(input.substring(input.indexOf(" ") + 1));
					
					if (proposedPacketSizeI > 0  && proposedPacketSizeI < 256) { 
						packetSizeI = proposedPacketSizeI;
						return ("PACKET SIZE SET TO: " + proposedPacketSizeI);
					}
					else {
						return ("INVALID PACKET SIZE. TRY SOMETHING BETWEEN 1 AND 255");  
					}		
				} catch (NumberFormatException e) {
					return ("INVALID PACKET SIZE. TRY SOMETHING BETWEEN 1 AND 255");
				}
							
				
			//return this if the command is unrecognized 
			default: return ("'" + input + "'" + " IS AN UNKNOWN COMMAND \n");
		
		}
		
		
		
	}
	
	
	
	
	/*
	 * enumeration of all possible commands
	 */
	public enum commands {
		HELP,
		LISTEN,
		LIST,
		GET,
		CLOSE,
		SETPS, 
		UNKNOWN;
		
		//lets us know if a given command is in the enum list
		static public boolean isMember(String cmdIn) {
			int spaceIndex = cmdIn.indexOf(" ");	   
			if (spaceIndex > -1) 
				cmdIn = cmdIn.substring(0, cmdIn.indexOf(" "));
			cmdIn = cmdIn.toUpperCase();
			commands[] commandsArr = commands.values();
			boolean returnVal = false;

			for (int i = 0; i < commandsArr.length; i ++) {

				if (commandsArr[i].toString().equals(cmdIn))
					returnVal = true;

			}

			return returnVal;
		}

		
	}
	
	
	
	
	
	/*
	 * opens up a socket 
	 */
	public int openPort(String input)	{
		
		//do some format checking and input string parsing
		int spaceIndex = input.indexOf(" ");
		if (spaceIndex < 0)
			return -1;
		
		portS = input.substring((spaceIndex + 1));		

		try {
			portI = Integer.parseInt(portS);
		} catch (NumberFormatException e) {
			return -1;
		}
		
		//if we're here there are no obvious problems with the input parameters. 
		try {		
			SwingWorker worker = new SwingWorker() {
				
				public Integer doInBackground() {				
					try {
						//open socket
						serverSock = new ServerSocket (portI);
						
						//setup buffer vars
						byte[] msgSizeBuffer = new byte[8];
						byte[] msgTypeBuffer = new byte[2];
						int msgSize;
						
						//accept and service clients (that sounds bad...) 
						while (true) {
							//setup sockets 
							Socket clientSock = serverSock.accept();
							SocketAddress clientAddress = clientSock.getRemoteSocketAddress();
							start.ssm.writeToConsole("SERVICING CLIENT AT: " + clientAddress.toString().substring(1));
							InputStream inStream = clientSock.getInputStream();
							OutputStream outStream = clientSock.getOutputStream();
							
							//get message size, message type, and parse what the client sends 
							inStream.read(msgSizeBuffer);
							msgSize = util.sizeArr2Int(msgSizeBuffer);
							inStream.read(msgTypeBuffer); //SERVER CAN SAFELY ASSUME THE MESSAGE TYPE IS STRING FOR NOW
							int bytesRead = 0;
							byte[] msgBuffer = new byte[msgSize];
				
							while (bytesRead < msgSize) {	
								bytesRead = bytesRead + inStream.read(msgBuffer);
								start.ssm.writeToConsole("MESSAGE FROM CLIENT: " + new String(msgBuffer));
							}
							
							//check the command sent by the client, and return the appropriate data (either a file or a text response)
							String command = new String(msgBuffer);
							int spaceIndexI = command.indexOf(" ");
							String fileNameS = null;
							
							/*
							 * FUTURE ENHANCEMENT - ADD METHOD TO UTIL CLASS THAT PARSES OUT ALL THE PARTS OF A CLIENT
							 * MESSAGE AND RETURNS A STRING ARRAY 
							 */
							if (spaceIndexI > -1) {
								fileNameS = command.substring(spaceIndexI + 1);
								command = command.substring(0, spaceIndexI);
							}
				
							if (command.toUpperCase().equals("GET") == false) {
								outStream.write(util.Object2ByteArr(inputHandler(new String(msgBuffer))));
							}
							else {
								
								try {
									File file = new File( new File(".").getCanonicalPath() + "//file_repository//" + fileNameS);
									FileInputStream fileIS = new FileInputStream(file);
							
									for (int i = 0; i < file.length(); i ++) {
										
										//fire off header packet, describing message and packet size. 
										byte[] packetArr = util.sizeInt2BytePacketArr((int)file.length(), packetSizeI);
										
										 //if it's not the first run through, send only data. else, send only header info
										if (i > 0) {
											packetArr = new byte[packetSizeI];
											fileIS.read(packetArr);
											outStream.write(packetArr);	
										}
										else {
											outStream.write(packetArr);
										}
										
									}
									
									fileIS.close();
						
								} catch (IOException e) {
									
									if (e.toString().contains("Broken pipe") == true) {
										clientSock.close();
										//start.ssm.writeToConsole("DONE SERVICING CLIENT AT: " + clientAddress.toString().substring(1));
									}
									else if (e.toString().contains("Software caused connection abort") == true) { //bug in win8x only
										clientSock.close();
									}
									else {
										outStream.write(util.Object2ByteArr(inputHandler(e.toString())));
										start.ssm.writeToConsole(e.toString());
									}
									
								} catch (Exception e) {
									
									if (e.toString().contains("Broken pipe") == true) {
										clientSock.close();
										//start.ssm.writeToConsole("DONE SERVICING CLIENT AT: " + clientAddress.toString().substring(1));
									}
									else if (e.toString().contains("Software caused connection abort") == true) { //bug in win8x only
										clientSock.close();
									}
									else {
										outStream.write(util.Object2ByteArr(inputHandler(e.toString())));
										start.ssm.writeToConsole(e.toString());
									}
								}
								
								
								
								
								
							}
							
							
							clientSock.close();
							start.ssm.writeToConsole("DONE SERVICING CLIENT AT: " + clientAddress.toString().substring(1));
							
						}
						
						
					} catch (IOException e) {
						
						//only throw an error if we didn't intend to close the socket
						if (closeRequested == true) {
							closeRequested = false;
						}
						else {
							start.ssm.writeToConsole(e.toString());
						}
					}
					
					
					return 0;
				}
				
			};

			worker.execute();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
				
		
		return 0;
		
	}
	
	
	


	
	/*
	 * writes to the output field
	 */
	public void writeToConsole(String msg) {
		this.cOutArea.append(util.getTime() + " " + msg + "\n"); 
	}
	
	

	
	

	
	
	
}