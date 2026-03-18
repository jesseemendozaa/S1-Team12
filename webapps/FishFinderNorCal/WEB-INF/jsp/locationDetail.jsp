<%@ page import="java.util.*, java.sql.Timestamp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<%
Map<String, Object> location = (Map<String, Object>) request.getAttribute("location");
String locName = (String) location.get("name");
String description = (String) location.get("description");
String typeName = (String) location.get("typeName");
String region = (String) location.get("region");
Object latitude = location.get("latitude");
Object longitude = location.get("longitude");
String creatorName = (String) location.get("creatorName");
Timestamp createdAt = (Timestamp) location.get("createdAt");
int locationId = (int) location.get("locationId");
%>

<h1><%= locName %></h1>

<div class="info-section">
<% if (typeName != null) { %>
    <p><strong>Type:</strong> <span class="badge"><%= typeName %></span></p>
<% } %>
<% if (region != null) { %>
    <p><strong>Region:</strong> <%= region %></p>
<% } %>
<% if (latitude != null && longitude != null) { %>
    <p><strong>Coordinates:</strong> <%= latitude %>, <%= longitude %></p>
<% } %>
<% if (creatorName != null) { %>
    <p><strong>Created by:</strong> <%= creatorName %></p>
<% } %>
<% if (createdAt != null) { %>
    <p><strong>Created at:</strong> <%= createdAt %></p>
<% } %>
<% if (description != null) { %>
    <p><%= description %></p>
<% } %>
</div>

<%
HttpSession sess = request.getSession(false);
if (sess != null && sess.getAttribute("userId") != null) {
    Boolean isFavorited = (Boolean) request.getAttribute("isFavorited");
%>
    <form method="POST" action="<%= request.getContextPath() %>/favorite" class="inline-form">
        <input type="hidden" name="locationId" value="<%= locationId %>">
<% if (isFavorited != null && isFavorited) { %>
        <button type="submit" class="btn btn-secondary">Remove from Favorites</button>
<% } else { %>
        <button type="submit" class="btn">Add to Favorites</button>
<% } %>
    </form>
<%
}
%>

<h2>Species Found Here</h2>
<%
List<Map<String, Object>> species = (List<Map<String, Object>>) request.getAttribute("species");
if (species == null || species.isEmpty()) {
%>
    <p>No species reported yet.</p>
<%
} else {
%>
    <table>
        <thead>
            <tr>
                <th>Common Name</th>
                <th>Scientific Name</th>
                <th>Evidence Count</th>
                <th>Last Reported</th>
            </tr>
        </thead>
        <tbody>
<%
    for (Map<String, Object> sp : species) {
%>
            <tr>
                <td><a href="<%= request.getContextPath() %>/species?id=<%= sp.get("speciesId") %>"><%= sp.get("commonName") %></a></td>
                <td><em><%= sp.get("scientificName") %></em></td>
                <td><%= sp.get("evidenceCount") %></td>
                <td><%= sp.get("lastReported") != null ? sp.get("lastReported") : "N/A" %></td>
            </tr>
<%
    }
%>
        </tbody>
    </table>
<%
}
%>

<h2>Recent Catch Reports</h2>
<%
List<Map<String, Object>> reports = (List<Map<String, Object>>) request.getAttribute("reports");
if (reports == null || reports.isEmpty()) {
%>
    <p>No catch reports yet.</p>
<%
} else {
%>
    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Species</th>
                <th>Angler</th>
                <th>Weight (lbs)</th>
                <th>Length (in)</th>
                <th>Method</th>
            </tr>
        </thead>
        <tbody>
<%
    for (Map<String, Object> r : reports) {
%>
            <tr>
                <td><a href="<%= request.getContextPath() %>/report/edit?id=<%= r.get("reportId") %>"><%= r.get("catchDate") %></a></td>
                <td><%= r.get("speciesName") %></td>
                <td><%= r.get("username") != null ? r.get("username") : "Anonymous" %></td>
                <td><%= r.get("weightLbs") != null ? r.get("weightLbs") : "N/A" %></td>
                <td><%= r.get("lengthInches") != null ? r.get("lengthInches") : "N/A" %></td>
                <td><%= r.get("method") != null ? r.get("method") : "N/A" %></td>
            </tr>
<%
    }
%>
        </tbody>
    </table>
<%
}
%>

<%
if (sess != null && sess.getAttribute("userId") != null) {
%>
    <p><a href="<%= request.getContextPath() %>/report/new?locationId=<%= locationId %>" class="btn">Log a Catch Here</a></p>
<%
}
%>

<%
if (sess != null && sess.getAttribute("role") != null && "moderator".equals(sess.getAttribute("role"))) {
%>
    <p><a href="<%= request.getContextPath() %>/location/edit?id=<%= locationId %>" class="btn btn-secondary">Edit Location</a></p>
<%
}
%>

<jsp:include page="footer.jsp" />
