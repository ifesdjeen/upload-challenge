$(document).ready(
  function() {

    // Initializes iframe listener,
    function finalize_progress() {
      $(".progress .bar").css("width", "100%");
      $("#file-upload-submit").hide();
      $("#description").show();
    }

    // Starts periodic checks for upload status
    function start_upload_tracking() {
      setTimeout(check_upload_status, 1000);
    }

    // Checks upload status and updates progress bar
    function check_upload_status() {
      var fileid = $("#file-upload-id").val();
      var url = "/blobs/" + fileid;

      $.getJSON(url, {}, function( a,b,c ) {
                  // That check is here to prevent _any_ updates after upload was finished.
                  // If we track status only once 2 seconds, for instance, download may finish
                  // and we'll get to know about it from iframe earlier than from status check.
                  if (!$("#description").is(":visible")) {
                    var done = a['uploaded-size'], total = a.size;
                    var percentage = (Math.ceil(done/total*1000)/10);
                    if (percentage == 100)
                      finalize_progress();
                    $(".progress .bar").css("width", percentage + '%');
                    setTimeout(check_upload_status, 2000);
                  }
                });
    }

    $("#file-upload-submit").click(
      function(e) {
        start_upload_tracking();
      });

    // Sends description to the server
    $("#file-description-submit").click(
      function() {
        var fileid = $("#file-upload-id").val();
        var url = "/blobs/" + fileid;

        $.ajax({
                 url: url,
                 type: "post",
                 data: { description : $("#file-upload-description").val() }
               });
      });
  });




