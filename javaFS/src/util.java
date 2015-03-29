import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class util {
	
	
	/*
	 * checks validity of IP Address field
	 */
	public static boolean validIP (String ip) {

		try {
	        if (ip == null || ip.isEmpty()) {
	            return false;
	        }

	        String[] parts = ip.split( "\\." );
	       
	        if ( parts.length != 4 ) {
	            return false;
	        }

	        for ( String s : parts ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }

	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
		
	}
	
	
	
	/*
	 * simple date/time parser
	 */
	public static String getTime () {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("<yyyy/MM/dd  HH:mm:ss>");
		return sdf.format(date);	
	}
	    
    
	
	/*
	 * takes an Object and packages it up into a byte array where the first 
	 * five elements of the array represent the size of the Object
	 */
	public static byte[] Object2ByteArr(Object inputO) throws IOException {
		byte[] inputA = {-1};
		
		//figure out what kind of Object we're looking at and process it 
		if (inputO.getClass().equals(String.class)) {
			String inputS = (String)inputO;
			
			//add size info to the array
			int size = inputS.length();
			inputA = sizeInt2ByteArr(size);
			
			//add data to the array
			byte[] inputTempA = inputS.getBytes();
			
			for (int i = 0; i < size; i ++) {
				inputA[i + 10] = inputTempA[i];
			}	
			
			//mark this bad boy as a string (0x53 = "S")
			inputA[8] = 0x53;
			
		}
		else if (inputO.getClass().equals(File.class)) {
			File inputF = (File)inputO;
			int size = (int)inputF.length();
			
			for (int i = 0; i < size; i ++) {

				//add size info to the array
				inputA = sizeInt2ByteArr(size);
				
				//add data to the array
				//byte[] inputTempA = new byte[size];

				//for (int i = 0; i < size; i ++) {
				//	inputA[i + 10] = inputTempA[i];
				//}	

				//mark this bad mamma jamma as a file (0x46 = "F")
				inputA[8] = 0x46;
							
			}
			
			
			
			
		}
		
		
		return inputA;
	}

	
	
	/*
	 * takes an integer, treats it as a size value, and returns a byte array of n + 8 elements.
	 * the first eight elements of the array are a byte representation of the length of the
	 * message in the array. element 0 is 16^0, element 1 is 16^1, etc. 
	 * 
	 * the remaining two cells in the header are meta information describing the type of data 
	 * contained in the array
	 */
	public static byte[] sizeInt2ByteArr(int size) {
		byte[] returnArr = new byte[size + 10/*11*/]; 
		String size16S = Integer.toHexString(size);
		int startIndex = 8 - size16S.length(); 
		
		for (int i = startIndex; i < 8; i ++) {
			returnArr[i] = (byte)size16S.charAt(i - startIndex);
		}
	
		return returnArr;
		
	}	
	
	
	/*
	 * 
	 * takes size variable and creates a packet of the format 
	 * [total message size] [message type] [packet length] [message]
	 * 
	 */
	public static byte[] sizeInt2BytePacketArr(int msgSize, int packetSize) {
		byte[] returnArr = new byte[10 + packetSize]; 
		String msgSize16S = Integer.toHexString(msgSize);
		int startIndex = 8 - msgSize16S.length(); 
		
		for (int i = startIndex; i < 8; i ++) {
			returnArr[i] = (byte)msgSize16S.charAt(i - startIndex);
		}
	
		//mark this bad mamma jamma as a file (0x46 = "F")
		returnArr[8] = 0x46;
		
		//mark the packet size
		returnArr[9] = (byte)packetSize;
		
		return returnArr;
		
	}
	
	
	
	/*
	 * takes the 8-byte array that represents, in hex, the buffer size and 
	 * returns the integer value
	 */
	public static int sizeArr2Int(byte[] msgSizeBuffer) {
		StringBuffer sizeSB = new StringBuffer();
		
		for (int i = 0; i < 8; i ++) {
			
			//check for non-alpha numeric characters (ie unicode zero)
			if ((int)msgSizeBuffer[i] > 47) {
				String appendS = Character.toString((char) (int)msgSizeBuffer[i]);
				sizeSB.append(appendS);
			}


		}
		
		return Integer.parseInt(sizeSB.toString(), 16);
		
	}
	
	
	
	/*
	 * returns a file size formatted to the correct format
	 */
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	
	
	
}