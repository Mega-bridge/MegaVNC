function requestRegistUser() {
    resetFailMessage();
    var userName = $('.userName').val();
    var userPassword = $('.userPassword').val();
    var checkUserPassword = $('.checkUserPassword').val();

    if (checkValues(userName, userPassword, checkUserPassword) == false) {
        return false;
    }

    var param = {
        'username': userName,
        'password': userPassword,
        'passwordConfirm': checkUserPassword
    }

    var url = '/api/users/regist';
    ajax(url, param, "사용자가 등록되었습니다.");
}

function resetUsersInfo() {
    $('.userName').val("");
    $('.userPassword').val("");
    $('.checkUserPassword').val("");
}

function resetFailMessage() {
    $('.ajaxFailMessage').text('');
    $('.failMessageUserName').text('');
    $('.failMessageUserPassword').text('');
    $('.failMessageCheckUserPassword').text('');
}

function registUser() {
    resetUsersInfo();
    resetFailMessage();
    $('.modalBackWrap').fadeIn(200);
}

function cancelUser() {
    $('.modalBackWrap').fadeOut(200);
    resetUsersInfo();
    resetFailMessage();
}

function checkValues(userName, userPassword, checkUserPassword) {
    if (userName == '') {
        $('.failMessageUserName').text(' *사용자명을 입력해 주세요.');
        $('.userName').focus();
        return false;
    }
    if (userPassword == '') {
        $('.failMessageUserPassword').text(' *비밀번호를 입력해 주세요.');
        $('.userPassword').focus();
        return false;
    }
    if (checkUserPassword == '') {
        $('.failMessageCheckUserPassword').text(' *비밀번호를 확인해 주세요.');
        $('.checkUserPassword').focus();
        return false;
    }
    if (userPassword != checkUserPassword) {
        $('.failMessageCheckUserPassword').text(' *비밀번호가 다릅니다. 비밀번호를 확인해 주세요.');
        $('.checkUserPassword').focus();
        return false;
    }
    return true;
}

function handleBackWrapClick(event) {
    if (event.target.classList.contains('modalBackWrap')) {
        cancelUser();
    }
}

setTimeout(function () {
    let card = document.getElementById("errorMessageCard");
    card.style.transition = "opacity 2s ease";
    card.style.opacity = 0;
    setTimeout(function () {
        card.style.display = "none";
    }, 2000);
}, 5000);