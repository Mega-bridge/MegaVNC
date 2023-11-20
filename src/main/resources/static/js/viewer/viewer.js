import RFB from "./noVNC-1.4.0/core/rfb.js";

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