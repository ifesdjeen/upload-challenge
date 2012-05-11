$(document).ready(
  function() {

    function extract_filename(filename) {
      var file = "";
      if(filename.indexOf('\\') != '-1') {
        file = filename.split('\\');
        var max = file.length;
        filename = file[max-1];
      }
      return filename;
    }

    function initialize_iframe_listener() {
      $("#file-upload-iframe").ready(
        function (){
          $(".progress .bar").css("width", "100%");
          $("#file-upload-submit").hide()
          $("#description").show()
        });
    }

    function start_upload_tracking() {
      initialize_iframe_listener()
      setTimeout(check_upload_status, 1000);
    }

    function render_upload_status() {

    }

    function check_upload_status() {
      var filename = extract_filename($("#file-upload").val());
      var url = "/blobs/" + filename;

      $.getJSON(url, {}, function( a,b,c ) {
                  if (!$("#description").is(":visible")) {
                    var done = a['uploaded-size'], total = a.size;
                    var percentage = (Math.floor(done/total*1000)/10) + '%';
                    $(".progress .bar").css("width", percentage);
                    setTimeout(check_upload_status, 2000);
                  }
                });
    }

    $("#file-upload-submit").click(
      function(e) {
        start_upload_tracking();
      });

    $("#file-description-submit").click(
      function() {
        var filename = extract_filename($("#file-upload").val());
        var url = "/blobs/" + filename;

        $.ajax({
                 url: url,
                 type: "post",
                 data: { description : $("#file-upload-description").val() }
               })
      });
  });




