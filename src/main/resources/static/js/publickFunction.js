function ajax(url, param, successMessage) {
    console.log('ajax public Function');
    $.ajax({
        url: url,
        data: JSON.stringify(param),
        type: 'POST',
        dataType: 'json',
        contentType: ' application/json',
        success: function (data) {
            console.log(data);
            if (data === "ok") {
                location.reload();
            } else {
                console.log("Response: " + data);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            try {
                if (jqXHR.responseText == "ok") {
                    location.reload();
                } else {
                    $('.ajaxFailMessage').text('');
                    var errorResponse = JSON.parse(jqXHR.responseText);
                    var result_code = errorResponse.code;
                    var result_message = errorResponse.message;
                    var result_status = errorResponse.status;

                    // result_message 길이 제한
                    var maxLength = 45;
                    if (result_message.length > maxLength) {
                        result_message = result_message.substring(0, maxLength - 3) + '...';
                    }

                    $('.ajaxFailMessage').text("*" + result_message);
                }

            } catch (e) {
                console.error("Parsing error:", e);
            }
        }
    });
}