const PROJECT_STORAGE_KEY = "projectData";
let USER_INFO =null;
let progressInterval=null;
const HEADER_MAP = {
    home:'',
    project:'<ul class="nav_list">\n' +
        '       <li class="on">\n' +
        '           <span>테마</span>\n' +
        '       </li>\n' +
        '       <li>/</li>\n' +
        '       <li>\n' +
        '           <span>콘텐츠</span>\n' +
        '       </li>\n' +
        '     </ul>',
    quiz:'<ul class="nav_list">\n' +
        '   <li class="on">\n' +
        '       <span>퀴즈</span>\n' +
        '   </li>\n' +
        '   <li>/</li>\n' +
        '   <li>\n' +
        '       <span>AI퀴즈</span>\n' +
        '       <div class="tag">\n' +
        '           <img src="../assets/images/tag.svg" alt="">\n' +
        '           <button type="button" class="hide-tag-btn"></button>\n' +
        '       </div>\n' +
        '   </li>\n' +
        '</ul>'

}
$(document).ready(function(){
    console.log("main.js start!!")

    console.log(`userInfo = ${userInfo.user_id} , ${userInfo.user_name}`);

    USER_INFO = userInfo;

    let intervalId = setInterval(function (){
        console.log('keep-alive :: Request');
        fetch('/session/keep-alive').then(response =>{
            console.log('keep-alive :: Session refreshed');
        }).catch(error =>{
            alert("서버 연결 종료");
            console.log('Error refreshing session:', error);
        });
    },30000)

    let user_id = USER_INFO.user_id;
    console.log("user_id = {}",user_id);
    RequestServer("GET",`/main/my-project?user_id=${encodeURIComponent(user_id)}`,"",AddConDivHtml,"project");

    // .create_new 클릭 이벤트
    $('.book_mark .create_new').on('click', function (event) {
        event.preventDefault(); // 기본 동작 방지

        // 새 폴더 이름 입력받기
        var folderName = prompt("새 폴더 이름을 입력하세요:");
        if (folderName && folderName.trim() !== "") {
            // 새 li 요소 생성
            var newLi = `
                    <li>
                        <input type="checkbox" name="">
                        <label>
                            <div class="txt">
                                <i>
                                    <img src="../assets/images/i_nav6.svg" alt="">
                                </i>
                                <p>${folderName}</p>
                            </div>
                        </label>
                    </li>
                `;

            // .create_new li 위에 새 li 추가
            $(this).before(newLi);
        } else {
            alert("폴더 이름을 입력해주세요.");
        }
    });


    document.querySelectorAll('.modal_step_open').forEach(el => {
        el.addEventListener('click', event => {
            const folderId = el.getAttribute('data-folder-id');
            const folderName = el.getAttribute('data-folder-name');
            console.log('폴더 ID:', folderId);
            console.log('폴더 NAME:', folderName);
        });
    });
});

function CallSetting(){
    RequestServer("GET",
        `/main/setting?user_id=${encodeURIComponent(USER_INFO.user_id)}`,
        "",
        AddConDivHtmlForSetting,
        "setting");
}

function CallAccountDelete(){
    RequestServer("GET",
        `/main/account-delete`,
        "",
        AddConDivHtmlForSetting,
        "account-delete");
}

function CallQuiz(element){
    console.log("main.js :: CallQuiz");
    const projectId = element.getAttribute("data-project-id");

    RequestServerQuiz("GET",
        `/quiz/main?project_id=${projectId}`,
        "",
        AddContentDivHtmlForQuiz,
        projectId,
        1)
}

function CallQuizList(projectId,pageIndex){
    console.log(`main.js :: CallQuizList = ${projectId} , ${pageIndex}`);
    if(pageIndex == undefined){
        pageIndex = 1;
    }
    RequestServer("GET",
        `/quiz/main/page?project_id=${projectId}&currentPage=${pageIndex}`,
        "",
        AddTableWrapDivHtmlForQuiz);

}

function SaveAndProceed(step, value, nextUrl) {
    if (!value || value.length === 0) {
        alert("필수 정보를 입력해주세요.");
        return;
    }

    // 데이터를 저장
    SaveStepData(step, value);

    // 다음 단계로 이동
    if (nextUrl) {
        switch (step){
            case 'name':
                RequestServer("GET",nextUrl,"",AddContentDivHtmlForTheme,"theme");
                break;
            case 'theme':
                RequestServer("GET",nextUrl,"",AddContentDivHtmlForContent,"content");
                break;
        }
    }
}



// header_left 업데이트 함수
function UpdateHeaderLeft(menuKey) {
    console.log("main.js :: UpdateHeaderLeft");
    const headerLeft = document.getElementById('headerLeft');
    const newNavList = HEADER_MAP[menuKey];
    if (newNavList) {
        const navListElement = headerLeft.querySelector('.nav_list');
        if (navListElement) {
            navListElement.outerHTML = newNavList;
            console.log("main.js :: UpdateHeaderLeft :: outerHTML");
        }
    } else {
        console.warn(`No content mapped for menu: ${menuKey}`);
    }
}

// 데이터 저장
function SaveStepData(step, value) {
    let projectData = JSON.parse(localStorage.getItem(PROJECT_STORAGE_KEY)) || {};
    projectData[step] = value;
    localStorage.setItem(PROJECT_STORAGE_KEY, JSON.stringify(projectData));
    console.log("Saved Data:", projectData);
}

// 데이터 가져오기
function GetStepData() {
    return JSON.parse(localStorage.getItem(PROJECT_STORAGE_KEY)) || {};
}

// 특정 단계 데이터 가져오기
function GetSpecificStepData(step) {
    const projectData = GetStepData();
    return projectData[step] || null;
}

// 데이터 초기화
function resetProjectData() {
    localStorage.removeItem(PROJECT_STORAGE_KEY);
}

function RequestServer(type, url, dataString, successFunction,menu) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 30000,
        contentType: "application/json;charset=UTF-8",
        data : dataString,
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            //console.log('data : ', data);
            successFunction(data,menu);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
        }
    });
}

function RequestServerCreateAiQuiz(type, url, dataString, successFunction,failFunction) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 60000,
        contentType: "application/json;charset=UTF-8",
        data : dataString,
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            //console.log('data : ', data);
            successFunction(data);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
            failFunction(err);
        }
    });
}

function RequestServerQuiz(type, url, dataString, successFunction,projectId, pageIndex) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 30000,
        contentType: "application/json;charset=UTF-8",
        data : dataString,
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            //console.log('data : ', data);
            successFunction(data,projectId,pageIndex);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
        }
    });
}

function RequestServerCRUD(type, url, dataString, successFunction) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 30000,
        contentType: "application/x-www-form-urlencoded;charset=UTF-8",
        data : dataString,
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            //console.log('data : ', data);
            successFunction(data);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
        }
    });
}
function RequestServerCRUDWidthFail(type, url, dataString, successFunction, failFunction) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 30000,
        contentType: "application/x-www-form-urlencoded;charset=UTF-8",
        data : dataString,
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            //console.log('data : ', data);
            successFunction(data);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
            try {
                // 서버에서 보낸 JSON 응답 파싱
                const response = JSON.parse(xhr.responseText);
                failFunction(response); // 파싱된 데이터 전달
            } catch (e) {
                console.error("JSON 파싱 실패:", e);
                failFunction({ message: "알 수 없는 오류 발생", error_code: -1 });
            }
        }
    });
}

function RequestServerAiQuizItem(type, url, dataString, successFunction) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 30000,
        contentType: "application/json",
         data : JSON.stringify(dataString),
         dataType : "json", //xml, html, json, script, text
        success : function(data) {
            successFunction(data);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
        }
    });
}

function RequestServerFormData(type, url, dataString, successFunction) {
    if (type == null || $.trim(type) == "") type = "GET";
    if (url == null || $.trim(url) == "") return;

    console.log('type : ', type);
    console.log('action : ', url);
    console.log('data : ', dataString);

    $.ajax({
        type : type,
        url : url,
        timeout : 30000,
        contentType: false,  // FormData 전송을 위해 false로 설정
        processData: false,  // FormData 전송 시 필수 설정
        data : dataString,
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            //console.log('data : ', data);
            successFunction(data);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
        }
    });
}

function ChangeMaxParticipantsSuccess(data){
    console.log(`main.js :: ChangeMaxParticipantsSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.project_id} :: ${parsedData.max_participants}`);
    const participantElement  = document.getElementById(`p-max_participants-${parsedData.project_id}`);
    if(participantElement != null){
        console.log("participantElement :: not null");
        participantElement.textContent  =`참여인원 수정(${parsedData.max_participants})`;
    }else{
        console.log("participantElement :: null");
    }
}

function ChangeIsFavoriteSuccess(data){
    console.log(`main.js :: ChangeIsFavoriteSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.project_id} :: ${parsedData.is_favorite}`);
    const favoriteElement  = document.getElementById(`div-favorite-${parsedData.project_id}`);
    if(favoriteElement != null){
        console.log("favoriteElement :: not null");

        if(parsedData.is_favorite == 1){
            favoriteElement.classList.add('favorite-active');
        }else{
            favoriteElement.classList.remove('favorite-active');
        }
        favoriteElement.setAttribute("data-project-favorite",parsedData.is_favorite);
    }else{
        console.log("favoriteElement :: null");
    }
}

function ChangeFolderIsSelectedSuccess(data){
    console.log(`main.js :: ChangeFolderIsSelectedSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    const folderElement  = document.getElementById(`div-folder-${parsedData.project_id}-${parsedData.folder_id}`);

    if(parsedData.is_selected){
        folderElement.classList.add('favorite-active');
    }else{
        folderElement.classList.remove('favorite-active');
    }

    folderElement.setAttribute("data-folder-selected",parsedData.is_selected);
}

function ChangeIsDeletedSuccess(data){
    console.log(`main.js :: ChangeIsDeletedSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.project_id} :: ${parsedData.is_deleted}`);
    const deletedElement  = document.getElementById(`li-project-${parsedData.project_id}`);
    if(deletedElement != null){
        deletedElement.remove();
        console.log(`Element with ID li-project-${parsedData.project_id} has been removed.`);
    }else{
        console.log("deletedElement :: null");
    }
}
function PermanentDeleteSuccess(data){
    console.log(`main.js :: PermanentDeleteSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.project_id}`);
    const deletedElement  = document.getElementById(`li-project-${parsedData.project_id}`);
    if(deletedElement != null){
        deletedElement.remove();
        console.log(`Element with ID li-project-${parsedData.project_id} has been removed.`);
    }else{
        console.log("deletedElement :: null");
    }
}

function CreateFolderSuccess(data){
    console.log(`main.js :: CreateFolderSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.folder_id} :: ${parsedData.folder_name}`);

    // 새로운 li 요소 생성
    const newFolder = document.createElement('li');
    newFolder.setAttribute('data-folder-id', parsedData.folder_id);
    newFolder.setAttribute('data-folder-name', parsedData.folder_name);
    newFolder.setAttribute('id',`li-folder-${parsedData.folder_id}`);

    newFolder.innerHTML = `
        <a href="javascript:void(0);" onclick="RequestCon('folder-project', this)">
            <i><img src="../assets/images/i_nav6.svg" alt=""></i>
            <p id="'li-p-folder-'+${parsedData.folder_id}">${parsedData.folder_name}</p>
        </a>
        <button type="button" class="dropdown">
            <img src="../assets/images/dots.svg" alt="">
        </button>
        <ul class="drop_con">
            <li data-folder-id="${parsedData.folder_id}" data-folder-name="${parsedData.folder_name}">
                <a href="javascript:void(0);" onclick="OpenChangeFolderNameModal(this)">
                    <i><img src="../assets/images/i_drop7.svg" alt=""></i>
                    <p>폴더 이름 수정</p>
                </a>
            </li>
            <li class="del" data-folder-id="${parsedData.folder_id}" data-folder-name="${parsedData.folder_name}">
                <a href="javascript:void(0);" onclick="DeleteFolder(this)">
                    <i><img src="../assets/images/i_drop6.svg" alt=""></i>
                    <p>폴더 삭제</p>
                </a>
            </li>
        </ul>
    `;

    const list = document.getElementById("bookmark-menu");
    const items = list.getElementsByTagName('li');

    if(items != null){
        list.insertBefore(newFolder,items[items.length-1]);
        ReloadJS('script');
    }else{
        console.log("main.js :: CreateFolderSuccess :: Fail!!");
    }
}

function DeleteFolderSuccess(data) {
    console.log(`main.js :: DeleteFolderSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.folder_id}`);
    const deletedElement  = document.getElementById(`li-folder-${parsedData.folder_id}`);
    if(deletedElement != null){
        deletedElement.remove();
        console.log(`Element with ID li-folder-${parsedData.folder_id} has been removed.`);
    }else{
        console.log("deletedElement :: null");
    }
}

function ChangeFolderNameSuccess(data) {
    console.log(`main.js :: ChangeFolderNameSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.folder_id} : ${parsedData.folder_name}`);
    const folderElement  = document.getElementById(`li-p-folder-${parsedData.folder_id}`);
    if(folderElement != null){
        folderElement.textContent = parsedData.folder_name;
        console.log(`${parsedData.folder_name} 으로 폴더명이 변경 되었습니다.`);
        CloseModal('changeFolderNameModal');
    }else{
        console.log("folderElement :: null");
    }
}

function ChangeProjectNameSuccess(data){
    console.log(`main.js :: ChangeProjectNameSuccess :: ${data}`);
    let parsedData = JSON.parse(data);
    console.log(`${parsedData.project_id} : ${parsedData.project_name}`);
    const projectElement  = document.getElementById(`h-project-name-${parsedData.project_id}`);
    if(projectElement != null){
        projectElement.textContent = parsedData.project_name;
        console.log(`${parsedData.project_name} 으로 프로젝트명이 변경 되었습니다.`);

        CloseModal('changeProjectNameModal');
    }else{
        console.log("projectElement :: null");
    }
}
function CreateProjectSuccess(data){
    console.log(`main.js :: CreateProjectSuccess :: ${data}`);
    alert("프로젝트 생성 완료!!");
    let parsedData = JSON.parse(data);
    ///main/my-project?user_id=${encodeURIComponent(user_id)}
    RequestServerQuiz("GET",
        `/quiz/main?project_id=${parsedData.project_id}`,
        "",
        AddContentDivHtmlForQuiz,
        parsedData.project_id,
        1);
}
function AddConDivHtml(html,menu){
    console.log("main.js ::  AddConDivHtml Start")
    const contentDiv = document.querySelector('.con');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체
    ReloadJS('script');
    if(menu =='my-project' || menu == 'folder-project'){
        menu = 'project';
    }
    BindEvents(menu);
}
function AddConDivHtmlForSetting(html,menu){
    console.log("main.js ::  AddConDivHtmlForSetting Start")
    const contentDiv = document.querySelector('.content');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체
    ReloadJS('script');
    ReloadJS('setting');
    reloadCSS("common.css");
}
function AddContentDivHtmlForTheme(html,menu){
    console.log("main.js ::  AddContentDivHtmlForTheme Start")
    const contentDiv = document.querySelector('.content');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체

    //UpdateHeaderLeft('project');
    reloadCSS("common.css");
    ReloadJS('theme');
    console.log("main.js ::  AddContentDivHtmlForTheme End");
}

function AddContentDivHtmlForContent(html,menu){

    console.log("main.js ::  AddContentDivHtmlForContent Start")
    const contentDiv = document.querySelector('.content');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체

    reloadCSS("common.css");
    ReloadJS('content');
}

function AddContentDivHtmlForQuiz(html,projectId,pageIndex){
    console.log("main.js ::  AddContentDivHtmlForQuiz Start")
    const contentDiv = document.querySelector('.content');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체

    //UpdateHeaderLeft('quiz');
    reloadCSS("common.css");
    //ReloadJS('quiz');
    CallQuizList(projectId,pageIndex);
}
function AddTableWrapDivHtmlForQuiz(html){
    console.log("main.js ::  AddTableWrapDivHtmlForQuiz Start")
    const contentDiv = document.querySelector('.table_wrap');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체
    reloadCSS("common.css");
    ReloadJS('script');
    ReloadJS('quiz');
}

function ReloadJS(name){
    console.log(`ReloadJS :: ${name}`);
    let existingScript = document.querySelector(`script[src="../assets/scripts/${name}.js"]`);

    if (existingScript) {
        existingScript.remove();
        console.log(`delete ${name}.js`);
    }

    const script = document.createElement('script');
    script.src = `../assets/scripts/${name}.js`;
    script.onload = () => console.log(`Loaded ${name}.js`);
    script.onerror = () => console.error(`Failed to load ${name}.js`);
    document.head.appendChild(script);
    console.log(`add ${name}.js`);
}
function RequestCon(menu, element){
    console.log(`main.js :: RequestCon :: ${menu}`);
    setActiveMenu(element);

    switch (menu){
        case 'my-project':
        case 'trash':
        case 'favorite':
            RequestServer("GET",`/main/${menu}?user_id=${encodeURIComponent(USER_INFO.user_id)}`,"",AddConDivHtml,menu);
            break;
        case'folder-project':
            const li = element.closest('li');
            const folder_id = li.getAttribute('data-folder-id');
            const folder_name = li.getAttribute('data-folder-name');
            console.log(`folder_id = ${folder_id} , folder_name = ${folder_name}`);
            RequestServer("GET",`/main/${menu}?folder_id=${folder_id}&folder_name=${encodeURIComponent(folder_name)}&user_id=${encodeURIComponent(USER_INFO.user_id)}`,
                "",
                AddConDivHtml,
                menu);
            break;
        case 'quiz':
            RequestServer("GET",`/quiz/main?user_id=${encodeURIComponent(USER_INFO.user_id)}`,"",AddConDivHtml,menu);
            break;
    }
}

function setActiveMenu(element) {
    console.log("setActiveMenu");

    // 모든 메뉴 그룹에서 'on' 클래스 제거
    const allMenus = document.querySelectorAll('#main-menu li, #bookmark-menu li');
    allMenus.forEach(item => item.classList.remove('on'));

    // 클릭된 메뉴에 'on' 클래스 추가
    const parentLi = element.closest('li');
    if (parentLi) {
        parentLi.classList.add('on');
    }
}
function BindEvents(menu) {
    console.log(`BindEvents :: ${menu} :: START`);

    // 정렬 버튼
    const sortAscButton = document.getElementById('sort1');
    const sortDescButton = document.getElementById('sort2');
    const objectList = document.getElementById(`${menu}List`);

    let pre ="project";

    switch (menu){
        case'theme':
        case'content':
            pre=menu;
            return;
    }
    //검색
    const searchInput = document.getElementById(`${pre}Search`); // 검색 입력 필드



    // 정렬 함수
    function sortObjects(order) {
        // 프로젝트 리스트의 모든 항목 가져오기
        const objects = Array.from(objectList.children);

        // 정렬 실행
        objects.sort((a, b) => {
            const nameA = a.getAttribute(`data-${pre}-name`);
            const nameB = b.getAttribute(`data-${pre}-name`);
            return order === 'asc'
                ? nameA.localeCompare(nameB) // 오름차순
                : nameB.localeCompare(nameA); // 내림차순
        });

        // 정렬된 항목 다시 추가
        objects.forEach(object => objectList.appendChild(object));
    }

    // 이벤트 리스너 등록
    sortAscButton.addEventListener('change', () => sortObjects('asc'));
    sortDescButton.addEventListener('change', () => sortObjects('desc'));


    //검색로직
    const objects = Array.from(objectList.children); // 모든 프로젝트 리스트 항목

    // 검색 이벤트 리스너
    searchInput.addEventListener('input', function () {
        const query = searchInput.value.toLowerCase(); // 입력값 (소문자로 변환)

        objects.forEach(object => {
            const objectName = object.getAttribute(`data-${pre}-name`).toLowerCase(); // 프로젝트 이름
            if (objectName.includes(query)) {
                object.style.display = ''; // 검색어와 일치하면 표시
            } else {
                object.style.display = 'none'; // 일치하지 않으면 숨김
            }
        });
    });

    console.log(`BindEvents :: ${menu} :: END`);
}


function reloadCSS(cssFileName) {
    const links = document.querySelectorAll('link[rel="stylesheet"]');

    links.forEach(link => {
        // CSS 파일 이름이 common.css인지 확인
        if (link.href.includes(cssFileName)) {
            console.log("link.href.includes");
            const newLink = link.cloneNode(); // 기존 링크 복사
            newLink.href = link.href.split('?')[0] + '?' + new Date().getTime(); // 캐시 방지
            link.parentNode.replaceChild(newLink, link); // 새 링크로 교체
            console.log(`${cssFileName} reloaded`);
        }
    });
}
function StartUnity(element){
    console.log(`main.js :: StartUnity`);
    const projectId = element.getAttribute("data-project-id");
    window.open(`/main/unity?project_id=${projectId}`, 'newWindow', 'width=800,height=600');
}

function OpenParticipantModal(element){
    const li = element.closest('li');
    const maxParticipants = li.getAttribute('data-project-max_participants');
    const projectId = li.getAttribute('data-project-id');

    console.log(`main.js :: OpenParticipantModal :: ${projectId} : ${maxParticipants}` );
    // 모달 초기화
    const modal = document.getElementById('participantModal');
    const inputField = document.getElementById('max_participant');
    const saveButton = document.getElementById('saveParticipantButton');

    inputField.value = maxParticipants; // 초기값 설정

    saveButton.setAttribute('onclick', `UpdateMaxParticipant('${projectId}', document.getElementById('max_participant').value)`);
// 모달 열기
    modal.classList.remove('hidden');
}

function OpenProjectNameModal(element){
    console.log("main.js :: OpenProjectNameModal");

    const projectId = element.getAttribute("data-project-id");
    const projectName = element.getAttribute("data-project-name");
    const modal = document.getElementById('changeProjectNameModal');
    const inputField = document.getElementById('changeProjectName');
    const saveButton = document.getElementById('changeProjectNameButton');

    inputField.value = projectName; // 초기값 설정


    saveButton.setAttribute('onclick', `ChangeProjectName('${projectId}', document.getElementById('changeProjectName').value)`);
// 모달 열기
    modal.classList.remove('hidden');

    const currentLengthEL = document.querySelector(".change_project_name");
    if(currentLengthEL != null){
        currentLengthEL.textContent = projectName.length;
    }
}

function OpenChangeFolderNameModal(element){
    const li = element.closest('li');

    const folderId = li.getAttribute('data-folder-id');
    const folderName = li.getAttribute('data-folder-name');

    console.log(`main.js :: OpenChangeFolderNameModal :: ${folderId} : ${folderName}` );
    // 모달 초기화
    const modal = document.getElementById('changeFolderNameModal');
    const inputField = document.getElementById('changeFolderName');
    const saveButton = document.getElementById('changeFolderNameButton');

    inputField.value = folderName; // 초기값 설정

    saveButton.setAttribute('onclick', `ChangeFolderName('${folderId}', document.getElementById('changeFolderName').value)`);
// 모달 열기
    modal.classList.remove('hidden');

    const currentLengthEL = document.querySelector(".change_folder_name");
    if(currentLengthEL != null){
        currentLengthEL.textContent = folderName.length;
    }
}

function OpenDeleteModal(element){
    const li = element.closest('li');
    const projectId = li.getAttribute('data-project-id');
    const projectName = li.getAttribute('data-project-name');
    console.log(`main.js :: OpenDeleteModal :: ${projectId} :: ${projectName}` );
    // 모달 초기화
    const modal = document.getElementById('deleteModal');

    const deleteButton = document.getElementById('deleteButton');
    const modal_project_name = document.getElementById('modal-project-name');

    deleteButton.setAttribute('onclick', `PermanentDelete('${projectId}')`);
    modal_project_name.textContent=projectName;
// 모달 열기
    modal.classList.remove('hidden');
}

function CloseModal(id){
    const modal = document.getElementById(id);
    modal.classList.add('hidden');
}
function ChangeFolderName(folder_id, folder_name){
    console.log(`main.js :: ChangeFolderName :: ${folder_id} , ${folder_name}`);
    RequestServerCRUD("post",
        `/main/project/change-folder-name?folder_id=${folder_id}&folder_name=${encodeURIComponent(folder_name)}`,
        "",
        ChangeFolderNameSuccess);
}

function ChangeProjectName(project_id, project_name){
    console.log(`main.js :: ChangeProjectName :: ${project_id} , ${project_name}`);
    RequestServerCRUD("post",
        `/main/project/change-project-name?project_id=${project_id}&project_name=${encodeURIComponent(project_name)}`,
        "",
        ChangeProjectNameSuccess);
}

function UpdateMaxParticipant(project_id, max_participant){
    console.log(`main.js :: UpdateMaxParticipant :: ${project_id} , ${max_participant}`);
    RequestServerCRUD("post",
        `/main/project/max_participants?project_id=${project_id}&max_participants=${max_participant}`,
        "",
        ChangeMaxParticipantsSuccess);

}

function PermanentDelete(project_id){
    console.log(`main.js :: PermanentDelete :: ${project_id}`);
    RequestServerCRUD("post",
        `/main/project/permanent-deleted?project_id=${project_id}`,
        "",
        PermanentDeleteSuccess);
}

function UpdateFavorite(element){
    // 클릭된 요소의 데이터 가져오기
    const projectId = element.getAttribute('data-project-id');
    const isFavorite = element.getAttribute('data-project-favorite');

    console.log(`main.js :: UpdateFavorite :: project_id=${projectId}, is_favorite=${isFavorite}`);

    // 서버에 요청을 보낼 때, 상태를 반전해서 전달 (1 -> 0, 0 -> 1)
    const updatedIsFavorite = isFavorite == 1 ? 0 : 1;

    RequestServerCRUD("post",
        `/main/project/favorite?project_id=${projectId}&is_favorite=${updatedIsFavorite}`,
        "",
        ChangeIsFavoriteSuccess);
}

function ChangeFolderProject(element){
    console.log("main.js :: ChangeFolderProject");

    const projectId = element.getAttribute('data-project-id');
    const folderId=element.getAttribute('data-folder-id');
    console.log(`main.js :: ChangeFolderProject :: project_id=${projectId}, folderId=${folderId}`);

    const is_selected = element.getAttribute("data-folder-selected");

    RequestServerCRUD("post",
        `/main/project/folder?project_id=${projectId}&folder_id=${folderId}&is_selected=${is_selected}`,
        "",
        ChangeFolderIsSelectedSuccess);

}

function UpdateIsDeleted(element){
    // 클릭된 요소의 데이터 가져오기
    const projectId = element.getAttribute('data-project-id');
    const isDeleted = element.getAttribute('data-project-deleted');

    console.log(`main.js :: UpdateIsDeleted :: project_id=${projectId}, is_deleted=${isDeleted}`);

    // 서버에 요청을 보낼 때, 상태를 반전해서 전달 (1 -> 0, 0 -> 1)
    const updatedIsDeleted = isDeleted == 1 ? 0 : 1;

    RequestServerCRUD("post",
        `/main/project/deleted?project_id=${projectId}&is_deleted=${updatedIsDeleted}`,
        "",
        ChangeIsDeletedSuccess)
}

function ProjectLinkCopyToClipboard(element){
    // data-project-unity-link 속성의 값을 가져옵니다.
    const link = element.getAttribute("data-project-unity-link");

    // 클립보드에 복사
    navigator.clipboard.writeText(link)
        .then(() => {
            console.log("링크 복사 성공")
        })
        .catch(err => {
            console.error("클립보드 복사 실패: ", err);
        });
}

function CreateFolder(){
    console.log("main.js :: CreateFolder");
    const newFolderName = document.getElementById("newFolderName").value;
    console.log(`newFolderName = ${newFolderName}`);
    RequestServerCRUD("post",
        `/main/project/new-folder?user_id=${encodeURIComponent(USER_INFO.user_id)}&folder_name=${encodeURIComponent(newFolderName)}`,
        "",
        CreateFolderSuccess);
}

function DeleteFolder(element){
    console.log("main.js :: DeleteFolder");
    const li = element.closest('li');
    const folderId = li.getAttribute('data-folder-id');
    console.log(`main.js :: DeleteFolder :: folderId=${folderId}`);

    RequestServerCRUD("post",
        `/main/project/delete-folder?user_id=${encodeURIComponent(USER_INFO.user_id)}&folder_id=${folderId}`,
        "",
        DeleteFolderSuccess);
}
function GoToMain(){
    window.location.href = "/main/project";
}

function UpdatePassword(){
    console.log("main.js :: UpdatePassword");
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const newPasswordConfirm = document.getElementById("newPasswordConfirm").value;

    if(newPassword.length < 8){
        AlertPopup("alert_pass_length");
        return;
    }

    if(currentPassword == newPassword){
        AlertPopup("alert_pass_eq");
        document.getElementById("newPassword").value="";
        document.getElementById("newPasswordConfirm").value="";
        return;
    }
    if(newPassword == newPasswordConfirm){
        RequestServerCRUDWidthFail("POST",
            `/update-password?user_id=${encodeURIComponent(USER_INFO.user_id)}&currentPassword=${encodeURIComponent(currentPassword)}&newPassword=${encodeURIComponent(newPassword)}`,
            "",
            UpdatePasswordSuccess,
            UpdatePasswordFail
        );
    }else{
        AlertPopup("alert_pass_confirm");
        document.getElementById("newPasswordConfirm").value ="";
    }


}

function UpdatePasswordSuccess(data){
    console.log(`main.js :: UpdatePasswordSuccess :: ${data}`);
    AlertPopup("alert_pass_success");
    document.getElementById("currentPassword").value="";
    document.getElementById("newPassword").value="";
    document.getElementById("newPasswordConfirm").value="";
}

function UpdatePasswordFail(response){
    console.log(`main.js :: UpdatePasswordFail :: ${response}`);
    const failAlert = document.getElementById("p-alert-pass-fail");
    failAlert.textContent = response.message;
    AlertPopup("alert_pass_fail");
    document.getElementById("currentPassword").value="";
    document.getElementById("newPassword").value="";
    document.getElementById("newPasswordConfirm").value="";
}

function AlertPopup(alertName){
    console.log(`AlertPopup = ${alertName}`);
    alertName = `.${alertName}`;
    console.log(`alertName = ${alertName}`);
    $(alertName).addClass("on");
    setTimeout(function () {
        $(alertName).removeClass('on');
    }, 3000);

}