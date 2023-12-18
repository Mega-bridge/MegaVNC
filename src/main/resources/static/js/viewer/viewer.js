import RFB from "./noVNC-1.4.0/core/rfb.js";

let rfb, repeaterId, passwordInput, connectButton, disconnectButton, screen, fullscreenButton, pasteButton;
let captureButton, qualityLevelInput, qualityLevel = 6;

window.onload = function() {
    repeaterId = document.getElementById("repeaterId").value;
    passwordInput = document.getElementById("password");
    connectButton = document.getElementById("connect");
    disconnectButton = document.getElementById("disconnect");
    screen = document.getElementById("screen");
    fullscreenButton = document.getElementById("fullscreen");
    pasteButton = document.getElementById("pasteButton");
    captureButton = document.getElementById("captureButton");
    qualityLevelInput = document.getElementById("qualityLevelInput");

    connectButton.addEventListener("click", handleConnect);
    disconnectButton.addEventListener("click", handleDisconnect);
    fullscreenButton.addEventListener("click", handleFullscreen);
    pasteButton.addEventListener("click", handlePaste);
    captureButton.addEventListener("click", handleCapture);
    qualityLevelInput.addEventListener("input", handleQualityLevel);
};

function handleConnect() {
    rfb = new RFB(
            screen,
            "ws://127.0.0.1:6080",                  // LOCAL
            // "wss://vnc.megabridge.co.kr:6080",   // DEV
            {
                credentials: { password: passwordInput.value },
                // repeaterID: repeaterId           // DEV
            }
    );

    rfb.showDotCursor = true;
    rfb.scaleViewport = true;
    rfb.qualityLevel = qualityLevel;
    rfb.addEventListener("securityfailure", () => {
        window.alert("접속 암호가 올바르지 않습니다.");
    });

    rfb.addEventListener("clipboard", handleClipboard);
}

function handleDisconnect() {
    rfb.disconnect();
}

function handleFullscreen() {
    screen.requestFullscreen();
}

function handleClipboard(event) {
    navigator.clipboard.writeText(event.detail.text)
}

function handlePaste() {
    navigator.clipboard.readText()
        .then(text => {
            rfb.clipboardPasteFrom(text);
            rfb.sendKey(0xFFE3, "ControlLeft", true);
            rfb.sendKey(0x0076, "KeyV");
            rfb.sendKey(0xFFE3, "ControlLeft", false);
        });
}

function handleCapture() {
    const current = new Date();
    const datetime = current.getFullYear() + "-" + (current.getMonth() + 1) + "-" + current.getDate() + "_" +
                     current.getHours() + "-" + current.getMinutes() + "-" + current.getSeconds();
    let link = document.createElement("a");
    link.download = "screen-capture_" + datetime;
    link.href = rfb.toDataURL();
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    link = null;
}

function handleQualityLevel() {
    qualityLevel = parseInt(this.value);
}