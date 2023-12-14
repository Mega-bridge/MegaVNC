import RFB from "./noVNC-1.4.0/core/rfb.js";

window.onload = function() {
    let rfb;

    const repeaterId = document.getElementById("repeaterId").value;
    const passwordInput = document.getElementById("password");
    const connectButton = document.getElementById("connect");
    const disconnectButton = document.getElementById("disconnect");
    const screen = document.getElementById("screen");
    const fullscreen = document.getElementById("fullscreen");

    connectButton.addEventListener("click", () => {

        rfb = new RFB(
            screen,
            "wss://vnc.megabridge.co.kr:6080",
            {
                credentials: { password: passwordInput.value },
                repeaterID: repeaterId
            }
        );
        rfb.showDotCursor = true;
        rfb.scaleViewport = true;
        rfb.addEventListener("securityfailure", () => {
            window.alert("접속 암호가 올바르지 않습니다.");
        });
    });

    disconnectButton.addEventListener("click", () => {
        rfb.disconnect();
    });

    fullscreen.addEventListener("click", () => {
        screen.requestFullscreen();
    });
};

/*
let rfb;

const url = new URL(window.location.href);

const name = url.searchParams.get("name");
const host = url.searchParams.get("host");
const port = url.searchParams.get("port");

const passwordInput = document.getElementById("password");
const connectButton = document.getElementById("connect");
const disconnectButton = document.getElementById("disconnect");

connectButton.addEventListener("click", () => {
    rfb = new RFB(
        document.getElementById('screen'),
        "ws://" + host + ":" + port,
        { credentials: { password: passwordInput.value } });
});

disconnectButton.addEventListener("click", () => {
    rfb.disconnect();
});
*/