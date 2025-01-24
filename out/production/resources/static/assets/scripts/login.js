$(document).ready(function(){
    console.log("login.js :: start!!");


});

function RetrySendCode(element){
    console.log("login.js :: RetrySendCode");

    const email = element.getAttribute("data-email");

    console.log(`login.js :: email = ${email}`);

    location.href = `/retry-verify-code?email=${encodeURIComponent(email)}`;
}

function GetSignUpEmail(){
    console.log("login.js :: GetSignUpEmail");

    const email = document.getElementById("sign-up-email").textContent;

    $.ajax({
        type : "GET",
        url : `/sign-up-email?email=${encodeURIComponent(email)}`,
        timeout : 30000,
        contentType: "application/json;charset=UTF-8",
        data : "",
        async : true,
        dataType : "text", //xml, html, json, script, text
        success : function(data) {
            console.log('data : ', data);
            //successFunction(data,menu);
        },
        error : function(xhr, status, err) {
            console.log('xhr : ', xhr);
            console.log('status : ', status);
            console.log('err : ', err);
        }
    });
}