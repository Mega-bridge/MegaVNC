function ajax(url, param, successMessage) {
    $.ajax({
        url: url,
        data: JSON.stringify(param),
        type: 'POST',
        dataType: 'json',
        contentType: ' application/json',
        success: function (data) {
            console.log(data);
            if (data === "ok") {
                Swal.fire({
                    icon: 'success',
                    title: '성공',
                    html: successMessage
                });
            } else {
                console.log("Response: " + data);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            try {
                if (jqXHR.responseText == "ok") {
                    Swal.fire({
                        icon: 'success',
                        title: '성공',
                        html: successMessage
                    }).then((result) => {
                        location.reload();
                    })
                } else {
                    var errorResponse = JSON.parse(jqXHR.responseText);
                    console.log("Error Message: " + errorResponse.message);
                    console.log("Status: " + errorResponse.status);
                    console.log("Code: " + errorResponse.code);

                    Swal.fire({
                        icon: 'error',
                        title: '실패(' + errorResponse.code + ")",
                        text: errorResponse.message
                    });
                }

            } catch (e) {
                console.error("Parsing error:", e);
            }
        }
    });
}