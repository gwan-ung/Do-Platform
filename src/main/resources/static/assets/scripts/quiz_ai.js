
$(document).ready(function() {
    console.log("quiz_ai js script");



    function updateQuizList() {
        $('.input_list > li').each(function (index) {
            const $inputItem = $(this);
            const $quizView = $('.quiz_list > li').eq(index);
            const $quizParagraph = $quizView.find('p');
            let value = '';

            // Handle input[type="text"]
            const $inputField = $inputItem.find('input[type="text"]');
            if ($inputField.length) {
                value = $inputField.val().trim();
            }

            // Handle textarea
            const $textareaField = $inputItem.find('textarea');
            if ($textareaField.length) {
                value = $textareaField.val().trim();
            }


            // Handle select fields
            const $selectFields = $inputItem.find('select');
            if ($selectFields.length > 0) {
                value = $selectFields.map(function () {
                    const selectedValue = $(this).find('option:selected').text().trim();
                    return selectedValue !== "선택" ? selectedValue : '';
                }).get().filter(v => v).join(' / ');
            }

            // Update the corresponding quiz view paragraph
            $quizParagraph.text(value || '');
        });
    }

    // Attach events to all input, textarea, and select elements
    $('.input_list').on('input change', 'input[type="text"], textarea, select', updateQuizList);

    // Trigger an initial update to populate any pre-filled values
    updateQuizList();

    const checkboxes = document.querySelectorAll('.quiz-checkbox');
    const applyQuizButton = document.getElementById('apply-quiz-btn');

    function updateButtonState() {
        const isAnyChecked = Array.from(checkboxes).some(checkbox => checkbox.checked);
        if(applyQuizButton != null){
            applyQuizButton.disabled = !isAnyChecked;  // 하나라도 체크되어 있으면 활성화
        }
    }

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', updateButtonState);
    });

    // 페이지 로드 시 버튼 상태 초기화
    updateButtonState();
});

function createAiMeta(element){
    console.log("quiz_ai.js :: createAiMeta");

    const is_new = element.getAttribute("data-is-new");

    const quizAiMeta={
        project_id:element.getAttribute("data-project-id"),
        topic: document.getElementById('input-topic').value.trim(),
        objective: document.getElementById('input-objective').value.trim(),
        content: document.getElementById('input-content').value.trim(),
        grade_level: document.getElementById('select-grade-level').value,
        difficulty_level: document.getElementById('select-difficulty-level').value,
        num_options: parseInt(document.getElementById('select-num-options').value) || null,
        num_questions:10,
        keyword: document.getElementById('input-keyword').value
    }
    console.log("퀴즈 메타 데이터:", quizAiMeta);

    if(is_new == "true"){
        RequestServerCRUD("POST",
            "/quiz/add-ai-quiz-meta",
            quizAiMeta,
            AddAiQuizMetaSuccess);
    }else{
        const id = element.getAttribute("data-id");
        quizAiMeta.id=id;
        RequestServerCRUD("POST",
            "/quiz/update-ai-quiz-meta",
            quizAiMeta,
            UpdateAiQuizMetaSuccess);
    }

}



function AddAiQuizMetaSuccess(data){
    console.log(`quiz_ai.js :: AddAiQuizMetaSuccess :: ${data}`);

    const modal = document.getElementById("createAiQuizModal");
    modal.classList.remove('hidden');
}

function UpdateAiQuizMetaSuccess(data){
    console.log(`quiz_ai.js :: UpdateAiQuizMetaSuccess :: ${data}`);

    const modal = document.getElementById("createAiQuizModal");
    modal.classList.remove('hidden');
}

function CreateAiQuizSuccess(html){
    console.log("quiz_ai.js :: CreateAiQuizSuccess");
    const modal = document.getElementById("creatingAiQuizModal");

    modal.classList.add('hidden');
    // 초기화

    const contentDiv = document.querySelector('.content');
    contentDiv.innerHTML = html; // 받은 HTML로 내용 교체

    ReloadJS('script');
    ReloadJS('quiz_ai');
}
function CreateAiQuizFail(err){
    alert("AI-퀴즈를 생성하는데 실패 하였습니다.");
    const modal = document.getElementById("creatingAiQuizModal");
    modal.classList.add('hidden');
}

function RetryAiQuizCreateModal(){
    console.log("quiz_ai.js :: RetryAiQuizCreateModal");

    const modal = document.getElementById("createAiQuizModal");
    modal.classList.remove('hidden');
}
function RetryAiQuizCreate(element){
    console.log("quiz_ai.js :: RetryAiQuizCreate");

    const modal = document.getElementById("createAiQuizModal");
    modal.classList.add('hidden');

    const modal2 = document.getElementById("creatingAiQuizModal");
    modal2.classList.remove('hidden');
    const progressBar = modal2.querySelector('progress');
    let progress = 0;
    let direction = 1;
    progressBar.value = progress;

    // 기존 interval이 있으면 중지
    if (progressInterval !== null) {
        clearInterval(progressInterval);
    }

    progressInterval = setInterval(() => {
        progress += 2 * direction;  // 2씩 증가 또는 감소
        progressBar.value = progress;

        // 최대치 또는 최소치에 도달하면 방향 변경
        if (progress >= 100 || progress <= 0) {
            direction *= -1;  // 방향 반전
        }
    }, 50);

    const projectId = element.getAttribute("data-project-id");

    RequestServer("GET",
        `/quiz/create-ai-quiz?project_id=${projectId}`,
        "",
        CreateAiQuizSuccess
    )
}

function CreateAiQuizForServer(element){
    console.log("quiz_ai.js :: CreateAiQuizForServer");

    const modal = document.getElementById("createAiQuizModal");
    modal.classList.add('hidden');

    const modal2 = document.getElementById("creatingAiQuizModal");
    modal2.classList.remove('hidden');
    const progressBar = modal2.querySelector('progress');
    let progress = 0;
    let direction = 1;
    progressBar.value = progress;

    // 기존 interval이 있으면 중지
    if (progressInterval !== null) {
        clearInterval(progressInterval);
    }

    progressInterval = setInterval(() => {
        progress += 2 * direction;  // 2씩 증가 또는 감소
        progressBar.value = progress;

        // 최대치 또는 최소치에 도달하면 방향 변경
        if (progress >= 100 || progress <= 0) {
            direction *= -1;  // 방향 반전
        }
    }, 50);


    const projectId = element.getAttribute("data-project-id");

    RequestServerCreateAiQuiz("GET",
        `/quiz/create-ai-quiz?project_id=${projectId}`,
        "",
        CreateAiQuizSuccess,
        CreateAiQuizFail
    )

}

function getCheckedQuestions() {

    const checkboxes = document.querySelectorAll('.quiz-checkbox');

    const checkedQuestions = [];
    checkboxes.forEach((checkbox) => {
        if (checkbox.checked) {
            const questionContainer = checkbox.closest('li');
            const questionText = questionContainer.querySelector('.head_left label').textContent.trim().split('. ')[1];
            const optionsElements = questionContainer.querySelectorAll('.select_list p');
            const answer = questionContainer.querySelector('.correct p').textContent;
            const explanationsElements = questionContainer.querySelectorAll('.info_txt li');

            const options = Array.from(optionsElements).map(option => option.textContent);
            //const explanations = {};

            const explanationsArray = [];
            explanationsElements.forEach(exp => {
                const key = exp.querySelector('strong').textContent.trim();
                const value = exp.textContent.replace(key, '').trim();  // `strong` 태그를 제거한 설명
                //explanations[key] = value;
                explanationsArray.push(`${key}`);
            });
            const explanations = explanationsArray.join('\n');
            checkedQuestions.push({
                question: questionText,
                options: options,
                answer: answer,
                explanations: explanations
            });
        }
    });
    return checkedQuestions;
}

function ApplyAiQuiz(element){
    const selectedQuestions = getCheckedQuestions();
    const projectId = element.getAttribute("data-project-id");

    const requestData ={
        project_id:parseInt(projectId),
        quizItems:selectedQuestions
    }
    if (selectedQuestions.length > 0) {
        console.log("체크된 문제 리스트:", selectedQuestions);

        RequestServerAiQuizItem("POST",
            "/quiz/add-ai-quiz-items",
            requestData,
            AdjustAiQuizItemsSuccess)

    } else {
        alert("선택된 문제가 없습니다.");
    }
}

function AdjustAiQuizItemsSuccess(data){
    console.log(`quiz_ai.js :: AdjustAiQuizItemsSuccess :: ${data.message}`);

    RequestServerQuiz("GET",
        `/quiz/main?project_id=${data.project_id}`,
        "",
        AddContentDivHtmlForQuiz,
        data.project_id,
        1)
}