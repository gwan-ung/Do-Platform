window.addEventListener('wheel', function (event) {
    // Unity로 마우스 휠 데이터를 전달 (Y 방향 값)
    //SendMessage('View', 'HandleWheelInput', event.deltaY);
    event.preventDefault(); // 과도한 스크롤 방지
}, { passive: false });
