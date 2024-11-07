package kr.co.megabridge.megavnc.ftp;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class RfbProto {

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


    String host;
    // End of File Transfer part

    int port;


    Socket sock;
    DataInputStream is;
    OutputStream os;
    OutputStreamWriter osw;

    boolean inNormalProtocol = false;


    Vector remoteList;


    //
    // Constructor. Make TCP connection to RFB server.
    //

    RfbProto(String h, int p) throws IOException {
        host = h;
        port = p;
        sock = new Socket(host, port);
        System.out.println("소켓 연결됨: " + host + ", port: " + port);
        is = new DataInputStream(new BufferedInputStream(sock.getInputStream(), 16384));
        os = sock.getOutputStream();
        osw = new OutputStreamWriter(sock.getOutputStream());
        inDirectory2 = false;
        a = new ArrayList();
        // sf@2004
        remoteDirsList = new ArrayList();
        remoteFilesList = new ArrayList();
        remoteList = new Vector<>();

        sendFileSource = "";
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
        System.out.println("버전 메시지 읽는중 ...");
        is.readFully(b);
        System.out.println("받은 메시지 버전: " + new String(b));
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
        System.out.println("클라이언트 버전 메시지 전송: " + versionMsg);
        os.write(versionMsg.getBytes());
    }

    //
    // Find out the authentication scheme.
    //

    int readAuthScheme() throws Exception {
        System.out.println("서버에서 인증 스키마 읽는중...");
        int authScheme = is.readInt();
        System.out.println("받은 인증 스키마: " + authScheme);

        switch (authScheme) {

            case ConnFailed:
                int reasonLen = is.readInt();
                byte[] reason = new byte[reasonLen];
                is.readFully(reason);
                throw new Exception(new String(reason));

            case NoAuth:
            case VncAuth:
            case MsLogon:
                return authScheme;

            default:
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

    void readRfbFileTransferMsg() throws IOException {
        int contentType = is.readUnsignedByte();
        int contentParamT = is.readUnsignedByte();
        int contentParam = contentParamT;
        contentParamT = is.readUnsignedByte();
        contentParamT = contentParamT << 8;
        contentParam = contentParam | contentParamT;
        if (contentType == rfbRDrivesList || contentType == rfbDirPacket) {
            readDriveOrDirectory(contentParam);
        } else if (contentType == rfbFileHeader) {
            receiveFileHeader();
        } else if (contentType == rfbFilePacket) {
            receiveFileChunk();
        } else if (contentType == rfbEndOfFile) {
            endOfReceiveFile(true); // Ok
        } else if (contentType == rfbAbortFileTransfer) {
            if (fFileReceptionRunning) {
                endOfReceiveFile(false); // Error
            } else {
                // sf@2004 - Todo: Add TestPermission
                // System.out.println("File Transfer Aborted!");
            }

        } else if (contentType == rfbCommandReturn) {
            createDirectoryorDeleteFile(contentParam);
        } else if (contentType == rfbFileAcceptHeader) {
            sendFile();
        } else if (contentType == rfbFileChecksums) {
            ReceiveDestinationFileChecksums();
        }
    }

    //Refactored from readRfbFileTransferMsg()
    public void createDirectoryorDeleteFile(int contentParam)
            throws IOException {
        if (contentParam == rfbADirCreate) {
            createRemoteDirectoryFeedback();
        } else if (contentParam == rfbAFileDelete) {
            deleteRemoteFileFeedback();
        }
    }

    //Refactored from readRfbFileTransferMsg()
    public void readDriveOrDirectory(int contentParam) throws IOException {

        if (contentParam == rfbADirectory && !inDirectory2) {
            inDirectory2 = true;
            readFTPMsgDirectoryList();
        } else if (contentParam == rfbADirectory && inDirectory2) {
            readFTPMsgDirectoryListContent();
        } else if (contentParam == 0) {
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
            String text) throws IOException {
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


        if (text != null) {
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
    ) throws IOException {
        File f = new File(source);
        fis = new FileInputStream(f);
        byte byteBuffer[] = new byte[sz_rfbBlockSize];
        int bytesRead = fis.read(byteBuffer);
        long counter = 0;
        boolean fError = false;

        // sf@ - Manage compression
        boolean fCompress = true;
        Deflater myDeflater = new Deflater();
        byte[] CompressionBuffer = new byte[sz_rfbBlockSize + 1024];
        int compressedSize = 0;
        System.out.println("파일전송 시작: " + source);
        while (bytesRead != -1) {
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
                    (fCompress ? compressedSize - 1 : bytesRead - 1),
                    null
            );
            // Todo: Test write error !
            System.out.println("파일청크 보내는중: " + counter + " bytes sent so far...");
            os.write(
                    fCompress ? CompressionBuffer : byteBuffer,
                    0,
                    fCompress ? compressedSize : bytesRead
            );

            // Todo: test read error !
            bytesRead = fis.read(byteBuffer);

            if (fAbort == true) {
                fAbort = false;
                fError = true;
                break;
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.err.println("Interrupted");
            }
        }
        System.out.println("파일전송 완료: " + source);
        writeRfbFileTransferMsg(fError ? rfbAbortFileTransfer : rfbEndOfFile, 0, 0, 0, null);
        fis.close();
        return (fError ? -1 : 1);
    }

    //This method is internally used to send the file to the server once the server is ready
    void sendFile() {
        try {
            System.out.println("파일 수락 응답 받는중...");
            int size = is.readInt();
            System.out.println("Server response size: " + size);
            int length = is.readInt();
            for (int i = 0; i < length; i++) {
                System.out.print((char) is.readUnsignedByte());
            }

            writeRfbFileTransferMsgForSendFile(
                    rfbFilePacket,
                    0,
                    sendFileSource);


        } catch (IOException e) {
            System.err.println(e);
        }
    }

    //Call this method to send a file from local pc to server
    void offerLocalFile(File f, String fileName, String destinationPath) {
        try {
            sendFileSource = f.getPath();
            /* File f = new File(source);*/
            // sf@2004 - Add support for huge files
            long lSize = f.length();
            int iLowSize = (int) (lSize & 0x00000000FFFFFFFF);
            int iHighSize = (int) (lSize >> 32);

            String temp = destinationPath + fileName;
            System.out.println("서버에 제공할 파일: " + f.getPath());
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
        } catch (IOException e) {
            System.err.println(e);
        }
    }


    //Internally used.
    //Handles acknowledgement that the file has been deleted on the server
    void deleteRemoteFileFeedback() throws IOException {
        is.readInt();
        int length = is.readInt();
        String f = "";
        for (int i = 0; i < length; i++) {
            f += (char) is.readUnsignedByte();
        }

    }


    //Internally used.
    // Handles acknowledgement that the directory has been created on the server
    void createRemoteDirectoryFeedback() throws IOException {
        is.readInt();
        int length = is.readInt();
        String f = "";
        for (int i = 0; i < length; i++) {
            f += (char) is.readUnsignedByte();
        }
    }

    //Call this method to create a directory at server
    void createRemoteDirectory(String text) {
        try {
            String temp = text;
            writeRfbFileTransferMsg(
                    rfbCommand,
                    rfbCDirCreate,
                    0,
                    temp.length(),
                    temp);
        } catch (IOException e) {
            System.err.println(e);
        }
    }


    //Internally used when transferring file from server. Here, the server sends
    //a rfb packet signalling that it is ready to send the file requested
    void receiveFileHeader() throws IOException {
        fFileReceptionRunning = true;
        fFileReceptionError = false;
        int size = is.readInt();
        int length = is.readInt();

        String tempName = "";
        for (int i = 0; i < length; i++) {
            tempName += (char) is.readUnsignedByte();
        }

        // sf@2004 - Read the high part of file size (not yet in rfbFileTransferMsg for
        // backward compatibility reasons...)
        int sizeH = is.readInt();
        long lSize = ((long) (sizeH) << 32) + size;

        receiveFileSize = lSize;
        fileSize = 0;
        fileChunkCounter = 0;
        String fileName = receivePath;
        fos = new FileOutputStream(fileName);
        writeRfbFileTransferMsg(rfbFileHeader, 0, 0, 0, null);
    }

    //Internally used when transferring file from server. This method receives one chunk
    //of the file
    void receiveFileChunk() throws IOException {
        // sf@2004 - Size = 0 means file chunck not compressed
        int size = is.readInt();
        boolean fCompressed = (size != 0);
        int length = is.readInt();
        fileChunkCounter++;

        // sf@2004 - allocates buffers for file chunck reception and decompression
        byte[] ReceptionBuffer = new byte[length + 32];

        // Read the incoming file data
        // Todo: check error !
        is.readFully(ReceptionBuffer, 0, length);

        if (fCompressed) {
            int bufSize = sz_rfbBlockSize + 1024; // Todo: set a more accurate value here
            int decompressedSize = 0;
            byte[] DecompressionBuffer = new byte[bufSize];
            Inflater myInflater = new Inflater();
            myInflater.setInput(ReceptionBuffer);
            try {
                decompressedSize = myInflater.inflate(DecompressionBuffer);
            } catch (DataFormatException e) {
                System.err.println(e);
            }
            // Todo: check error !
            fos.write(DecompressionBuffer, 0, decompressedSize);
            fileSize += decompressedSize;
        } else {
            //	 Todo: check error !
            fos.write(ReceptionBuffer, 0, length);
            fileSize += length;
        }

        if (fAbort == true) {
            fAbort = false;
            fFileReceptionError = true;
            writeRfbFileTransferMsg(rfbAbortFileTransfer, 0, 0, 0, null);

        }

    }

    //Internally used when transferring file from server. Server signals end of file.
    void endOfReceiveFile(boolean fReceptionOk) throws IOException {
        fileSize = 0;
        fos.close();

        if (!fReceptionOk || fFileReceptionError) {
            File f = new File(receivePath);
            f.delete();
        }
        fFileReceptionError = false;
        fFileReceptionRunning = false;
    }

    //1.C 드라이브 전체 디렉토리 읽어옴
    void readServerDirectory(String text) {
        try {
            String temp = text;
            writeRfbFileTransferMsg(
                    rfbDirContentRequest,
                    rfbRDirContent,
                    0,
                    temp.length(),
                    temp);
        } catch (IOException e) {
            System.out.println("IOException in readServerDirectory(String text) in RfbProto!");
            System.out.println(e);
            System.out.println("End of exception");
        }
    }


    //Internally used to receive directory content from server
    //Here, the server marks the start of the directory listing
    void readFTPMsgDirectoryList() throws IOException {
        is.readInt();
        int length = is.readInt();
        if (length == 0) {
            inDirectory2 = false;
        } else {
            // sf@2004 - New File Transfer Protocol sends remote directory name
            String str = "";
            for (int i = 0; i < length; i++) {
                char temp = (char) is.readUnsignedByte();
                if (temp != '\0') {
                    str += temp;
                }
            }

        }
    }

    //Internally used to receive directory content from server
    //Here, the server sends one file/directory with it's attributes
    void readFTPMsgDirectoryListContent() throws IOException {
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
        while (cFileName != '\0') {
            fileName += cFileName;
            cFileName = (char) is.readUnsignedByte();
            length--;
        }
        cAlternateFileName = (char) is.readByte();
        length--;
        while (length != 0) {
            alternateFileName += cAlternateFileName;
            cAlternateFileName = (char) is.readUnsignedByte();
            length--;
        }

        // Added Jef Fix (jdp) - check for FILE_ATTRIBUTE_DIRECTORY attribute bit
        // note that we're looking at a little-endian value in a big-endian world
        if ((dwFileAttributes & 0x10000000) == 0x10000000) {
            fileName = " [" + fileName + "]";
            remoteDirsList.add(fileName); // sf@2004
        } else {
            remoteFilesList.add(" " + fileName); // sf@2004
        }


    }

    //Internally used to read directory content of server.
    //Here, server signals end of directory.
    void readFTPMsgDirectoryListEndContent() throws IOException {
        is.readInt();

        // sf@2004
        a.clear();
        for (int i = 0; i < remoteDirsList.size(); i++)
            a.add(remoteDirsList.get(i));
        for (int i = 0; i < remoteFilesList.size(); i++)
            a.add(remoteFilesList.get(i));
        remoteDirsList.clear();
        remoteFilesList.clear();

        listRemoteDirectory(a);
    }


    // sf@2004 - Read the destination file checksums data
    // We don't use it for now
    void ReceiveDestinationFileChecksums() throws IOException {

        int length = is.readInt();

        byte[] ReceptionBuffer = new byte[length + 32];

        // Read the incoming file data
        is.readFully(ReceptionBuffer, 0, length);

    }


    ////////////////////////////////
    // Send File to the RFB server//
    ///////////////////////////////


    public void listRemoteDirectory(ArrayList a) {

        remoteList.clear();

        ArrayList files = new ArrayList();
        ArrayList dirs = new ArrayList();

        for (Iterator i = a.iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            if (name.equals("[..]")) {
                remoteList.add(name);
            }
            // Blank before '[' is mandatory!
            else if (name.startsWith(" [") && name.endsWith("]")) {
                dirs.add(name.substring(2, name.length() - 1));
            } else {
                files.add(name);
            }
        }
        Collections.sort(dirs, new RfbProto.StrComp());
        Collections.sort(files, new RfbProto.StrComp());

        for (Iterator i = dirs.iterator(); i.hasNext(); ) {
            String dirname = (String) i.next();
            // blank before '[' is mandatory!
            remoteList.add(" [" + dirname + "]");
        }
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            String filename = (String) i.next();
            remoteList.add(filename);
        }

        System.out.println("remoteList.get(0) = " + remoteList.get(0));
    }


    /**
     * 파일을 전송함
     */
    public void doSend(File file, String fileName, String destinationPath) {
      /*  if (remoteList.contains(fileName)) {
            System.out.println("이미 존재하는 피일입니다.");
            return;
        }*/
        offerLocalFile(file, fileName, destinationPath);
    }


    /**
     * 새 폴더를 만듬
     */
    public void doNewFolder(String destinationPath, String name) {
        if (name == null) {
            return;
        }
        name = destinationPath + name;
        createRemoteDirectory(name);

    }


    public class StrComp implements java.util.Comparator {
        public int compare(Object obj1, Object obj2) {
            String str1 = obj1.toString().toUpperCase();
            String str2 = obj2.toString().toUpperCase();
            return str1.compareTo(str2);
        }
    }

}
