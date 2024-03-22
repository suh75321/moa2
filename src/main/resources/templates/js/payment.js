    var IMP = window.IMP;
    IMP.init("imp80172878");//요거랑

    // 결제 요청 함수
    function requestPay() {

    // 서버에서 받아온 주문 정보
    var orderUid = "[[${requestDto.orderUid}]]";
    var itemName = "[[${requestDto.itemName}]]";
    var paymentPrice = "[[${requestDto.paymentPrice}]]";
    var buyerName = "[[${requestDto.buyerName}]]";
    var buyerAddress = "[[${requestDto.buyerAddress}]]";
    var buyerPhone = "[[${requestDto.buyerPhone}]]";

    IMP.request_pay({
    pg : "nice_v2.iamport00m",//요고
    pay_method : "card",
    merchant_uid: orderUid, // 주문 번호
    name : itemName, // 상품 이름
    amount : paymentPrice, // 상품 가격
    buyer_name : buyerName, // 구매자 이름
    buyer_tel : buyerPhone, // 전화번호
    buyer_addr : buyerAddress, // 구매자 주소
    buyer_postcode : "123-456", // 임의의 값
},
    rsp => { // callback
    console.log(rsp);
    if (rsp == null) {
    let msg = '결제에 실패하였습니다.';
    msg += '에러내용 : ' + rsp.error_msg;
    console.log(msg)
} else {
    let data = {};
    data.imp_uid= rsp.imp_uid;
    data.merchant_uid= rsp.merchant_uid;
    console.log(data)
    $.ajax({
    headers: {"Content-Type": "application/json"},
    method: "POST",
    url: "/payment",
    data: JSON.stringify({
    "payment_uid": rsp.imp_uid,      // 결제 고유번호
    "order_uid": rsp.merchant_uid,
})
}).done(function(response) {
    console.log(response.data);
    alert("주문에 성공했습니다.")
    window.location.href = "/success-payment"
}).fail(function(xhr, status, error) {
    console.error(xhr.response);
    alert("주문에 실패하셨습니다.")
    window.location.href = "/fail-payment"
});
}
});
}
