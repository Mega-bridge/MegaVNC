function requestRegistGroup() {
    resetFailMessage();
    var groupName = $('.groupName').val();

    if (checkValues(groupName) == false) {
        return false;
    }

    var param = {
        'groupName': groupName
    }

    var url = '/api/groups/create';
    ajax(url, param, "그룹이 등록되었습니다.");
}

function resetGroupInfo() {
    $('.groupName').val("");
}

function resetFailMessage() {
    $('.ajaxFailMessage').text('');
    $('.failMessageGroupName').text('');
}

function registGroup() {
    resetGroupInfo();
    resetFailMessage();
    $('.modalBackWrap').fadeIn(500);
}

function cancelGroup() {
    $('.modalBackWrap').fadeOut(500);
    resetGroupInfo();
    resetFailMessage();
}

function checkValues(groupName) {
    if (groupName == '') {
        $('.failMessageGroupName').text(' *그룹명을 입력해 주세요.');
        $('.groupName').focus();
        return false;
    }
    return true;
}

function handleBackWrapClick(event) {
    if (event.target.classList.contains('modalBackWrap')) {
        cancelGroup();
    }
}

setTimeout(function() {
    let card = document.getElementById("errorMessageCard");
    card.style.transition = "opacity 2s ease";
    card.style.opacity = 0;
    setTimeout(function() {
        card.style.display = "none";
    }, 2000);
}, 5000);