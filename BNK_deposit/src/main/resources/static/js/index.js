function openCalculatorPopup() {
    // calculator.html에 설정된 크기에 맞춰 팝업창 크기 지정
    var width = 670;
    var height = 980;
    
    // 사용자의 모니터 해상도 중앙에 팝업이 뜨도록 좌표 계산
    var left = (window.screen.width / 2) - (width / 2);
    var top = (window.screen.height / 2) - (height / 2);
    
    // window.open('주소', '창이름', '옵션')을 사용하여 팝업 호출
    // 컨트롤러에 매핑해두신 /calc/popup 주소를 사용합니다.
    window.open(
        '/calc/popup', 
        'BnkCalculatorPopup', 
        'width=' + width + ', height=' + height + ', top=' + top + ', left=' + left + ', scrollbars=yes, resizable=yes'
    );
}/**
 * 
 */