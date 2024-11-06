package kr.co.megabridge.megavnc.ftp;



import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


class RfbProto {

	final String versionMsg = "RFB 003.003\n";
	final static int ConnFailed = 0, NoAuth = 1, VncAuth = 2, MsLogon = 0xfffffffa;
	final static int VncAuthOK = 0, VncAuthFailed = 1, VncAuthTooMany = 2;

	final static int rfbFileTransfer = 7;

	final int
		FramebufferUpdateRequest = 3;


	// sf@2004 - File Transfer part
	ArrayList remoteDirsList;
	ArrayList remoteFilesList;
	ArrayList a;
	boolean fAbort = false;
	boolean fFileReceptionError = false;
	boolean fFileReceptionRunning = false;
	boolean inDirectory2;
	FileOutputStream fos;
	FileInputStream fis;
	String sendFileSource;
	String receivePath;
	long fileSize;
	long receiveFileSize;
	long fileChunkCounter;

	final static int sz_rfbFileTransferMsg = 12,
	// File Transfer Content types and Params defines
	rfbDirContentRequest = 1,
	// Client asks for the content of a given Server directory
	rfbDirPacket = 2, // Full directory name or full file name.
	// Null content means end of Directory
	rfbFileTransferRequest = 3,
	// Client asks the server for the tranfer of a given file
	rfbFileHeader = 4,
	// First packet of a File Transfer, containing file's features
	rfbFilePacket = 5, // One slice of the file
	rfbEndOfFile = 6,
	// End of File Transfer (the file has been received or error)
	rfbAbortFileTransfer = 7,
	// The File Transfer must be aborted, whatever the state
	rfbFileTransferOffer = 8,
	// The client offers to send a file to the server
	rfbFileAcceptHeader = 9, // The server accepts or rejects the file
	rfbCommand = 10,
	// The Client sends a simple command (File Delete, Dir create etc...)
	rfbCommandReturn = 11,
	//	New v2 File Transfer Protocol: The zipped checksums of the destination file (Delta Transfer)
	rfbFileChecksums = 12,
	// The Client receives the server's answer about a simple command
	// rfbDirContentRequest client Request - content params 
	rfbRDirContent = 1, // Request a Server Directory contents
	rfbRDrivesList = 2, // Request the server's drives list
	
	// rfbDirPacket & rfbCommandReturn  server Answer - content params
	rfbADirectory = 1, // Reception of a directory name
	rfbADrivesList = 3, // Reception of a list of drives
	rfbADirCreate = 4, // Response to a create dir command
	rfbAFileDelete = 7, // Response to a delete file command
	
	// rfbCommand Command - content params
	rfbCDirCreate = 1, // Request the server to create the given directory
	rfbCFileDelete = 4, // Request the server to delete the given file
	

	// Error when a command fails on remote side (ret in "size" field)
	sz_rfbBlockSize = 8192;// New v2 File Transfer Protocol
	




	// End of File Transfer part 
	
	String host;
	int port;
	Socket sock;
	DataInputStream is;
	OutputStream os;
	OutputStreamWriter osw;

	boolean inNormalProtocol = false;
	VncViewer viewer;





	//
	// Constructor. Make TCP connection to RFB server.
	//

	RfbProto(String h, int p, VncViewer v, String repeaterHost, int repeaterPort) throws IOException {
		viewer = v;
		host = h;
		port = p;
		
        if (repeaterHost != null) {
            sock = new Socket(repeaterHost, repeaterPort);
            doRepeater(sock,host,port);
        } else {
            sock = new Socket(host, port);
        }
		is =
			new DataInputStream(
				new BufferedInputStream(sock.getInputStream(), 16384));
		os = sock.getOutputStream();
		osw = new OutputStreamWriter(sock.getOutputStream());
		inDirectory2 = false;
		a = new ArrayList();
		// sf@2004
		remoteDirsList = new ArrayList();
		remoteFilesList = new ArrayList();
	
		sendFileSource = "";
	}
	
	    private void doRepeater(Socket sock, String host, int port) throws IOException {
        // Read the RFB protocol version
        final String buf2 = "";
        sock.getOutputStream().write(buf2.getBytes());


        String dest = host + ":" + port;
        byte[] buf = new byte[250];
        System.arraycopy(dest.getBytes("ISO-8859-1"), 0, buf, 0, dest.length());

        sock.getOutputStream().write(buf);
    }

	void close() {
		try {
			sock.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//
	// Read server's protocol version message
	//

	int serverMajor, serverMinor;

	void readVersionMsg() throws Exception {

		byte[] b = new byte[12];

		is.readFully(b);

		if ((b[0] != 'R')
			|| (b[1] != 'F')
			|| (b[2] != 'B')
			|| (b[3] != ' ')
			|| (b[4] < '0')
			|| (b[4] > '9')
			|| (b[5] < '0')
			|| (b[5] > '9')
			|| (b[6] < '0')
			|| (b[6] > '9')
			|| (b[7] != '.')
			|| (b[8] < '0')
			|| (b[8] > '9')
			|| (b[9] < '0')
			|| (b[9] > '9')
			|| (b[10] < '0')
			|| (b[10] > '9')
			|| (b[11] != '\n')) {
			throw new Exception(
				"Host " + host + " port " + port + " is not an RFB server");
		}

		serverMajor = (b[4] - '0') * 100 + (b[5] - '0') * 10 + (b[6] - '0');
		serverMinor = (b[8] - '0') * 100 + (b[9] - '0') * 10 + (b[10] - '0');
	}

	//
	// Write our protocol version message
	//

	void writeVersionMsg() throws IOException {
		os.write(versionMsg.getBytes());
	}

	//
	// Find out the authentication scheme.
	//

	int readAuthScheme() throws Exception {
		int authScheme = is.readInt();

		switch (authScheme) {

			case ConnFailed :
				int reasonLen = is.readInt();
				byte[] reason = new byte[reasonLen];
				is.readFully(reason);
				throw new Exception(new String(reason));

			case NoAuth :
			case VncAuth :
			case MsLogon:
				return authScheme;

			default :
				throw new Exception(
					"Unknown authentication scheme from RFB server: "
						+ authScheme);

		}
	}

	//
	// Write the client initialisation message
	//

	void writeClientInit() throws IOException {
		os.write(1);
	}

	//
	// Read the server initialisation message
	//

	String desktopName;
	int framebufferWidth, framebufferHeight;
	int bitsPerPixel, depth;
	boolean bigEndian, trueColour;
	int redMax, greenMax, blueMax, redShift, greenShift, blueShift;

	void readServerInit() throws IOException {

		framebufferWidth = is.readUnsignedShort();
		framebufferHeight = is.readUnsignedShort();
		bitsPerPixel = is.readUnsignedByte();
		depth = is.readUnsignedByte();
		bigEndian = (is.readUnsignedByte() != 0);
		trueColour = (is.readUnsignedByte() != 0);
		redMax = is.readUnsignedShort();

		greenMax = is.readUnsignedShort();
		blueMax = is.readUnsignedShort();
		redShift = is.readUnsignedByte();
		greenShift = is.readUnsignedByte();
		blueShift = is.readUnsignedByte();
		byte[] pad = new byte[3];
		is.readFully(pad);
		int nameLength = is.readInt();
		byte[] name = new byte[nameLength];
		is.readFully(name);
		desktopName = new String(name);
		inNormalProtocol = true;
	}





	//
	// Read the server message type
	//

	int readServerMessageType() throws IOException {
		int msgType = is.readUnsignedByte();

		return msgType;
	}



	//	Parsing Rfb message to see what type 

	void readRfbFileTransferMsg() throws IOException
	{
		int contentType = is.readUnsignedByte();
		int contentParamT = is.readUnsignedByte();
		int contentParam = contentParamT;
		contentParamT = is.readUnsignedByte();
		contentParamT = contentParamT << 8;
		contentParam = contentParam | contentParamT;
		if (contentType == rfbRDrivesList || contentType == rfbDirPacket)
		{
			readDriveOrDirectory(contentParam);
		}
		else if (contentType == rfbFileHeader)
		{
			receiveFileHeader();
		}
		else if (contentType == rfbFilePacket)
		{
				receiveFileChunk();
		}
		else if (contentType == rfbEndOfFile)
		{
			endOfReceiveFile(true); // Ok
		}
		else if (contentType == rfbAbortFileTransfer)
		{
			if (fFileReceptionRunning)
			{
				endOfReceiveFile(false); // Error
			}
			else
			{
				// sf@2004 - Todo: Add TestPermission 
				// System.out.println("File Transfer Aborted!");
			}
			
		}
		else if (contentType == rfbCommandReturn)
		{
			createDirectoryorDeleteFile(contentParam);
		}
		else if (contentType == rfbFileAcceptHeader)
		{
			sendFile();
		}
		else if (contentType == rfbFileChecksums)
		{
			ReceiveDestinationFileChecksums();
		}

	}

	//Refactored from readRfbFileTransferMsg()
	public void createDirectoryorDeleteFile(int contentParam)
		throws IOException {
		if (contentParam == rfbADirCreate)
		{
			createRemoteDirectoryFeedback();
		}
		else if (contentParam == rfbAFileDelete)
		{
			deleteRemoteFileFeedback();
		}
	}

	//Refactored from readRfbFileTransferMsg()
	public void readDriveOrDirectory(int contentParam) throws IOException {
		if (contentParam == rfbADrivesList)
		{
			readFTPMsgDriveList();
		}
		else if (contentParam == rfbADirectory && !inDirectory2)
		{
			inDirectory2 = true;
			readFTPMsgDirectoryList();
		}
		else if (contentParam == rfbADirectory && inDirectory2)
		{
			readFTPMsgDirectoryListContent();
		}
		else if (contentParam == 0)
		{
			readFTPMsgDirectoryListEndContent();
			inDirectory2 = false;
		}

	}

	// Internally used. Write an Rfb message to the server
	void writeRfbFileTransferMsg(
								int contentType,
								int contentParam,
								long size, // 0 : compression not supported - 1 : compression supported
								long length,
								String text) throws IOException
	{
		byte b[] = new byte[12];

		b[0] = (byte) rfbFileTransfer;
		b[1] = (byte) contentType;
		b[2] = (byte) contentParam;

		byte by = 0;
		long c = 0;
		length++;
		c = size & 0xFF000000;
		by = (byte) (c >>> 24);
		b[4] = by;
		c = size & 0xFF0000;
		by = (byte) (c >>> 16);
		b[5] = by;
		c = size & 0xFF00;
		by = (byte) (c >>> 8);
		b[6] = by;
		c = size & 0xFF;
		by = (byte) c;
		b[7] = by;

		c = length & 0xFF000000;
		by = (byte) (c >>> 24);
		b[8] = by;
		c = length & 0xFF0000;
		by = (byte) (c >>> 16);
		b[9] = by;
		c = length & 0xFF00;
		by = (byte) (c >>> 8);
		b[10] = by;
		c = length & 0xFF;
		by = (byte) c;
		b[11] = by;
		os.write(b);
		

		if (text != null)
		{
			byte byteArray[] = text.getBytes();
			byte byteArray2[] = new byte[byteArray.length + 1];
			for (int i = 0; i < byteArray.length; i++) {
				byteArray2[i] = byteArray[i];
			}
			byteArray2[byteArray2.length - 1] = 0;
			os.write(byteArray2);
		}
		
	}

	//Internally used. Write an rfb message to the server for sending files ONLY 
	int writeRfbFileTransferMsgForSendFile(
											int contentType,
											int contentParam,
											String source
											) throws IOException
	{
		File f = new File(source);
		fis = new FileInputStream(f);
		byte byteBuffer[] = new byte[sz_rfbBlockSize]; 
		int bytesRead = fis.read(byteBuffer);
		long counter=0;
		boolean fError = false;
		
		// sf@ - Manage compression
		boolean fCompress = true;
		Deflater myDeflater = new Deflater();
		byte[] CompressionBuffer = new byte[sz_rfbBlockSize + 1024];
		int compressedSize = 0;
	
		while (bytesRead!=-1)
		{
				counter += bytesRead;
				myDeflater.setInput(byteBuffer, 0, bytesRead);
				myDeflater.finish();
				compressedSize = myDeflater.deflate(CompressionBuffer);
				myDeflater.reset();
				// If the compressed data is larger than the original one, we're dealing with
				// already compressed data
				if (compressedSize > bytesRead)
					fCompress = false;
				this.writeRfbFileTransferMsg(
											contentType,
											contentParam,
											(fCompress ? 1 : 0), 
											(fCompress ? compressedSize-1 : bytesRead-1),
											null
											);
				// Todo: Test write error !
				os.write(
						fCompress ? CompressionBuffer : byteBuffer,
						0,
						fCompress ? compressedSize : bytesRead
						);
				
				// Todo: test read error !
				bytesRead = fis.read(byteBuffer);

				viewer.ftp.jProgressBar.setValue((int)((counter * 100) / f.length()));
				viewer.ftp.connectionStatus.setText(">>> Sending File: " + source + " - Size: " + f.length() + " bytes - Progress: " + ((counter * 100) / f.length()) + "%");
				
				if (fAbort == true)
				{
					fAbort = false;
					fError = true;
					break;
				}
				try
				{
			        Thread.sleep(5);
			    }
				catch(InterruptedException e)
				{
			        System.err.println("Interrupted");
			    }				
		}
		
		writeRfbFileTransferMsg(fError ? rfbAbortFileTransfer : rfbEndOfFile, 0, 0, 0, null);
		fis.close();
		return (fError ? -1 : 1);
	}

	//This method is internally used to send the file to the server once the server is ready
	void sendFile()
	{
		try
		{
			viewer.ftp.disableButtons();
			int size = is.readInt();
			int length = is.readInt();
			for (int i = 0; i < length; i++)
			{
				System.out.print((char) is.readUnsignedByte());
			}
			
			int ret = writeRfbFileTransferMsgForSendFile(
															rfbFilePacket,
															0,
															sendFileSource);
	
			viewer.ftp.refreshRemoteLocation();
			if (ret != 1)
			{
				viewer.ftp.connectionStatus.setText(" > Error - File NOT sent");
				viewer.ftp.historyComboBox.insertItemAt(new String(" > Error - File: <" + sendFileSource) + "> was not correctly sent (aborted by user or error)",0);
			}
			else
			{
				viewer.ftp.connectionStatus.setText(" > File sent");
				viewer.ftp.historyComboBox.insertItemAt(new String(" > File: <" + sendFileSource) + "> was sent to Remote Machine",0);
			}
			viewer.ftp.historyComboBox.setSelectedIndex(0);
			viewer.ftp.enableButtons();
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	//Call this method to send a file from local pc to server
	void offerLocalFile(String source, String destinationPath)
	{
		try
		{
			sendFileSource = source;
			File f = new File(source);
			// sf@2004 - Add support for huge files
			long lSize = f.length();
			int iLowSize = (int)(lSize & 0x00000000FFFFFFFF); 
			int iHighSize = (int)(lSize >> 32);
			
			String temp = destinationPath + f.getName();
			writeRfbFileTransferMsg(
									rfbFileTransferOffer,
									0,
									iLowSize, // f.length(),
									temp.length(),
									temp);
			
			// sf@2004 - Send the high part of the size			
			byte b[] = new byte[4];
			byte by = 0;
			long c = 0;
			c = iHighSize & 0xFF000000;
			by = (byte) (c >>> 24);
			b[0] = by;
			c = iHighSize & 0xFF0000;
			by = (byte) (c >>> 16);
			b[1] = by;
			c = iHighSize & 0xFF00;
			by = (byte) (c >>> 8);
			b[2] = by;
			c = iHighSize & 0xFF;
			by = (byte) c;
			b[3] = by;			
			os.write(b); 
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	//Internally used.
	//Handles acknowledgement that the file has been deleted on the server
	void deleteRemoteFileFeedback() throws IOException
	{
		is.readInt();
		int length = is.readInt();
		String f = "";
		for (int i = 0; i < length; i++)
		{
			f += (char)is.readUnsignedByte();
		}
		
		viewer.ftp.refreshRemoteLocation();	
		viewer.ftp.historyComboBox.insertItemAt(new String(" > Deleted File On Remote Machine: " + f.substring(0, f.length()-1)),0);
		viewer.ftp.historyComboBox.setSelectedIndex(0);
		viewer.ftp.setEnabled(false);
		viewer.ftp.repaint();
		viewer.ftp.setEnabled(true);
	}

	//Call this method to delete a file at server
	void deleteRemoteFile(String text)
	{
		try
		{
			String temp = text;
			writeRfbFileTransferMsg(
									rfbCommand,
									rfbCFileDelete,
									0,
									temp.length(),
									temp);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	//Internally used.
	// Handles acknowledgement that the directory has been created on the server
	void createRemoteDirectoryFeedback() throws IOException
	{
		is.readInt();
		int length = is.readInt();
		String f="";
		for (int i = 0; i < length; i++)
		{
			f += (char)is.readUnsignedByte();
		}
		viewer.ftp.refreshRemoteLocation();	
		viewer.ftp.historyComboBox.insertItemAt(new String(" > Created Directory on Remote Machine: " + f.substring(0, f.length()-1)),0);
		viewer.ftp.historyComboBox.setSelectedIndex(0);
	}

	//Call this method to create a directory at server
	void createRemoteDirectory(String text)
	{
		try
		{
			String temp = text;
			writeRfbFileTransferMsg(
				rfbCommand,
				rfbCDirCreate,
				0,
				temp.length(),
				temp);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	//Call this method to get a file from the server
	void requestRemoteFile(String text, String localPath)
	{
		try
		{
			String temp = text;
			receivePath = localPath;
					
			writeRfbFileTransferMsg(
									rfbFileTransferRequest,
									0,
									1, // 0 : compression not supported - 1 : compression supported
									temp.length(),
									temp);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	//Internally used when transferring file from server. Here, the server sends
	//a rfb packet signalling that it is ready to send the file requested
	void receiveFileHeader() throws IOException
	{
		fFileReceptionRunning = true;
		fFileReceptionError = false;
		viewer.ftp.disableButtons();
		int size = is.readInt();
		int length = is.readInt();
		
		String tempName = "";
		for (int i = 0; i < length; i++)
		{
			tempName += (char) is.readUnsignedByte();
		}

		// sf@2004 - Read the high part of file size (not yet in rfbFileTransferMsg for 
		// backward compatibility reasons...)
		int sizeH = is.readInt();
		long lSize = ((long)(sizeH) << 32) + size;
		
		receiveFileSize = lSize;
		viewer.ftp.connectionStatus.setText("Received: 0 bytes of " + lSize + " bytes");
		fileSize=0;
		fileChunkCounter = 0;
		String fileName = receivePath;
		fos = new FileOutputStream(fileName);
		writeRfbFileTransferMsg(rfbFileHeader, 0, 0, 0, null);
	}

	//Internally used when transferring file from server. This method receives one chunk
	//of the file
	void receiveFileChunk() throws IOException
	{
		// sf@2004 - Size = 0 means file chunck not compressed
		int size = is.readInt();
		boolean fCompressed = (size != 0);
		int length = is.readInt();
		fileChunkCounter++;

		// sf@2004 - allocates buffers for file chunck reception and decompression 
		byte[] ReceptionBuffer = new byte[length + 32];

		// Read the incoming file data
		// Todo: check error !
		is.readFully(ReceptionBuffer,0, length);
		
		if (fCompressed)
		{
			int bufSize = sz_rfbBlockSize + 1024; // Todo: set a more accurate value here
			int decompressedSize = 0;
			byte[] DecompressionBuffer = new byte[bufSize];
			Inflater myInflater = new Inflater();
			myInflater.setInput(ReceptionBuffer);
			try
			{
				decompressedSize = myInflater.inflate(DecompressionBuffer);
			}
			catch (DataFormatException e)
			{
				System.err.println(e);
			}
			// Todo: check error !
			fos.write(DecompressionBuffer, 0, decompressedSize);
			fileSize += decompressedSize;
		}
		else
		{
			//	 Todo: check error !
			fos.write(ReceptionBuffer, 0, length);
			fileSize += length;
		}

		viewer.ftp.jProgressBar.setValue((int)((fileSize * 100) / receiveFileSize));
		viewer.ftp.connectionStatus.setText(">>> Receiving File: " + receivePath + " - Size: " + receiveFileSize + " bytes - Progress: " + ((fileSize * 100) / receiveFileSize) + "%");
		
		if (fAbort == true)
		{
			fAbort = false;
			fFileReceptionError = true;
			writeRfbFileTransferMsg(rfbAbortFileTransfer, 0, 0, 0, null);
			
		}

	}
	
	//Internally used when transferring file from server. Server signals end of file.
	void endOfReceiveFile(boolean fReceptionOk) throws IOException
	{

		fileSize=0;
		fos.close();
		
		viewer.ftp.refreshLocalLocation();
		if (fReceptionOk && !fFileReceptionError)
		{
			viewer.ftp.connectionStatus.setText(" > File successfully received");
			viewer.ftp.historyComboBox.insertItemAt(new String(" > File: <" + receivePath + "> received from Remote Machine" ),0);
		}
		else
		{
			// sf@2004 - Delete the incomplete receieved file for now (until we use Delta Transfer)
			File f = new File(receivePath);
			f.delete();		
			viewer.ftp.connectionStatus.setText(" > Error - File NOT received");
			viewer.ftp.historyComboBox.insertItemAt(new String(" > Error - File: <" + receivePath + "> not correctly received from Remote Machine (aborted by user or error)") ,0);
		}

		fFileReceptionError = false;
		fFileReceptionRunning = false;
		viewer.ftp.historyComboBox.setSelectedIndex(0);
		viewer.ftp.enableButtons();
	}

	//Call this method to read the contents of the server directory
	void readServerDirectory(String text)
	{
		try
		{
			String temp = text;
			writeRfbFileTransferMsg(
									rfbDirContentRequest,
									rfbRDirContent,
									0,
									temp.length(),
									temp);
		}
		catch (IOException e)
		{
			System.out.println("IOException in readServerDirectory(String text) in RfbProto!");
			System.out.println(e);
			System.out.println("End of exception");
		}

	}

	//Internally used to receive list of drives available on the server
	void readFTPMsgDriveList() throws IOException
	{
		String str = "";
		for (int i = 0; i < 4; i++)
		{
			is.readUnsignedByte();
		}
		int length = is.readInt();
		for (int i = 0; i < length; i++)
		{
			char temp = (char) is.readUnsignedByte();
			if (temp != '\0')
			{
				str += temp;
			}
		}
		viewer.ftp.printDrives(str);
		
		// sf@2004
		// Finds the first readable drive and populates the local directory
		viewer.ftp.changeLocalDirectory(viewer.ftp.getFirstReadableLocalDrive());
		// Populate the remote directory
		viewer.ftp.changeRemoteDrive();
		viewer.ftp.refreshRemoteLocation();

	}

	//Internally used to receive directory content from server
	//Here, the server marks the start of the directory listing
	void readFTPMsgDirectoryList() throws IOException
	{
		is.readInt();
		int length = is.readInt();
		if (length == 0)
		{
			readFTPMsgDirectorydriveNotReady();
			inDirectory2 = false;
		}
		else
		{
			// sf@2004 - New File Transfer Protocol sends remote directory name
			String str = "";
			for (int i = 0; i < length; i++)
			{
				char temp = (char) is.readUnsignedByte();
				if (temp != '\0')
				{
					str += temp;
				}
			}

		}
	}

	//Internally used to receive directory content from server
	//Here, the server sends one file/directory with it's attributes
	void readFTPMsgDirectoryListContent() throws IOException
	{
		String fileName = "", alternateFileName = "";
		int dwFileAttributes,
			nFileSizeHigh,
			nFileSizeLow,
			dwReserved0,
			dwReserved1;
		long ftCreationTime, ftLastAccessTime, ftLastWriteTime;
		char cFileName, cAlternateFileName;
		int length = 0;
		is.readInt();
		length = is.readInt();
		dwFileAttributes = is.readInt();
		length -= 4;
		ftCreationTime = is.readLong();
		length -= 8;
		ftLastAccessTime = is.readLong();
		length -= 8;
		ftLastWriteTime = is.readLong();
		length -= 8;
		nFileSizeHigh = is.readInt();
		length -= 4;
		nFileSizeLow = is.readInt();
		length -= 4;
		dwReserved0 = is.readInt();
		length -= 4;
		dwReserved1 = is.readInt();
		length -= 4;
		cFileName = (char) is.readUnsignedByte();
		length--;
		while (cFileName != '\0')
		{
			fileName += cFileName;
			cFileName = (char) is.readUnsignedByte();
			length--;
		}
		cAlternateFileName = (char) is.readByte();
		length--;
		while (length != 0)
		{
			alternateFileName += cAlternateFileName;
			cAlternateFileName = (char) is.readUnsignedByte();
			length--;
		}

		// Added Jef Fix (jdp) - check for FILE_ATTRIBUTE_DIRECTORY attribute bit
		// note that we're looking at a little-endian value in a big-endian world
		if ((dwFileAttributes & 0x10000000) == 0x10000000)
 		{
 			fileName = " [" + fileName + "]";
 			remoteDirsList.add(fileName); // sf@2004
		}
		else
		{
			remoteFilesList.add(" " + fileName); // sf@2004
		}


	}

	//Internally used to read directory content of server.
	//Here, server signals end of directory.
	void readFTPMsgDirectoryListEndContent() throws IOException
	{
		is.readInt();

		// sf@2004
		a.clear();
		for (int i = 0; i < remoteDirsList.size(); i++) 
			a.add(remoteDirsList.get(i));
		for (int i = 0; i < remoteFilesList.size(); i++) 
			a.add(remoteFilesList.get(i));
		remoteDirsList.clear();
		remoteFilesList.clear();
		
		viewer.ftp.printRemoteDirectory(a);
	}

	//Internally used to signify the drive requested is not ready

	void readFTPMsgDirectorydriveNotReady() throws IOException
	{
		System.out.println("Remote Drive unavailable");
		viewer.ftp.connectionStatus.setText(" > WARNING - Remote Drive unavailable (possibly restricted access or media not present)");
		viewer.ftp.remoteStatus.setText("WARNING: Remote Drive unavailable");
	}

	//Call this method to request the list of drives on the server.
	void readServerDriveList()
	{
		try
		{
			viewer.rfb.writeRfbFileTransferMsg(
												RfbProto.rfbDirContentRequest,
												RfbProto.rfbRDrivesList,
												0,
												0,
												null);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
	}

	// sf@2004 - Read the destination file checksums data
	// We don't use it for now
	void ReceiveDestinationFileChecksums() throws IOException
	{

		int length = is.readInt();
		
		byte[] ReceptionBuffer = new byte[length + 32];

		// Read the incoming file data
		is.readFully(ReceptionBuffer,0, length);

	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	//
	// Write a FramebufferUpdateRequest message
	//

	void writeFramebufferUpdateRequest(
		int x,
		int y,
		int w,
		int h,
		boolean incremental)
		throws IOException {
			if (!viewer.ftp.isVisible()) {
		byte[] b = new byte[10];

		b[0] = (byte) FramebufferUpdateRequest;
		b[1] = (byte) (incremental ? 1 : 0);
		b[2] = (byte) ((x >> 8) & 0xff);
		b[3] = (byte) (x & 0xff);
		b[4] = (byte) ((y >> 8) & 0xff);
		b[5] = (byte) (y & 0xff);
		b[6] = (byte) ((w >> 8) & 0xff);
		b[7] = (byte) (w & 0xff);
		b[8] = (byte) ((h >> 8) & 0xff);
		b[9] = (byte) (h & 0xff);

		os.write(b);
		}
	}


}
