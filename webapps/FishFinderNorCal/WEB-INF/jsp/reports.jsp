<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<h1>Catch Reports</h1>

<%
HttpSession sess = request.getSession(false);
if (sess != null && sess.getAttribute("userId") != null) {
%>
    <p><a href="<%= request.getContextPath() %>/report/new" class="btn">Log a Catch</a></p>
<%
}
%>

<%
List<Map<String, Object>> reports = (List<Map<String, Object>>) request.getAttribute("reports");
if (reports == null || reports.isEmpty()) {
%>
    <p>No catch reports found.</p>
<%
} else {
%>
    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Species</th>
                <th>Location</th>
                <th>Angler</th>
                <th>Weight (lbs)</th>
                <th>Length (in)</th>
                <th>Time</th>
            </tr>
        </thead>
        <tbody>
<%
    for (Map<String, Object> r : reports) {
%>
            <tr>
                <td><a href="<%= request.getContextPath() %>/report/edit?id=<%= r.get("reportId") %>"><%= r.get("catchDate") %></a></td>
                <td><%= r.get("speciesName") %></td>
                <td><a href="<%= request.getContextPath() %>/location?id=<%= r.get("locationId") %>"><%= r.get("locationName") %></a></td>
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

<jsp:include page="footer.jsp" />
