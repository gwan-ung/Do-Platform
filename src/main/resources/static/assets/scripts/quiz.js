$(document).ready(function() {
    console.log("quiz script");
    const pagiList = document.querySelector('.pagi_list');
    let currentPage = parseInt(pagiList.dataset.currentPage);  // let으로 변경
    const totalPages = parseInt(pagiList.dataset.totalPages);
    const projectId = pagiList.dataset.projectId;
    console.log(`quiz.js :: projectId=${projectId} ,currentPage=${currentPage}, totalPages=${totalPages}`);

    function updatePagination(page) {
        if (totalPages === 0) {
            pagiList.style.display = 'none';
            return;
        } else {
            pagiList.style.display = 'flex';
        }
        const startPage = Math.floor((page - 1) / 5) * 5 + 1;
        const endPage = Math.min(startPage + 4, totalPages);

        let pageHtml = `
            <li class="prev ${startPage === 1 ? 'disabled' : ''}">
                <a href="#" id="prevButton" ${startPage === 1 ? 'style="pointer-events:none; opacity:0.5;"' : ''}>
                    <img src="../assets/images/pagi_prev.svg" alt="이전">
                </a>
            </li>
        `;
        for (let i = startPage; i <= endPage; i++) {
            pageHtml += `
                <li class="page-num ${i === page ? 'on' : ''}">
                    <a href="#" data-page="${i}">${i}</a>
                </li>
            `;
        }
        pageHtml += `
            <li class="next ${endPage === totalPages ? 'disabled' : ''}">
                <a href="#" id="nextButton" ${endPage === totalPages ? 'style="pointer-events:none; opacity:0.5;"' : ''}>
                    <img src="../assets/images/pagi_next.svg" alt="다음">
                </a>
            </li>
        `;
        pagiList.innerHTML = pageHtml;
    }

    pagiList.addEventListener('click', (e) => {
        e.preventDefault();  // preventDefault 위치 이동
        const target = e.target.closest('a');
        if (!target) return;

        if (target.dataset.page) {
            currentPage = parseInt(target.dataset.page);
            updatePagination(currentPage);
            goToPage(projectId,currentPage);  // 페이지 이동
        } else if (target.id === 'nextButton') {
            if (currentPage + 5 <= totalPages) {
                currentPage = Math.floor((currentPage - 1) / 5) * 5 + 6;
                updatePagination(currentPage);
                goToPage(projectId,currentPage);  // 페이지 이동
            }
        } else if (target.id === 'prevButton') {
            if (currentPage - 5 > 0) {
                currentPage = Math.floor((currentPage - 1) / 5) * 5 - 4;
                updatePagination(currentPage);
                goToPage(projectId,currentPage);  // 페이지 이동
            }
        }
    });

    // 초기 페이지네이션 설정 호출
    updatePagination(currentPage);

    // document.getElementById("add-form").addEventListener("submit", function (e){
    //
    // });
});

function goToPage(project_id, currentPage){
    console.log(`quiz.js :: goToPage project_id=${project_id } , currentPage =${currentPage}`);
    CallQuizList(project_id,currentPage);
}

function CallAiPage(project_id){
    console.log(`quiz.js :: CallAiPage :: project_id = ${project_id}`);
    RequestServer("GET",
        `/quiz/ai-quiz?project_id=${project_id}`,
        "",
        AddContentDivHtmlForAi
        );
}

function AddContentDivHtmlForAi(html){
    console.log("quiz.js ::  AddContentDivHtmlForAi Start")
    const contentDiv = document.querySelector('.content');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체

    reloadCSS("common.css");
    ReloadJS('script');
    ReloadJS('quiz_ai');

}

function OpenQuizModal(element){
    console.log("quiz.js :: OpenQuizModal")
    const projectId = element.getAttribute("data-project-id");

    // 모달 초기화
    const modal = document.getElementById('quizAddModal');
    document.getElementById("modal-project-id").setAttribute("data-project-id",projectId);
    modal.classList.remove('hidden');
}

function OpenQuizDeleteModal(element){
    console.log("quiz.js :: OpenQuizDeleteModal")
    const quizId = element.getAttribute("data-quiz-id");
    const projectId = element.getAttribute("data-project-id");
    const fileName = element.getAttribute("data-quiz-file-name");
    const quizCount = element.getAttribute("data-quiz-count");
    // 모달 초기화
    const modal = document.getElementById('quizDeleteModal');
    document.getElementById("modal-delete-project-id").setAttribute("data-project-id",projectId);
    document.getElementById("modal-delete-quiz-id").setAttribute("data-quiz-id",quizId);
    document.getElementById("modal-delete-quiz-file-name").setAttribute("data-quiz-file-name",fileName);
    document.getElementById("modal-delete-quiz-count").setAttribute("data-quiz-count",quizCount);
    modal.classList.remove('hidden');
}

function DeleteQuizItem(){
    console.log("quiz.js :: DeleteQuizItem");

    const projectId = document.getElementById("modal-delete-project-id").getAttribute("data-project-id");
    const quizId = document.getElementById("modal-delete-quiz-id").getAttribute("data-quiz-id");
    const fileName = document.getElementById("modal-delete-quiz-file-name").getAttribute("data-quiz-file-name");
    let currentPage = document.getElementById("ul-page-list").getAttribute("data-current-page");
    const quizCount = document.getElementById("modal-delete-quiz-count").getAttribute("data-quiz-count");
    console.log(`projectId=${projectId} , quizId=${quizId} , fileName=${fileName}, currentPage=${currentPage} , quizCount=${quizCount}`);

    if(quizCount == 1 || quizCount == "1"){
        if(parseInt(currentPage) != 1){
            currentPage = currentPage -1;
        }
    }
    let _fileName="";
    if(fileName == null || fileName.length ==0){
        _fileName = null;
    }else{
        _fileName = encodeURIComponent(fileName);
    }


    RequestServerCRUD(
        "POST",
        `/quiz/delete-quiz-item?project_id=${projectId}&currentPage=${currentPage}&item_id=${quizId}&file_name=${_fileName}`,
        "",
        DeleteQuizItemSuccess
    )
}

function AddProjectQuiz(){
    console.log("quiz.js :: AddProjectQuiz");
    let isValid = true; // 유효성 검사를 위한 변수
    const questionInput = document.getElementById("input-question");
    const answerInput = document.getElementById("input-answer");
    const explanationInput = document.getElementById("input-explanation");

    // 입력 필드 배열
    const fields = [
        { element: questionInput, errorMessage: "문제를 입력해주세요." },
        { element: answerInput, errorMessage: "정답을 입력해주세요." },
        { element: explanationInput, errorMessage: "해설을 입력해주세요." }
    ];

    // 각 필드 유효성 검사
    fields.forEach((field) => {
        const { element, errorMessage } = field;

        // 값이 비어 있는 경우
        if (!element.value.trim()) {
            isValid = false;
            element.classList.add("input-error"); // 시각적 강조를 위해 클래스 추가
            if (!element.nextElementSibling || element.nextElementSibling.className !== "error-text") {
                const errorText = document.createElement("p");
                errorText.className = "error-text";
                errorText.style.color = "red";
                errorText.textContent = errorMessage;
                element.parentNode.appendChild(errorText);
            }
        } else {
            element.classList.remove("input-error"); // 에러 클래스 제거
            if (element.nextElementSibling && element.nextElementSibling.className === "error-text") {
                element.nextElementSibling.remove(); // 기존 에러 메시지 제거
            }
        }
    });

    if (!isValid) {
        console.log("quiz.js :: isValid = false");
       return;
    }

    const question = document.getElementById("input-question").value;
    const answer = document.getElementById("input-answer").value;
    const explanation = document.getElementById("input-explanation").value;
    const option1 = document.getElementById("input-option1").value;
    const option2 = document.getElementById("input-option2").value;
    const option3 = document.getElementById("input-option3").value;
    const option4 = document.getElementById("input-option4").value;
    const fileInput = document.getElementById("input-file").files[0];  // 파일 객체
    const projectId = document.getElementById("modal-project-id").getAttribute("data-project-id");
    const currentPage = document.getElementById("ul-page-list").getAttribute("data-current-page");
    // 값 확인 로그
    console.log("문제:", question);
    console.log("정답:", answer);
    console.log("해설:", explanation);
    console.log("보기1:", option1);
    console.log("보기2:", option2);
    console.log("보기3:", option3);
    console.log("보기4:", option4);
    console.log("업로드된 파일:", fileInput ? fileInput.name : "파일 없음");
    console.log("프로젝트 아이디 :",projectId);
    console.log("최근페이지:",currentPage);

    let formData = new FormData();
    formData.append("currentPage", currentPage);
    formData.append("project_id", parseInt(projectId));
    formData.append("is_ai_created", 0);
    formData.append("question", question);
    formData.append("answer", answer);
    formData.append("explanation", explanation);
    formData.append("option1", option1);
    formData.append("option2", option2);
    formData.append("option3", option3);
    formData.append("option4", option4);

    let url = `/quiz/add-quiz-item`;
    // 파일이 있으면 추가
    if (fileInput) {
        formData.append("file", fileInput);
    }

    RequestServerFormData("POST",
        url,
        formData,
        InsertQuizItemSuccess);
}

function CloseAddQuizModal(){
    console.log("quiz.js :: CloseAddQuizModal");
    document.getElementById("input-question").value = "";
    document.getElementById("input-answer").value = "";
    document.getElementById("input-explanation").value = "";
    document.getElementById("input-option1").value = "";
    document.getElementById("input-option2").value = "";
    document.getElementById("input-option3").value = "";
    document.getElementById("input-option4").value = "";
    document.getElementById("input-file").value = "";
    document.getElementById("modal-project-id").setAttribute("data-project-id","")
    // 모달 숨기기
    document.getElementById("quizAddModal").classList.add("hidden");
}

function InsertQuizItemSuccess(data){
    console.log(`quiz.js :: InsertQuizItemSuccess :: ${data}`);

    let parseData = JSON.parse(data);

    RequestServer("GET",
        `/quiz/main/page?project_id=${parseData.project_id}&currentPage=${parseData.currentPage}`,
        "",
        AddTableWrapDivHtmlForQuiz
        );
    CloseAddQuizModal();

}

function UpdateQuizItemSuccess(data){
    console.log(`quiz.js :: UpdateQuizItemSuccess :: ${data}`);

    let parseData = JSON.parse(data);
    RequestServer("GET",
        `/quiz/main/page?project_id=${parseData.project_id}&currentPage=${parseData.currentPage}`,
        "",
        AddTableWrapDivHtmlForQuiz
    );
    CloseQuizEditModal();

}

function DeleteQuizItemSuccess(data){
    console.log(`quiz.js :: DeleteQuizItemSuccess :: ${data}`);

    let parseData = JSON.parse(data);
    RequestServer("GET",
        `/quiz/main/page?project_id=${parseData.project_id}&currentPage=${parseData.currentPage}`,
        "",
        AddTableWrapDivHtmlForQuiz
    );
    CloseQuizDeleteModal();

}

function showFileName(event) {
    const fileInput = event.target;  // 파일 input 요소
    const fileNameDisplay = document.getElementById("file-name-display");  // 파일 이름을 표시할 요소
    const errorMessage = document.getElementById("errorMessage");

    errorMessage.textContent = "";
    if (fileInput.files.length > 0) {
        fileNameDisplay.textContent = fileInput.files[0].name;  // 파일 이름 표시
        const maxSize = 2 * 1024 * 1024; // 2MB
        if (fileInput.files[0].size > maxSize) {
            errorMessage.textContent = "파일 크기는 2MB를 초과할 수 없습니다.";
            return;
        }

        const validTypes = ["image/jpeg", "image/png"];
        if (!validTypes.includes(fileInput.files[0].type)) {
            errorMessage.textContent = "JPG 또는 PNG 파일만 업로드 가능합니다.";
            return;
        }
    } else {
        fileNameDisplay.textContent = "선택된 파일이 없습니다.";  // 파일이 없을 때 기본 텍스트
    }


}

function showFileNameForEdit(event) {
    const fileInput = event.target;  // 파일 input 요소
    const fileNameDisplay = document.getElementById("p-edit-input-image-url");  // 파일 이름을 표시할 요소

    if (fileInput.files.length > 0) {
        fileNameDisplay.textContent = fileInput.files[0].name;  // 파일 이름 표시
    } else {
        fileNameDisplay.textContent = "선택된 파일이 없습니다.";  // 파일이 없을 때 기본 텍스트
    }
}

function OpenQuizEditModal(element){
    const projectId= element.getAttribute("data-project-id");
    const id = element.getAttribute("data-id");
    const question = element.getAttribute("data-question");
    const answer = element.getAttribute("data-answer");
    const explanation = element.getAttribute("data-explanation");
    const option1 = element.getAttribute("data-option1");
    const option2 = element.getAttribute("data-option2");
    const option3 = element.getAttribute("data-option3");
    const option4 = element.getAttribute("data-option4");
    const imageUrl = element.getAttribute("data-image-url");

    document.getElementById("edit-input-question").value = question;
    document.getElementById("edit-input-answer").value = answer;
    document.getElementById("edit-input-explanation").value = explanation;
    document.getElementById("edit-input-option1").value = option1;
    document.getElementById("edit-input-option2").value = option2;
    document.getElementById("edit-input-option3").value = option3;
    document.getElementById("edit-input-option4").value = option4;

    // 이미지 URL 표시
    const imageElement = document.getElementById("p-edit-input-image-url");
    imageElement.textContent = imageUrl ? imageUrl : "파일 없음";

    document.getElementById("edit-modal-project-id").setAttribute("data-project-id",projectId);
    document.getElementById("edit-modal-quiz-id").setAttribute("data-quiz-id",id);
    // 모달창 보이기
    const modal = document.getElementById("quizEditModal");
    modal.classList.remove("hidden");

}
function EditProjectQuiz(){
    console.log("quiz.js :: EditProjectQuiz");
    let isValid = true; // 유효성 검사를 위한 변수
    const questionInput = document.getElementById("edit-input-question");
    const answerInput = document.getElementById("edit-input-answer");
    const explanationInput = document.getElementById("edit-input-explanation");

    // 입력 필드 배열
    const fields = [
        { element: questionInput, errorMessage: "문제를 입력해주세요." },
        { element: answerInput, errorMessage: "정답을 입력해주세요." },
        { element: explanationInput, errorMessage: "해설을 입력해주세요." }
    ];

    // 각 필드 유효성 검사
    fields.forEach((field) => {
        const { element, errorMessage } = field;

        // 값이 비어 있는 경우
        if (!element.value.trim()) {
            isValid = false;
            element.classList.add("input-error"); // 시각적 강조를 위해 클래스 추가
            if (!element.nextElementSibling || element.nextElementSibling.className !== "error-text") {
                const errorText = document.createElement("p");
                errorText.className = "error-text";
                errorText.style.color = "red";
                errorText.textContent = errorMessage;
                element.parentNode.appendChild(errorText);
            }
        } else {
            element.classList.remove("input-error"); // 에러 클래스 제거
            if (element.nextElementSibling && element.nextElementSibling.className === "error-text") {
                element.nextElementSibling.remove(); // 기존 에러 메시지 제거
            }
        }
    });

    if (!isValid) {
        console.log("quiz.js :: EditProjectQuiz :: isValid = false");
        return;
    }

    const question = document.getElementById("edit-input-question").value;
    const answer = document.getElementById("edit-input-answer").value;
    const explanation = document.getElementById("edit-input-explanation").value;
    const option1 = document.getElementById("edit-input-option1").value;
    const option2 =document.getElementById("edit-input-option2").value;
    const option3 =document.getElementById("edit-input-option3").value;
    const option4 =document.getElementById("edit-input-option4").value;
    const image_url =document.getElementById("edit-input-image-url").files[0];
    const projectId =document.getElementById("edit-modal-project-id").getAttribute("data-project-id");
    const quizId =document.getElementById("edit-modal-quiz-id").getAttribute("data-quiz-id");
    const currentPage = document.getElementById("ul-page-list").getAttribute("data-current-page");

    let formData = new FormData();
    formData.append("id",parseInt(quizId));
    formData.append("project_id",parseInt(projectId));
    formData.append("id",quizId);
    formData.append("is_ai_created",0);
    formData.append("question",question);
    formData.append("answer",answer);
    formData.append("explanation",explanation);
    formData.append("option1",option1);
    formData.append("option2",option2);
    formData.append("option3",option3);
    formData.append("option4",option4);
    formData.append("currentPage",currentPage);
    if(image_url){
        formData.append("file",image_url);
    }

    RequestServerFormData("POST",
        `/quiz/edit-quiz-item?currentPage=${currentPage}`,
        formData,
        UpdateQuizItemSuccess);

}
function CloseQuizEditModal(){
    console.log("quiz.js :: CloseQuizEditModal");
    document.getElementById("edit-input-question").value = "";
    document.getElementById("edit-input-answer").value = "";
    document.getElementById("edit-input-explanation").value = "";
    document.getElementById("edit-input-option1").value = "";
    document.getElementById("edit-input-option2").value = "";
    document.getElementById("edit-input-option3").value = "";
    document.getElementById("edit-input-option4").value = "";
    document.getElementById("edit-input-image-url").value ="";
    document.getElementById("edit-modal-project-id").setAttribute("data-project-id","");
    document.getElementById("edit-modal-quiz-id").setAttribute("data-quiz-id","");

    const modal = document.getElementById("quizEditModal");
    modal.classList.add("hidden");
}

function CloseQuizDeleteModal(){
    console.log("quiz.js :: CloseQuizDeleteModal");
    document.getElementById("modal-delete-project-id").setAttribute("data-project-id","");
    document.getElementById("modal-delete-quiz-id").setAttribute("data-quiz-id","");

    const modal = document.getElementById("quizDeleteModal");
    modal.classList.add("hidden");
}