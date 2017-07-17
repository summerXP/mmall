<html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<body>
<h2>Hello World!</h2>

springmvc上传文件
<form action="/manage/product/upload.do" name="form1" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" name="springmvc上传文件"/>
</form>

<hr>

富本文件上传
<form action="/manage/product/richtext_img_upload.do" name="form2" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" name="富本文件上传"/>
</form>
</body>
</html>
