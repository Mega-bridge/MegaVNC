package kr.co.megabridge.megavnc.ftp;


import kr.co.megabridge.megavnc.exception.BusinessException;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    boolean isFileReadDone = false;

    // sf@2004 - File Transfer part
    ArrayList remoteDirsList;
    ArrayList remoteFilesList;
    ArrayList a;
    boolean fFileReceptionError = false;
    boolean fFileReceptionRunning = true;
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




    Socket sock;
    DataInputStream is;
    OutputStream os;
    OutputStreamWriter osw;

    boolean inNormalProtocol = false;


    Vector remoteList;


    //
    // Constructor. Make TCP connection to RFB server.
    //

    RfbProto( String repeaterHost, int repeaterPort, String repeaterId) throws IOException {

        sock = new Socket(repeaterHost, repeaterPort);
        System.out.println("리피터 소켓 연결됨: " + repeaterHost + ", port: " + repeaterPort);
        doRepeater(sock, repeaterId);
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

    private void doRepeater(Socket sock, String repeaterId) throws IOException {
        // Read the RFB protocol version
        final String buf2 = "";
        sock.getOutputStream().write(buf2.getBytes());
        System.out.println("빈 문자열 전송 완료");

        String dest = "ID:" + repeaterId;
        byte[] buf = new byte[250];
        System.arraycopy(dest.getBytes(StandardCharsets.ISO_8859_1), 0, buf, 0, dest.length());
        System.out.println("Repeater ID 전송 완료: " + dest);
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
                " is not an RFB server");
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
        return is.readUnsignedByte();
    }

    private void logDebug(String message) {
        System.out.println("[DEBUG]: " + message);
    }
    //	Parsing Rfb message to see what type

    void readRfbFileTransferMsg() throws IOException {
        int contentType = is.readUnsignedByte();
        int contentParamT = is.readUnsignedByte();
        int contentParam = contentParamT;
        contentParamT = is.readUnsignedByte();
        contentParamT = contentParamT << 8;
        contentParam = contentParam | contentParamT;

        logDebug("읽은 파일 전송 메시지 - contentType: " + contentType + ", contentParam: " + contentParam);

        if (contentType == rfbDirPacket) {
            logDebug("디렉토리 패킷을 처리 중...");
            readDriveOrDirectory(contentParam);
        } else if (contentType == rfbFileHeader) {
            logDebug("파일 헤더를 처리 중...");
            receiveFileHeader();
        } else if (contentType == rfbFilePacket) {
            logDebug("파일 청크를 처리 중...");
            receiveFileChunk();
        } else if (contentType == rfbEndOfFile) {
            logDebug("파일 전송 끝 신호를 받음");
            endOfReceiveFile(true); // Ok
        }  else if (contentType == rfbCommandReturn) {
            logDebug("명령 응답 처리 중...");
            createDirectoryorDeleteFile(contentParam);
        } else if (contentType == rfbFileAcceptHeader) {
            logDebug("서버에서 파일 수락 신호를 받음");
            sendFile();
        } else if (contentType == rfbFileChecksums) {
            logDebug("파일 체크섬을 처리 중...");
            ReceiveDestinationFileChecksums();
        } else {
            logDebug("알 수 없는 contentType: " + contentType);
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
        logDebug("readDriveOrDirectory 호출됨 - contentParam: " + contentParam);
        if (contentParam == rfbADirectory && !inDirectory2) {
            inDirectory2 = true;
            logDebug("디렉토리 시작 신호를 받음, 디렉토리 리스트 읽기 시작...");
            readFTPMsgDirectoryList();
        } else if (contentParam == rfbADirectory) {
            logDebug("디렉토리 내용 읽기...");
            readFTPMsgDirectoryListContent();
        } else if (contentParam == 0) {
            logDebug("디렉토리 리스트 끝 신호를 받음");
            readFTPMsgDirectoryListEndContent();
            inDirectory2 = false;
        } else {
            logDebug("알 수 없는 contentParam: " + contentParam);
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
    void writeRfbFileTransferMsgForSendFile(
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
                    RfbProto.rfbFilePacket,
                    0,
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


            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.err.println("Interrupted");
            }

        }
        System.out.println("파일전송 완료: " + source);
        writeRfbFileTransferMsg(rfbEndOfFile, 0, 0, 0, null);
        fis.close();
        fileSize = 0;

        fFileReceptionRunning = false;
        //fos.close();
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

            String temp = destinationPath + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
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
        //fFileReceptionRunning = true;
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



    }

    //Internally used when transferring file from server. Server signals end of file.
    void endOfReceiveFile(boolean fReceptionOk) throws IOException {
        System.out.println("fReceptionOk = " + fReceptionOk);
        fileSize = 0;
        fos.close();

        if (!fReceptionOk || fFileReceptionError) {
            File f = new File(receivePath);
            f.delete();
        }
        fFileReceptionError = false;
        fFileReceptionRunning = false;
    }


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
        logDebug("readFTPMsgDirectoryList 호출됨");
        is.readInt();
        int length = is.readInt();
        if (length == 0) {
            logDebug("디렉토리의 길이가 0입니다 - 드라이브가 준비되지 않았거나 액세스 권한이 없을 수 있습니다.");
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
            logDebug("받은 디렉토리 이름: " + str);

        }
    }

    //Internally used to receive directory content from server
    //Here, the server sends one file/directory with it's attributes
    void readFTPMsgDirectoryListContent() throws IOException {
        logDebug("readFTPMsgDirectoryListContent 호출됨");
        StringBuilder fileName = new StringBuilder();

        int dwFileAttributes = is.readInt();
        logDebug("파일 속성: " + dwFileAttributes);
        long ftCreationTime = is.readLong();
        long ftLastAccessTime = is.readLong();
        long ftLastWriteTime = is.readLong();

        logDebug("생성 시간: " + ftCreationTime + ", 마지막 액세스 시간: " + ftLastAccessTime + ", 마지막 수정 시간: " + ftLastWriteTime);


        int length = is.readInt();
        for (int i = 0; i < length; i++) {
            char cFileName = (char) is.readUnsignedByte();
            fileName.append(cFileName);
        }
        logDebug("읽은 파일 이름: " + fileName);

    }

    //Internally used to read directory content of server.
    //Here, server signals end of directory.
    void readFTPMsgDirectoryListEndContent() throws IOException {
        logDebug("readFTPMsgDirectoryListEndContent 호출됨");
        is.readInt(); // 패딩 읽기
        logDebug("디렉토리 목록 끝을 정상적으로 읽음");
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



    public String findDestinationPath() throws IOException, InterruptedException {
        System.out.println("🚀 목적지 조회를 시작합니다!");
        // 폴더가 있는지 검사하고 없으면 생성
        List<String> directoryStack = new ArrayList<>();
        Stack<String> pathStack = new Stack<>();
        directoryStack.add("C:\\");
        pathStack.push("C:\\");
        boolean desktopFound = false;

        while (!directoryStack.isEmpty()) {
            String currentDirectory = directoryStack.remove(directoryStack.size() - 1);


            // 스택 정리: 현재 디렉토리가 pathStack의 최상위와 다를 경우, 상위 경로를 맞추기 위해 pop
            while (!pathStack.isEmpty() && !pathStack.peek().equals(currentDirectory)) {
                System.out.println("🔄 스택 조정 중: 현재 디렉토리 " + currentDirectory + " 에 맞춰 상위 경로 정리");
                pathStack.pop();
            }

            System.out.println("📂 탐색 중인 디렉토리: " + currentDirectory);
            pathStack.push(currentDirectory);
            readServerDirectory(currentDirectory);




            // "Desktop" 폴더를 찾은 경우
            if (!desktopFound && remoteList.contains(" [Desktop]")) {
                System.out.println("✨ 데스크탑 폴더 발견! 현재 디렉토리: " + currentDirectory);
                desktopFound = true;
                currentDirectory = currentDirectory + "\\Desktop";
                directoryStack.add(currentDirectory);
                pathStack.push("\\Desktop");
                continue;
            }


            // "mega_vnc_remote_files" 폴더가 있는 경우 해당 디렉토리를 반환
            if (desktopFound && remoteList.contains(" [mega_vnc_remote_files]")) {
                System.out.println("🎉 mega_vnc_remote_files 폴더 발견! 현재 디렉토리: " + currentDirectory);
                String absolutePath = String.join("\\", pathStack) + "\\mega_vnc_remote_files";
                return absolutePath;
            } else if (desktopFound) {
                // "mega_vnc_remote_files" 폴더가 없는 경우 원격 경로에 생성 후 반환
                System.out.println("🔨 mega_vnc_remote_files 폴더가 없어서 새로 생성 중... 현재 디렉토리: " + currentDirectory);
                createRemoteDirectory(currentDirectory + "\\mega_vnc_remote_files");
                String absolutePath = String.join("\\", pathStack) + "\\mega_vnc_remote_files";
                return absolutePath;
            }

            for (Object dirObj : remoteList) {
                String dir = (String) dirObj;
                if (dir.startsWith(" [") && dir.endsWith("]")) {
                    String subDir = dir.substring(2, dir.length() - 1);
                    System.out.println("📁 하위 디렉토리 발견: " + subDir + " 를 탐색 대기열에 추가합니다.");
                    directoryStack.add(subDir);
                }
            }
            System.out.println("⬆️ 디렉토리 탐색이 끝나서 스택에서 경로 제거: " + pathStack.peek());
            pathStack.pop();

        }
        throw new IOException("🚫 경로 생성 실패.");
    }
/*

    private boolean isDirectoryLoading() {
        // 서버의 응답을 확인하는 로직을 구현해야 함. 예시로 remoteList가 비어있는지 확인.
        boolean loading = remoteList.isEmpty();
        logDebug("isDirectoryLoading 호출됨 - 현재 로딩 상태: " + loading);
        return loading;
    }

*/



    public class StrComp implements java.util.Comparator {
        public int compare(Object obj1, Object obj2) {
            String str1 = obj1.toString().toUpperCase();
            String str2 = obj2.toString().toUpperCase();
            return str1.compareTo(str2);
        }
    }

}
