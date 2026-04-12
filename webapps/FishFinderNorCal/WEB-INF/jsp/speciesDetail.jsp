<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<%
Map<String, Object> species = (Map<String, Object>) request.getAttribute("species");
String commonName = (String) species.get("commonName");
String scientificName = (String) species.get("scientificName");
HttpSession sess = request.getSession(false);
%>

<h1><%= commonName %></h1>

<div class="info-section">
<% if (scientificName != null && !scientificName.trim().isEmpty()) { %>
    <p><strong>Scientific Name:</strong> <em><%= scientificName %></em></p>
<% } %>
</div>

<% if (sess != null && "moderator".equals(sess.getAttribute("role"))) { %>
    <p><a href="<%= request.getContextPath() %>/species/edit?id=<%= species.get("speciesId") %>" class="btn btn-secondary">Edit Species</a></p>
<% } %>

<h2>Known Locations</h2>
<%
List<Map<String, Object>> locations = (List<Map<String, Object>>) request.getAttribute("locations");
if (locations == null || locations.isEmpty()) {
%>
    <p>No locations have been linked to this species yet.</p>
<%
} else {
%>
    <table>
        <thead>
            <tr>
                <th>Location</th>
                <th>City</th>
                <th>Type</th>
                <th>Evidence Count</th>
            </tr>
        </thead>
        <tbody>
<%
    for (Map<String, Object> location : locations) {
%>
            <tr>
                <td><a href="<%= request.getContextPath() %>/location?id=<%= location.get("locationId") %>"><%= location.get("name") %></a></td>
                <td><%= location.get("city") != null ? location.get("city") : "N/A" %></td>
                <td><%= location.get("typeName") != null ? location.get("typeName") : "N/A" %></td>
                <td><%= location.get("evidenceCount") %></td>
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
    <p>No catch reports for this species yet.</p>
<%
} else {
%>
    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Location</th>
                <th>Angler</th>
                <th>Weight (lbs)</th>
                <th>Length (in)</th>
                <th>Method</th>
            </tr>
        </thead>
        <tbody>
<%
    for (Map<String, Object> report : reports) {
%>
            <tr>
                <td><a href="<%= request.getContextPath() %>/report/edit?id=<%= report.get("reportId") %>"><%= report.get("catchDate") %></a></td>
                <td><a href="<%= request.getContextPath() %>/location?id=<%= report.get("locationId") %>"><%= report.get("locationName") %></a></td>
                <td><%= report.get("username") != null ? report.get("username") : "Anonymous" %></td>
                <td><%= report.get("weightLbs") != null ? report.get("weightLbs") : "N/A" %></td>
                <td><%= report.get("lengthInches") != null ? report.get("lengthInches") : "N/A" %></td>
                <td><%= report.get("method") != null ? report.get("method") : "N/A" %></td>
            </tr>
<%
    }
%>
        </tbody>
    </table>
<%
}
%>

<jsp:include page="footer.jsp" />
