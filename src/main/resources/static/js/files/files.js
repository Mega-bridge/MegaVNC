function excuteSearchQues(){
    resetFailMessage();

    var selectGroup = $('.selectGroup').val();
    var pcName = $('.pcName').val();
    var accessPassword = $('.accessPassword').val();

    if(selectGroup == ''){
        $('.selectGroupFailMessage').text('*그룹을 선택해 주세요.');
        document.querySelector('.selectGroup').focus();
        return false;
    }
    if(pcName == ''){
        $('.pcNameFailMessage').text('*PC이름을 입력해 주세요.');
        document.querySelector('.pcName').focus();
        return false;
    }
    if(accessPassword == ''){
        $('.accessPasswordFailMessage').text('*접근 비밀번호를 입력해 주세요.');
        document.querySelector('.accessPassword').focus();
        return false;
    }

    console.log(selectGroup, pcName, accessPassword);
    //해당 부분에서 ajax통신으로
    //[이름, 파일크기, 다운로드링크, 등록날짜, 파일아이콘] 받아오기
    //리턴받은 데이터 리스트 그릴 때 사용

    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';

    for (let i = 0; i < 5; i++) {
        // 새로운 행 생성
        const newRow = document.createElement('tr');
        newRow.classList.add('data-row');

        // 각 열 생성 및 추가
        const idCell = document.createElement('td');
        idCell.textContent = i+1;
        newRow.appendChild(idCell);

        const groupNameCell = document.createElement('td');
        groupNameCell.textContent = 'filesDown' + i + ".exe";
        newRow.appendChild(groupNameCell);

        const dateCell = document.createElement('td');
        dateCell.textContent = '2024-06-14 09:24:26';
        newRow.appendChild(dateCell);

        const downloadCell = document.createElement('td');
        const downloadLink = document.createElement('a');
        downloadLink.href = 'https://www.naver.com';
        downloadLink.target = '_blank';
        downloadLink.classList.add('btn', 'btn-primary');
        downloadLink.innerHTML = '<i class="fa-solid fa-download" me-2" aria-hidden="true"></i>다운로드';
        downloadLink.onclick = function(event) {
            event.preventDefault();
            // window.open(downloadLink.href, '_blank');
            Swal.fire({
                position: "top-end",
                icon: "success",
                title: "Downloading . . .",
                showConfirmButton: false,
                timer: 2000
            });
        };
        downloadCell.appendChild(downloadLink);
        newRow.appendChild(downloadCell);

        // 새로운 행을 tbody에 추가
        tbody.appendChild(newRow);
    }

    $('.inputWrap').hide();
    $('.displayNone').show();
}

function goBack(){
    $('.inputWrap').show();
    $('.displayNone').hide();
}

function resetFailMessage(){
    $('.selectGroupFailMessage').text('');
    $('.pcNameFailMessage').text('');
    $('.accessPasswordFailMessage').text('');
}