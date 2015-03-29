import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

public class start {
	
	static ServerMonitor ssm;
	static ClientEmulator ce;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		//change look/feel to something less gnarly
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
			
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
			
		}
		
		
		//show start dialogue
		Object[] possibilities = {"START AS SERVER", "START AS CLIENT", "START AS BOTH"};
		String returnS = (String)JOptionPane.showInputDialog(
		                    new JFrame(),
		                    "In what mode would you like to run the program?",
		                    "",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    possibilities,
		                    "START AS SERVER");

		
		if (returnS.contains("SERVER") || returnS.contains("BOTH")) {
			ssm = new ServerMonitor();
			ssm.buildGUI();
		}
		
		if (returnS.contains("CLIENT") || returnS.contains("BOTH")) {
			ce = new ClientEmulator();
			ce.buildGUI();
		}
		
	}
	
}