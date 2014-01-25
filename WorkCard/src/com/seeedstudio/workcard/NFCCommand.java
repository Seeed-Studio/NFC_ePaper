// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.seeedstudio.workcard;

import android.nfc.Tag;
import android.nfc.tech.NfcV;
//import android.util.Log;

public class NFCCommand {

	 //***********************************************************************/
	 //* the function send an Inventory command (0x26 0x01 0x00) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //***********************************************************************/
	 public static byte[] SendInventoryCommand (Tag myTag)
	 {
		 byte[] UIDFrame = new byte[] { (byte) 0x26, (byte) 0x01, (byte) 0x00 };
		 byte[] response = new byte[] { (byte) 0x01 };
		 
		 int errorOccured = 1;
		 while(errorOccured != 0)
		 {
			 try 
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(UIDFrame);
				 nfcvTag.close();
				 if (response[0] == (byte) 0x00) 
				 {
					//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteToString((byte) 0x26) + " " + Helper.ConvertHexByteToString((byte) 0x01) + " " + Helper.ConvertHexByteToString((byte) 0x00) );
					 errorOccured = 0;				
				 }
			 }
			 catch (Exception e) 
			 {
				 errorOccured ++;
				//Used for DEBUG : Log.i("Polling**ERROR***", "SendInventoryCommand" + Integer.toString(errorOccured));				 
				 if(errorOccured >= 2)
				 {
					//Used for DEBUG : Log.i("Exception","Inventory Exception " + e.getMessage());
					 return response;	
				 }
			}
		 }
		//Used for DEBUG : Log.i("NFCCOmmand", "Response " + Helper.ConvertHexByteToString((byte)response[0]));
		 return response;
	 }
	
	 
	//***********************************************************************/
	 //* the function send an Get System Info command (0x02 0x2B) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //***********************************************************************/
	 public static byte[] SendGetSystemInfoCommandCustom (Tag myTag, DataDevice ma)
	 {
		 int cpt = 0;		 
		 byte[] response = new byte[] { (byte) 0xAA };
		 byte[] GetSystemInfoFrame = new byte[2]; 
		 
		 // to know if tag's addresses are coded on 1 or 2 byte we consider 2  
		 // then we wait the response if it's not good we trying with 1
		 ma.setBasedOnTwoBytesAddress(true);	 
		 
		 //1st flag=1 for 2 bytes address products
		 GetSystemInfoFrame = new byte[] { (byte) 0x0A, (byte) 0x2B };	
		 while ((response == null || response[0] == 1 || response[0] == (byte)0xAA) && cpt <= 1)
		 {	 
			 try 
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(GetSystemInfoFrame);
				 nfcvTag.close();
				 if (response[0] == (byte) 0x00)
				 {
					ma.setBasedOnTwoBytesAddress(true);	//1st (flag=1) = 2 add bytes (M24LR64 FREEDOM2)
				 	return response;
				 }
			 }
			 catch (Exception e) 
			 {
				//Used for DEBUG : Log.i("Exception","Get System Info Exception " + e.getMessage());
				 cpt++;
			}
		 }

		 //2nd flag=0 for 1 byte address products
		 cpt=0;
		 GetSystemInfoFrame = new byte[] { (byte) 0x02, (byte) 0x2B }; 	
		 while ((response == null || response[0] == 1 || response[0] == (byte)0xAA) && cpt <= 1)
		 {	 
			 try 
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(GetSystemInfoFrame);
				 nfcvTag.close();
				 if (response[0] == (byte) 0x00)
				 {
					ma.setBasedOnTwoBytesAddress(false);	//1st (flag=1) = 2 add bytes (M24LR64 FREEDOM2)
				 	return response;
				 }
			 }
			 catch (Exception e) 
			 {
				//Used for DEBUG : Log.i("Exception","Get System Info Exception " + e.getMessage());
				 cpt++;
			}
		 }
		 
		//Used for DEBUG : Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response)); 
		return response;
	 }

		//***********************************************************************/
	 //* the function send an Get System Info command (0x02 0x2B) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //***********************************************************************/
	 public static byte[] SendGetSystemInfoCommandCustom_OLD (Tag myTag, DataDevice ma)
	 {
			
		 byte[] response = new byte[] { (byte) 0xAA };
		 byte[] GetSystemInfoFrame = new byte[2]; 
		 
		 // to know if tag's addresses are coded on 1 or 2 byte we consider 2  
		 // then we wait the response if it's not good we trying with 1
		 ma.setBasedOnTwoBytesAddress(true);	 
		 
		 GetSystemInfoFrame = new byte[] { (byte) 0x0A, (byte) 0x2B };	//1st flag=1
		 
		 for(int h=0; h<=1;h++)
		 {
			 try 
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(GetSystemInfoFrame);
				 nfcvTag.close();
				 if (response[0] == (byte) 0x00) 
				 {
					//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(GetSystemInfoFrame));
					 if (h == 0)
						 ma.setBasedOnTwoBytesAddress(true);	//1st (flag=1) = 2 add bytes (M24LR64 FREEDOM2)
					 else
						 ma.setBasedOnTwoBytesAddress(false);	//2nd (flag=0) = 1 add bytes (LRI M24LR04 FREEDOM1 !)
					 h = 2;// to get out of the loop
				 }
			 }
			 catch (Exception e) 
			 {
				//Used for DEBUG : Log.i("Exception","Get System Info Exception " + e.getMessage());
				//Used for DEBUG : Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
				ma.setBasedOnTwoBytesAddress(false);
			}

			//Used for DEBUG : Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
			 GetSystemInfoFrame = new byte[] { (byte) 0x02, (byte) 0x2B }; //2nd flag=0
		 }
		 return response;
	 }
	 
	 
	//***********************************************************************/
	 //* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	 public static byte[] SendReadSingleBlockCommand (Tag myTag, byte[] StartAddress,  DataDevice ma)
	 {
		 byte[] response = new byte[] {(byte) 0x0A}; 
		 byte[] ReadSingleBlockFrame;
		 
		 if(ma.isBasedOnTwoBytesAddress())
			 ReadSingleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x20, StartAddress[1], StartAddress[0]};
		 else
			 ReadSingleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x20, StartAddress[1]};

		 int errorOccured = 1;
		 while(errorOccured != 0)
		 {
			 try
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(ReadSingleBlockFrame);
				 if(response[0] == (byte) 0x00)
				 {
					 errorOccured = 0;
					//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadSingleBlockFrame));
				 }
			 }
			 catch(Exception e)
			 {
				 errorOccured++;
				//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Single Block" + Helper.ConvertHexByteArrayToString(response));
				 if(errorOccured == 2)
				 {
					//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
					 return response;
				 }
			 }
		 }
		//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Sigle Block" + Helper.ConvertHexByteArrayToString(response));
		 return response;
	 }
	 
	 
	 //***********************************************************************/
	 //* the function send an ReadSingle Custom command (0x0A 0x20) || (0x02 0x20) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	 public static byte[] SendReadMultipleBlockCommandCustom (Tag myTag, byte[] StartAddress, byte NbOfBlockToRead,   DataDevice ma)
	 {
		long cpt =0;
		boolean checkCorrectAnswer = true;
		
		//int NbBytesToRead = (NbOfBlockToRead*4)+1;
		int NbBytesToRead = NbOfBlockToRead*4;
		byte[] FinalResponse = new byte[NbBytesToRead+1];
			 
		for(int i =0;i<=(NbOfBlockToRead*4)-4; i = i+4)
		{
			byte[] temp = new byte[5];
			int incrementAddressStart0 = (StartAddress[0]+i/256)  ;								//Most Important Byte
			int incrementAddressStart1 = (StartAddress[1]+i/4) - (incrementAddressStart0*255);	//Less Important Byte
			
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 2)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},ma);
				cpt ++;
			}
			cpt =0;
			
			//Check  if Read problem
			if (temp[0] != 0x00)
				checkCorrectAnswer = false;
				
			if(i==0)
			{
				for(int j=0;j<=4;j++)
				{
					if (temp[0] == 0x00)
						FinalResponse[j] = temp[j];
					else
						FinalResponse[j] = (byte) 0xFF;
				}
			}
			else 
			{
				for(int j=1;j<=4;j++)
				{
					if (temp[0] == 0x00)
						FinalResponse[i+j] = temp[j];
					else
						FinalResponse[j] = (byte) 0xFF;
				}
			}
		}
		
		if (checkCorrectAnswer == false)
			FinalResponse[0] = (byte)0xAE;
		
		return FinalResponse;
	 }		
	 
	 public static byte[] SendReadMultipleBlockCommandCustom_JPG (Tag myTag, byte[] StartAddress, byte NbOfBlockToRead,   DataDevice ma)
	 {
		long cpt =0;
		boolean EndOfJpgFile = false;
		boolean checkCorrectAnswer = true;
		
		//int NbBytesToRead = (NbOfBlockToRead*4)+1;
		int NbBytesToRead = NbOfBlockToRead*4;
		byte[] FinalResponse = new byte[NbBytesToRead+1];
			 
		for(int i =0;i<=(NbOfBlockToRead*4)-4; i = i+4)
		{
			byte[] temp = new byte[5];
			int incrementAddressStart0 = (StartAddress[0]+i/256)  ;								//Most Important Byte
			int incrementAddressStart1 = (StartAddress[1]+i/4) - (incrementAddressStart0*255);	//Less Important Byte
			
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 2)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},ma);
				cpt ++;
			}
			cpt =0;
			
			if (temp[0] != 0x00)
				checkCorrectAnswer = false;
				
			if(i==0)
			{
				for(int j=0;j<=4;j++)
				{
					if (temp[0] == 0x00)
						FinalResponse[j] = temp[j];
					else
						FinalResponse[j] = (byte) 0xFF;
				}
			}
			else 
			{
				for(int j=1;j<=4;j++)
				{
					if (temp[0] == 0x00)
						FinalResponse[i+j] = temp[j];
					else
						FinalResponse[i+j] = (byte) 0xFF;
				}
			}
			
			//check JPG Start of Frame 
			for (int j=1;j<=4;j++)
				if (FinalResponse[i+j]==(byte)0xD9 && FinalResponse[i+j-1]==(byte)0xFF)
					{
						i = (NbOfBlockToRead*4)+50;
						EndOfJpgFile = true;
						j=15;
					}
			
		}
		if (EndOfJpgFile == false)
			FinalResponse[0] = (byte)0xAE;
		if (checkCorrectAnswer == false)
			FinalResponse[0] = (byte)0xAF;
		
		return FinalResponse;
	 }
	 
	 public static byte[] Send_several_ReadSingleBlockCommands (Tag myTag, byte[] StartAddress, byte[] bytNbBytesToRead,   DataDevice ma)
	 {
		long cpt =0;
		boolean checkCorrectAnswer = true;
		
		int NbBytesToRead = Helper.Convert2bytesHexaFormatToInt(bytNbBytesToRead);		
		int iNbOfBlockToRead = (NbBytesToRead / 4);
		byte[] FinalResponse = new byte[iNbOfBlockToRead*4 + 1];		
		
        byte[] bytAddress = new byte[2];
        
		//int intAddress = 0;
		int intAddress = Helper.Convert2bytesHexaFormatToInt(StartAddress);
		
		int index = 0;
		
		byte[] temp = new byte[5];
		
		//boucle for(int i=0;i<iNbOfBlockToRead; i++)
		do
		 {			
			bytAddress = Helper.ConvertIntTo2bytesHexaFormat(intAddress);
			
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 5)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)bytAddress[0],(byte)bytAddress[1]},ma);
				cpt ++;
			}
			cpt =0;				
			
			if (temp[0] != 0x00)
				checkCorrectAnswer = false;
			
			if (temp[0] == 0)
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = temp[j];
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = temp[j];
				}
		 	}
			else
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = (byte)0xFF;
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = (byte)0xFF;
				}
		 	}
			
			intAddress++;
			index++;
			
		} while(index < iNbOfBlockToRead);
		
		if (checkCorrectAnswer == false)
			FinalResponse[0] = (byte)0xAF;
					
		return FinalResponse;
	 }

	 public static byte[] Send_several_ReadSingleBlockCommands_NbBlocks (Tag myTag, byte[] StartAddress, byte[] bNbOfBlockToRead,   DataDevice ma)
	 {
		long cpt =0;
		boolean checkCorrectAnswer = true;
		
		int iNbOfBlockToRead = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);		
		byte[] FinalResponse = new byte[iNbOfBlockToRead*4 + 1];		
		
        byte[] bytAddress = new byte[2];
        
		//int intAddress = 0;
		int intAddress = Helper.Convert2bytesHexaFormatToInt(StartAddress);
		
		int index = 0;
		
		byte[] temp = new byte[5];
		
		//boucle for(int i=0;i<iNbOfBlockToRead; i++)
		do
		 {			
			bytAddress = Helper.ConvertIntTo2bytesHexaFormat(intAddress);
			
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 5)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)bytAddress[0],(byte)bytAddress[1]},ma);
				cpt ++;
			}
			cpt =0;				
			
			if (temp[0] != 0x00)				
				checkCorrectAnswer = false;
			
			if (temp[0] == 0)
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = temp[j];
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = temp[j];
				}
		 	}
			else
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = (byte)0xFF;
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = (byte)0xFF;
				}
		 	}
			
			intAddress++;
			index++;
			
		} while(index < iNbOfBlockToRead);
		
		if (checkCorrectAnswer == false)
			FinalResponse[0] = (byte)0xAF;
		
		return FinalResponse;
	 }

	 public static byte[] Send_several_ReadSingleBlockCommands_NbBlocks_JPG (Tag myTag, byte[] StartAddress, byte[] bNbOfBlockToRead,   DataDevice ma)
	 {
		long cpt =0;
		boolean EndOfJpgFile = false;
		boolean checkCorrectAnswer = true;
		
		int iNbOfBlockToRead = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);		
		byte[] FinalResponse = new byte[iNbOfBlockToRead*4 + 1];		
		
        byte[] bytAddress = new byte[2];
        
		//int intAddress = 0;
		int intAddress = Helper.Convert2bytesHexaFormatToInt(StartAddress);
		
		int index = 0;
		
		byte[] temp = new byte[5];
		
		//boucle for(int i=0;i<iNbOfBlockToRead; i++)
		do
		 {			
			bytAddress = Helper.ConvertIntTo2bytesHexaFormat(intAddress);
			
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 5)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)bytAddress[0],(byte)bytAddress[1]},ma);
				cpt ++;
			}
			cpt =0;				
			
			if (temp[0] != 0x00)
				checkCorrectAnswer = false;
			
			if (temp[0] == 0)
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = temp[j];
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = temp[j];
				}
		 	}
			else
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = (byte)0xFF;
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = (byte)0xFF;
				}
		 	}
			
			//check JPG Start of Frame 
			for (int j=1;j<=4;j++)
				if (FinalResponse[(index*4)+j]==(byte)0xD9 && FinalResponse[(index*4)+j-1]==(byte)0xFF)
					{
						index = iNbOfBlockToRead+50;
						EndOfJpgFile = true;
						j=15;
					}
			
			intAddress++;
			index++;
			
		} while(index < iNbOfBlockToRead);
		
		if (EndOfJpgFile == false)
			 FinalResponse[0] = (byte)0xAE;
		if (checkCorrectAnswer == false)
			FinalResponse[0] = (byte)0xAF;
		
		return FinalResponse;
	 }
	 
	//***********************************************************************/
	 //* the function send an ReadSingle Custom command (0x0A 0x20) || (0x02 0x20) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	
	 public static byte[] SendReadMultipleBlockCommandCustom2 (Tag myTag, byte[] StartAddress, byte[] bNbOfBlockToRead, DataDevice ma)
	 {
		 
		 boolean checkCorrectAnswer = true;
		 
		 int iNbOfBlockToRead = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);
		 int iNumberOfSectorToRead;
		 int iStartAddress = Helper.Convert2bytesHexaFormatToInt(StartAddress);
		 int iAddressStartRead = (iStartAddress/32)*32;
		 if(iNbOfBlockToRead%32 == 0)
		 {
			 iNumberOfSectorToRead = (iNbOfBlockToRead/32);
		 }
		 else
		 {
			 iNumberOfSectorToRead = (iNbOfBlockToRead/32)+1;
		 }
		 byte[] bAddressStartRead = Helper.ConvertIntTo2bytesHexaFormat(iAddressStartRead);
		 
		 byte[] AllReadDatas = new byte[((iNumberOfSectorToRead*128)+1)];
		 byte[] FinalResponse = new byte[(iNbOfBlockToRead*4)+1] ;

		 String sMemorySize = ma.getMemorySize();
		 sMemorySize = Helper.StringForceDigit(sMemorySize,4);
		 byte[] bLastMemoryAddress = Helper.ConvertStringToHexBytes(sMemorySize);
		 
		 //Loop needed for number of sector o read
		 for(int i=0; i<iNumberOfSectorToRead;i++)
		 {
			 byte[] temp = new byte[33]; 
			 
			 int incrementAddressStart0 = (bAddressStartRead[0]+i/8)  ;									//Most Important Byte
			 int incrementAddressStart1 = (bAddressStartRead[1]+i*32) - (incrementAddressStart0*256);	//Less Important Byte
			 
			 
			 if(bAddressStartRead[0]<0)
			 	 incrementAddressStart0 = ((bAddressStartRead[0]+256)+i/8);	
			 
			 if(bAddressStartRead[1]<0)
				 incrementAddressStart1 = ((bAddressStartRead[1]+256)+i*32) - (incrementAddressStart0*256);
			
			 
			 if(incrementAddressStart1 > bLastMemoryAddress[1] && incrementAddressStart0 > bLastMemoryAddress[0])
			 {
				 
			 
			 }
			 else
			 {
				temp = null;	
				temp = SendReadMultipleBlockCommand (myTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},(byte)0x1F,ma);
				
				if (temp[0] != 0x00)
					checkCorrectAnswer = false;
				
				// if any error occurs during 
				if(temp[0] == (byte)0x01)
				{
					return temp;
				}
				else
				{
					// to construct a response with first byte = 0x00
					if(i==0)
					{
						for(int j=0;j<=128;j++)
						{
							AllReadDatas[j] = temp[j];
						}
					}
					else 
					{
						for(int j=1;j<=128;j++)
						{
							AllReadDatas[(i*128)+j] = temp[j];
						}
					}
				}
			 }
		 }
		 
		 int iNbBlockToCopyInFinalReponse = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);		 
		 int iNumberOfBlockToIgnoreInAllReadData = 4*(Helper.Convert2bytesHexaFormatToInt(StartAddress)%32);
		 
		 for(int h=1; h <= iNbBlockToCopyInFinalReponse*4 ; h++)
		 {
			 FinalResponse[h] = AllReadDatas[h + iNumberOfBlockToIgnoreInAllReadData];
		 }
		 
		 if (checkCorrectAnswer == true)
			 FinalResponse[0] = AllReadDatas[0];
		 else
			 FinalResponse[0] = (byte)0xAF;
			 
		 return FinalResponse;
	 }
	 
	 public static byte[] SendReadMultipleBlockCommandCustom2_JPG (Tag myTag, byte[] StartAddress, byte[] bNbOfBlockToRead, DataDevice ma)
	 {
		 boolean checkCorrectAnswer = true;
		 boolean EndOfJpgFile = false;
		 
		 int iNbOfBlockToRead = Helper.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);
		 int iNumberOfSectorToRead = 0;
		 int iNumberOfSectorToRead_New = 0;
		 int iStartAddress = Helper.Convert2bytesHexaFormatToInt(StartAddress);
		 int iAddressStartRead = (iStartAddress/32)*32;		 		 
		 
		 if(iNbOfBlockToRead%32 == 0)
		 {
			 iNumberOfSectorToRead = (iNbOfBlockToRead/32);
		 }
		 else
		 {
			 iNumberOfSectorToRead = (iNbOfBlockToRead/32)+1;
		 }
		 byte[] bAddressStartRead = Helper.ConvertIntTo2bytesHexaFormat(iAddressStartRead);
		 
		 byte[] AllReadDatas = new byte[((iNumberOfSectorToRead*128)+1)];
		 byte[] FinalResponse = new byte[(iNbOfBlockToRead*4)+1] ;

		 String sMemorySize = ma.getMemorySize();
		 sMemorySize = Helper.StringForceDigit(sMemorySize,4);
		 byte[] bLastMemoryAddress = Helper.ConvertStringToHexBytes(sMemorySize);
		 
		 //Loop needed for number of sector o read
		 for(int i=0; i<iNumberOfSectorToRead;i++)
		 {
			 byte[] temp = new byte[33]; 
			 
			 int incrementAddressStart0 = (bAddressStartRead[0]+i/8)  ;									//Most Important Byte
			 int incrementAddressStart1 = (bAddressStartRead[1]+i*32) - (incrementAddressStart0*256);	//Less Important Byte
			 
			 
			 if(bAddressStartRead[0]<0)
			 	 incrementAddressStart0 = ((bAddressStartRead[0]+256)+i/8);	
			 
			 if(bAddressStartRead[1]<0)
				 incrementAddressStart1 = ((bAddressStartRead[1]+256)+i*32) - (incrementAddressStart0*256);
			
			 
			 if(incrementAddressStart1 > bLastMemoryAddress[1] && incrementAddressStart0 > bLastMemoryAddress[0])
			 {
				 
			 
			 }
			 else
			 {
				temp = null;	
				temp = SendReadMultipleBlockCommand (myTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},(byte)0x1F,ma);
				
				if (temp[0] != 0x00)
					checkCorrectAnswer = false;
				
				// if any error occurs during 
				if(temp[0] == (byte)0x01)
				{
					return temp;
				}
				else
				{
					// to construct a response with first byte = 0x00
					if(i==0)
					{
						for(int j=0;j<=128;j++)
						{
							AllReadDatas[j] = temp[j];
						}
					}
					else 
					{
						for(int j=1;j<=128;j++)
						{
							AllReadDatas[(i*128)+j] = temp[j];
						}
					}
					
					//check JPG Start of Frame 
					for (int j=1;j<=128;j++)
						if (AllReadDatas[(i*128)+j]==(byte)0xD9 && AllReadDatas[(i*128)+j-1]==(byte)0xFF)
						{
							iNumberOfSectorToRead_New = i+1;
							i = iNumberOfSectorToRead+10;
							EndOfJpgFile = true;
							j=200;
						}
				}
			 }
		 }
		 
		 int iNbBlockToCopyInFinalReponse = iNumberOfSectorToRead_New * 32;		 
		 int iNumberOfBlockToIgnoreInAllReadData = 4*(Helper.Convert2bytesHexaFormatToInt(StartAddress)%32);
		 
		 for(int h=1; h <= iNbBlockToCopyInFinalReponse*4 ; h++)
		 {
			 FinalResponse[h] = AllReadDatas[h + iNumberOfBlockToIgnoreInAllReadData];
		 }
		 
		 if (EndOfJpgFile = false)
			 FinalResponse[0] = (byte)0xAE;
		 else
		 {
			 if (checkCorrectAnswer == true)
				 FinalResponse[0] = AllReadDatas[0];
			 else
				 FinalResponse[0] = (byte)0xAF;
		 }
		 
		 return FinalResponse;
	 }
		
		
	 //32 blocks max
	 //Begining from Address 0x0000
	 public static byte[] Send_several_ReadMultipleBlockCommands (Tag myTag, byte[] bytNbBytesToRead,   DataDevice ma)
	 {
		 long cpt =0;
		 boolean checkCorrectAnswer = true;
		 
		 int NbBytesToRead = Helper.Convert2bytesHexaFormatToInt(bytNbBytesToRead);								
		 int iNumberOfSectorToRead;
		 int iResteOfBytes;
		 int iResteOfRows;
		 
		 if(NbBytesToRead%128 == 0)
			 iNumberOfSectorToRead = (NbBytesToRead/128);
		 else		
			 iNumberOfSectorToRead = (NbBytesToRead/128)+1;
		 iResteOfBytes = NbBytesToRead%128;
		 iResteOfRows = iResteOfBytes / 4;
 	 	 if (iResteOfBytes % 4 > 0)
 	 		iResteOfRows += 1;
 	 	
		 //String sMemorySize = ma.getMemorySize();
		 //sMemorySize = Helper.StringForceDigit(sMemorySize,4);
		 //byte[] bLastMemoryAddress = Helper.ConvertStringToHexBytes(sMemorySize);
		 
		 byte[] bytAddress = new byte[2];
		 int intAddress = 0;
		 int index = 0;
		 
		 byte[] FinalResponse = new byte[(iNumberOfSectorToRead*128)+1] ;
		 byte[] temp = new byte[128+1];
		 
		 //Loop needed for number of sector o read
		 do
		 {
			bytAddress = Helper.ConvertIntTo2bytesHexaFormat(intAddress);
			
			temp = null;
			byte byteNbRowsToRead;
			if(index == iNumberOfSectorToRead-1 && iResteOfRows>0)
				byteNbRowsToRead = (byte)iResteOfRows;
			else
				byteNbRowsToRead = (byte)0x20;
						
			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 5)
			{
				temp = SendReadMultipleBlockCommand (myTag, bytAddress, (byte)(byteNbRowsToRead-1),ma);
				cpt ++;
			}
			cpt =0;	
			
			intAddress += 0x20;			 
	        
			if (temp[0] != 0x00)
				checkCorrectAnswer = false;
			
			// to construct a response with first byte = 0x00
			if (temp[0] == 0)
			{
				if(index==0)
				{
					for(int j=0;j<=byteNbRowsToRead*4;j++)
						FinalResponse[j] = temp[j];
				}
				else 
				{
					for(int j=1;j<=byteNbRowsToRead*4;j++)
						FinalResponse[(index*128)+j] = temp[j];
				}
			}
			else
			{
				if(index==0)
				{
					for(int j=0;j<=byteNbRowsToRead*4;j++)
						FinalResponse[j] = (byte)0xFF;
				}
				else 
				{
					for(int j=1;j<=byteNbRowsToRead*4;j++)
						FinalResponse[(index*128)+j] = (byte)0xFF;
				}
			}
			
			index++;
	         
		 } while(index < iNumberOfSectorToRead);				 

		 if (checkCorrectAnswer == false)
			 FinalResponse[0] = (byte)0xAF;
		
		 return FinalResponse;
	 }
	 
	 
	//***********************************************************************/
	 //* the function send an ReadMultiple command (0x0A 0x23) || (0x02 0x23) 
	 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
	 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	 //* the function will return 04 blocks read from address 0002
	 //* According to the ISO-15693 maximum block read is 32 for the same sector
	 //***********************************************************************/
	 public static byte[] SendReadMultipleBlockCommand (Tag myTag, byte[] StartAddress, byte NbOfBlockToRead,  DataDevice ma)
	 {
		 byte[] response = new byte[] {(byte) 0x01}; 
		 byte[] ReadMultipleBlockFrame;
		 
		 if(ma.isBasedOnTwoBytesAddress())
			 ReadMultipleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x23, StartAddress[1], StartAddress[0], NbOfBlockToRead};
		 else
			 ReadMultipleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x23, StartAddress[1], NbOfBlockToRead};

		//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadMultipleBlockFrame));
		 
		 int errorOccured = 1;
		 while(errorOccured != 0)
		 {
			 try
			 {
				 NfcV nfcvTag = NfcV.get(myTag);
				 nfcvTag.close();
				 nfcvTag.connect();
				 response = nfcvTag.transceive(ReadMultipleBlockFrame);
				 if(response[0] == (byte) 0x00)
				 {
					 errorOccured = 0;
					//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadMultipleBlockFrame));

				 }
			 }
			 catch(Exception e)
			 {
				 errorOccured++;
				//Used for DEBUG : Log.i("NFCCOmmand", "SendReadMultipleBlockCommand errorOccured " + errorOccured);
				 if(errorOccured == 3)
				 {
					//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
					//Used for DEBUG : Log.i("NFCCOmmand", "Error when try to read from address  " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 return response;
				 }
			 }
		 }
		//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Multiple Block" + Helper.ConvertHexByteArrayToString(response));	
		 return response;
	 }
	 
	 

		//***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWriteSingleBlockCommand (Tag myTag, byte[] StartAddress, byte[] DataToWrite, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0xFF}; 
			 byte[] WriteSingleBlockFrame;
			 
			 if(ma.isBasedOnTwoBytesAddress())
				 WriteSingleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x21, StartAddress[1], StartAddress[0], DataToWrite[0], DataToWrite[1], DataToWrite[2], DataToWrite[3]};
			 else
				 WriteSingleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x21, StartAddress[1], DataToWrite[0], DataToWrite[1], DataToWrite[2], DataToWrite[3]};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(WriteSingleBlockFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;						 
						//Used for DEBUG : Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						//Used for DEBUG : Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }
		 
		 
		//***********************************************************************/
		//* the function send an Write command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWriteMultipleBlockCommand (Tag myTag, byte[] StartAddress, byte[] DataToWrite, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x01}; 
			 long cpt = 0;
			 
			 int NBByteToWrite = DataToWrite.length;
			 while (NBByteToWrite % 4 !=0)
				 NBByteToWrite ++;
			 
			 byte[] fullByteArrayToWrite = new byte[NBByteToWrite];
			 for(int j=0;j<NBByteToWrite;j++)
			 {
				 if(j<DataToWrite.length)
				 {
					 fullByteArrayToWrite[j]=DataToWrite[j];
				 }
				 else
				 {
					 fullByteArrayToWrite[j] = (byte)0xFF;
				 }
			 }
				 
			for(int i =0;i<NBByteToWrite; i = i+4)
			{
				
				int incrementAddressStart0 = (StartAddress[0]+i/256)  ;								//Most Important Byte
				int incrementAddressStart1 = (StartAddress[1]+i/4) - (incrementAddressStart0*255);	//Less Important Byte
				response[0] = (byte)0x01;
				
				while((response[0] == (byte)0x01) && cpt <= 2)
				{
					response = SendWriteSingleBlockCommand(myTag,new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},new byte[]{(byte)fullByteArrayToWrite[i],(byte)fullByteArrayToWrite[i+1],(byte)fullByteArrayToWrite[i+2],(byte)fullByteArrayToWrite[i+3]},ma);
					cpt ++;
				}
				if (response[0] == (byte)0x01)
					return response;
				cpt = 0;
			}
			 return response;
		 }

			//***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendPresentPasswordCommand (Tag myTag, byte PasswordNumber, byte[] PasswordData, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0xFF}; 
			 byte[] PresentPasswordFrame;
			 
			PresentPasswordFrame = new byte[]{(byte) 0x02, (byte) 0xB3, (byte) 0x02, PasswordNumber, PasswordData[0], PasswordData[1], PasswordData[2], PasswordData[3]};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(PresentPasswordFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;						 
						//Used for DEBUG : Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						//Used for DEBUG : Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }

		 //***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWritePasswordCommand (Tag myTag, byte PasswordNumber, byte[] PasswordData, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0xFF}; 
			 byte[] PresentPasswordFrame;
			 
			PresentPasswordFrame = new byte[]{(byte) 0x02, (byte) 0xB1, (byte) 0x02, PasswordNumber, PasswordData[0], PasswordData[1], PasswordData[2], PasswordData[3]};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(PresentPasswordFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;						 
						//Used for DEBUG : Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						//Used for DEBUG : Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }

		 //***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendLockSectorCommand (Tag myTag, byte[] SectorNumberAddress, byte LockSectorByte, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0xFF}; 
			 byte[] LockSectorFrame;
			 
			 if(ma.isBasedOnTwoBytesAddress())
				 LockSectorFrame = new byte[]{(byte) 0x0A, (byte) 0xB2, (byte) 0x02, SectorNumberAddress[1], SectorNumberAddress[0], LockSectorByte};
			 else
				 LockSectorFrame = new byte[]{(byte) 0x02, (byte) 0xB2, (byte) 0x02, SectorNumberAddress[1], LockSectorByte};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(LockSectorFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;						 
						//Used for DEBUG : Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						//Used for DEBUG : Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }

		 //***********************************************************************/
		 //* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
		 //* the function will return 04 blocks read from address 0002
		 //* According to the ISO-15693 maximum block read is 32 for the same sector
		 //***********************************************************************/
		 public static byte[] SendReadEHconfigCommand (Tag myTag, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x0A}; 
			 byte[] ReadEHconfigFrame;
			 
			ReadEHconfigFrame = new byte[]{(byte) 0x02, (byte) 0xA0, (byte) 0x02};

			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(ReadEHconfigFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;
						//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadSingleBlockFrame));
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Single Block" + Helper.ConvertHexByteArrayToString(response));
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						 return response;
					 }
				 }
			 }
			//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Sigle Block" + Helper.ConvertHexByteArrayToString(response));
			 return response;
		 }

		 //***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWriteEHconfigCommand (Tag myTag, byte EHconfigByte, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0xFF}; 
			 byte[] WriteEHconfigFrame;
			 
			 WriteEHconfigFrame = new byte[]{(byte) 0x02, (byte) 0xA1, (byte) 0x02, EHconfigByte};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(WriteEHconfigFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;						 
						//Used for DEBUG : Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						//Used for DEBUG : Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }
		 
		 //***********************************************************************/
		 //* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
		 //* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
		 //***********************************************************************/
		 public static byte[] SendWriteD0configCommand (Tag myTag, byte D0configByte, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0xFF}; 
			 byte[] WriteD0configFrame;
			 
			 WriteD0configFrame = new byte[]{(byte) 0x02, (byte) 0xA4, (byte) 0x02, D0configByte};
			 
			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(WriteD0configFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;						 
						//Used for DEBUG : Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						//Used for DEBUG : Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  Helper.ConvertHexByteArrayToString(StartAddress));
						 return response;
					 }
				 }
			 }
			 return response;
		 }
		 
		 //***********************************************************************/
		 //* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
		 //* the function will return 04 blocks read from address 0002
		 //* According to the ISO-15693 maximum block read is 32 for the same sector
		 //***********************************************************************/
		 public static byte[] SendCheckEHenableCommand (Tag myTag, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x0A}; 
			 byte[] ReadEHconfigFrame;
			 
			ReadEHconfigFrame = new byte[]{(byte) 0x02, (byte) 0xA3, (byte) 0x02};

			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(ReadEHconfigFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;
						//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadSingleBlockFrame));
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Single Block" + Helper.ConvertHexByteArrayToString(response));
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						 return response;
					 }
				 }
			 }
			//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Sigle Block" + Helper.ConvertHexByteArrayToString(response));
			 return response;
		 }

		 //***********************************************************************/
		 //* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
		 //* the function will return 04 blocks read from address 0002
		 //* According to the ISO-15693 maximum block read is 32 for the same sector
		 //***********************************************************************/
		 public static byte[] SendResetEHenableCommand (Tag myTag, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x0A}; 
			 byte[] ResetEHenableFrame;
			 
			 ResetEHenableFrame = new byte[]{(byte) 0x02, (byte) 0xA2, (byte) 0x02, (byte) 0x00};

			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(ResetEHenableFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;
						//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadSingleBlockFrame));
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Single Block" + Helper.ConvertHexByteArrayToString(response));
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						 return response;
					 }
				 }
			 }
			//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Sigle Block" + Helper.ConvertHexByteArrayToString(response));
			 return response;
		 }

		 //***********************************************************************/
		 //* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
		 //* the argument myTag is the intent triggered with the TAG_DISCOVERED
		 //* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
		 //* the function will return 04 blocks read from address 0002
		 //* According to the ISO-15693 maximum block read is 32 for the same sector
		 //***********************************************************************/
		 public static byte[] SendSetEHenableCommand (Tag myTag, DataDevice ma)
		 {
			 byte[] response = new byte[] {(byte) 0x0A}; 
			 byte[] SetEHenableFrame;
			 
			SetEHenableFrame = new byte[]{(byte) 0x02, (byte) 0xA2, (byte) 0x02, (byte) 0x01};

			 int errorOccured = 1;
			 while(errorOccured != 0)
			 {
				 try
				 {
					 NfcV nfcvTag = NfcV.get(myTag);
					 nfcvTag.close();
					 nfcvTag.connect();
					 response = nfcvTag.transceive(SetEHenableFrame);
					 if(response[0] == (byte) 0x00)
					 {
						 errorOccured = 0;
						//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadSingleBlockFrame));
					 }
				 }
				 catch(Exception e)
				 {
					 errorOccured++;
					//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Single Block" + Helper.ConvertHexByteArrayToString(response));
					 if(errorOccured == 2)
					 {
						//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
						 return response;
					 }
				 }
			 }
			//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Sigle Block" + Helper.ConvertHexByteArrayToString(response));
			 return response;
		 }
		 
}
