package kr.co.megabridge.megavnc.ftp;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ButtonPanel extends JPanel implements ActionListener {

  VncViewer viewer;
  Button disconnectButton;
  Button ftpButton;

  ButtonPanel(VncViewer v) {
    viewer = v;

    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    disconnectButton = new Button("Disconnect");
    disconnectButton.setEnabled(false);
    add(disconnectButton);
    disconnectButton.addActionListener(this);
    ftpButton = new Button("File Transfer");
    ftpButton.setEnabled(false);
    add(ftpButton);
    ftpButton.addActionListener(this);
  }

  //
  // Enable buttons on successful connection.
  //

  public void enableButtons() {
    disconnectButton.setEnabled(true);
    ftpButton.setEnabled(true);
  }

  //
  // Disable all buttons on disconnect.
  //

  public void disableButtonsOnDisconnect() {
    remove(disconnectButton);
    disconnectButton = new Button("Hide desktop");
    disconnectButton.setEnabled(true);
    add(disconnectButton, 0);
    disconnectButton.addActionListener(this);
    ftpButton.setEnabled(false);

    validate();
  }


  public void actionPerformed(ActionEvent evt) {


    if (evt.getSource() == disconnectButton) {
      viewer.disconnect();

    }
    else if (evt.getSource() == ftpButton)
    {
		viewer.ftp.setVisible(!viewer.ftp.isVisible());
		viewer.rfb.readServerDriveList();
	
    }
  }
}

