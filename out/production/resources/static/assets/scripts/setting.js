$(document).ready(function(){
    console.log("setting.js :: start");


});

function OpenChangeName(element){
    console.log("setting.js :: OpenChangeName");
    const userName = document.getElementById('input-user-name').value;
    const modal = document.getElementById('changeUserNameModal');
    const inputField = document.getElementById('changeUserName');
    const saveButton = document.getElementById('changeUserNameButton');

    inputField.value = userName; // 초기값 설정

    saveButton.setAttribute('onclick', `ChangeUserName(document.getElementById('changeUserName').value)`);
// 모달 열기
    modal.classList.remove('hidden');

    const currentLengthEL = document.querySelector(".change_user_name");
    if(currentLengthEL != null){
        currentLengthEL.textContent = userName.length;
    }
}

function ChangeUserName(userName){
    console.log(`setting.js :: ChangeUserName :: ${userName}`);
    RequestServerCRUD("post",
        `/main/change-user-name?user_id=${encodeURIComponent(USER_INFO.user_id)}&user_name=${encodeURIComponent(userName)}`,
        "",
        ChangeUserNameSuccess);
}

function ChangeUserNameSuccess(data) {
    console.log(`setting.js :: ChangeUserNameSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.user_name}`);
    const userElement  = document.getElementById(`input-user-name`);
    if(userElement != null){
        userElement.setAttribute("value",parsedData.user_name);
        USER_INFO.user_name = parsedData.user_name;

        const tilUserElements  = document.querySelectorAll(".til-user-name");
        tilUserElements.forEach((element) => {
            element.innerText = parsedData.user_name; // 사용자 이름 변경
        });

        const circleIconElement = document.querySelector(".circle-icon");
        circleIconElement.innerText = parsedData.user_name.charAt(0);

        console.log(`${parsedData.user_name} 으로 폴더명이 변경 되었습니다.`);
        CloseModal('changeUserNameModal');
    }else{
        console.log("folderElement :: null");
    }
}

function OpenUserDeleteModal(){
    console.log("account_delete.js :: OpenUserDeleteModal");
    const modal = document.getElementById('userDeleteModal');
// 모달 열기
    modal.classList.remove('hidden');
}

function UserDelete(){
    console.log(`account_delete.js :: UserDelete`);
    RequestServerCRUD("post",
        `/main/delete-user?user_id=${encodeURIComponent(USER_INFO.user_id)}`,
        "",
        UserDeleteSuccess);
}

function UserDeleteSuccess(data) {
    console.log(`account_delete.js :: UserDeleteSuccess :: ${data}`);
    window.location.href  = "/logout";
}