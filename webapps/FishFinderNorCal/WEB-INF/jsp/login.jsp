<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="header.jsp" />

<h1>Login</h1>

<% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-error"><%= request.getAttribute("error") %></div>
<% } %>

<% if (session.getAttribute("successMessage") != null) { %>
    <div class="alert alert-success"><%= session.getAttribute("successMessage") %></div>
    <% session.removeAttribute("successMessage"); %>
<% } %>

<form class="form-card" action="${pageContext.request.contextPath}/login" method="post">
    <div class="form-group">
        <label for="username">Username</label>
        <input type="text" id="username" name="username" required>
    </div>
    <div class="form-group">
        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>
    </div>
    <button type="submit" class="btn btn-primary">Login</button>
</form>

<p>Don't have an account? <a href="${pageContext.request.contextPath}/register">Register here</a></p>

<jsp:include page="footer.jsp" />
