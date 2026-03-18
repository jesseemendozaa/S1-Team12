<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" %>
<% request.setAttribute("pageTitle", "Error"); %>
<jsp:include page="header.jsp" />

<div class="error-page">
    <h1>Oops! Something went wrong.</h1>
    <p class="error-code">Error Code: <%= response.getStatus() %></p>
    <p>We're sorry, but the page you were looking for could not be found or an unexpected error occurred.</p>
    <p>Please try again later or return to the home page.</p>
    <a href="${pageContext.request.contextPath}/home.html" class="btn btn-primary">Back to Home</a>
</div>

<jsp:include page="footer.jsp" />
