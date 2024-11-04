import RFB from "./noVNC-1.4.0/core/rfb.js";

let rfb, repeaterId, accessPassword, disconnectButton, screen, fullscreenButton, pasteButton, status;
let captureButton, qualityLevelInput, qualityLevel = 6, shutdownButton, uploadForm;


window.onload = function () {
    status = document.getElementById("status").value
    repeaterId = document.getElementById("repeaterId").value;
    accessPassword = document.getElementById("accessPassword").value;
    disconnectButton = document.getElementById("disconnect");
    screen = document.getElementById("screen");
    fullscreenButton = document.getElementById("fullscreen");
    pasteButton = document.getElementById("pasteButton");
    captureButton = document.getElementById("captureButton");
    uploadForm = document.getElementById("uploadForm");
    //qualityLevelInput = document.getElementById("qualityLevelInput");
    //shutdownButton = document.getElementById("shutdownButton");
    disconnectButton.addEventListener("click", handleDisconnect);
    fullscreenButton.addEventListener("click", handleFullscreen);
    pasteButton.addEventListener("click", handlePaste);
    captureButton.addEventListener("click", handleCapture);
    uploadForm.addEventListener("submit", handleFormSubmit);
    // qualityLevelInput.addEventListener("input", handleQualityLevel);
    //shutdownButton.addEventListener("click", handleShutdown);
    if (status === "OFFLINE") {
        window.alert("해당 PC는 오프라인 상태 입니다.");
        window.location.href = "/remote-pcs";
    } else {
        handleConnect();
    }
};

function handleConnect() {
    rfb = new RFB(
        screen,
        "wss://vnc.megabridge.co.kr:6080",          // DEV
        {
            credentials: {password: accessPassword},
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



const handleFormSubmit = async (event) => {
    event.preventDefault(); // 폼 제출을 중지합니다.
    const COMMON_URL = window.location.origin;
    const fileInput = document.getElementById("fileInput").files[0];

    if (!fileInput) {
        alert("파일을 선택해 주세요");
    } else {
        // 폼 데이터를 가져옵니다.
        let formData = new FormData();
        formData.append("file", fileInput); // 파일 데이터 추가

        const option = {
            method: 'POST',
            headers: {},
            body: formData
        };

        const res = await fetch(`${COMMON_URL}/file?repeaterId=${repeaterId}`, {
            ...option
        });
        if (!res.ok) { // 응답이 성공이 아닌 경우
            const errorResponse = await res.json();
            const errorMessage = errorResponse.message;
            throw new Error('HTTP error, status = ' + errorMessage);
        }

        alert("파일 전송이 완료되었습니다.");
    }
}


function handleDisconnect() {
    rfb.disconnect();
}

function handleFullscreen() {
    screen.requestFullscreen();
}

function handleClipboard(event) {
    navigator.clipboard.writeText(event.detail.text);
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

/*

function handleQualityLevel() {
    qualityLevel = parseInt(this.value);
}

function handleShutdown() {
    rfb.machineShutdown();
    console.log("sent");
}*/
