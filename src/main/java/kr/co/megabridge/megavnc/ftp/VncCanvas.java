package kr.co.megabridge.megavnc.ftp;

import java.awt.*;
import java.io.IOException;

class VncCanvas extends Canvas {

	VncViewer viewer;
	RfbProto rfb;


	// The constructor.
	VncCanvas(VncViewer v) throws IOException {
		viewer = v;
		rfb = viewer.rfb;
	}

	// processNormalProtocol() - executed by the rfbThread to deal with the
	// RFB socket.
	public void processNormalProtocol() throws Exception {
		// Main dispatch loop
		while (true) {
			// Read message type from the server.
			int msgType = rfb.readServerMessageType();

			// Process the message depending on its type.
			switch (msgType) {
				case RfbProto.rfbFileTransfer:
					viewer.rfb.readRfbFileTransferMsg();
					break;

				default:
					System.out.println("msgType = " + msgType);
					break;
			}
		}
	}
}
