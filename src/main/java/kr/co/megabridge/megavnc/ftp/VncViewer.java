package kr.co.megabridge.megavnc.ftp;

import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

public class VncViewer
        implements Runnable {

    boolean mslogon = false;


    public static void main(String[] argv) {
        VncViewer v = new VncViewer();
        v.mainArgs = argv;
        v.init();

    }

    String[] mainArgs;

    RfbProto rfb;
    Thread rfbThread;

    FTPFrame ftp; // KMC: FTP Frame declaration

    String host = "192.168.0.23";
    int port = 5900;
    String passwordParam="1234";


    // MS-Logon support 2
    String usernameParam="";
    String dm;
    byte[] domain = new byte[256];
    byte[] user = new byte[256];
    byte[] passwd = new byte[32];
    int i;


    public void init() {
        ftp = new FTPFrame(this); // KMC: FTPFrame creation
        rfbThread = new Thread(this);
        rfbThread.start();
        ftp.setVisible(true);
    }

  /*  public void paint(Graphics g) {
        super.paint(g);
    }*/

    //
    // run() - executed by the rfbThread to deal with the RFB socket.
    //

    public void run() {



        try {
            connectAndAuthenticate();

            doProtocolInitialisation();


          rfb.readServerDriveList();


            while (true) {
                // Read message type from the server.
                int msgType = rfb.readServerMessageType();

                // Process the message depending on its type.
                switch (msgType) {
                    case RfbProto.rfbFileTransfer:
                        rfb.readRfbFileTransferMsg();
                        break;

                    default:
                        System.out.println("msgType = " + msgType);
                        break;
                }
            }



        } catch (NoRouteToHostException e) {
            e.printStackTrace();
            fatalError("Network error: no route to server: " + host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            fatalError("Network error: server name unknown: " + host);
        } catch (ConnectException e) {
            e.printStackTrace();
            fatalError("Network error: could not connect to server: " +
                    host + ":" + port);
        } catch (EOFException e) {
            e.printStackTrace();

                if (rfb != null) {
                    rfb.close();
                    rfb = null;
                }
                fatalError("Network error: remote side closed connection");

        } catch (IOException e) {
            String str = e.getMessage();
            e.printStackTrace();
            if (str != null && str.length() != 0) {
                fatalError("Network Error: " + str);
            } else {
                fatalError(e.toString());
            }
        } catch (Exception e) {
            String str = e.getMessage();
            e.printStackTrace();
            if (str != null && str.length() != 0) {
                fatalError("Error: " + str);
            } else {
                fatalError(e.toString());
            }
        }

    }


    //
    // Connect to the RFB server and authenticate the user.
    //

    void connectAndAuthenticate() throws Exception {

        if (passwordParam != null) {
            if (!tryAuthenticate(usernameParam,passwordParam)) {
                throw new Exception("VNC authentication failed");
            }
            return;
        }
        prologueDetectAuthProtocol();





    }

    void prologueDetectAuthProtocol() throws Exception {

        rfb = new RfbProto(host, port, this, null, 0); // Modif: troessner - sf@2007: not yet used

        rfb.readVersionMsg();

        System.out.println("RFB server supports protocol version " +
                rfb.serverMajor + "." + rfb.serverMinor);

        // MS-Logon support
        if (rfb.serverMinor == 4) {
            mslogon = true;
            System.out.println("UltraVNC MS-Logon detected");
        }

        rfb.writeVersionMsg();

    }

    // MS-Logon support end


    //
    // Try to authenticate with a given password.
    //

    boolean tryAuthenticate(String us, String pw) throws Exception {

        rfb = new RfbProto(host, port, this, null, 0); // Modif: troessner - sf@2007: not yet used

        rfb.readVersionMsg();

        System.out.println("RFB server supports protocol version " +
                rfb.serverMajor + "." + rfb.serverMinor);

        rfb.writeVersionMsg();

        int authScheme = rfb.readAuthScheme();

        switch (authScheme) {

            case RfbProto.NoAuth:
                System.out.println("No authentication needed");
                return true;

            case RfbProto.VncAuth:

                if (mslogon) {
                    System.out.println("showing JOptionPane warning.");
                    int n = JOptionPane.showConfirmDialog(
                            ftp, "The current authentication method does not transfer your password securely."
                                    + "Do you want to continue?",
                            "Warning",
                            JOptionPane.YES_NO_OPTION);
                    if (n != JOptionPane.YES_OPTION) {
                        throw new Exception("User cancelled insecure MS-Logon");
                    }
                }
                // MS-Logon support
                byte[] challengems = new byte[64];
                if (mslogon) {
                    // copy the us (user) parameter into the user Byte formated variable
                    System.arraycopy(us.getBytes(), 0, user, 0, us.length());
                    // and pad it with Null
                    if (us.length() < 256) {
                        for (i = us.length(); i < 256; i++) {
                            user[i] = 0;
                        }
                    }

                    dm = ".";
                    // copy the dm (domain) parameter into the domain Byte formated variable
                    System.arraycopy(dm.getBytes(), 0, domain, 0, dm.length());
                    // and pad it with Null
                    if (dm.length() < 256) {
                        for (i = dm.length(); i < 256; i++) {
                            domain[i] = 0;
                        }
                    }

                    // equivalent of vncEncryptPasswdMS

                    // copy the pw (password) parameter into the password Byte formated variable
                    System.arraycopy(pw.getBytes(), 0, passwd, 0, pw.length());
                    // and pad it with Null
                    if (pw.length() < 32) {
                        for (i = pw.length(); i < 32; i++) {
                            passwd[i] = 0;
                        }
                    }

                    // Encrypt the full given password
                    byte[] fixedkey = {23, 82, 107, 6, 35, 78, 88, 7};
                    DesCipher desme = new DesCipher(fixedkey);
                    desme.encrypt(passwd, 0, passwd, 0);

                    // end equivalent of vncEncryptPasswdMS

                    // get the MS-Logon Challenge from server
                    rfb.is.readFully(challengems);
                }
                // MS-Logon support end

                byte[] challenge = new byte[16];
                rfb.is.readFully(challenge);

                if (pw.length() > 8)
                    pw = pw.substring(0, 8); // Truncate to 8 chars

                // vncEncryptBytes in the UNIX libvncauth truncates password
                // after the first zero byte. We do to.
                int firstZero = pw.indexOf(0);
                if (firstZero != -1)
                    pw = pw.substring(0, firstZero);

                // MS-Logon support
                if (mslogon) {
                    for (i = 0; i < 32; i++) {
                        challengems[i] = (byte) (passwd[i] ^ challengems[i]);
                    }
                    rfb.os.write(user);
                    rfb.os.write(domain);
                    rfb.os.write(challengems);
                }
                // MS-Logon support end

                byte[] key = {0, 0, 0, 0, 0, 0, 0, 0};
                System.arraycopy(pw.getBytes(), 0, key, 0, pw.length());

                DesCipher des = new DesCipher(key);

                des.encrypt(challenge, 0, challenge, 0);
                des.encrypt(challenge, 8, challenge, 8);

                rfb.os.write(challenge);

                int authResult = rfb.is.readInt();

                switch (authResult) {
                    case RfbProto.VncAuthOK:
                        System.out.println("VNC authentication succeeded");
                        return true;
                    case RfbProto.VncAuthFailed:
                        System.out.println("VNC authentication failed");
                        break;
                    case RfbProto.VncAuthTooMany:
                        throw new Exception("VNC authentication failed - too many tries");
                    default:
                        throw new Exception("Unknown VNC authentication result " + authResult);
                }
                break;

            case RfbProto.MsLogon:
                System.out.println("MS-Logon (DH) detected");
                if (AuthMsLogon(us, pw)) {
                    return true;
                }
                break;
            default:
                throw new Exception("Unknown VNC authentication scheme " + authScheme);
        }
        return false;
    }

    // marscha@2006: Try to better hide the windows password.
    // I know that this is no breakthrough in modern cryptography.
    // It's just a patch/kludge/workaround.
    boolean AuthMsLogon(String us, String pw) throws Exception {
        byte user[] = new byte[256];
        byte passwd[] = new byte[64];

        long gen = rfb.is.readLong();
        long mod = rfb.is.readLong();
        long resp = rfb.is.readLong();

        DH dh = new DH(gen, mod);
        long pub = dh.createInterKey();

        rfb.os.write(DH.longToBytes(pub));

        long key = dh.createEncryptionKey(resp);
        System.out.println("gen=" + gen + ", mod=" + mod
                + ", pub=" + pub + ", key=" + key);

        DesCipher des = new DesCipher(DH.longToBytes(key));

        System.arraycopy(us.getBytes(), 0, user, 0, us.length());
        // and pad it with Null
        if (us.length() < 256) {
            for (i = us.length(); i < 256; i++) {
                user[i] = 0;
            }
        }

        // copy the pw (password) parameter into the password Byte formated variable
        System.arraycopy(pw.getBytes(), 0, passwd, 0, pw.length());
        // and pad it with Null
        if (pw.length() < 32) {
            for (i = pw.length(); i < 32; i++) {
                passwd[i] = 0;
            }
        }

        //user = domain + "\\" + user;

        des.encryptText(user, user, DH.longToBytes(key));
        des.encryptText(passwd, passwd, DH.longToBytes(key));

        rfb.os.write(user);
        rfb.os.write(passwd);


        int authResult = rfb.is.readInt();

        switch (authResult) {
            case RfbProto.VncAuthOK:
                System.out.println("MS-Logon (DH) authentication succeeded");
                return true;
            case RfbProto.VncAuthFailed:
                System.out.println("MS-Logon (DH) authentication failed");
                break;
            case RfbProto.VncAuthTooMany:
                throw new Exception("MS-Logon (DH) authentication failed - too many tries");
            default:
                throw new Exception("Unknown MS-Logon (DH) authentication result " + authResult);
        }
        return false;
    }


    //
    // Do the rest of the protocol initialisation.
    //

    void doProtocolInitialisation() throws IOException {

        rfb.writeClientInit();

        rfb.readServerInit();

        System.out.println("Desktop name is " + rfb.desktopName);
        System.out.println("Desktop size is " + rfb.framebufferWidth + " x " +
                rfb.framebufferHeight);

    }



    //
    // disconnect() - close connection to server.
    //

    boolean disconnectRequested = false;

    synchronized public void disconnect() {
        disconnectRequested = true;
        if (rfb != null) {
            rfb.close();
            rfb = null;
        }
        System.out.println("Disconnect");

        JOptionPane.showMessageDialog(ftp, "Disconnected", "Info", JOptionPane.INFORMATION_MESSAGE);

    }

    //
    // fatalError() - print out a fatal error message.
    //

    synchronized public void fatalError(String str) {
        if (rfb != null) {
            rfb.close();
            rfb = null;
        }
        System.out.println(str);

        if (disconnectRequested) {
            disconnectRequested = false;
            return;
        }

        JOptionPane.showMessageDialog(ftp, str, "Error", JOptionPane.ERROR_MESSAGE);
    }



}