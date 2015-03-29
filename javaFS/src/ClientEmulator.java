import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleConstants;
import javax.swing.UnsupportedLookAndFeelException;

public class ClientEmulator {
	//console output field and holders for ip address and port number
	final JTextArea cOutArea = new JTextArea();
	String ip = new String();
	String portS = new String();
	int portI;
	
	//reader and writer stuff for server client communication
	BufferedReader reader;
	PrintWriter writer;
	Socket socket;
	
	//so we know when the user intentionally closed the socket prematurely 
	boolean stopRequested = false;
	
		
	/*
	 * builds GUI and displays it to the user
	 */
	public void buildGUI()  {
		//create console window 
		JFrame frame = new JFrame("Client Emulator");
		frame.setSize(640, 480);
		frame.setLocation(650, 0);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);	
		
		//create consoleOUT panel
		JPanel cOutPanel = new JPanel();
		cOutPanel.setLayout(new BorderLayout());
		cOutArea.setBackground(Color.black);
		cOutArea.setForeground(Color.orange);
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
				"Client Emulator\n" +
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
		cInArea.setForeground(Color.orange);
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
					start.ce.writeToConsole(response);
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
		
			case HELP: 
				return ("\n\tSEND: this command will cause a message to be sent to the Socket Server Monitor.\n" +
						"\t\t syntax: SEND [IP ADDRESS] [PORT NUMBER] [MESSAGE TO BE SENT] \n\n" +
						"\tGET: is a message sent to the server requesting a file.\n" +
						"\t\t syntax SEND [IP ADDRESS] [PORT NUMBER] GET [FILE NAME] \n\n" +
						"\tSTOP: stops a GET operation (e.g. halts a download in progress)\n\n" +
						"\tHELP: displays this helpful message.\n\n"	
						);
				
			case SEND: 
				int returnVal = sendMessage(input);
			
				if (returnVal == -1) {
					return ("ERROR PARSING " + "'" + input + "'" + ". ARE THE PARAMETERS VALID AND PROPERLLY FORMATTED?");
				}
				else {			
					return ("MESSAGE SENT TO " + ip + " " + portS);
				}
				
			case STOP:			
				if (socket != null && socket.isClosed() == false) {
					try {
						socket.close();
						stopRequested = true;
						return ("DOWNLOAD CANCLELLED!");
					} catch (IOException e) {
						return (e.toString());
					}
				}
				else {
					return ("NO DOWNLOAD OPERATION TO STOP!");
				}
				
				
			
			default: return ("'" + input + "'" + " IS AN UNKNOWN COMMAND \n");
		
		}
		
		
		
	}
	
	
	
	
	/*
	 * enumeration of all possible commands
	 */
	public enum commands {
		HELP,
		SEND,
		STOP,
		UNKNOWN;
		
		
		
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
	 * opens up a socket and sends the message
	 */
	public int sendMessage(String input)	{
		
		//do some format checking and input string parsing
		int spaceIndex = input.indexOf(" ");
		if (spaceIndex < 0)
			return -1;
		
		input = input.substring((spaceIndex + 1));
		spaceIndex = input.indexOf(" ");
		if (spaceIndex < 0)
			return -1;
		
		int spaceIndex2 = input.indexOf(" ", spaceIndex + 1);
		if (spaceIndex2 < 0)
			return -1;
		ip = input.substring(0, spaceIndex);
		portS = input.substring(spaceIndex + 1, spaceIndex2);
		final String message = input.substring(spaceIndex2 + 1); 

		if (util.validIP(ip) == false)
			return -1;
		
		try {
			portI = Integer.parseInt(portS);
		} catch (NumberFormatException e) {
			return -1;
		}
		
		//if we're here there are no obvious problems with the input parameters. 
		try {		
			SwingWorker worker = new SwingWorker() {
				
				public Integer doInBackground() throws Exception {	
					
					//setup buffer vars
					byte[] msgBufferTemp = message.getBytes();
					byte[] msgBuffer = util.sizeInt2ByteArr(message.length());					
					
					//insert length into first element of msgBuffer array
					for (int i = 10; i < (msgBufferTemp.length + 10); i ++) {
						msgBuffer[i] = msgBufferTemp[i - 10];
					}
					
					//create socket and send request
					try {				
						socket = new Socket(ip, portI);
						InputStream inStream = socket.getInputStream();
						OutputStream outStream = socket.getOutputStream();
						outStream.write(msgBuffer);
						
						//get message size and parse what the client sends 				
						byte[] msgSizeBuffer = new byte[8];
						byte[] msgTypeBuffer = new byte[2];
						inStream.read(msgSizeBuffer);
						int msgSize = util.sizeArr2Int(msgSizeBuffer);
						inStream.read(msgTypeBuffer); 
						int bytesRead = 0;
						//msgBuffer = new byte[msgSize];
						
						//0x53 = "S" for String
						//0x46 = "F" for File
						if (msgTypeBuffer[0] == 0x53) {
						
							while (bytesRead < msgSize) {
								msgBuffer = new byte[msgSize];
								bytesRead = bytesRead + inStream.read(msgBuffer);
								start.ce.writeToConsole("SERVER ACKNOWLEDGES : ~THUSLY~ : \n" + new String(msgBuffer) );
							}
						
						}
						else if (msgTypeBuffer[0] == 0x46) {
							//create temp file 
							File file = new File( new File(".").getCanonicalPath() + "//downloads//" + message.substring(message.indexOf(" ") + 1));
							FileOutputStream fileOS = new FileOutputStream(file);	
							
							//figure out packet size, set file buffer size, and figure out what 1% of the file size is (for the blinker)
							int packetSizeI = msgTypeBuffer[1] & 0x000000FF;					
							int fileInBufferSizeI = 100000; 
							int completionUnit = msgSize / 100;
							
							//tell the user what's going on
							String progressS = ("GETTING " + message.substring(message.indexOf(" ") + 1));	
							start.ce.writeToConsole(progressS + " | " + "0% ");
							
							//setup a few variables and away we go...
							int prgStrIndexI = cOutArea.getText().indexOf(progressS);
							String progressNowS;
							int lastPercentI = 0;			
							boolean firstRun = true;
				
							//loop until we know we've got all the file
							while (bytesRead < msgSize) {
								int endIndexI = progressS.length() + 5;
								String blinkerL = null;
								int currentBufferIndexI = 0;
								byte[] fileInBuffer = new byte[fileInBufferSizeI];	

								//change buffer size if we're going to go beyond the size of the file
								if ( (fileInBufferSizeI + bytesRead) > msgSize ) {
									fileInBuffer = new byte[msgSize - bytesRead];
									fileInBufferSizeI = msgSize - bytesRead;

									//ensure packetSizeI also gets updated 
									if (fileInBufferSizeI < packetSizeI) {
										packetSizeI = fileInBufferSizeI;
									}

								}
								
								//inner loop to fill the file buffer
								while (currentBufferIndexI < fileInBufferSizeI) {

									//if it's the first time through, do a byte read to shave off the dead byte in the buffer
									if (firstRun == true) {
										firstRun = false;
										inStream.read(new byte[packetSizeI]);
									}
									//otherwise read the entire packet
									else {
										
										for (int b = 0; b < packetSizeI; b ++) {
											inStream.read(fileInBuffer, currentBufferIndexI, 1);
											currentBufferIndexI ++;
											bytesRead++;
											
											//break if we know we're beyond the size of the file
											if ( currentBufferIndexI == fileInBufferSizeI) {
												break;
											}
											
										}
										
									}		
									

									
									
															
								}

								//write to the file 
								fileOS.write(fileInBuffer);
								fileOS.flush();
												
								//randomize the blinker 
								int randomI = (int) (Math.random() * 10);
					
								if (randomI < 10) {
									blinkerL = " /";
								}
								
								if (randomI < 8) {
									blinkerL = " |";
								}

								if (randomI < 6) {
									blinkerL = " \\";
								}
								
								if (randomI < 4) {
									blinkerL = " -";
								}
 				
								//figure out how much of the file we've received
								Integer percentDoneI = bytesRead / completionUnit;
								int endIndexModifierI = 0;
								
								if (endIndexModifierI < percentDoneI.toString().length() - 1 ) {						
									endIndexModifierI = percentDoneI.toString().length() - 1;
									endIndexI = endIndexI + endIndexModifierI;
								}

								//catches a calculation error when handling files smaller than the current packet size
								if (percentDoneI > 100) {
									percentDoneI = 100;
								}
								
								progressNowS = progressS + blinkerL + " " + percentDoneI  + "%";
								
								//no need to update the console too much
								//if ((percentDoneI - lastPercentI) >= 1) {
									lastPercentI = percentDoneI;
									start.ce.cOutArea.replaceRange(progressNowS, prgStrIndexI, prgStrIndexI + endIndexI);
								//}
								/*else*/ if (percentDoneI == 100) {
									start.ce.cOutArea.replaceRange(progressNowS, prgStrIndexI, prgStrIndexI + endIndexI);
								}
								
							

							}
							
							//close everything up and tell the user we've got the file
							fileOS.close();
							start.ce.cOutArea.append("\n");
							start.ce.writeToConsole("done!");
							
												
						}
						
						socket.close();
						
					} catch (IOException e) {
						
						if (stopRequested == true) {
							stopRequested = false;
						}
						else {
							start.ce.writeToConsole(e.toString());
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