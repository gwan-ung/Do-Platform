$(document).ready(function(){
    console.log("content script");
    ReloadJS("script");
    BindEvents('content');

    $('.prod_list li').on('click', function () {
        console.log("click :: prod list");

        // 모든 li에서 .on 클래스 제거
        $('.prod_list li').removeClass('on');

        // 현재 클릭된 li의 .on 클래스 토글
        $(this).toggleClass('on');

        // .on 클래스가 활성화된 li가 있는지 확인
        if ($('.prod_list li.on').length > 0) {
            // .btn button.disabled_btn의 disabled 속성 제거
            $('.btn button.disabled_btn').prop('disabled', false).removeClass('disabled_btn');
        } else {
            // 활성화된 li가 없으면 disabled 속성 추가
            $('.btn button').prop('disabled', true).addClass('disabled_btn');
        }
    });
});
function GoTheme(){
    console.log("content.js :: GoTheme :: START");
    RequestServer("GET","/main/theme","",AddContentDivHtmlForTheme,"theme");
}

function saveProject(){
    if(validateInputNum()){

        let _name =  GetSpecificStepData('name');
        let _theme = GetSpecificStepData('theme');
        let _content = document.querySelector('.prod_list li.on').getAttribute('data-content-id');
        const inputField = document.getElementById('numberInput'); // 입력 필드 선택
        let _max_participants = inputField.value;

        let Project_Object ={
            user_id : USER_INFO.user_id,
            name : _name,
            theme:_theme,
            content:_content,
            max_participants:_max_participants
        }

        console.log("Project_Object:",Project_Object);

        RequestServerCRUD("POST","/main/new/project",Project_Object,CreateProjectSuccess);

    }else{
        return;
    }
}

function validateInputNum() {
    const inputField = document.getElementById('numberInput');
    const value = parseFloat(inputField.value);
    const min = parseFloat(inputField.min);
    const max = parseFloat(inputField.max);

    // 모든 팝업 초기화
    const confirms = document.querySelectorAll('.del_confirm');
    confirms.forEach(confirm => confirm.classList.remove('on'));

    if (!value) {
        document.querySelector('.del_confirm:nth-of-type(1)').classList.add('on'); // 최소값 팝업
        inputField.focus();
    }

    if (value < min) {
        document.querySelector('.del_confirm:nth-of-type(1)').classList.add('on'); // 최소값 팝업
        inputField.focus();
        return false;
    } else if (value > max) {
        document.querySelector('.del_confirm:nth-of-type(2)').classList.add('on'); // 최대값 팝업
        inputField.focus();
    } else {
        return true;
        // 여기서 저장 로직을 추가할 수 있습니다.
    }
    return false;
}