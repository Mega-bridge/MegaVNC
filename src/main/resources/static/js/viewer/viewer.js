import RFB from "./noVNC-1.4.0/core/rfb.js";

let rfb, repeaterId, accessPassword, connectButton, disconnectButton, screen, fullscreenButton, pasteButton,status;
let captureButton, qualityLevelInput, qualityLevel = 6, shutdownButton;

window.onload = function() {
    status = document.getElementById("status").value
    repeaterId = document.getElementById("repeaterId").value;
    accessPassword = document.getElementById("accessPassword").value;
   // connectButton = document.getElementById("connect");
    disconnectButton = document.getElementById("disconnect");
    screen = document.getElementById("screen");
    fullscreenButton = document.getElementById("fullscreen");
    pasteButton = document.getElementById("pasteButton");
    captureButton = document.getElementById("captureButton");
    qualityLevelInput = document.getElementById("qualityLevelInput");
    shutdownButton = document.getElementById("shutdownButton");
    //다 완성 하고 보기
    disconnectButton.addEventListener("click", handleDisconnect);
    fullscreenButton.addEventListener("click", handleFullscreen);
    pasteButton.addEventListener("click", handlePaste);
    captureButton.addEventListener("click", handleCapture);
    qualityLevelInput.addEventListener("input", handleQualityLevel);
    shutdownButton.addEventListener("click", handleShutdown);
    //connectButton.addEventListener("click",handleConnect)
    if(status === "ACTIVE"){
        window.alert("다른 PC에서 사용중입니다.");
        window.location.href = "/remote-pcs";
    }
    else if(status === "OFFLINE_ASSIGNED"){
        handleConnect();
        window.alert("클라이언트의 접속을 기다려 주세요.");
    }
    else{
        handleConnect();
    }
};

function handleConnect() {
    rfb = new RFB(
            screen,
             "wss://192.168.0.228:6080",                   // LOCAL
            //"wss://vnc.megabridge.co.kr:6080",          // DEV
            {
                credentials: { password: accessPassword },
                repeaterID: repeaterId
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
    window.location.href = "/remote-pcs";
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

function handleShutdown() {
    rfb.machineShutdown();
    console.log("sent");
}