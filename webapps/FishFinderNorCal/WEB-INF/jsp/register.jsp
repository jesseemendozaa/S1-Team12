<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="header.jsp" />

<h1>Create Account</h1>

<% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-error"><%= request.getAttribute("error") %></div>
<% } %>

<form class="form-card" action="${pageContext.request.contextPath}/register" method="post">
    <div class="form-group">
        <label for="username">Username</label>
        <input type="text" id="username" name="username" required
               value="<%= request.getAttribute("username") != null ? request.getAttribute("username") : "" %>">
    </div>
    <div class="form-group">
        <label for="email">Email</label>
        <input type="email" id="email" name="email" required
               value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>">
    </div>
    <div class="form-group">
        <label for="password">Password</label>
        <input type="password" id="password" name="password" required minlength="6">
    </div>
    <div class="form-group">
        <label for="confirmPassword">Confirm Password</label>
        <input type="password" id="confirmPassword" name="confirmPassword" required>
    </div>
    <button type="submit" class="btn btn-primary">Register</button>
</form>

<p>Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a></p>

<jsp:include page="footer.jsp" />
