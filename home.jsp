<%
String username = (String) session.getAttribute("username");
if (username == null) {
    response.sendRedirect("login.html");
    return;
}
%>

<html>
<head>
    <title>Home</title>
</head>
<body>
    <h2>Welcome <%= username %></h2>
    <a href="logout">Logout</a>
</body>
</html>