function requestRegistPc(){
    resetFailMessage();
    var addPcSelectGroup = $('.addPcSelectGroup').val();
    var pcName = $('.pcName').val();
    var pcPassword = $('.pcPassword').val();

    if(checkValues(addPcSelectGroup, pcName, pcPassword) == false){
        return false;
    }

    var param = {
        'groupName': addPcSelectGroup,
        'remotePcName': pcName,
        'accessPassword': pcPassword
    }

    var url = '/api/remote-pcs/register-pc';
    ajax(url, param, "원격 PC가 등록되었습니다.");
}

function checkValues(addPcSelectGroup, pcName, pcPassword){
    if(addPcSelectGroup == ''){
        $('.failMessageGroup').text(' *그룹을 선택해 주세요.');
        $('.addPcSelectGroup').focus();
        return false;
    }
    if(pcName == ''){
        $('.failMessagePcName').text(' *PC 이름을 입력해 주세요.');
        $('.pcName').focus();
        return false;
    }
    if(pcPassword == ''){
        $('.failMessagePassword').text(' *접근 비밀번호를 입력해 주세요.');
        $('.pcPassword').focus();
        return false;
    }
    return true;
}

function registPc(){
    resetRegistInfo();
    resetFailMessage();
    $('.modalBackWrap').fadeIn(200);
    if($('#group').val() != "All Group"){
        $('.addPcSelectGroup').val( $('#group').val() );
    }

}

function cancelPc() {
    $('.modalBackWrap').fadeOut(200);
    resetRegistInfo();
    resetFailMessage();
}

function resetRegistInfo(){
    $('.addPcSelectGroup').val("");
    $('.pcName').val("");
    $('.pcPassword').val("");
}

function resetFailMessage(){
    $('.ajaxFailMessage').text('');
    $('.failMessageGroup').text('');
    $('.failMessagePcName').text('');
    $('.failMessagePassword').text('');
}

$(document).ready(function () {
    $.ajax({
        url: '/vnc/websocket-url', // 서버에서 WebSocket URL을 가져옴
        method: 'GET',
        success: function (websocketUrl) {
            // 성공적으로 URL을 가져오면 WebSocket 연결 시작
            const socket = new WebSocket(websocketUrl);

            socket.onmessage = function (event) {
                const data = JSON.parse(event.data);
                // 각 데이터 항목을 업데이트
                const row = $(`#remotePcList tr[data-pc-id="${data.id}"]`);
                const statusCell = row.find('.pc-status');
                const registeredAtCell = row.find('.pc-registeredAt');

                // 등록일 업데이트
                if (data.registeredAt === "null") {
                    data.registeredAt = "";
                }
                registeredAtCell.text(data.registeredAt);

                // 상태 업데이트
                if (data.status === 'OFFLINE') {
                    statusCell.html('<span class="text-secondary">오프라인</span>');
                    row.find('.btn-primary').addClass('disabled');
                    row.find('.btn-primary').removeClass('btn-primary').addClass('btn-secondary');
                    row.find('.btn-danger').removeClass('disabled');
                } else if (data.status === 'STANDBY') {
                    statusCell.html('<span class="text-success">온라인(대기중)</span>');
                    row.find('.btn-secondary').removeClass('disabled');
                    row.find('.btn-secondary').removeClass('btn-secondary').addClass('btn-primary');
                    row.find('.btn-primary').removeClass('disabled');
                    row.find('.btn-danger').addClass('disabled');
                } else if (data.status === 'ACTIVE') {
                    statusCell.html('<span class="text-info">온라인(사용중)</span>');
                    row.find('.btn-primary').addClass('disabled');
                    row.find('.btn-secondary').removeClass('btn-secondary').addClass('btn-primary').addClass('disabled');
                    row.find('.btn-danger').addClass('disabled');
                }
            };

            socket.onclose = function (event) {
                console.log('WebSocket closed: ', event);
            };

            socket.onerror = function (error) {
                console.error('WebSocket error: ', error);
            };
        },
        error: function (error) {
            console.error("WebSocket URL을 가져오는 데 실패했습니다:", error);
        }
    });
});

setTimeout(function () {
    let card = document.getElementById("errorMessageCard");
    card.style.transition = "opacity 2s ease";
    card.style.opacity = 0;
    setTimeout(function () {
        card.style.display = "none";
    }, 2000);
}, 5000);

function handleBackWrapClick(event) {
    if (event.target.classList.contains('modalBackWrap')) {
        cancelPc();
    }
}