$(document).ready(function(){


    // 모든 필드 및 버튼 선택
    const $formInputs = $(".input_list").find("input, textarea, select");
    const $submitButton = $(".btn_yellow.disabled_btn2");

    // 유효성 검사 함수
    function validateFields() {
        let allValid = true;

        $formInputs.each(function () {
            // 각각의 필드 값 확인
            const value = $(this).val().trim();
            if (value === "" || value === "선택") {
                allValid = false;
                return false; // 값이 비어있으면 반복 종료
            }
        });

        if (allValid) {
            $submitButton.removeAttr("disabled");
        } else {
            $submitButton.attr("disabled", true);
        }
    }

    // 이벤트 바인딩
    $formInputs.on("input change", function () {
        validateFields(); // 필드 값 변경 시마다 검사
    });

    // 초기 상태 확인
    validateFields();    

    // .tag의 button 클릭 이벤트
    $('.tag button').off("click").on('click', function () {
        $(this).closest('.tag').css('display', 'none'); // .tag 요소 숨기기
    });    

    // .input_edit button 클릭 이벤트
    $('.input_edit button').off("click").on('click', function () {
        var $input = $(this).closest('.input_func').find('input'); // 형제 input 선택

        if ($input.prop('readonly')) {
            $input.prop('readonly', false); // readonly 제거
            $input.focus(); // 입력 가능 상태로 전환 시 포커스 추가
        } else {
            $input.prop('readonly', true); // readonly 추가
        }
    });    

    // 범용 버튼 클릭 이벤트
    $('[class*="_btn"]').off("click").on('click', function (event) {
        event.preventDefault(); // 기본 동작 방지

        // 버튼의 클래스에서 숫자 추출
        var btnClass = $(this).attr('class');
        var alertNumber = btnClass.match(/alert(\d+)_btn/);
        var link = $(this).attr('href'); // a 태그의 href 링크 가져오기

        if (alertNumber) {
            var alertClass = `.alert${alertNumber[1]}`; // 대응되는 alert 클래스 생성

            // .alert(숫자)에 .on 클래스 추가
            $(alertClass).addClass('on');

            // 3초 후 .on 클래스 제거 및 페이지 이동
            setTimeout(function () {
                $(alertClass).removeClass('on');

                // href가 존재하면 이동
                if (link) {
                    window.location.href = link;
                }
            }, 3000);
        } else {
            //console.error("No matching alert class found for button:", btnClass);
        }
    });

    // .book_mark_open 클릭 시 동작
    $(".book_mark_open").off("click").on("click", function(e) {
        e.preventDefault(); // 기본 동작 방지
        // 부모 .drop_con hide
        $(this).closest(".drop_con").hide();
        // 형제 .book_mark toggleSlide
        $(this).closest(".drop_con").siblings(".book_mark").slideToggle("fast");
    });

    // .book_close 버튼 클릭 시 동작
    $(".book_mark .book_close button").off("click").on("click", function(e) {
        e.preventDefault(); // 기본 동작 방지
        // .book_mark toggleSlide
        $(this).closest(".book_mark").slideToggle("fast");
    });


    $('.head_right button').off("click").on('click', function(){
        var $parentLi = $(this).closest('li'); // 현재 클릭한 버튼의 가장 가까운 상위 li 선택
        $parentLi.toggleClass('on'); // li에 .on 클래스 토글
        $parentLi.find('.que_con').slideToggle(); // 해당 li 내의 .que_con 영역 토글 슬라이드
    });
    

    // 클릭 이벤트 핸들러
    $(".click_area").off("click").on("click", function (e) {
        e.stopPropagation(); // 이벤트 버블링 방지
        $(this).parent(".side_bar").toggleClass("on");
    });    


    $(".project_name_input").on("input", function () {
        const maxLength = 30; // 최대 글자 수
        let currentLength = $(this).val().length; // 현재 입력된 글자 수

        // 최대 길이를 초과하지 않도록 잘라내기
        if (currentLength > maxLength) {
            $(this).val($(this).val().substring(0, maxLength));
            currentLength = maxLength; // 잘라낸 후 현재 길이를 업데이트
        }

        // 현재 길이를 업데이트
        $(this)
            .siblings(".input_txt")
            .find(".current_length")
            .text(currentLength);
    });

    $(".folder_name_input").on("input", function () {
        const maxLength = 15; // 최대 글자 수
        let currentLength = $(this).val().length; // 현재 입력된 글자 수

        // 최대 길이를 초과하지 않도록 잘라내기
        if (currentLength > maxLength) {
            $(this).val($(this).val().substring(0, maxLength));
            currentLength = maxLength; // 잘라낸 후 현재 길이를 업데이트
        }

        // 현재 길이를 업데이트
        $(this)
            .siblings(".input_txt")
            .find(".current_length")
            .text(currentLength);
    });

    // $(".confirm_btn").on("click", function () {
    //     const $confirm = $(".del_confirm"); // .del_confirm 선택
    //     $confirm.addClass("on"); // .on 클래스 추가
    //     setTimeout(function () {
    //         $confirm.removeClass("on"); // 3초 후 .on 클래스 제거
    //     }, 3000);
    // });    

    // 버튼 클릭 시 .drop_con 토글
    $(".dropdown").off("click").on("click", function (e) {
        e.stopPropagation(); // 이벤트 버블링 방지

        // 다른 .drop_con 닫기
        $(".drop_con").not($(this).siblings(".drop_con")).slideUp("fast");
        $(".book_mark").slideUp();
        // 현재 버튼의 .drop_con 토글
        $(this).siblings(".drop_con").slideToggle("fast");
    });

    // 문서 클릭 시 모든 .drop_con 닫기
    $(document).off("click").on("click", function () {
        $(".drop_con").slideUp("fast");
    });    


    // 글자수 카운팅하기
    $('.input_len').keyup(function (e){
        var content = $(this).val();
        var num = $(this).attr('maxlength');
        
        $(this).next('.len_counter').html(content.length+ "/" + num +"자");    //글자수 실시간 카운팅
        if (content.length > num){
        // alert("최대 100자까지 입력 가능합니다.");
        $(this).val(content.substring(0, num));
        $(this).next('.len_counter').html(num + "/" + num +"자");
        }
    });

    // 비밀번호 보기/숨기기 기능 구현
    $('.input_visible').on('click', function () {
        const $input = $(this).closest('.input_func').find('.password_input'); // 현재 버튼과 연결된 입력 필드
        const $img = $(this).find('img'); // 현재 버튼의 이미지

        $input.toggleClass('active'); // 'active' 클래스 토글
        if ($input.hasClass('active')) {
            $img.attr('src', "./assets/images/input_visible.svg"); // 아이콘 변경 (보이기 상태)
            $input.attr('type', 'text').focus(); // 비밀번호 표시
        } else {
            $img.attr('src', "./assets/images/input_invisible.svg"); // 아이콘 변경 (숨기기 상태)
            $input.attr('type', 'password').focus(); // 비밀번호 숨김
        }
    });

  
  // input 내용 삭제
  $('.input_del').off("click").on('click',function(){
    $(this).prev('input').val('').focus();
    $(this).css('visibility',"hidden");
  });
  
  // input 입력시 버튼 나오게 하기
  $('.input_func input').on('keyup',function(){
    $(this).next().css('visibility',"visible");
    if( !$(this).val() ) {
      $(this).next().css('visibility',"hidden");
    }
  });
  

    // 체크박스 전체 선택
    $(".checkbox_group").off("click").on("click", "#check_all", function () {
        $(this).parents(".checkbox_group").find('input').prop("checked", $(this).is(":checked"));
    });

    // 체크박스 개별 선택
    $(".checkbox_group").off("click").on("click", ".normal", function() {
        var is_checked = true;

        $(".checkbox_group .normal").each(function(){
            is_checked = is_checked && $(this).is(":checked");
        });

        $("#check_all").prop("checked", is_checked);
    });    

    // 햄버거버튼
    $('.ham').click(function(){
        if ($('.gnb').hasClass('on')) {
            $('.gnb').removeClass('on');
            $('header').removeClass('on');
        }else{
            $('.gnb').addClass('on');
            $('header').addClass('on');
        }
    });

    // 탑버튼
    $('#etc').hide();
    $(window).scroll(function() {

        if ($(this).scrollTop() > 2000) {
            $('#etc').fadeIn();
        } else {
            $('#etc').fadeOut();
        }
    });
    $("#top_btn").click(function() {
        $('html, body').animate({
            scrollTop : 0
        }, 400);
        return false;
    });

    // 모달창 켜기1
    $('.modal_step_open').click(function(){
        var modal_step = $(this).attr('modal_step');

        $('.modal_step').addClass('hidden'); // 이전 모달창을 숨김
        
        $(this).addClass('current');
        $("."+ "modal_step" +modal_step).removeClass('hidden');
    })

    // 모달창 켜기2
    $('.modal_open').off("click").on("click", function() {
        $(this).next().removeClass("hidden");
    });

    // 모달창 끄기
    $('.cancerButton').off("click").on("click", function() {
        if ($(this).closest(".modal")) {
            $(this).closest(".modal").addClass("hidden");
        }
        else {
            $(this).closest(".modal").addClass("hidden");
        }
    });

    // 모든 모달창 끄기
    $('.allModalClose').off("click").on("click", function() {
        $('.modal').addClass("hidden");
    });

    // 첫번째 모달창 끄기
    $('.firstModalClose2').off("click").on("click", function() {
        $(this).closest('.modal').addClass("hidden");
        $(this).closest('.modal').next('.modal').removeClass("hidden");
    });   
});
