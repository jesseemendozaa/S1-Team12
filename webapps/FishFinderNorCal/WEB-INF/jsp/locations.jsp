<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<h1>Fishing Locations</h1>

<form method="GET" action="<%= request.getContextPath() %>/locations" class="search-form">
    <input type="text" name="search" placeholder="Search by name, city, or type..."
           value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
    <button type="submit">Search</button>
</form>

<%
List<Map<String, Object>> locations = (List<Map<String, Object>>) request.getAttribute("locations");
if (locations == null || locations.isEmpty()) {
%>
    <p>No locations found.</p>
<%
} else {
%>
    <div class="card-grid">
<%
    for (Map<String, Object> loc : locations) {
        String name = (String) loc.get("name");
        String typeName = (String) loc.get("typeName");
        String city = (String) loc.get("region");
        String description = (String) loc.get("description");
        int locationId = (int) loc.get("locationId");

        String truncatedDesc = "";
        if (description != null && description.length() > 150) {
            truncatedDesc = description.substring(0, 150) + "...";
        } else if (description != null) {
            truncatedDesc = description;
        }
%>
        <div class="card">
            <h3><a href="<%= request.getContextPath() %>/location?id=<%= locationId %>"><%= name %></a></h3>
            <p>
<% if (typeName != null) { %>
                <span class="badge"><%= typeName %></span>
<% } %>
<% if (city != null) { %>
                <%= city %>
<% } %>
            </p>
            <p><%= truncatedDesc %></p>
        </div>
<%
    }
%>
    </div>
<%
}
%>

<%
HttpSession sess = request.getSession(false);
if (sess != null && sess.getAttribute("userId") != null) {
%>
    <p><a href="<%= request.getContextPath() %>/location/new" class="btn">Add New Location</a></p>
<%
}
%>

<jsp:include page="footer.jsp" />
