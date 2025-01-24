$(document).ready(function(){
    console.log("theme script");
    ReloadJS("script");
    BindEvents('theme');

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

    const themes = [
        /* [[${themeList.stream().map(theme -> 'https://firebasestorage.googleapis.com/v0/b/do-platform.firebasestorage.app/o/Theme%2F' + theme.image_url + '?alt=media').collect(Collectors.joining(","))}]] */
    ];

    themes.forEach(url => {
        const img = new Image();
        img.src = url; // 프리로드
    });



});

