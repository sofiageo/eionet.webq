<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript" src="<c:url value="/js/jquery-1.10.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery-ui-1.10.3.custom.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/file-content.js"/>"></script>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/jquery-ui-1.10.3.custom.min.css"/>">
<style type="text/css">
    .dialogTemplate {
        display: none;
    }
</style>
<script type="text/javascript">
    function removeDialog(dialogElement, callUrl) {
        dialogElement.dialog({
            resizable: false,
            maxHeight: 300,
            modal: true,
            buttons: {
                "Delete" : function() {
                    window.location = callUrl;
                },
                Cancel: function() {
                    $(this).dialog("close");
                }
            }
        });
    }

    function view_file(popupElement, width) {
        popupElement.dialog({
            width: width,
            modal: true
        });
    }

    function removeFile(id) {
        removeDialog($("#remove-file"), "<c:url value="/projects/${projectEntry.projectId}/webform/remove/?fileId="/>" + id);
    }

    function removeProject() {
        removeDialog($("#remove-project"), "<c:url value="/projects/remove?projectId=${projectEntry.projectId}"/>");
    }
</script>
