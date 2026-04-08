<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<jsp:include page="header.jsp" />

<h1>My Favorite Locations</h1>

<%
    List<Map<String, Object>> favorites = (List<Map<String, Object>>) request.getAttribute("favorites");
    if (favorites == null || favorites.isEmpty()) {
%>
    <p>You haven't saved any favorites yet. Browse <a href="${pageContext.request.contextPath}/locations">locations</a> to find your next fishing spot!</p>
<% } else { %>
    <div class="card-grid">
        <% for (Map<String, Object> fav : favorites) { %>
        <div class="card">
            <h3>
                <a href="${pageContext.request.contextPath}/location?id=<%= fav.get("locationId") %>">
                    <%= fav.get("name") %>
                </a>
            </h3>
            <% if (fav.get("typeName") != null) { %>
                <span class="badge"><%= fav.get("typeName") %></span>
            <% } %>
            <% if (fav.get("region") != null) { %>
                <p><strong>Region:</strong> <%= fav.get("region") %></p>
            <% } %>
            <% if (fav.get("favoritedAt") != null) { %>
                <p><strong>Favorited:</strong> <%= fav.get("favoritedAt") %></p>
            <% } %>
            <form method="post" action="${pageContext.request.contextPath}/favorite">
                <input type="hidden" name="locationId" value="<%= fav.get("locationId") %>" />
                <button type="submit" class="btn btn-danger btn-sm">Remove</button>
            </form>
        </div>
        <% } %>
    </div>
<% } %>

<jsp:include page="footer.jsp" />
